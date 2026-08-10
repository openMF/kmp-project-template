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

import cmp.navigation.registry.FeatureRegistry

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import cmp.navigation.authenticatednavbar.AuthenticatedNavbarRoute
import cmp.navigation.authenticatednavbar.authenticatedNavbarGraph
import kotlinx.serialization.Serializable
import kpt.core.base.security.isReleaseBuild
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.bills.navigation.navigateToBills
import kpt.feature.calculators.navigation.navigateToAffordability
import kpt.feature.calculators.navigation.navigateToAmortization
import kpt.feature.calculators.navigation.navigateToLoanCalcWizard
import kpt.feature.calculators.navigation.navigateToLoanComparison
import kpt.feature.crypto.navigation.navigateToCrypto
import kpt.feature.currencyrates.navigation.navigateToCurrencyRates
import kpt.feature.currencyrates.navigation.navigateToRateHistory
import kpt.feature.emicalculator.navigation.navigateToEmiCalculator
import kpt.feature.loans.navigation.navigateToLoans
import kpt.feature.macro.navigation.navigateToMacroGraph
import kpt.feature.rates.navigation.navigateToRates
import kpt.feature.settings.navigateToSettings
import kpt.feature.settings.notificationDestination
import kpt.feature.settings.settingsDestination
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
            // demo:begin
            navigateToLoans = { navController.navigateToLoans() },
            navigateToBills = { navController.navigateToBills() },
            navigateToRates = { navController.navigateToRates() },
            navigateToExchangeRates = { navController.navigateToCurrencyRates() },
            navigateToRateHistory = { navController.navigateToRateHistory() },
            navigateToMacro = { navController.navigateToMacroGraph() },
            navigateToEmi = { navController.navigateToEmiCalculator() },
            navigateToAffordability = { navController.navigateToAffordability() },
            navigateToAmortization = { navController.navigateToAmortization() },
            navigateToLoanComparison = { navController.navigateToLoanComparison() },
            navigateToLoanCalcWizard = { navController.navigateToLoanCalcWizard() },
            navigateToCrypto = { navController.navigateToCrypto() },
            // demo:end
        )

        notificationDestination(onBackClick = { navController.popBackStackSafely() })

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
