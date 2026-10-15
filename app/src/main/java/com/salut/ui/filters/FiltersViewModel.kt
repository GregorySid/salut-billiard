package com.salut.ui.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salut.data.local.preferences.UserPreferences
import com.salut.domain.model.TableStatus
import com.salut.domain.model.TableType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val preferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FiltersUiState())
    val uiState: StateFlow<FiltersUiState> = _uiState.asStateFlow()

    private val _applied = Channel<Unit>(Channel.BUFFERED)
    val applied: Flow<Unit> = _applied.receiveAsFlow()

    init {
        viewModelScope.launch {
            val filter = preferences.tableFilter.first()
            _uiState.value = FiltersUiState.fromFilter(filter)
        }
    }

    fun onTypeChange(type: TableType?) = _uiState.update { it.copy(type = type) }

    fun onStatusChange(status: TableStatus?) = _uiState.update { it.copy(status = status) }

    fun onCapacityRangeChange(range: ClosedFloatingPointRange<Float>) =
        _uiState.update { it.copy(capacityRange = range) }

    fun onPriceRangeChange(range: ClosedFloatingPointRange<Float>) =
        _uiState.update { it.copy(priceRange = range) }

    fun onEquipmentToggle(key: String) = _uiState.update { current ->
        val equipment = current.equipment.toMutableSet().apply {
            if (!add(key)) remove(key)
        }
        current.copy(equipment = equipment)
    }

    fun apply() {
        viewModelScope.launch {
            preferences.setTableFilter(_uiState.value.toFilter())
            _applied.send(Unit)
        }
    }

    fun reset() {
        viewModelScope.launch {
            _uiState.value = FiltersUiState()
            preferences.clearTableFilter()
        }
    }
}
