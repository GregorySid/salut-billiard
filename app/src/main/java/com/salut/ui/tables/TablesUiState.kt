package com.salut.ui.tables

import com.salut.core.util.AppError
import com.salut.domain.model.BilliardTable
import com.salut.domain.model.TableFilter

data class TablesUiState(
    val tables: List<BilliardTable> = emptyList(),
    val search: String = "",
    val filter: TableFilter = TableFilter.EMPTY,
    val page: Int = 1,
    val hasMore: Boolean = false,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: AppError? = null,
    val isOffline: Boolean = false,
) {
    val activeFilterCount: Int get() = filter.activeCount
    val isFiltered: Boolean get() = filter.isActive || search.isNotBlank()
}
