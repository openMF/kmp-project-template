/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.addtowatchlist.di.AddToWatchlistModule
import kpt.feature.alerts.di.AlertsModule
import kpt.feature.alerts.navigation.alertsGraph
import kpt.feature.amortization.di.AmortizationModule
import kpt.feature.bills.di.BillsModule
import kpt.feature.bills.navigation.billsGraph
import kpt.feature.calculators.di.CalculatorsModule
import kpt.feature.calculators.navigation.calculatorsGraph
import kpt.feature.crypto.di.CryptoFeatureModule
import kpt.feature.crypto.navigation.cryptoGraph
import kpt.feature.currencyrates.di.CurrencyRatesModule
import kpt.feature.currencyrates.navigation.currencyRatesGraph
import kpt.feature.emicalculator.di.EmiCalculatorModule
import kpt.feature.emicalculator.navigation.emiCalculatorDestination
import kpt.feature.loans.di.LoansModule
import kpt.feature.loans.navigation.loansGraph
import kpt.feature.macro.di.MacroModule
import kpt.feature.macro.navigation.macroGraph
import kpt.feature.rates.di.RatesModule
import kpt.feature.rates.navigation.ratesGraph
import kpt.feature.watchlist.di.WatchlistModule
import kpt.feature.watchlist.navigation.watchlistGraph
import org.koin.core.module.Module

/**
 * FeatureRegistry — the FORK-OWNED white-label seam for feature contributions.
 *
 * The template infra modules READ from this registry; a fork extends the app by editing THIS ONE file
 * (+ its build.gradle deps + settings.gradle include), never the template infra files:
 *   - `cmp-navigation/di/KoinModules.kt` includes [featureKoinModules] into the app DI graph.
 *   - `cmp-navigation/.../AuthenticatedNavigation.kt` invokes [featureDestinations] to register routes.
 *
 * Ownership: `owner: fork` in customization-surface.yaml — `sync-dirs`/`white-label-doctor` NEVER
 * overwrite it, so a template sync full-copies the infra modules while your features survive. The
 * template ships this file pre-populated with its demo feature set as the default; a fork replaces the
 * contents with its own (the customizer `--clean` empties both lists).
 */
object FeatureRegistry {
    /**
     * Feature Koin modules the app installs. The framework SHELL modules (Home, Settings) live in
     * [cmp.navigation.di.KoinModules] and are always present; this is the fork's own features.
     */
    val featureKoinModules: List<Module> = listOf(
        // ── default demo feature set (customizer --clean empties this) — replace with your fork's ──
        CurrencyRatesModule,
        EmiCalculatorModule,
        BillsModule,
        LoansModule,
        AmortizationModule,
        RatesModule,
        CalculatorsModule,
        MacroModule,
        CryptoFeatureModule,
        AlertsModule,
        WatchlistModule,
        AddToWatchlistModule,
    )

    /**
     * Feature nav destinations — registered into the authenticated graph. The shell destinations
     * (settings, notification) stay in [cmp.navigation.authenticated] template; this is the fork's routes.
     */
    val featureDestinations: NavGraphBuilder.(NavController) -> Unit = { navController ->
        // ── default demo feature routes (customizer --clean empties this) — replace with your fork's ──
        currencyRatesGraph(navController)
        emiCalculatorDestination(onBackClick = { navController.popBackStackSafely() })
        loansGraph(navController)
        billsGraph(navController)
        calculatorsGraph(navController)
        ratesGraph(navController)
        macroGraph(navController)
        cryptoGraph(navController)
        alertsGraph(navController)
        watchlistGraph(navController)
    }
}
