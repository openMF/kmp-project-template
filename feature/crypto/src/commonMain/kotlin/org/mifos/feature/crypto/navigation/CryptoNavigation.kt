/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.crypto.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.mifos.feature.crypto.ui.CoinDetailScreen
import org.mifos.feature.crypto.ui.CryptoWatchlistScreen
import template.core.base.ui.nav.composableWithPushTransitions

@Serializable
data object CryptoGraphRoute

@Serializable
data object CryptoWatchlistRoute

@Serializable
data class CoinDetailRoute(val coinId: String)

fun NavController.navigateToCrypto(navOptions: NavOptions? = null) {
    navigate(route = CryptoGraphRoute, navOptions = navOptions)
}

fun NavGraphBuilder.cryptoGraph(navController: NavController) {
    navigation<CryptoGraphRoute>(startDestination = CryptoWatchlistRoute) {
        composableWithPushTransitions<CryptoWatchlistRoute> {
            CryptoWatchlistScreen(
                onCoinClick = { coinId -> navController.navigate(CoinDetailRoute(coinId)) },
                onBackClick = navController::popBackStack,
            )
        }

        composableWithPushTransitions<CoinDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<CoinDetailRoute>()
            CoinDetailScreen(
                coinId = route.coinId,
                onBackClick = navController::popBackStack,
            )
        }
    }
}
