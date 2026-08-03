/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.watchlist.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.watchlist.ui.WatchlistScreen

/** Watchlist entry destination. */
@Serializable
data object WatchlistRoute

fun NavController.navigateToWatchlist(navOptions: NavOptions? = null) {
    navigate(route = WatchlistRoute, navOptions = navOptions)
}

/**
 * Watchlist feature's navigation graph. The host (cmp-navigation) wires this into
 * the top-level RootNavGraph by calling [watchlistGraph] inside its own
 * `NavHost { ... }` builder once this feature is enabled.
 */
fun NavGraphBuilder.watchlistGraph(navController: NavController) {
    composableWithPushTransitions<WatchlistRoute> {
        WatchlistScreen(
            onBackClick = { navController.popBackStackSafely() },
        )
    }
}
