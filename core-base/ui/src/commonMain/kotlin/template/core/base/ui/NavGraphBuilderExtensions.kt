/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Adds a composable to the navigation graph with slide up/down transitions.
 * Enters with a slide-up animation and exits during pop with a slide-down animation.
 *
 * @param route The route path used for navigation
 * @param arguments Optional navigation arguments for the route
 * @param deepLinks Optional deep links that can navigate to this composable
 * @param content The composable content to be rendered for this destination
 */
fun NavGraphBuilder.composableWithSlideTransitions(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = TransitionProviders.Enter.slideUp,
        exitTransition = TransitionProviders.Exit.stay,
        popEnterTransition = TransitionProviders.Enter.stay,
        popExitTransition = TransitionProviders.Exit.slideDown,
        content = content,
    )
}

/**
 * Adds a composable to the navigation graph with no transitions.
 * All transition states use the "stay" animation, resulting in no visible movement.
 *
 * @param route The route path used for navigation
 * @param arguments Optional navigation arguments for the route
 * @param deepLinks Optional deep links that can navigate to this composable
 * @param content The composable content to be rendered for this destination
 */
fun NavGraphBuilder.composableWithStayTransitions(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = TransitionProviders.Enter.stay,
        exitTransition = TransitionProviders.Exit.stay,
        popEnterTransition = TransitionProviders.Enter.stay,
        popExitTransition = TransitionProviders.Exit.stay,
        content = content,
    )
}

/**
 * Adds a composable to the navigation graph with horizontal push transitions.
 * Enters by pushing from the right (pushLeft) and exits during pop by pushing to the right.
 *
 * @param route The route path used for navigation
 * @param arguments Optional navigation arguments for the route
 * @param deepLinks Optional deep links that can navigate to this composable
 * @param content The composable content to be rendered for this destination
 */
fun NavGraphBuilder.composableWithPushTransitions(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = TransitionProviders.Enter.pushLeft,
        exitTransition = TransitionProviders.Exit.stay,
        popEnterTransition = TransitionProviders.Enter.stay,
        popExitTransition = TransitionProviders.Exit.pushRight,
        content = content,
    )
}

/**
 * Adds a composable to the navigation graph with specialized root navigation transitions.
 * Stays in place when entering, pushes left when exiting, pushes right when popping enter,
 * and fades out when popping exit.
 *
 * @param route The route path used for navigation
 * @param arguments Optional navigation arguments for the route
 * @param deepLinks Optional deep links that can navigate to this composable
 * @param content The composable content to be rendered for this destination
 */
fun NavGraphBuilder.composableWithRootPushTransitions(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = TransitionProviders.Enter.stay,
        exitTransition = TransitionProviders.Exit.pushLeft,
        popEnterTransition = TransitionProviders.Enter.pushRight,
        popExitTransition = TransitionProviders.Exit.fadeOut,
        content = content,
    )
}
