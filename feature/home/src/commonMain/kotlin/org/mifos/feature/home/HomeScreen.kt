/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home

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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.common.formatDecimal
import org.mifos.core.common.formatGrouped
import org.mifos.feature.home.ui.HomeAction
import org.mifos.feature.home.ui.HomeViewModel
import template.core.base.store.screen.ScreenState
import template.core.base.ui.screen.ScreenContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onSettingsClick: () -> Unit,
    onNavigateToRates: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCrypto: () -> Unit,
    onNavigateToEmi: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fintech Demo") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
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
            // Live widgets — combine showcase. Each card observes a slice of
            // HomeUiState and renders its own per-card ScreenState transitions.
            TopMoversCard(
                state = state.topMovers,
                onRetry = { viewModel.trySendAction(HomeAction.RetryTopMovers) },
                onSeeAll = onNavigateToCrypto,
            )

            ExchangeRateCard(
                state = state.exchangeRate,
                onRetry = { viewModel.trySendAction(HomeAction.RetryExchangeRate) },
                onSeeAll = onNavigateToRates,
            )

            // Navigation cards — preserved from the original menu so existing
            // features stay discoverable. Sub-plans 03/04 will add Watchlist
            // and Alerts cards to the widget section above when their data is on dev.
            Text(
                text = "Quick access",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            FeatureCard(
                title = "Currency Rates",
                subtitle = "Store + Search filter + emptyIfContent",
                icon = Icons.Default.CurrencyExchange,
                onClick = onNavigateToRates,
            )
            FeatureCard(
                title = "Rate History",
                subtitle = "Dynamic key flow + auto-refresh",
                icon = Icons.Default.Timeline,
                onClick = onNavigateToHistory,
            )
            FeatureCard(
                title = "Crypto Watchlist",
                subtitle = "PagingScreenStream + infinite scroll",
                icon = Icons.AutoMirrored.Default.ShowChart,
                onClick = onNavigateToCrypto,
            )
            FeatureCard(
                title = "EMI Calculator",
                subtitle = "Pure local state (no Store)",
                icon = Icons.Default.Calculate,
                onClick = onNavigateToEmi,
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TopMoversCard(
    state: ScreenState<List<*>>,
    onRetry: () -> Unit,
    onSeeAll: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Top Movers", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onSeeAll),
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                ScreenContent(
                    state = state,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                ) { items, _ ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items.take(5).forEach { rawItem ->
                            val coin = rawItem as? org.mifos.core.model.crypto.CoinMarket
                                ?: return@forEach
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "${coin.symbol.uppercase()}  ${coin.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                val pct = coin.priceChangePercent24h.formatDecimal(2)
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (coin.priceChangePercent24h >= 0) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExchangeRateCard(
    state: ScreenState<org.mifos.core.model.currency.ExchangeRates>,
    onRetry: () -> Unit,
    onSeeAll: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "USD Exchange",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onSeeAll),
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                ScreenContent(
                    state = state,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize(),
                ) { rates, _ ->
                    val keyRates = listOf("EUR", "GBP", "INR")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        keyRates.forEach { code ->
                            val value = rates.rates[code] ?: return@forEach
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "USD → $code",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = value.formatGrouped(4),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
