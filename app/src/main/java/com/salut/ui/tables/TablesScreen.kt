package com.salut.ui.tables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salut.R
import com.salut.ui.components.EmptyState
import com.salut.ui.components.ErrorState
import com.salut.ui.components.OfflineBanner
import com.salut.ui.components.SearchField
import com.salut.ui.components.SalutTopAppBar
import com.salut.ui.components.SkeletonTableCard
import com.salut.ui.components.TableCard
import com.salut.ui.util.asMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(
    onTableClick: (Long) -> Unit,
    onOpenFilters: () -> Unit,
    onOpenBookings: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: TablesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SalutTopAppBar(
                title = stringResource(R.string.screen_tables_title),
                actions = {
                    IconButton(onClick = onOpenBookings) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.action_bookings),
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.action_settings),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (state.isOffline) {
                OfflineBanner()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SearchField(
                    value = state.search,
                    onValueChange = viewModel::onSearchChange,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    IconButton(onClick = onOpenFilters) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.action_filters),
                        )
                    }
                    if (state.activeFilterCount > 0) {
                        Badge(modifier = Modifier.align(Alignment.TopEnd)) {
                            Text(state.activeFilterCount.toString())
                        }
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.isInitialLoading -> SkeletonGrid()
                    state.error != null && state.tables.isEmpty() -> ErrorState(
                        message = state.error!!.asMessage(),
                        onRetry = viewModel::retry,
                        modifier = Modifier.fillMaxSize(),
                    )

                    state.tables.isEmpty() -> EmptyState(
                        title = if (state.isFiltered) {
                            stringResource(R.string.empty_nothing_found)
                        } else {
                            stringResource(R.string.empty_no_tables)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    else -> TablesGrid(
                        state = state,
                        onTableClick = onTableClick,
                        onLoadMore = viewModel::onLoadMore,
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(6) { SkeletonTableCard() }
    }
}

@Composable
private fun TablesGrid(
    state: TablesUiState,
    onTableClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(state.tables, key = { it.id }) { table ->
            TableCard(table = table, onClick = { onTableClick(table.id) })
        }
        if (state.hasMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LaunchedEffect(state.tables.size) { onLoadMore() }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isLoadingMore) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}
