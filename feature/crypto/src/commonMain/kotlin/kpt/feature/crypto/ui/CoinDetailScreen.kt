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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.designsystem.component.HeroCard
import kpt.core.base.ui.freshness.FreshnessIndicator
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.component.AmountDisplay
import kpt.core.designsystem.theme.spacing
import kpt.feature.crypto.generated.resources.Res
import kpt.feature.crypto.generated.resources.screens_crypto_coin_detail_about_label
import kpt.feature.crypto.generated.resources.screens_crypto_coin_detail_back_cd
import kpt.feature.crypto.generated.resources.screens_crypto_coin_detail_circulating_label
import kpt.feature.crypto.generated.resources.screens_crypto_coin_detail_high_label
import kpt.feature.crypto.generated.resources.screens_crypto_coin_detail_low_label
import kpt.feature.crypto.generated.resources.screens_crypto_coin_detail_market_cap_label
import kpt.feature.crypto.generated.resources.screens_crypto_coin_detail_max_supply_label
import kpt.feature.crypto.generated.resources.screens_crypto_coin_detail_max_supply_unlimited
import kpt.feature.crypto.generated.resources.screens_crypto_coin_detail_rank_label
import kpt.feature.crypto.generated.resources.screens_crypto_coin_detail_rank_value
import kpt.feature.crypto.generated.resources.screens_crypto_coin_detail_refresh_cd
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs
import kotlin.math.round

/**
 * **Coin detail** — the drill-down target for a tapped `CoinMarketsScreen` row.
 *
 * Consumes `CryptoRepository.coinDetailStream` (a `NETWORK_WITH_CACHE`
 * `ScreenDataStream<CoinDetail>` backed by `CoinDetailStore`) directly through
 * the framework's `ScreenContent(stream = …)`. The hero shows the current price;
 * a stats card lists rank / market-cap / 24h high-low / supply; the "About" card
 * renders the coin description. State, freshness and retry are handled by the
 * framework — mirrors `feature/rates`'s `InterestRateDetailScreen`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CoinDetailScreen(
    coinId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CoinDetailViewModel = koinViewModel { parametersOf(coinId) },
) {
    val freshness by viewModel.freshness.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.testTag(TestTags.DETAIL_SCREEN),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(coinId)
                        FreshnessIndicator(
                            signal = freshness,
                            onRefresh = viewModel::onRefresh,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.screens_crypto_coin_detail_back_cd),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(Res.string.screens_crypto_coin_detail_refresh_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        ScreenContent(
            stream = viewModel.detail,
            onRetry = viewModel::onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { coin, _ ->
            val sp = MaterialTheme.spacing
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = sp.lg, vertical = sp.md),
                verticalArrangement = Arrangement.spacedBy(sp.md),
            ) {
                HeroCard {
                    AmountDisplay(
                        amountText = formatUsd(coin.currentPrice),
                        label = "${coin.name} (${coin.symbol.uppercase()})",
                        supporting = {
                            Text(
                                text = formatChange(coin.priceChangePercent24h),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                    )
                }

                AppCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(sp.sm),
                        verticalArrangement = Arrangement.spacedBy(sp.sm),
                    ) {
                        StatRow(
                            label = stringResource(Res.string.screens_crypto_coin_detail_rank_label),
                            value = stringResource(
                                Res.string.screens_crypto_coin_detail_rank_value,
                                coin.marketCapRank,
                            ),
                        )
                        HorizontalDivider()
                        StatRow(
                            label = stringResource(Res.string.screens_crypto_coin_detail_market_cap_label),
                            value = formatCompactUsd(coin.marketCap.toDouble()),
                        )
                        HorizontalDivider()
                        StatRow(
                            label = stringResource(Res.string.screens_crypto_coin_detail_high_label),
                            value = formatUsd(coin.high24h),
                        )
                        HorizontalDivider()
                        StatRow(
                            label = stringResource(Res.string.screens_crypto_coin_detail_low_label),
                            value = formatUsd(coin.low24h),
                        )
                        HorizontalDivider()
                        StatRow(
                            label = stringResource(Res.string.screens_crypto_coin_detail_circulating_label),
                            value = formatCompact(coin.circulatingSupply),
                        )
                        HorizontalDivider()
                        StatRow(
                            label = stringResource(Res.string.screens_crypto_coin_detail_max_supply_label),
                            value = coin.maxSupply?.let { formatCompact(it) }
                                ?: stringResource(Res.string.screens_crypto_coin_detail_max_supply_unlimited),
                        )
                    }
                }

                if (coin.description.isNotBlank()) {
                    Text(
                        text = stringResource(Res.string.screens_crypto_coin_detail_about_label),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    AppCard {
                        Text(
                            text = coin.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(sp.sm),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

/**
 * Locale-free USD formatter — two decimals, `$` prefix. Safe on Kotlin/JS and
 * Kotlin/wasmJs where `java.util.Locale` is unavailable (same constraint as
 * `CoinMarketsScreen.formatPrice`).
 */
private fun formatUsd(value: Double): String {
    val cents = round(value * 100.0).toLong()
    val whole = cents / 100
    val frac = abs(cents % 100)
    val fracStr = if (frac < 10) "0$frac" else frac.toString()
    return "$$whole.$fracStr"
}

/** Locale-free signed percent, two decimals. */
private fun formatChange(pct: Double): String {
    val sign = if (pct >= 0.0) "+" else ""
    val rounded = round(pct * 100.0) / 100.0
    return "$sign$rounded%"
}

/** Compact `$`-prefixed magnitude (K / M / B / T) for large monetary values. */
private fun formatCompactUsd(value: Double): String = "$" + formatCompact(value)

/** Compact magnitude formatter (K / M / B / T), locale-free, two decimals. */
private fun formatCompact(value: Double): String {
    val a = abs(value)
    return when {
        a >= 1_000_000_000_000.0 -> "${round2(value / 1_000_000_000_000.0)}T"
        a >= 1_000_000_000.0 -> "${round2(value / 1_000_000_000.0)}B"
        a >= 1_000_000.0 -> "${round2(value / 1_000_000.0)}M"
        a >= 1_000.0 -> "${round2(value / 1_000.0)}K"
        else -> round2(value).toString()
    }
}

private fun round2(value: Double): Double = round(value * 100.0) / 100.0
