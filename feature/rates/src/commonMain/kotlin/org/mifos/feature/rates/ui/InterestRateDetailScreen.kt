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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.mifos.core.common.formatDecimal
import org.mifos.core.designsystem.component.AmountDisplay
import org.mifos.core.designsystem.component.AppCard
import org.mifos.core.designsystem.component.HeroCard
import org.mifos.core.designsystem.theme.spacing
import org.mifos.core.model.economic.InterestRateSeries
import org.mifos.feature.rates.chart.AreaChart
import template.core.base.ui.screen.ScreenContent

/**
 * **B7 Interest Rate Tracker** — per-series detail.
 *
 * Hero is a 365-day [AreaChart] of the series's published observations; below
 * it, a scrolling table of every observation. State, source attribution and
 * freshness are surfaced via the framework's `ScreenContent`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InterestRateDetailScreen(
    seriesId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InterestRateDetailViewModel = koinViewModel { parametersOf(seriesId) },
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(viewModel.seriesKey.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                        )
                    }
                },
            )
        },
    ) { padding ->
        ScreenContent(
            state = screenState,
            onRetry = viewModel::onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { series, _ ->
            val sp = MaterialTheme.spacing
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = sp.lg),
                verticalArrangement = Arrangement.spacedBy(sp.md),
            ) {
                HeroCard {
                    AmountDisplay(
                        amountText = "${series.current.formatDecimal(2)}${series.unit}",
                        label = "Current",
                        supporting = {
                            Text(
                                text = "Source: ${series.source}",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
                ChartCard(series)
                Text(
                    text = "Observations",
                    style = MaterialTheme.typography.titleMedium,
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(series.observations.reversed()) { obs ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = sp.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = obs.date.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "${obs.value.formatDecimal(2)}${series.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(series: InterestRateSeries) {
    val sp = MaterialTheme.spacing
    AppCard {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).padding(sp.sm)) {
            AreaChart(
                values = series.observations.map { it.value },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
