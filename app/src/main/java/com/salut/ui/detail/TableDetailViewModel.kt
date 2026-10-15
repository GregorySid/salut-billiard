package com.salut.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salut.core.network.NetworkMonitor
import com.salut.core.util.AppError
import com.salut.core.util.AppResult
import com.salut.domain.model.BilliardTable
import com.salut.domain.model.TimeSlot
import com.salut.domain.repository.TableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TableDetailUiState(
    val table: BilliardTable? = null,
    val slots: List<TimeSlot> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val slotsLoading: Boolean = false,
    val error: AppError? = null,
    val isOffline: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TableDetailViewModel @Inject constructor(
    private val tableRepository: TableRepository,
    private val networkMonitor: NetworkMonitor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val tableId: Long = requireNotNull(savedStateHandle.get<Long>("tableId")) {
        "tableId is required"
    }

    private val _uiState = MutableStateFlow(TableDetailUiState())
    val uiState: StateFlow<TableDetailUiState> = _uiState.asStateFlow()

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
                    _uiState.update { it.copy(slots = slots, slotsLoading = false) }
                }
        }
        refresh()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, slotsLoading = true) }
        refreshSlots()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            val tableResult = tableRepository.refreshTable(tableId)
            val slotsResult = tableRepository.refreshSlots(tableId, _uiState.value.selectedDate)
            val failure = (tableResult as? AppResult.Failure)?.error
                ?: (slotsResult as? AppResult.Failure)?.error
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    slotsLoading = false,
                    error = if (it.table == null) failure else null,
                )
            }
        }
    }

    private fun refreshSlots() {
        viewModelScope.launch {
            val result = tableRepository.refreshSlots(tableId, _uiState.value.selectedDate)
            if (result is AppResult.Failure) {
                _uiState.update { it.copy(slotsLoading = false, error = result.error) }
            }
        }
    }
}
