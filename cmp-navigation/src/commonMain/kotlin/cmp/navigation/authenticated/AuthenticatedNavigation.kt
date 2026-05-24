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
import org.mifos.feature.alerts.navigation.navigateToAlerts
import org.mifos.feature.bills.navigation.billsGraph
import org.mifos.feature.calculators.navigation.calculatorsGraph
import org.mifos.feature.crypto.navigation.CoinDetailRoute
import org.mifos.feature.crypto.navigation.cryptoGraph
import org.mifos.feature.crypto.navigation.navigateToCrypto
import org.mifos.feature.currencyrates.navigation.currencyRatesGraph
import org.mifos.feature.currencyrates.navigation.navigateToCurrencyRates
import org.mifos.feature.currencyrates.navigation.navigateToRateHistory
import org.mifos.feature.emicalculator.navigation.emiCalculatorDestination
import org.mifos.feature.emicalculator.navigation.navigateToEmiCalculator
import org.mifos.feature.loans.navigation.loansGraph
import org.mifos.feature.macro.navigation.macroGraph
import org.mifos.feature.rates.navigation.ratesGraph
import org.mifos.feature.settings.navigateToSettings
import org.mifos.feature.settings.notificationDestination
import org.mifos.feature.settings.settingsDestination
import org.mifos.feature.watchlist.navigation.navigateToPersonalWatchlist
import org.mifos.feature.watchlist.navigation.personalWatchlistDestination

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
            navigateToRates = { navController.navigateToCurrencyRates() },
            navigateToHistory = { navController.navigateToRateHistory() },
            navigateToCrypto = { navController.navigateToCrypto() },
            navigateToEmi = { navController.navigateToEmiCalculator() },
            navigateToWatchlist = { navController.navigateToPersonalWatchlist() },
            navigateToAlerts = { navController.navigateToAlerts() },
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

        // Personal watchlist (local-only — SubmitHandler showcase target)
        personalWatchlistDestination(
            onBackClick = navController::popBackStack,
            onCoinClick = { coinId -> navController.navigate(CoinDetailRoute(coinId)) },
        )

        // Price alerts (DraftSubmitHandler showcase — offline-resilient form submit)
        alertsGraph(navController)

        // Banking utility toolkit — local-only personal tools
        loansGraph(navController) // B1 — multi-formKey draft showcase
        billsGraph(navController) // B4 — multi-formKey + platform notification scheduler
        calculatorsGraph(navController) // B2/B3/B5/B6 — affordability + amortization + comparison + wizard
        ratesGraph(navController) // B7 — CACHE_THEN_NETWORK rate tracker
        macroGraph(navController) // B8 — multi-source combine (GDP / CPI / Unemployment)
    }
}
