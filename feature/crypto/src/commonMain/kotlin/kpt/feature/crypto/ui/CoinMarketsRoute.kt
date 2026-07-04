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

/**
 * Thin nav-facing wrapper around [CoinMarketsScreen] that binds a single stable
 * `routeKey` for the retained-state contract. Every consumer that reaches
 * CoinMarkets via `cryptoGraph(...)` shares the same `feature.crypto.coinMarkets`
 * route key — so returning to the graph replays the same persisted cursor and
 * scroll position. Split from the Screen so the retained-state key is not a
 * caller concern (satisfies RULE-IMPL-NAVIGATION-CONNECTED-001 NAV-1: this
 * function has ≥1 external caller in `CryptoNavigation.cryptoGraph`).
 */
@Composable
fun CoinMarketsRoute(
    onBackClick: () -> Unit,
    onCoinClick: (String) -> Unit,
) {
    CoinMarketsScreen(
        routeKey = ROUTE_KEY,
        onBackClick = onBackClick,
        onCoinClick = onCoinClick,
    )
}

/** Stable [rememberRetainedScreenState] key for the CoinMarkets screen. */
private const val ROUTE_KEY: String = "feature.crypto.coinMarkets"
