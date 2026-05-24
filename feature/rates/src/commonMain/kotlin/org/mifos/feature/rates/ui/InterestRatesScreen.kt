/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.rates.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.common.formatDecimal
import org.mifos.core.model.economic.InterestRateSeries
import org.mifos.feature.rates.chart.Sparkline
import template.core.base.store.screen.ScreenState
import template.core.base.ui.screen.ScreenContent

/**
 * **B7 Interest Rate Tracker** — list screen.
 *
 * Four independent rate rows, each rendering its own `ScreenContent` so a single
 * failed series doesn't blank the whole screen. The framework's `DataFreshness`
 * banner is rendered automatically by `ScreenContent` based on each row's
 * `Content.freshness` slot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InterestRatesScreen(
    onBackClick: () -> Unit,
    onSeriesClick: (seriesId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InterestRatesViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Interest Rates") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.trySendAction(RatesAction.RefreshAll) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh all",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RateRowCard(
                state = state.fedFunds,
                onRetry = { viewModel.trySendAction(RatesAction.RetryFedFunds) },
                onSeriesClick = onSeriesClick,
            )
            RateRowCard(
                state = state.prime,
                onRetry = { viewModel.trySendAction(RatesAction.RetryPrime) },
                onSeriesClick = onSeriesClick,
            )
            RateRowCard(
                state = state.mortgage30Y,
                onRetry = { viewModel.trySendAction(RatesAction.RetryMortgage30Y) },
                onSeriesClick = onSeriesClick,
            )
            RateRowCard(
                state = state.treasury10Y,
                onRetry = { viewModel.trySendAction(RatesAction.RetryTreasury10Y) },
                onSeriesClick = onSeriesClick,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RateRowCard(
    state: ScreenState<InterestRateSeries>,
    onRetry: () -> Unit,
    onSeriesClick: (seriesId: String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp).padding(16.dp)) {
            ScreenContent(
                state = state,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            ) { series, _ ->
                RateRowContent(
                    series = series,
                    onClick = { onSeriesClick(series.seriesId) },
                )
            }
        }
    }
}

@Composable
private fun RateRowContent(
    series: InterestRateSeries,
    onClick: () -> Unit,
) {
    val delta = computeOneDayDelta(series)
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: name + current + delta.
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = series.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${series.current.formatDecimal(2)}${series.unit}",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.width(12.dp))
                DeltaBadge(delta = delta, unit = series.unit)
            }
        }

        // Right: sparkline.
        Sparkline(
            values = series.observations.map { it.value },
            modifier = Modifier.weight(1f).fillMaxSize(),
        )
    }
}

@Composable
private fun DeltaBadge(delta: Double?, unit: String) {
    if (delta == null) return
    val (icon, tint) = when {
        delta > 0 -> Icons.AutoMirrored.Default.TrendingUp to MaterialTheme.colorScheme.primary
        delta < 0 -> Icons.AutoMirrored.Default.TrendingDown to MaterialTheme.colorScheme.error
        else -> Icons.AutoMirrored.Default.TrendingFlat to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "${if (delta >= 0) "+" else ""}${delta.formatDecimal(2)}$unit",
            style = MaterialTheme.typography.labelMedium,
            color = tint,
        )
    }
}

/**
 * 1-day delta = `observations.last - observations[last-1]`, or `null` if there
 * are fewer than two observations.
 *
 * FRED may publish "no-data" days (weekends/holidays for daily series). The
 * domain mapper already strips those, so consecutive observations represent
 * the actual two most-recent published values, not necessarily calendar days.
 */
private fun computeOneDayDelta(series: InterestRateSeries): Double? {
    val obs = series.observations
    if (obs.size < 2) return null
    return obs.last().value - obs[obs.size - 2].value
}
