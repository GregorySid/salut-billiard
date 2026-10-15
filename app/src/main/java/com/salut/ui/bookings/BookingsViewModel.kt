package com.salut.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salut.core.network.NetworkMonitor
import com.salut.core.util.AppError
import com.salut.core.util.AppResult
import com.salut.domain.model.Booking
import com.salut.domain.model.BookingStatus
import com.salut.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingsUiState(
    val active: List<Booking> = emptyList(),
    val past: List<Booking> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
    val isOffline: Boolean = false,
) {
    val isEmpty: Boolean get() = active.isEmpty() && past.isEmpty()
}

sealed interface BookingsEvent {
    data object Cancelled : BookingsEvent
    data object Offline : BookingsEvent
    data class Error(val error: AppError) : BookingsEvent
}

@HiltViewModel
class BookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingsUiState())
    val uiState: StateFlow<BookingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<BookingsEvent>(Channel.BUFFERED)
    val events: Flow<BookingsEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOffline = !online) }
            }
        }
        viewModelScope.launch {
            bookingRepository.observeBookings().collect { bookings ->
                _uiState.update {
                    it.copy(
                        active = bookings.filter { booking -> booking.status == BookingStatus.ACTIVE },
                        past = bookings.filter { booking -> booking.status != BookingStatus.ACTIVE },
                        isLoading = false,
                    )
                }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            val result = bookingRepository.refreshBookings()
            _uiState.update { current ->
                current.copy(
                    isRefreshing = false,
                    error = if (result is AppResult.Failure && current.isEmpty) result.error else null,
                )
            }
        }
    }

    fun cancel(id: String) {
        if (!networkMonitor.isOnline.value) {
            viewModelScope.launch { _events.send(BookingsEvent.Offline) }
            return
        }
        viewModelScope.launch {
            when (val result = bookingRepository.cancelBooking(id)) {
                is AppResult.Success -> _events.send(BookingsEvent.Cancelled)
                is AppResult.Failure -> _events.send(BookingsEvent.Error(result.error))
            }
        }
    }
}
