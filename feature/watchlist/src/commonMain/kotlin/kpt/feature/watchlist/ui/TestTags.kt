/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.watchlist.ui

/**
 * Append-only test-tag registry for the watchlist feature.
 * Consumed by Compose UI tests in `feature/watchlist/src/commonTest/`.
 * APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).
 */
object TestTags {

    /** Tags for [WatchlistScreen]. */
    object Watchlist {
        /** Root scaffold — always rendered regardless of load state. */
        const val SCREEN: String = "watchlist_screen"

        /** One row per saved coin. Suffix with the coinId for per-row assertions. */
        const val ROW_PREFIX: String = "watchlist_row_"

        /** Per-row remove button. Suffix with the coinId. */
        const val REMOVE_PREFIX: String = "watchlist_remove_"
    }
}
