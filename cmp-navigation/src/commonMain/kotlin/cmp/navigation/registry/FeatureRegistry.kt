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
import kpt.core.data.di.ProjectRepositoryModule
import kpt.core.database.di.ProjectDatabaseModule
import kpt.core.datastore.di.ProjectDatastoreModule
import kpt.core.network.di.ProjectNetworkModule
import kpt.feature.alerts.navigation.alertsGraph
import kpt.feature.bills.navigation.billsGraph
import kpt.feature.calculators.navigation.calculatorsGraph
import kpt.feature.crypto.navigation.cryptoGraph
import kpt.feature.currencyrates.navigation.currencyRatesGraph
import kpt.feature.emicalculator.navigation.emiCalculatorDestination
import kpt.feature.loans.navigation.loansGraph
import kpt.feature.macro.navigation.macroGraph
import kpt.feature.rates.navigation.ratesGraph
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
    /**
     * The four per-layer fork seams, plus every `feature/<f>/di` Koin module.
     *
     * The feature half is DERIVED — `:cmp-navigation:generateFeatureKoinBindings` reads each
     * feature module's `di` package and emits [GeneratedFeatureKoinBindings]. Adding a
     * feature no longer means editing this file: previously it took an import AND a list entry
     * here, and forgetting either compiled cleanly while the feature's ViewModels failed to
     * resolve at runtime.
     *
     * The `Project*Module` seams stay listed BY HAND on purpose — they are core-layer fork seams,
     * not features, and nothing under `feature/` declares them.
     */
    val featureKoinModules: List<Module> = listOf(
        ProjectRepositoryModule,
        ProjectNetworkModule,
        ProjectDatabaseModule,
        ProjectDatastoreModule,
        GeneratedFeatureKoinBindings,
    )

    /**
     * Feature nav destinations — registered into the authenticated graph. The shell destinations
     * (settings, notification) stay in [cmp.navigation.authenticated] template; this is the fork's routes.
     */
    val featureDestinations: NavGraphBuilder.(NavController) -> Unit = { navController ->
        // demo:begin — default demo feature routes (F3). customizer --clean strips this fenced block →
        // an empty lambda body for a clean fork; replace with your fork's routes.
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
        // demo:end
    }
}
