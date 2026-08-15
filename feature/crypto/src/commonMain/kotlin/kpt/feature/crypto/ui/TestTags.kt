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

/**
 * Test-tag registry for the crypto feature.
 *
 * Consumed by:
 *  - [CoinMarketsScreen] via `Modifier.testTag(...)`.
 *  - Maestro on-device flows in `maestro/screen-state/paging-restore.yaml`
 *    (references the `coinMarkets.list` / `coinMarkets.row` string constants
 *    directly via `assertVisible: { id: "..." }`).
 */
object TestTags {
    /**
     * Root [Scaffold] surface — always rendered regardless of paging state.
     * Stable assertion target for Compose UI tests.
     */
    const val SCREEN: String = "coinMarkets.screen"

    /** Scrollable [PagingScreenContent] LazyColumn. */
    const val LIST: String = "coinMarkets.list"

    /** Individual `CoinMarketRow` in the paging list. */
    const val ROW: String = "coinMarkets.row"

    /**
     * Root [Scaffold] surface of [CoinDetailScreen] — the drill-down target for
     * a tapped [ROW]. Stable assertion target regardless of stream state.
     */
    const val DETAIL_SCREEN: String = "coinDetail.screen"
}
