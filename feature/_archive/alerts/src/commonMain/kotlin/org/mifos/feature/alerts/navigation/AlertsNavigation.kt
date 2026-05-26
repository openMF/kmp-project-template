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

package org.mifos.feature.alerts.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import org.mifos.feature.alerts.ui.PriceAlertsListScreen
import org.mifos.feature.alerts.ui.SetPriceAlertScreen
import template.core.base.ui.nav.composableWithPushTransitions

@Serializable
data object AlertsGraphRoute

@Serializable
data object PriceAlertsListRoute

@Serializable
data object SetPriceAlertRoute

fun NavController.navigateToAlerts(navOptions: NavOptions? = null) {
    navigate(route = AlertsGraphRoute, navOptions = navOptions)
}

fun NavGraphBuilder.alertsGraph(navController: NavController) {
    navigation<AlertsGraphRoute>(startDestination = PriceAlertsListRoute) {
        composableWithPushTransitions<PriceAlertsListRoute> {
            PriceAlertsListScreen(
                onBackClick = navController::popBackStack,
                onNewAlertClick = { navController.navigate(SetPriceAlertRoute) },
            )
        }
        composableWithPushTransitions<SetPriceAlertRoute> {
            SetPriceAlertScreen(onBackClick = navController::popBackStack)
        }
    }
}
