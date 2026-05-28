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

package org.mifos.feature.loans.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.mifos.feature.loans.ui.AddOrEditLoanScreen
import org.mifos.feature.loans.ui.LoanDetailScreen
import org.mifos.feature.loans.ui.PersonalLoansListScreen
import template.core.base.ui.nav.composableWithPushTransitions
import template.core.base.ui.nav.popBackStackSafely

@Serializable
data object LoansGraphRoute

@Serializable
data object PersonalLoansListRoute

@Serializable
data class LoanDetailRoute(val loanId: String)

@Serializable
data class AddOrEditLoanRoute(val loanId: String? = null)

fun NavController.navigateToLoans(navOptions: NavOptions? = null) {
    navigate(route = LoansGraphRoute, navOptions = navOptions)
}

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
                onAmortizationClick = {
                    // TODO(banking-utility-toolkit-06): wire to amortization schedule route.
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
    }
}
