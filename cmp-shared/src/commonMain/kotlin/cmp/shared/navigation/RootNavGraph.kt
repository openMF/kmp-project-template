/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cmp.shared.ui.App
import org.mifos.core.data.utils.NetworkMonitor
import org.mifos.core.data.utils.TimeZoneMonitor

@Composable
internal fun RootNavGraph(
    networkMonitor: NetworkMonitor,
    timeZoneMonitor: TimeZoneMonitor,
    navHostController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        route = NavGraphRoute.ROOT_GRAPH,
        modifier = modifier,
    ) {
        composable(NavGraphRoute.MAIN_GRAPH) {
            App(
                networkMonitor = networkMonitor,
                timeZoneMonitor = timeZoneMonitor,
            )
        }
    }
}
