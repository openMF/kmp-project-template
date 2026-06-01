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

package kpt.feature.amortization.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kpt.feature.amortization.ui.AmortizationScheduleScreen
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely

@Serializable
data class AmortizationScheduleRoute(val loanId: String)

fun NavController.navigateToAmortizationSchedule(loanId: String) {
    navigate(AmortizationScheduleRoute(loanId))
}

fun NavGraphBuilder.amortizationScheduleDestination(navController: NavController) {
    composableWithPushTransitions<AmortizationScheduleRoute> { entry ->
        val route = entry.toRoute<AmortizationScheduleRoute>()
        AmortizationScheduleScreen(
            loanId = route.loanId,
            onBackClick = { navController.popBackStackSafely() },
        )
    }
}
