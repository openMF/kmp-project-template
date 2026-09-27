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

package kpt.feature.loans.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.amortization.navigation.AmortizationScheduleRoute
import kpt.feature.amortization.navigation.amortizationScheduleDestination
import kpt.feature.loans.ui.AddOrEditLoanScreen
import kpt.feature.loans.ui.LoanDetailScreen
import kpt.feature.loans.ui.PersonalLoansListScreen

/** Route for the loans nested graph. */
@Serializable
data object LoansGraphRoute

/** Route for the loan list — the graph's start destination. */
@Serializable
data object PersonalLoansListRoute

/**
 * Route for one loan's detail.
 *
 * @property loanId the loan to show.
 */
@Serializable
data class LoanDetailRoute(val loanId: String)

/**
 * Route for the add/edit form. One route for both, since the form is identical and the id decides which.
 *
 * @property loanId the loan to edit, or null when adding.
 */
@Serializable
data class AddOrEditLoanRoute(val loanId: String? = null)

/**
 * Navigates to the loans graph.
 *
 * @param navOptions optional nav options.
 */
fun NavController.navigateToLoans(navOptions: NavOptions? = null) {
    navigate(route = LoansGraphRoute, navOptions = navOptions)
}

/**
 * Registers the loans nested graph.
 *
 * @param navController used for back navigation and for moving between list, detail and form.
 */
@FeatureDestination
fun NavGraphBuilder.loansGraph(navController: NavController) {
    navigation<LoansGraphRoute>(startDestination = PersonalLoansListRoute) {
        composableWithPushTransitions<PersonalLoansListRoute> {
            PersonalLoansListScreen(
                onBackClick = { navController.popBackStackSafely() },
                onAddLoanClick = { navController.navigate(AddOrEditLoanRoute()) },
                onLoanClick = { loanId -> navController.navigate(LoanDetailRoute(loanId)) },
            )
        }
        composableWithPushTransitions<LoanDetailRoute> { entry ->
            val route = entry.toRoute<LoanDetailRoute>()
            LoanDetailScreen(
                loanId = route.loanId,
                onBackClick = { navController.popBackStackSafely() },
                onEditClick = { loanId ->
                    navController.navigate(AddOrEditLoanRoute(loanId = loanId))
                },
                onAmortizationClick = { loanId ->
                    navController.navigate(AmortizationScheduleRoute(loanId))
                },
            )
        }
        composableWithPushTransitions<AddOrEditLoanRoute> { entry ->
            val route = entry.toRoute<AddOrEditLoanRoute>()
            AddOrEditLoanScreen(
                loanId = route.loanId,
                onBackClick = { navController.popBackStackSafely() },
                onSaved = { navController.popBackStackSafely() },
            )
        }
        amortizationScheduleDestination(navController)
    }
}
