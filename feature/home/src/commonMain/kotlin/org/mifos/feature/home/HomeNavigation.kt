/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation

const val HOME_ROUTE = "home_route"
private const val DESIGN_SYSTEM_SHOWCASE_ROUTE = "designsystem_showcase_route"
const val HOME_GRAPH = "home_graph"

fun NavController.navigateToHome(navOptions: NavOptions? = null) = navigate(HOME_GRAPH, navOptions)

private fun NavGraphBuilder.homeScreen(
    navigateToShowcase: () -> Unit,
) {
    composable(route = HOME_ROUTE) {
        HomeScreen(
            navigateToShowcase = navigateToShowcase,
        )
    }
}

private fun NavGraphBuilder.designSystemShowcaseScreen(
    navigateBack: () -> Unit,
) {
    composable(route = DESIGN_SYSTEM_SHOWCASE_ROUTE) {
        DesignSystemCatalogScreen(
            navigateBack = navigateBack,
        )
    }
}

fun NavGraphBuilder.homeGraph(
    navController: NavController,
    startDestination: String = HOME_ROUTE,
) {
    navigation(
        route = HOME_GRAPH,
        startDestination = startDestination,
    ) {
        homeScreen(
            navigateToShowcase = {
                navController.navigate(DESIGN_SYSTEM_SHOWCASE_ROUTE)
            },
        )

        designSystemShowcaseScreen(
            navigateBack = navController::navigateUp,
        )
    }
}
