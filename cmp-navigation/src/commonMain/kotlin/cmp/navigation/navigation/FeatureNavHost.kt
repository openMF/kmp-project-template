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
import androidx.navigation.compose.composable
import org.mifos.feature.home.EditTaskDestination
import org.mifos.feature.home.TasksDestination
import org.mifos.feature.home.task.EditTaskScreen
import org.mifos.feature.home.tasks.TasksScreen
import org.mifos.feature.settings.navigateToSettings
import org.mifos.feature.settings.settingsScreen

@Composable
internal fun FeatureNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        route = NavGraphRoute.MAIN_GRAPH,
        startDestination = TasksDestination.route,
        navController = navController,
        modifier = modifier,
    ) {
        composable(route = TasksDestination.route) {
            TasksScreen(
                onAddNewTask = { navController.navigate("${EditTaskDestination.route}/${0}") },
                onSettingsClick = { navController.navigateToSettings() },
                onTaskClick = { navController.navigate("${EditTaskDestination.route}/${it.id}") },
            )
        }

        composable(
            route = EditTaskDestination.routeWithArgs,
            arguments = EditTaskDestination.arguments,
        ) {
            EditTaskScreen(
                navigateBack = { navController.popBackStack() },
                onTaskSaved = { navController.popBackStack() },
            )
        }

        settingsScreen(
            onBackClick = navController::popBackStack,
        )
    }
}
