/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import org.mifos.feature.home.HOME_GRAPH
import org.mifos.feature.home.homeGraph
import org.mifos.feature.profile.profileScreen
import org.mifos.feature.settings.notificationScreen
import org.mifos.feature.settings.settingsScreen

@Composable
internal fun FeatureNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        route = NavGraphRoute.MAIN_GRAPH,
        startDestination = HOME_GRAPH,
        navController = navController,
        modifier = modifier,
    ) {
        homeGraph(navController = navController)

        profileScreen()

        settingsScreen(
            onBackClick = navController::popBackStack,
        )

        notificationScreen(
            onBackClick = navController::popBackStack,
        )
    }
}
