package com.salut.ui.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salut.R
import com.salut.domain.model.TableStatus
import com.salut.domain.model.TableType
import com.salut.ui.components.SalutTopAppBar
import com.salut.ui.util.equipmentLabel
import com.salut.ui.util.label
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    onBack: () -> Unit,
    viewModel: FiltersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onApplied by rememberUpdatedState(onBack)

    LaunchedEffect(Unit) {
        viewModel.applied.collect { onApplied() }
    }

    Scaffold(
        topBar = {
            SalutTopAppBar(
                title = stringResource(R.string.screen_filters_title),
                onBack = onBack,
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.navigationBarsPadding(),
                tonalElevation = 3.dp,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = viewModel::reset,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.action_reset))
                    }
                    Button(
                        onClick = viewModel::apply,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.action_apply))
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            FilterSection(title = stringResource(R.string.filter_type)) {
                FilterChip(
                    selected = state.type == null,
                    onClick = { viewModel.onTypeChange(null) },
                    label = { Text(stringResource(R.string.filter_all)) },
                )
                TableType.entries.forEach { type ->
                    FilterChip(
                        selected = state.type == type,
                        onClick = { viewModel.onTypeChange(if (state.type == type) null else type) },
                        label = { Text(type.label()) },
                    )
                }
            }

            FilterSection(title = stringResource(R.string.filter_status)) {
                TableStatus.entries.forEach { status ->
                    FilterChip(
                        selected = state.status == status,
                        onClick = { viewModel.onStatusChange(if (state.status == status) null else status) },
                        label = { Text(status.label()) },
                    )
                }
            }

            FilterSection(title = stringResource(R.string.filter_capacity)) {
                RangeSlider(
                    value = state.capacityRange,
                    onValueChange = viewModel::onCapacityRangeChange,
                    valueRange = FiltersUiState.CAPACITY_MIN..FiltersUiState.CAPACITY_MAX,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${state.capacityRange.start.roundToInt()} — ${state.capacityRange.endInclusive.roundToInt()} " +
                        stringResource(R.string.unit_capacity),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FilterSection(title = stringResource(R.string.filter_price)) {
                RangeSlider(
                    value = state.priceRange,
                    onValueChange = viewModel::onPriceRangeChange,
                    valueRange = FiltersUiState.PRICE_MIN..FiltersUiState.PRICE_MAX,
                    steps = FiltersUiState.PRICE_STEP,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${state.priceRange.start.roundToInt()} — ${state.priceRange.endInclusive.roundToInt()} " +
                        stringResource(R.string.unit_currency),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FilterSection(title = stringResource(R.string.filter_equipment)) {
                FiltersUiState.EQUIPMENT_OPTIONS.forEach { key ->
                    FilterChip(
                        selected = key in state.equipment,
                        onClick = { viewModel.onEquipmentToggle(key) },
                        label = { Text(equipmentLabel(key)) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            content()
        }
    }
}
