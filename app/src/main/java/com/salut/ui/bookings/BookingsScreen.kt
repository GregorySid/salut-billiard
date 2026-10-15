package com.salut.ui.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salut.R
import com.salut.core.util.DateFormats.toDisplay
import com.salut.domain.model.Booking
import com.salut.ui.components.EmptyState
import com.salut.ui.components.ErrorState
import com.salut.ui.components.OfflineBanner
import com.salut.ui.components.SalutTopAppBar
import com.salut.ui.util.messageRes
import com.salut.ui.util.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    onBack: () -> Unit,
    viewModel: BookingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }

    val cancelledMessage = stringResource(R.string.booking_cancelled)
    val offlineMessage = stringResource(R.string.offline_action_message)
    val resources = LocalResources.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                BookingsEvent.Cancelled -> snackbarHostState.showSnackbar(cancelledMessage)
                BookingsEvent.Offline -> snackbarHostState.showSnackbar(offlineMessage)
                is BookingsEvent.Error -> snackbarHostState.showSnackbar(
                    resources.getString(event.error.messageRes()),
                )
            }
        }
    }

    Scaffold(
        topBar = {
            SalutTopAppBar(
                title = stringResource(R.string.screen_bookings_title),
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (state.isOffline) {
                OfflineBanner()
            }
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.isLoading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }

                    state.error != null && state.isEmpty -> ErrorState(
                        message = stringResource(state.error!!.messageRes()),
                        onRetry = viewModel::refresh,
                        modifier = Modifier.fillMaxSize(),
                    )

                    state.isEmpty -> EmptyState(
                        title = stringResource(R.string.empty_no_bookings),
                        modifier = Modifier.fillMaxSize(),
                    )

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (state.active.isNotEmpty()) {
                            item { SectionHeader(stringResource(R.string.bookings_active_section)) }
                            items(state.active, key = { it.id }) { booking ->
                                BookingCard(
                                    booking = booking,
                                    onCancel = { bookingToCancel = booking },
                                )
                            }
                        }
                        if (state.past.isNotEmpty()) {
                            item { SectionHeader(stringResource(R.string.bookings_past_section)) }
                            items(state.past, key = { it.id }) { booking ->
                                BookingCard(booking = booking, onCancel = null)
                            }
                        }
                    }
                }
            }
        }
    }

    bookingToCancel?.let { booking ->
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            title = { Text(stringResource(R.string.booking_cancel_confirm_title)) },
            text = { Text(stringResource(R.string.booking_cancel_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancel(booking.id)
                        bookingToCancel = null
                    },
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToCancel = null }) {
                    Text(stringResource(R.string.action_close))
                }
            },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun BookingCard(
    booking: Booking,
    onCancel: (() -> Unit)?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = booking.tableName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = booking.date.toDisplay(),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "${booking.start.toDisplay()}–${booking.end.toDisplay()} · " +
                        stringResource(R.string.price_value, booking.totalPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BookingStatusBadge(booking)
            }
            if (onCancel != null) {
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    }
}

@Composable
private fun BookingStatusBadge(booking: Booking) {
    val container = when (booking.status) {
        com.salut.domain.model.BookingStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
        com.salut.domain.model.BookingStatus.PAST -> MaterialTheme.colorScheme.secondaryContainer
        com.salut.domain.model.BookingStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
    }
    Surface(
        color = container,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
    ) {
        Text(
            text = booking.status.label(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
