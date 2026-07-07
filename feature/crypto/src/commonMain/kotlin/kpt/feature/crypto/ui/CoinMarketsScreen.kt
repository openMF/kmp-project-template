/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.crypto.ui

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kpt.core.base.ui.paging.PagingScreenContent
import kpt.core.model.crypto.CoinMarket
import kpt.feature.crypto.generated.resources.Res
import kpt.feature.crypto.generated.resources.screens_crypto_coin_markets_back_cd
import kpt.feature.crypto.generated.resources.screens_crypto_coin_markets_load_more_end
import kpt.feature.crypto.generated.resources.screens_crypto_coin_markets_load_more_loading
import kpt.feature.crypto.generated.resources.screens_crypto_coin_markets_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs
import kotlin.math.round

/**
 * CoinMarkets — CoinGecko-backed paged list.
 *
 * The `PagingScreenContent` overload owns a `rememberLazyListState()` internally
 * (which uses `rememberSaveable` with `LazyListState.Saver`), so scroll position
 * survives tab-switch and config-change via Navigation's state-holder and Android's
 * saved-instance-state Bundle — no per-screen retention code required.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinMarketsScreen(
    onBackClick: () -> Unit,
    onCoinClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CoinMarketsViewModel = koinViewModel(),
) {
    val loadingLabel = stringResource(Res.string.screens_crypto_coin_markets_load_more_loading)
    val endLabel = stringResource(Res.string.screens_crypto_coin_markets_load_more_end)
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.screens_crypto_coin_markets_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                Res.string.screens_crypto_coin_markets_back_cd,
                            ),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        PagingScreenContent(
            pagingStream = viewModel.pagingStream,
            onRetry = viewModel::retry,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag(TestTags.LIST),
            loadingMessage = loadingLabel,
            endMessage = endLabel,
        ) { coins ->
            items(items = coins, key = { it.id }) { coin ->
                CoinMarketRow(
                    coin = coin,
                    modifier = Modifier.testTag(TestTags.ROW),
                    onClick = { onCoinClick(coin.id) },
                )
            }
        }
    }
}

@Composable
private fun CoinMarketRow(coin: CoinMarket, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = coin.symbol.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Text(
                text = coin.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatPrice(coin.currentPrice),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Text(
                text = formatChange(coin.priceChangePercent24h),
                style = MaterialTheme.typography.bodySmall,
                color = if (coin.priceChangePercent24h >= 0.0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
    }
}

/**
 * Locale-free price formatter — safe on Kotlin/JS and Kotlin/wasmJs where
 * `java.util.Locale` is not available. Rounds to two decimals, prefixes `$`.
 * Consumer forks with a Money i18n library can override the row rendering.
 */
private fun formatPrice(value: Double): String {
    val cents = round(value * 100.0).toLong()
    val whole = cents / 100
    val frac = abs(cents % 100)
    val fracStr = if (frac < 10) "0$frac" else frac.toString()
    return "$$whole.$fracStr"
}

/**
 * Locale-free percent formatter — two decimals, explicit sign. Same portability
 * constraint as [formatPrice].
 */
private fun formatChange(pct: Double): String {
    val sign = if (pct >= 0.0) "+" else ""
    val rounded = round(pct * 100.0) / 100.0
    return "$sign$rounded%"
}
