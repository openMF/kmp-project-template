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
    /** Scrollable [PagingScreenContent] LazyColumn. */
    const val LIST: String = "coinMarkets.list"

    /** Individual `CoinMarketRow` in the paging list. */
    const val ROW: String = "coinMarkets.row"
}
