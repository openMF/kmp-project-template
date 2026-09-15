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

package kpt.feature.emicalculator.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.emicalculator.ui.EmiCalculatorScreen

@Serializable
data object EmiCalculatorRoute

fun NavController.navigateToEmiCalculator(navOptions: NavOptions? = null) {
    navigate(route = EmiCalculatorRoute, navOptions = navOptions)
}

@FeatureDestination
fun NavGraphBuilder.emiCalculatorDestination(navController: NavController) {
    composableWithPushTransitions<EmiCalculatorRoute> {
        // Takes the NavController like every other top-level destination, rather than an
        // `onBackClick` lambda: the aggregate invokes one uniform shape, and a bespoke signature
        // is exactly what kept this entry hand-wired in FeatureRegistry.
        EmiCalculatorScreen(onBackClick = { navController.popBackStackSafely() })
    }
}
