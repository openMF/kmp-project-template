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
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.calculators.affordability.AffordabilityCalculatorScreen
import kpt.feature.calculators.amortization.AmortizationScreen
import kpt.feature.calculators.comparison.LoanComparisonScreen
import kpt.feature.calculators.wizard.LoanCalcWizardScreen

@Serializable
data object CalculatorsGraphRoute

@Serializable
data object AffordabilityCalculatorRoute

@Serializable
data class AmortizationRoute(val loanId: String? = null)

@Serializable
data object LoanComparisonRoute

@Serializable
data class LoanCalcWizardRoute(val scenarioId: String? = null)

fun NavController.navigateToCalculators(navOptions: NavOptions? = null) {
    navigate(route = CalculatorsGraphRoute, navOptions = navOptions)
}

fun NavController.navigateToAffordability(navOptions: NavOptions? = null) {
    navigate(route = AffordabilityCalculatorRoute, navOptions = navOptions)
}

fun NavController.navigateToAmortization(loanId: String? = null, navOptions: NavOptions? = null) {
    navigate(route = AmortizationRoute(loanId), navOptions = navOptions)
}

fun NavController.navigateToLoanComparison(navOptions: NavOptions? = null) {
    navigate(route = LoanComparisonRoute, navOptions = navOptions)
}

fun NavController.navigateToLoanCalcWizard(
    scenarioId: String? = null,
    navOptions: NavOptions? = null,
) {
    navigate(route = LoanCalcWizardRoute(scenarioId), navOptions = navOptions)
}

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
