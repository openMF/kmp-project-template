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
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.feature.emicalculator.ui.EmiCalculatorScreen

@Serializable
data object EmiCalculatorRoute

fun NavController.navigateToEmiCalculator(navOptions: NavOptions? = null) {
    navigate(route = EmiCalculatorRoute, navOptions = navOptions)
}

fun NavGraphBuilder.emiCalculatorDestination(onBackClick: () -> Unit) {
    composableWithPushTransitions<EmiCalculatorRoute> {
        EmiCalculatorScreen(onBackClick = onBackClick)
    }
}
