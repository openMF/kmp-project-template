/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithStayTransitions

/**
 * Route for the home GRAPH — what `navigateToHome` targets.
 *
 * Distinct from [HomeRoute] so a fork can nest extra destinations under home without the
 * bottom bar's selection tracking breaking.
 */
@Serializable
data object HomeDestination

/** Route for the home SCREEN itself, the graph's start destination. */
@Serializable
data object HomeRoute

/**
 * Navigates to the home graph.
 *
 * @param navOptions optional nav options, e.g. to clear the back stack after sign-in.
 */
fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(HomeDestination, navOptions)
}

/**
 * The backbone home graph. [homeBody] is the fork-owned home content (default supplied by
 * `cmp-navigation`'s `BackboneRegistry.homeBody`); this template graph carries zero demo imports.
 */
fun NavGraphBuilder.homeGraph(
    onSettingsClick: () -> Unit,
    homeBody: @Composable () -> Unit = {},
) {
    navigation<HomeDestination>(
        startDestination = HomeRoute,
    ) {
        composableWithStayTransitions<HomeRoute> {
            HomeScreen(
                onSettingsClick = onSettingsClick,
                homeBody = homeBody,
            )
        }
    }
}
