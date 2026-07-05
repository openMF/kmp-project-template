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
 * Thin nav-facing wrapper around [CoinMarketsScreen]. Split from the Screen so
 * `CryptoNavigation.cryptoGraph` has a stable no-arg composable target
 * (satisfies RULE-IMPL-NAVIGATION-CONNECTED-001 NAV-1: this function has ≥1
 * external caller in `CryptoNavigation.cryptoGraph`).
 */
@Composable
fun CoinMarketsRoute(
    onBackClick: () -> Unit,
    onCoinClick: (String) -> Unit,
) {
    CoinMarketsScreen(
        onBackClick = onBackClick,
        onCoinClick = onCoinClick,
    )
}
