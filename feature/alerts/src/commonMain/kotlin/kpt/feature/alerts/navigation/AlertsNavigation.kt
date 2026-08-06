/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.alerts.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.alerts.ui.AlertCreateScreen
import kpt.feature.alerts.ui.AlertsListScreen

/** Root of the alerts nav graph. */
@Serializable
data object AlertsGraphRoute

/** Alerts list entry destination. */
@Serializable
data object AlertsListRoute

/** Offline-first create-alert form. */
@Serializable
data object AlertCreateRoute

fun NavController.navigateToAlertsGraph(navOptions: NavOptions? = null) {
    navigate(route = AlertsGraphRoute, navOptions = navOptions)
}

/**
 * Alerts feature's navigation graph — list → create. The host (cmp-navigation) wires this
 * into the top-level RootNavGraph by calling [alertsGraph] inside its own `NavHost { ... }`.
 */
fun NavGraphBuilder.alertsGraph(navController: NavController) {
    navigation<AlertsGraphRoute>(startDestination = AlertsListRoute) {
        composableWithPushTransitions<AlertsListRoute> {
            AlertsListScreen(
                onBackClick = { navController.popBackStackSafely() },
                onCreateClick = { navController.navigate(AlertCreateRoute) },
            )
        }
        composableWithPushTransitions<AlertCreateRoute> {
            AlertCreateScreen(
                onBackClick = { navController.popBackStackSafely() },
                onSubmitted = { navController.popBackStackSafely() },
            )
        }
    }
}
