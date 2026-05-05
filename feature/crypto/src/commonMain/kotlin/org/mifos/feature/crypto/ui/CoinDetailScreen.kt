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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import org.mifos.core.common.formatGrouped
import template.core.base.ui.ScreenContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    coinId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CoinDetailViewModel = koinViewModel { parametersOf(coinId) },
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Coin Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        ) { coin, _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "${coin.name} (${coin.symbol.uppercase()})",
                    style = MaterialTheme.typography.headlineMedium,
                )

                StatRow("Price", "$${coin.currentPrice.formatGrouped(2)}")
                StatRow("24h Change", "${coin.priceChangePercent24h.formatDecimal(2)}%")
                StatRow("Market Cap", "$${coin.marketCap.formatGrouped()}")
                StatRow("Rank", "#${coin.marketCapRank}")
                StatRow("24h High", "$${coin.high24h.formatGrouped(2)}")
                StatRow("24h Low", "$${coin.low24h.formatGrouped(2)}")
                StatRow("Circulating", coin.circulatingSupply.formatGrouped(0))
                coin.maxSupply?.let { StatRow("Max Supply", it.formatGrouped(0)) }

                if (coin.description.isNotBlank()) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        text = coin.description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
