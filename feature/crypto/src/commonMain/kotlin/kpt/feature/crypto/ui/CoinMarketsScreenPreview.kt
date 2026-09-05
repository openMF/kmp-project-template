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

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.crypto.CoinMarket
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier — see CoinDetailScreenPreview.kt for the
 * full rationale, including why the literals below carry `// i18n:skip` (preview fixture data,
 * never reachable from the running app).
 *
 * `CoinMarketsScreen` is not previewed directly: it resolves its ViewModel through Koin AND owns a
 * `PagingScreenStream`, neither of which exists outside a running graph.
 */

private fun sampleCoin(
    priceChangePercent24h: Double,
) = CoinMarket(
    id = "bitcoin", // i18n:skip
    symbol = "btc", // i18n:skip
    name = "Bitcoin", // i18n:skip
    imageUrl = "", // i18n:skip
    currentPrice = 50_000.0,
    marketCap = 1_284_000_000_000L,
    marketCapRank = 1,
    priceChangePercent24h = priceChangePercent24h,
    high24h = 51_000.0,
    low24h = 49_000.0,
)

@Preview
@Composable
internal fun CoinMarketRowGainerPreview() {
    KptTheme {
        CoinMarketRow(coin = sampleCoin(priceChangePercent24h = 4.21), onClick = {})
    }
}

@Preview
@Composable
internal fun CoinMarketRowLoserPreview() {
    // The sign of the 24h change drives the row's tint. Rendering only a gainer would leave the
    // loss treatment — the half a user notices — completely uncovered.
    KptTheme {
        CoinMarketRow(coin = sampleCoin(priceChangePercent24h = -7.85), onClick = {})
    }
}
