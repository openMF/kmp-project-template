/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
// demo:begin — the home-tab body + its feature nav actions. customizer --clean strips these imports.
import kpt.feature.bills.navigation.navigateToBills
import kpt.feature.calculators.navigation.navigateToAffordability
import kpt.feature.calculators.navigation.navigateToAmortization
import kpt.feature.calculators.navigation.navigateToLoanCalcWizard
import kpt.feature.calculators.navigation.navigateToLoanComparison
import kpt.feature.crypto.navigation.navigateToCrypto
import kpt.feature.currencyrates.navigation.navigateToCurrencyRates
import kpt.feature.currencyrates.navigation.navigateToRateHistory
import kpt.feature.emicalculator.navigation.navigateToEmiCalculator
import kpt.feature.home.demo.HomeDashboard
import kpt.feature.loans.navigation.navigateToLoans
import kpt.feature.macro.navigation.navigateToMacroGraph
import kpt.feature.rates.navigation.navigateToRates
// demo:end

/**
 * BackboneRegistry — the FORK-OWNED white-label seam for the app **backbone** (the home-tab body and,
 * as forks grow, backbone module overrides).
 *
 * It is the home-body twin of [FeatureRegistry] (which owns feature routes + DI). The template shell —
 * `feature/home`'s `HomeScreen` (top bar + settings) and the `cmp-navigation` navbar graph — carries ZERO
 * demo imports and simply renders whatever body this seam provides. A fork swaps its dashboard by editing
 * THIS ONE file, never the template shell. Ownership: `owner: fork` in customization-surface.yaml, so a
 * template sync full-copies the shell while this file (and its body) survives.
 *
 * S5 heal (epic pure-white-label-store5-network, T7): the 12 hardcoded `navigateToX` demo lambdas used to
 * thread through FIVE template layers (AuthenticatedNavigation → navbarGraph → navbarScreen → homeGraph →
 * HomeScreen) just to reach the fork-owned [HomeDashboard]. They now live here, in one fork-owned place —
 * the shell only forwards an opaque `homeBody`.
 */
object BackboneRegistry {
    /**
     * The home tab's body. `HomeScreen` renders this inside its framework-owned Scaffold. The template
     * default is the demo Money-Toolkit dashboard wired to its feature destinations; `customizer --clean`
     * strips the fenced block below, leaving `{ }` — an empty backbone body for the fork to fill.
     */
    val homeBody: @Composable (NavController) -> Unit = { navController ->
        // demo:begin — default demo dashboard body. Replace with your fork's home content.
        HomeDashboard(
            onNavigateToLoans = { navController.navigateToLoans() },
            onNavigateToBills = { navController.navigateToBills() },
            onNavigateToRates = { navController.navigateToRates() },
            onNavigateToExchangeRates = { navController.navigateToCurrencyRates() },
            onNavigateToRateHistory = { navController.navigateToRateHistory() },
            onNavigateToMacro = { navController.navigateToMacroGraph() },
            onNavigateToEmi = { navController.navigateToEmiCalculator() },
            onNavigateToAffordability = { navController.navigateToAffordability() },
            onNavigateToAmortization = { navController.navigateToAmortization() },
            onNavigateToLoanComparison = { navController.navigateToLoanComparison() },
            onNavigateToLoanCalcWizard = { navController.navigateToLoanCalcWizard() },
            onNavigateToCrypto = { navController.navigateToCrypto() },
        )
        // demo:end
    }
}
