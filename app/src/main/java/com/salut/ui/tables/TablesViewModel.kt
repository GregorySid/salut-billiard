package com.salut.ui.tables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salut.core.network.NetworkMonitor
import com.salut.core.util.AppResult
import com.salut.data.local.preferences.UserPreferences
import com.salut.domain.repository.TableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TablesViewModel @Inject constructor(
    private val tableRepository: TableRepository,
    private val networkMonitor: NetworkMonitor,
    private val preferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TablesUiState())
    val uiState: StateFlow<TablesUiState> = _uiState.asStateFlow()

    private val loadMutex = Mutex()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOffline = !online) }
            }
        }
        viewModelScope.launch {
            preferences.tableFilter.collect { filter ->
                _uiState.update { it.copy(filter = filter) }
                load(refresh = true)
            }
        }
    }

    fun onSearchChange(value: String) {
        _uiState.update { it.copy(search = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            load(refresh = true)
        }
    }

    fun refresh() = load(refresh = true)

    fun retry() = load(refresh = true)

    fun onLoadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || state.isInitialLoading || !state.hasMore) return
        load(refresh = false)
    }

    private fun load(refresh: Boolean) {
        viewModelScope.launch {
            loadMutex.withLock {
                val state = _uiState.value
                val targetPage = if (refresh) 1 else state.page + 1
                _uiState.update {
                    if (refresh) it.copy(isRefreshing = true, error = null)
                    else it.copy(isLoadingMore = true)
                }

                val filter = state.filter
                val search = state.search

                if (refresh) {
                    when (val result = tableRepository.refreshTables(filter, search, 1, PAGE_SIZE)) {
                        is AppResult.Failure -> {
                            Timber.w("Refresh tables failed: ${result.error.type}")
                            if (_uiState.value.tables.isEmpty()) {
                                _uiState.update { it.copy(error = result.error) }
                            }
                        }
                        is AppResult.Success -> Unit
                    }
                }

                val page = tableRepository.getTables(filter, search, targetPage, PAGE_SIZE)
                _uiState.update { current ->
                    val newTables = if (refresh) page.items else current.tables + page.items
                    current.copy(
                        tables = newTables,
                        page = targetPage,
                        hasMore = page.hasMore,
                        isInitialLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        error = if (newTables.isEmpty()) current.error else null,
                    )
                }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 10
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
