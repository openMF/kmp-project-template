/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.addtowatchlist.ui

/** Stable test tags for the embedded add-to-watchlist star toggle. */
object TestTags {
    /**
     * Test tags for the add-to-watchlist sheet. Constants rather than literals so a UI test and the composable cannot
     * drift.
     */
    object AddToWatchlist {
        /** Per-coin star toggle: `watchlist_star_{coinId}` (mirrors idea-layer ui.yaml). */
        const val STAR_PREFIX = "watchlist_star_"
    }
}
