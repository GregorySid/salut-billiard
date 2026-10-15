package com.salut.ui.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salut.core.network.NetworkMonitor
import com.salut.core.util.AppError
import com.salut.core.util.AppResult
import com.salut.domain.model.BilliardTable
import com.salut.domain.model.TimeSlot
import com.salut.domain.repository.BookingRepository
import com.salut.domain.repository.TableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BookingUiState(
    val table: BilliardTable? = null,
    val slots: List<TimeSlot> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedSlotIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val slotsLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isOffline: Boolean = false,
) {
    val totalPrice: Int
        get() = slots.filter { it.id in selectedSlotIds }.sumOf { it.price }

    val canSubmit: Boolean
        get() = selectedSlotIds.isNotEmpty() && !isSubmitting && !isOffline
}

sealed interface BookingEvent {
    data object Success : BookingEvent
    data object Offline : BookingEvent
    data class Error(val error: AppError) : BookingEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BookingViewModel @Inject constructor(
    private val tableRepository: TableRepository,
    private val bookingRepository: BookingRepository,
    private val networkMonitor: NetworkMonitor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val tableId: Long = requireNotNull(savedStateHandle.get<Long>("tableId")) {
        "tableId is required"
    }

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _events = Channel<BookingEvent>(Channel.BUFFERED)
    val events: Flow<BookingEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOffline = !online) }
            }
        }
        viewModelScope.launch {
            tableRepository.observeTable(tableId).collect { table ->
                _uiState.update { it.copy(table = table) }
            }
        }
        viewModelScope.launch {
            _uiState
                .map { it.selectedDate }
                .distinctUntilChanged()
                .flatMapLatest { date -> tableRepository.observeSlots(tableId, date) }
                .collect { slots ->
                    _uiState.update { it.copy(slots = slots, slotsLoading = false, isLoading = false) }
                }
        }
        refresh()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(selectedDate = date, selectedSlotIds = emptySet(), slotsLoading = true)
        }
        refreshSlots()
    }

    fun onSlotToggle(slot: TimeSlot) {
        if (!slot.available) return
        _uiState.update { current ->
            val ordered = current.slots.filter { it.available }
            val index = ordered.indexOfFirst { it.id == slot.id }
            val selectedIndexes = ordered.withIndex()
                .filter { it.value.id in current.selectedSlotIds }
                .map { it.index }

            val newSelection = when {
                slot.id in current.selectedSlotIds -> {
                    val min = selectedIndexes.min()
                    val max = selectedIndexes.max()
                    when (index) {
                        min, max -> current.selectedSlotIds - slot.id
                        else -> setOf(slot.id)
                    }
                }

                selectedIndexes.isEmpty() -> setOf(slot.id)
                index == selectedIndexes.max() + 1 || index == selectedIndexes.min() - 1 ->
                    current.selectedSlotIds + slot.id

                else -> setOf(slot.id)
            }
            current.copy(selectedSlotIds = newSelection)
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.selectedSlotIds.isEmpty() || state.isSubmitting) return
        if (!networkMonitor.isOnline.value) {
            viewModelScope.launch { _events.send(BookingEvent.Offline) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val orderedIds = state.slots
                .filter { it.id in state.selectedSlotIds }
                .map { it.id }
            when (val result = bookingRepository.createBooking(tableId, state.selectedDate, orderedIds)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, selectedSlotIds = emptySet()) }
                    _events.send(BookingEvent.Success)
                }

                is AppResult.Failure -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.send(BookingEvent.Error(result.error))
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            tableRepository.refreshTable(tableId)
        }
        refreshSlots()
    }

    private fun refreshSlots() {
        viewModelScope.launch {
            tableRepository.refreshSlots(tableId, _uiState.value.selectedDate)
            _uiState.update { it.copy(slotsLoading = false, isLoading = false) }
        }
    }
}
