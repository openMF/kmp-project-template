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

package kpt.feature.cloudtodo.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.cloudtodo.ui.CloudTodoScreen

@Serializable
data object CloudTodoRoute

fun NavController.navigateToCloudTodo() = navigate(CloudTodoRoute)

/**
 * Dev-only destination for the Store5 write-path demo.
 *
 * @param onResolveConflict hoisted so this module does NOT depend on feature/settings. The caller
 *   (ShowcaseRegistry) supplies `navController.navigateToSyncAndDrafts()` — the shipped conflict
 *   surface. Keeping the dependency at the registry rather than inside the feature is what stops
 *   this demo from either duplicating that screen or coupling two feature modules together.
 */
fun NavGraphBuilder.cloudTodoGraph(
    navController: NavController,
    onResolveConflict: () -> Unit,
) {
    composableWithPushTransitions<CloudTodoRoute> {
        CloudTodoScreen(
            onBackClick = { navController.popBackStackSafely() },
            onResolveConflict = onResolveConflict,
        )
    }
}
