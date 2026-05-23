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
import org.mifos.feature.alerts.navigation.alertsGraph
import org.mifos.feature.crypto.navigation.cryptoGraph
import org.mifos.feature.crypto.navigation.navigateToCrypto
import org.mifos.feature.currencyrates.navigation.currencyRatesGraph
import org.mifos.feature.currencyrates.navigation.navigateToCurrencyRates
import org.mifos.feature.currencyrates.navigation.navigateToRateHistory
import org.mifos.feature.emicalculator.navigation.emiCalculatorDestination
import org.mifos.feature.emicalculator.navigation.navigateToEmiCalculator
import org.mifos.feature.settings.navigateToSettings
import org.mifos.feature.settings.notificationDestination
import org.mifos.feature.settings.settingsDestination

@Serializable
internal data object AuthenticatedGraphRoute

internal fun NavController.navigateToAuthenticatedGraph(navOptions: NavOptions? = null) {
    navigate(route = AuthenticatedGraphRoute, navOptions = navOptions)
}

internal fun NavGraphBuilder.authenticatedGraph(
    navController: NavController,
) {
    navigation<AuthenticatedGraphRoute>(
        startDestination = AuthenticatedNavbarRoute,
    ) {
        authenticatedNavbarGraph(
            navigateToSettingsScreen = navController::navigateToSettings,
            navigateToRates = { navController.navigateToCurrencyRates() },
            navigateToHistory = { navController.navigateToRateHistory() },
            navigateToCrypto = { navController.navigateToCrypto() },
            navigateToEmi = { navController.navigateToEmiCalculator() },
        )

        notificationDestination(
            onBackClick = navController::popBackStack,
        )

        settingsDestination(
            onBackClick = navController::popBackStack,
        )

        // Fintech feature graphs
        cryptoGraph(navController)
        currencyRatesGraph(navController)
        emiCalculatorDestination(onBackClick = navController::popBackStack)

        // Price alerts (DraftSubmitHandler showcase — offline-resilient form submit)
        alertsGraph(navController)
    }
}
