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

package org.mifos.feature.bills.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.mifos.feature.bills.ui.AddOrEditBillReminderScreen
import org.mifos.feature.bills.ui.BillRemindersListScreen
import template.core.base.ui.nav.composableWithPushTransitions

/** Route for the entire Bill Reminders graph — entry for [NavController.navigateToBills]. */
@Serializable
data object BillsGraphRoute

/** Bill list dashboard. */
@Serializable
data object BillRemindersListRoute

/**
 * Add-or-edit screen. `billId == null` means "create new"; non-null means "edit existing".
 */
@Serializable
data class AddOrEditBillReminderRoute(val billId: String? = null)

/** Entry point — call this from `AuthenticatedNavigation` to jump into the bills graph. */
fun NavController.navigateToBills(navOptions: NavOptions? = null) {
    navigate(route = BillsGraphRoute, navOptions = navOptions)
}

/**
 * Wires the bills graph into a parent [NavGraphBuilder]. Mirrors `alertsGraph` —
 * push-transition routes, back-pop navigation, type-safe arg passing.
 */
fun NavGraphBuilder.billsGraph(navController: NavController) {
    navigation<BillsGraphRoute>(startDestination = BillRemindersListRoute) {
        composableWithPushTransitions<BillRemindersListRoute> {
            BillRemindersListScreen(
                onBackClick = navController::popBackStack,
                onAddBillClick = { navController.navigate(AddOrEditBillReminderRoute()) },
                onEditBillClick = { id -> navController.navigate(AddOrEditBillReminderRoute(id)) },
            )
        }
        composableWithPushTransitions<AddOrEditBillReminderRoute> { backStack ->
            val route = backStack.toRoute<AddOrEditBillReminderRoute>()
            AddOrEditBillReminderScreen(
                onBackClick = navController::popBackStack,
                billId = route.billId,
            )
        }
    }
}
