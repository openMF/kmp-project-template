/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.crypto.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.common.formatDecimal
import org.mifos.core.common.formatGrouped
import org.mifos.core.model.fintech.CoinMarket
import org.mifos.core.ui.scaffold.KptScaffold
import org.mifos.core.ui.scaffold.rememberKptPullToRefreshState
import template.core.base.ui.PagingScreenContent

@Composable
fun CryptoWatchlistScreen(
    onCoinClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CryptoWatchlistViewModel = koinViewModel(),
) {
    // KptScaffold owns pull-to-refresh (Material 3 pullToRefresh + Indicator at the top).
    // The bridge wires KptPullToRefreshState to viewModel.pagingStream so:
    //   - isRefreshing tracks ScreenState.Content.freshness == UPDATING
    //   - onRefresh invokes pagingStream.refresh() (clears error, refetches page 0 from network)
    KptScaffold(
        showNavigationIcon = true,
        onNavigationIconClick = onBackClick,
        title = "Crypto Watchlist",
        modifier = modifier,
        pullToRefreshState = rememberKptPullToRefreshState(viewModel.pagingStream),
    ) {
        // PagingScreenContent owns LazyColumn, load-more trigger, LoadMoreFooter.
        // It does NOT wrap a PullToRefreshBox — KptScaffold handles the gesture.
        PagingScreenContent(
            pagingStream = viewModel.pagingStream,
            onRetry = viewModel::onRetry,
            modifier = Modifier.fillMaxSize(),
        ) { coins ->
            items(coins) { coin ->
                CoinItem(coin = coin, onClick = { onCoinClick(coin.id) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun CoinItem(coin: CoinMarket, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = coin.name, style = MaterialTheme.typography.titleSmall)
            Text(
                text = coin.symbol.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${coin.currentPrice.formatGrouped(2)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "${coin.priceChangePercent24h.formatDecimal(2)}%",
                style = MaterialTheme.typography.bodySmall,
                color = if (coin.priceChangePercent24h >= 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
    }
}
