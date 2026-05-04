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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.model.fintech.CoinMarket
import template.core.base.ui.PagingScreenContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoWatchlistScreen(
    onCoinClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CryptoWatchlistViewModel = koinViewModel(),
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Crypto Watchlist") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        // PagingScreenContent owns the LazyColumn, listState, load-more trigger,
        // and LoadMoreFooter (loading / error+retry / end-of-list). The screen just
        // declares per-item content. Offline-first decisions live in core-base/store
        // (DecisionEngine) — this screen has zero state-handling code.
        PagingScreenContent(
            pagingStream = viewModel.pagingStream,
            onRetry = viewModel::onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
                text = "$${"%,.2f".format(coin.currentPrice)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "${"%.2f".format(coin.priceChangePercent24h)}%",
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
