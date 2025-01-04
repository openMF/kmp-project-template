/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import org.mifos.feature.home.HOME_ROUTE
import org.mifos.feature.home.homeScreen
import org.mifos.feature.profile.profileScreen
import org.mifos.feature.settings.notificationScreen
import org.mifos.feature.settings.settingsScreen
import org.mifos.shared.ui.MifosAppState

@Composable
internal fun MifosNavHost(
    appState: MifosAppState,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController

    NavHost(
        route = MifosNavGraph.MAIN_GRAPH,
        startDestination = HOME_ROUTE,
        navController = navController,
        modifier = modifier,
    ) {
        homeScreen()

        profileScreen()

        settingsScreen(
            onBackClick = navController::popBackStack,
        )

        notificationScreen(
            onBackClick = navController::popBackStack,
        )
    }
}
