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
import kotlinx.serialization.Serializable
import org.mifos.feature.bills.navigation.billsGraph
import org.mifos.feature.bills.navigation.navigateToBills
import org.mifos.feature.calculators.navigation.calculatorsGraph
import org.mifos.feature.calculators.navigation.navigateToAffordability
import org.mifos.feature.calculators.navigation.navigateToAmortization
import org.mifos.feature.calculators.navigation.navigateToLoanCalcWizard
import org.mifos.feature.calculators.navigation.navigateToLoanComparison
import org.mifos.feature.currencyrates.navigation.currencyRatesGraph
import org.mifos.feature.currencyrates.navigation.navigateToCurrencyRates
import org.mifos.feature.currencyrates.navigation.navigateToRateHistory
import org.mifos.feature.emicalculator.navigation.emiCalculatorDestination
import org.mifos.feature.emicalculator.navigation.navigateToEmiCalculator
import org.mifos.feature.loans.navigation.loansGraph
import org.mifos.feature.loans.navigation.navigateToLoans
import org.mifos.feature.macro.navigation.macroGraph
import org.mifos.feature.macro.navigation.navigateToMacroGraph
import org.mifos.feature.rates.navigation.navigateToRates
import org.mifos.feature.rates.navigation.ratesGraph
import org.mifos.feature.settings.navigateToSettings
import org.mifos.feature.settings.notificationDestination
import org.mifos.feature.settings.settingsDestination
import org.mifos.feature.showcase.stategallery.StateGalleryRoute
import org.mifos.feature.showcase.stategallery.stateGalleryGraph
import org.mifos.feature.showcase.transitions.TransitionGalleryRoute
import org.mifos.feature.showcase.transitions.transitionGalleryGraph
import template.core.base.security.isReleaseBuild
import template.core.base.ui.nav.popBackStackSafely

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
        )

        notificationDestination(onBackClick = { navController.popBackStackSafely() })

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
        settingsDestination(
            onBackClick = { navController.popBackStackSafely() },
            onTransitionGalleryClick = onTransitionGalleryClick,
            onStateGalleryClick = onStateGalleryClick,
        )

        // Money Toolkit feature graphs — generic personal-finance utilities.
        currencyRatesGraph(navController)
        emiCalculatorDestination(onBackClick = { navController.popBackStackSafely() })

        // Banking utility toolkit — local-only personal tools.
        loansGraph(navController) // B1 — multi-formKey draft showcase
        billsGraph(navController) // B4 — multi-formKey + platform notification scheduler
        calculatorsGraph(navController) // B2/B3/B5/B6 — affordability + amortization + comparison + wizard
        ratesGraph(navController) // B7 — NETWORK_WITH_CACHE rate tracker
        macroGraph(navController) // B8 — multi-source combine (GDP / CPI / Unemployment)

        // Dev-only transition gallery (Phase 08 Task 14 — Task 12-13 ground work).
        transitionGalleryGraph(navController)

        // Dev-only state gallery (Phase 02 Task 17 — ScreenState variants + component-scale primitives).
        stateGalleryGraph(navController)
    }
}
