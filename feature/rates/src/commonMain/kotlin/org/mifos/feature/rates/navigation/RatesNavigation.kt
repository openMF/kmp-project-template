/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.rates.navigation

import androidx.navigation.NavController
import template.core.base.ui.nav.popBackStackSafely
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.mifos.feature.rates.ui.InterestRateDetailScreen
import org.mifos.feature.rates.ui.InterestRatesScreen
import template.core.base.ui.nav.composableWithPushTransitions

/** Parent graph route for the B7 Interest Rate Tracker. */
@Serializable
data object RatesGraphRoute

/** Default destination — the multi-series dashboard. */
@Serializable
data object RatesListRoute

/**
 * Per-series detail destination.
 *
 * @property seriesId FRED series id, e.g. `"DFF"`.
 */
@Serializable
data class RateDetailRoute(val seriesId: String)

fun NavController.navigateToRates(navOptions: NavOptions? = null) {
    navigate(route = RatesGraphRoute, navOptions = navOptions)
}

fun NavController.navigateToRateDetail(seriesId: String, navOptions: NavOptions? = null) {
    navigate(route = RateDetailRoute(seriesId = seriesId), navOptions = navOptions)
}

/**
 * Add the B7 Interest Rate Tracker nav graph to the host `NavGraphBuilder`.
 *
 * Host apps wire this from their root navigation by calling `ratesGraph(navController)`.
 */
fun NavGraphBuilder.ratesGraph(navController: NavController) {
    navigation<RatesGraphRoute>(startDestination = RatesListRoute) {
        composableWithPushTransitions<RatesListRoute> {
            InterestRatesScreen(
                onBackClick = { navController.popBackStackSafely() },
                onSeriesClick = { seriesId ->
                    navController.navigateToRateDetail(seriesId = seriesId)
                },
            )
        }
        composableWithPushTransitions<RateDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<RateDetailRoute>()
            InterestRateDetailScreen(
                seriesId = route.seriesId,
                onBackClick = { navController.popBackStackSafely() },
            )
        }
    }
}
