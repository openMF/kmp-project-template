/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.rates.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.formatDecimal
import kpt.core.designsystem.chart.KptSparkline
import kpt.core.designsystem.component.RateBadge
import kpt.core.designsystem.component.RateDirection
import kpt.core.designsystem.theme.finance
import kpt.core.designsystem.theme.spacing
import kpt.core.model.economic.InterestRateSeries
import org.koin.compose.viewmodel.koinViewModel

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
        val sp = MaterialTheme.spacing
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(sp.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(sp.md),
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
            Spacer(Modifier.height(sp.md))
        }
    }
}

@Composable
private fun RateRowCard(
    state: ScreenState<InterestRateSeries>,
    onRetry: () -> Unit,
    onSeriesClick: (seriesId: String) -> Unit,
) {
    val sp = MaterialTheme.spacing
    val f = MaterialTheme.finance
    // Direction-driven accent stripe: up = rateUp tone, down = rateDown tone, flat = none.
    // Only available once we have Content — Loading/Error rows render unstriped.
    val accent: androidx.compose.ui.graphics.Color? = if (state is ScreenState.Content) {
        when {
            computeOneDayDelta(state.data).let { it != null && it > 0 } -> f.rateUp
            computeOneDayDelta(state.data).let { it != null && it < 0 } -> f.rateDown
            else -> null
        }
    } else {
        null
    }
    AppCard(accentColor = accent) {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp).padding(sp.md)) {
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
    val sp = MaterialTheme.spacing
    val delta = computeOneDayDelta(series)
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(sp.xs),
        ) {
            Text(
                text = series.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(sp.sm),
            ) {
                Text(
                    text = "${series.current.formatDecimal(2)}${series.unit}",
                    style = MaterialTheme.typography.headlineSmall,
                )
                delta?.let {
                    val direction = when {
                        it > 0 -> RateDirection.Up
                        it < 0 -> RateDirection.Down
                        else -> RateDirection.Flat
                    }
                    val sign = if (it >= 0) "+" else ""
                    RateBadge(
                        delta = "$sign${it.formatDecimal(2)}${series.unit}",
                        direction = direction,
                    )
                }
            }
        }

        KptSparkline(
            values = series.observations.map { it.value },
            modifier = Modifier.weight(1f).fillMaxSize(),
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
