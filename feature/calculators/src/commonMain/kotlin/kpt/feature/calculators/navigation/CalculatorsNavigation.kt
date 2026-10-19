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

package kpt.feature.calculators.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.calculators.affordability.AffordabilityCalculatorScreen
import kpt.feature.calculators.amortizationcalc.AmortizationScreen
import kpt.feature.calculators.comparison.LoanComparisonScreen
import kpt.feature.calculators.wizard.LoanCalcWizardScreen

/** Route for the calculators nested graph. */
@Serializable
data object CalculatorsGraphRoute

/** Route for the affordability calculator — the graph's start destination. */
@Serializable
data object AffordabilityCalculatorRoute

/**
 * Route for the amortization schedule.
 *
 * @property loanId prefill from this tracked loan, or null to start from the defaults.
 */
@Serializable
data class AmortizationRoute(val loanId: String? = null)

/** Route for the side-by-side loan comparison. */
@Serializable
data object LoanComparisonRoute

/**
 * Route for the multi-step loan wizard.
 *
 * @property scenarioId resume this scenario's draft, or null to start a new one.
 */
@Serializable
data class LoanCalcWizardRoute(val scenarioId: String? = null)

/**
 * Navigates to the calculators graph.
 *
 * @param navOptions optional nav options.
 */
fun NavController.navigateToCalculators(navOptions: NavOptions? = null) {
    navigate(route = CalculatorsGraphRoute, navOptions = navOptions)
}

/**
 * Navigates to the affordability calculator.
 *
 * @param navOptions optional nav options.
 */
fun NavController.navigateToAffordability(navOptions: NavOptions? = null) {
    navigate(route = AffordabilityCalculatorRoute, navOptions = navOptions)
}

/**
 * Navigates to the amortization schedule.
 *
 * @param loanId prefill from this tracked loan, or null to start from the defaults.
 * @param navOptions optional nav options.
 */
fun NavController.navigateToAmortization(loanId: String? = null, navOptions: NavOptions? = null) {
    navigate(route = AmortizationRoute(loanId), navOptions = navOptions)
}

/**
 * Navigates to the loan comparison.
 *
 * @param navOptions optional nav options.
 */
fun NavController.navigateToLoanComparison(navOptions: NavOptions? = null) {
    navigate(route = LoanComparisonRoute, navOptions = navOptions)
}

/**
 * Navigates to the loan wizard.
 *
 * @param scenarioId resume this scenario's draft, or null to start a new one.
 * @param navOptions optional nav options.
 */
fun NavController.navigateToLoanCalcWizard(scenarioId: String? = null, navOptions: NavOptions? = null) {
    navigate(route = LoanCalcWizardRoute(scenarioId), navOptions = navOptions)
}

/**
 * Registers the calculators nested graph.
 *
 * @param navController used for back navigation out of each screen.
 */
@FeatureDestination
fun NavGraphBuilder.calculatorsGraph(navController: NavController) {
    navigation<CalculatorsGraphRoute>(
        startDestination = AffordabilityCalculatorRoute,
    ) {
        composableWithPushTransitions<AffordabilityCalculatorRoute> {
            AffordabilityCalculatorScreen(onBackClick = { navController.popBackStackSafely() })
        }
        composableWithPushTransitions<AmortizationRoute> { entry ->
            val route = entry.toRoute<AmortizationRoute>()
            AmortizationScreen(
                onBackClick = { navController.popBackStackSafely() },
                loanId = route.loanId,
            )
        }
        composableWithPushTransitions<LoanComparisonRoute> {
            LoanComparisonScreen(onBackClick = { navController.popBackStackSafely() })
        }
        composableWithPushTransitions<LoanCalcWizardRoute> { entry ->
            val route = entry.toRoute<LoanCalcWizardRoute>()
            LoanCalcWizardScreen(
                onBackClick = { navController.popBackStackSafely() },
                scenarioId = route.scenarioId,
            )
        }
    }
}
