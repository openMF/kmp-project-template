/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.base.store.screen.DataFreshness
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.formatDecimal
import kpt.core.common.formatTimeAgo
import kpt.core.designsystem.component.AmountDisplay
import kpt.core.designsystem.theme.spacing
import kpt.core.model.economic.IndicatorKind
import kpt.feature.macro.ui.components.Sparkline
import kpt.feature.macro.ui.components.displayName
import kpt.feature.macro.ui.components.headlineValue
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Full-history view for a single (country, indicator) pair.
 *
 * The top half is a full-width sparkline of the historical series; below
 * it, a table of year/value rows for users who want exact numbers. The
 * dashboard's per-card sparkline is a 10-year hint; this screen surfaces
 * the entire range the toolkit fetches (25 years by default — see
 * [kpt.core.store.economic.impl.MacroIndicatorKey.years]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroIndicatorDetailScreen(
    countryCode: String,
    indicatorKind: IndicatorKind,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MacroIndicatorDetailViewModel = koinViewModel {
        parametersOf(countryCode, indicatorKind)
    },
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("${indicatorKind.displayName()} • $countryCode") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        ) { indicator, freshness ->
            if (freshness == DataFreshness.STALE) {
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
                modifier = Modifier.fillMaxSize().padding(sp.lg),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(sp.md),
            ) {
                HeroCard {
                    AmountDisplay(
                        amountText = indicator.headlineValue(),
                        label = indicatorKind.displayName(),
                        supporting = {
                            Text(
                                text = "Source: ${indicator.source}",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
                AppCard {
                    Sparkline(
                        values = indicator.observations.map { it.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(sp.md),
                    )
                }
                Text(
                    text = "Year-by-year",
                    style = MaterialTheme.typography.titleSmall,
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = indicator.observations.asReversed(),
                        key = { it.year },
                    ) { obs ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = sp.sm),
                        ) {
                            Text(
                                text = obs.year.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = sp.md),
                            )
                            Text(
                                text = obs.value?.formatDecimal(2) ?: "—",
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

@OptIn(ExperimentalTime::class)
@Composable
private fun OfflineDataBanner(
    fetchedAt: Instant?,
    modifier: Modifier = Modifier,
) {
    val label = formatTimeAgo(fetchedAt)
        ?.let { "No network · Updated $it" }
        ?: "No network · Cached data"
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
