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

import androidx.navigation.NavType.Companion.IntType
import androidx.navigation.navArgument

/**
 * Represents a navigation destination in the TaskMinder app.
 * Each destination corresponds to a screen or feature in the app, with a unique route identifier.
 */
interface TaskMinderDestination {
    /**
     * The route associated with the destination, used for navigation purposes.
     * This route is typically used in a navigation graph to define the screen's path.
     */
    val route: String
}

/**
 * A destination representing the tasks overview screen.
 * This screen shows a list of tasks and their statuses.
 */
object TasksDestination : TaskMinderDestination {
    override val route: String = "tasks"
}

/**
 * A destination representing the screen for adding or editing a specific task.
 * This screen allows users to add details of a new task or modify the details of an existing task.
 */
object EditTaskDestination : TaskMinderDestination {
    override val route: String = "edit_task"

    /**
     * The argument key used to pass the task ID in the route.
     */
    const val TASK_ID_ARG = "taskId"

    /**
     * The route pattern that includes the task ID as a dynamic argument.
     */
    val routeWithArgs = "$route/{$TASK_ID_ARG}"

    /**
     * List of navigation arguments for the edit task screen.
     * This argument is used to pass the task ID for editing.
     */
    val arguments = listOf(
        navArgument(TASK_ID_ARG) {
            type = IntType
        },
    )
}
