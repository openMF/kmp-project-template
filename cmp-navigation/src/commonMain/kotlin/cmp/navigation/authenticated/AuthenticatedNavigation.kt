/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MatchingDeclarationName")

package cmp.navigation.authenticated

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import cmp.navigation.authenticatednavbar.AuthenticatedNavbarRoute
import cmp.navigation.authenticatednavbar.authenticatedNavbarGraph
import cmp.navigation.registry.BackboneRegistry
import cmp.navigation.registry.FeatureRegistry
import kotlinx.serialization.Serializable
import kpt.core.base.security.isReleaseBuild
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.settings.navigateToSettings
import kpt.feature.settings.navigateToSyncAndDrafts
import kpt.feature.settings.notificationDestination
import kpt.feature.settings.settingsDestination
import kpt.feature.settings.syncAndDraftsDestination
import kpt.feature.showcase.stategallery.StateGalleryRoute
import kpt.feature.showcase.stategallery.stateGalleryGraph
import kpt.feature.showcase.transitions.TransitionGalleryRoute
import kpt.feature.showcase.transitions.transitionGalleryGraph

@Serializable
internal data object AuthenticatedGraphRoute

internal fun NavController.navigateToAuthenticatedGraph(navOptions: NavOptions? = null) {
    navigate(route = AuthenticatedGraphRoute, navOptions = navOptions)
}

internal fun NavGraphBuilder.authenticatedGraph(navController: NavController) {
    navigation<AuthenticatedGraphRoute>(
        startDestination = AuthenticatedNavbarRoute,
    ) {
        authenticatedNavbarGraph(
            navigateToSettingsScreen = navController::navigateToSettings,
            // Home body from the fork-owned BackboneRegistry seam (default: demo dashboard). The
            // template shell carries zero demo imports — a fork edits BackboneRegistry, not this file.
            homeBody = { BackboneRegistry.homeBody(navController) },
        )

        notificationDestination(onBackClick = { navController.popBackStackSafely() })

        // Template-level Sync & Drafts screen (Settings → "Sync & Drafts"). Framework feature —
        // available in every fork; reads the cross-form DraftInventory seam.
        syncAndDraftsDestination(onBackClick = { navController.popBackStackSafely() })

        // demo:begin
        // Dev-only entry points to the showcase galleries (only wired in non-release builds).
        // Released builds receive null → SettingsScreen hides the dev menu entirely.
        // See feature/showcase for the gallery destinations.
        val onTransitionGalleryClick: (() -> Unit)? = if (!isReleaseBuild()) {
            { navController.navigate(TransitionGalleryRoute) }
        } else {
            null
        }
        val onStateGalleryClick: (() -> Unit)? = if (!isReleaseBuild()) {
            { navController.navigate(StateGalleryRoute) }
        } else {
            null
        }
        // demo:end
        settingsDestination(
            onBackClick = { navController.popBackStackSafely() },
            onSyncAndDraftsClick = { navController.navigateToSyncAndDrafts() },
            // demo:begin
            onTransitionGalleryClick = onTransitionGalleryClick,
            onStateGalleryClick = onStateGalleryClick,
            // demo:end
        )

        // Fork feature routes — from the fork-owned FeatureRegistry seam (white-label: the template infra
        // never edits this; a fork adds/removes routes in cmp-navigation/registry/FeatureRegistry.kt).
        // `this` here is the NavGraphBuilder receiver of the enclosing navigation{} block.
        FeatureRegistry.featureDestinations.invoke(this, navController)

        // demo:begin

        // Dev-only transition gallery (Phase 08 Task 14 — Task 12-13 ground work).
        transitionGalleryGraph(navController)

        // Dev-only state gallery (Phase 02 Task 17 — ScreenState variants + component-scale primitives).
        stateGalleryGraph(navController)
        // demo:end
    }
}
