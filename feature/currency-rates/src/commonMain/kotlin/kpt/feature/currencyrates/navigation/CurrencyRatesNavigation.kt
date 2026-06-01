/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.currencyrates.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import kpt.feature.currencyrates.ui.CurrencyRatesScreen
import kpt.feature.currencyrates.ui.RateHistoryScreen
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely

@Serializable
data object CurrencyRatesGraphRoute

@Serializable
data object CurrencyRatesRoute

@Serializable
data object RateHistoryRoute

fun NavController.navigateToCurrencyRates(navOptions: NavOptions? = null) {
    navigate(route = CurrencyRatesGraphRoute, navOptions = navOptions)
}

fun NavGraphBuilder.currencyRatesGraph(navController: NavController) {
    navigation<CurrencyRatesGraphRoute>(startDestination = CurrencyRatesRoute) {
        composableWithPushTransitions<CurrencyRatesRoute> {
            CurrencyRatesScreen(onBackClick = { navController.popBackStackSafely() })
        }

        composableWithPushTransitions<RateHistoryRoute> {
            RateHistoryScreen(onBackClick = { navController.popBackStackSafely() })
        }
    }
}

fun NavController.navigateToRateHistory(navOptions: NavOptions? = null) {
    navigate(route = RateHistoryRoute, navOptions = navOptions)
}
