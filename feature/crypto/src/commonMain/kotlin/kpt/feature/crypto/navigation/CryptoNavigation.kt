/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MatchingDeclarationName")

package kpt.feature.crypto.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.crypto.ui.CoinDetailScreen
import kpt.feature.crypto.ui.CoinMarketsRoute

/** Root of the crypto feature graph. Navigated to via [navigateToCrypto]. */
@Serializable
data object CryptoGraphRoute

/**
 * Nav destination for the CoinMarkets list.
 *
 * Named `CoinMarketsListRoute` (not the shorter `CoinMarketsRoute`) so it does
 * not collide with the [CoinMarketsRoute] Composable — the file-structure
 * contract of sub-plan T7 puts both under the same simple name; the Composable
 * is the user-facing entry point (Phase 4 `File Structure` table row for
 * `CoinMarketsRoute.kt`) and this destination adopts the sibling convention
 * from `LoansNavigation.PersonalLoansListRoute`. See DEVIATIONS in the sub-plan
 * hand-off notes.
 */
@Serializable
data object CoinMarketsListRoute

/**
 * Per-coin detail destination — the drill-down target for a tapped CoinMarkets row.
 *
 * @property coinId CoinGecko coin id, e.g. `"bitcoin"`.
 */
@Serializable
data class CoinDetailRoute(val coinId: String)

/** Convenience navigator so callers do not construct the route type by hand. */
fun NavController.navigateToCrypto(navOptions: NavOptions? = null) {
    navigate(route = CryptoGraphRoute, navOptions = navOptions)
}

fun NavController.navigateToCoinDetail(coinId: String, navOptions: NavOptions? = null) {
    navigate(route = CoinDetailRoute(coinId = coinId), navOptions = navOptions)
}

/**
 * Wires the crypto graph into the parent NavHost. Mirrors
 * `RatesNavigation.ratesGraph` shape: a list destination whose row-tap navigates
 * to a per-item detail destination inside the same `navigation<CryptoGraphRoute>`
 * block.
 *
 * The coin-row `onCoinClick(coinId)` now navigates to [CoinDetailRoute], which
 * renders [CoinDetailScreen] — consuming the `coinDetailStream` / `CoinDetailStore`
 * read path that was already wired at HEAD (turning the former dead click +
 * orphan store into a real drill-down).
 */
fun NavGraphBuilder.cryptoGraph(navController: NavController) {
    navigation<CryptoGraphRoute>(startDestination = CoinMarketsListRoute) {
        composableWithPushTransitions<CoinMarketsListRoute> {
            CoinMarketsRoute(
                onBackClick = { navController.popBackStackSafely() },
                onCoinClick = { coinId ->
                    navController.navigateToCoinDetail(coinId = coinId)
                },
            )
        }
        composableWithPushTransitions<CoinDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<CoinDetailRoute>()
            CoinDetailScreen(
                coinId = route.coinId,
                onBackClick = { navController.popBackStackSafely() },
            )
        }
    }
}
