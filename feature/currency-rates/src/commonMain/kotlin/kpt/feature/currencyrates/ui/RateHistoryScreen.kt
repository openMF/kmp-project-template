/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.currencyrates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.store.freshness.FreshnessBand
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.freshness.FreshnessIndicator
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.formatDecimal
import kpt.core.common.formatTimeAgo
import kpt.core.designsystem.theme.spacing
import kpt.feature.currencyrates.generated.resources.Res
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_history_back_cd
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_history_currency_label
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_history_period_days
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_history_period_label
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_history_title
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_history_to_label
import kpt.feature.currencyrates.generated.resources.screens_currencyrates_history_usd_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RateHistoryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RateHistoryViewModel = koinViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val localState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val freshness by viewModel.freshness.collectAsStateWithLifecycle()

    val currencies = listOf("INR", "EUR", "GBP", "JPY", "AUD")
    val periods = listOf(7, 14, 30, 90)

    Scaffold(
        modifier = modifier.testTag(TestTags.RateHistory.ROOT),
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(stringResource(Res.string.screens_currencyrates_history_title))
                        FreshnessIndicator(
                            signal = freshness,
                            onRefresh = viewModel::onRetry,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_currencyrates_history_back_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ScreenContent(
                state = screenState,
                onRetry = viewModel::onRetry,
                modifier = Modifier.fillMaxSize(),
            ) { history, freshnessSignal ->
                if (freshnessSignal.band == FreshnessBand.Stale || freshnessSignal.band == FreshnessBand.VeryStale) {
                    val fetchedAt = (screenState as? ScreenState.Content)?.fetchedAt
                    OfflineDataBanner(
                        fetchedAt = fetchedAt,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.lg,
                            vertical = MaterialTheme.spacing.xs,
                        ),
                    )
                }
                val sp = MaterialTheme.spacing
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = sp.lg),
                    verticalArrangement = Arrangement.spacedBy(sp.sm),
                ) {
                    RateHistoryControls(
                        currencies = currencies,
                        periods = periods,
                        selectedCurrency = localState.targetCurrency,
                        selectedPeriod = localState.periodDays,
                        onSelectCurrency = {
                            viewModel.trySendAction(HistoryAction.SelectCurrency(it))
                        },
                        onSelectPeriod = {
                            viewModel.trySendAction(HistoryAction.SelectPeriod(it))
                        },
                    )

                    // Header \u2014 "USD \u2192 $to ($startDate to $endDate)" rendered with a
                    // Material Icon for the arrow. Outfit (the project's bundled font)
                    // does not include U+2192; Compose Icon is a vector path that
                    // renders correctly on every platform including wasmJs.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(sp.xs),
                    ) {
                        Text(
                            text = stringResource(Res.string.screens_currencyrates_history_usd_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = stringResource(
                                Res.string.screens_currencyrates_history_to_label,
                                history.to,
                                history.startDate.toString(),
                                history.endDate.toString(),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(history.rates) { point ->
                            // Per-row "$date \u2192 $value" \u2014 same Material Icon arrow.
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(sp.sm),
                                modifier = Modifier.padding(vertical = sp.sm),
                            ) {
                                Text(
                                    text = point.date.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = point.value.formatDecimal(4),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            } // \u2190 close ScreenContent { history, freshness -> ... }
        } // \u2190 close outer Column wrapper
    }
}

@OptIn(ExperimentalTime::class)
@Composable
internal fun OfflineDataBanner(fetchedAt: Instant?, modifier: Modifier = Modifier) {
    val label = formatTimeAgo(fetchedAt)
        ?.let { "No network \u00b7 Updated $it" }
        ?: "No network \u00b7 Cached data"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
internal fun RateHistoryControls(
    currencies: List<String>,
    periods: List<Int>,
    selectedCurrency: String,
    selectedPeriod: Int,
    onSelectCurrency: (String) -> Unit,
    onSelectPeriod: (Int) -> Unit,
) {
    val sp = MaterialTheme.spacing
    AppCard(modifier = Modifier.padding(top = sp.sm)) {
        Column(
            modifier = Modifier.padding(sp.md),
            verticalArrangement = Arrangement.spacedBy(sp.sm),
        ) {
            Text(
                stringResource(Res.string.screens_currencyrates_history_currency_label),
                style = MaterialTheme.typography.labelMedium,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(sp.sm)) {
                currencies.forEach { code ->
                    FilterChip(
                        selected = selectedCurrency == code,
                        onClick = { onSelectCurrency(code) },
                        label = { Text(code) },
                    )
                }
            }
            Text(
                stringResource(Res.string.screens_currencyrates_history_period_label),
                style = MaterialTheme.typography.labelMedium,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(sp.sm)) {
                periods.forEach { days ->
                    FilterChip(
                        selected = selectedPeriod == days,
                        onClick = { onSelectPeriod(days) },
                        label = {
                            Text(
                                stringResource(
                                    Res.string.screens_currencyrates_history_period_days,
                                    days,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}
