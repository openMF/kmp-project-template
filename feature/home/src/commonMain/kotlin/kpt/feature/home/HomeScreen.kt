/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import kpt.feature.home.demo.HomeDashboard
import kpt.feature.home.demo.ui.HomeViewModel
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.screens_home_app_title
import kpt.feature.home.generated.resources.screens_home_settings_cd
import kpt.feature.home.ui.TestTags
import org.jetbrains.compose.resources.stringResource

/**
 * The home tab shell — a framework-owned [Scaffold] with the app-title top bar and a
 * settings action. Always present in every fork.
 *
 * The demo Money-Toolkit dashboard ([HomeDashboard], under `kpt.feature.home.demo`) is
 * rendered inside the demo-marked block below. `customizer --clean` deletes the
 * `feature/home/demo/` package and strips that block, leaving this shell with an empty
 * body for the fork to fill — the top bar and settings entry point survive.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onSettingsClick: () -> Unit,
    // demo:begin
    onNavigateToLoans: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToRates: () -> Unit,
    onNavigateToExchangeRates: () -> Unit,
    onNavigateToRateHistory: () -> Unit,
    onNavigateToMacro: () -> Unit,
    onNavigateToEmi: () -> Unit,
    onNavigateToAffordability: () -> Unit,
    onNavigateToAmortization: () -> Unit,
    onNavigateToLoanComparison: () -> Unit,
    onNavigateToLoanCalcWizard: () -> Unit,
    /** Injected for Compose UI tests; production callers use the default. */
    dashboardViewModel: HomeViewModel? = null,
    // demo:end
) {
    Scaffold(
        modifier = Modifier.testTag(TestTags.Home.SCREEN),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.screens_home_app_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(Res.string.screens_home_settings_cd),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // demo:begin
            if (dashboardViewModel != null) {
                HomeDashboard(
                    onNavigateToLoans = onNavigateToLoans,
                    onNavigateToBills = onNavigateToBills,
                    onNavigateToRates = onNavigateToRates,
                    onNavigateToExchangeRates = onNavigateToExchangeRates,
                    onNavigateToRateHistory = onNavigateToRateHistory,
                    onNavigateToMacro = onNavigateToMacro,
                    onNavigateToEmi = onNavigateToEmi,
                    onNavigateToAffordability = onNavigateToAffordability,
                    onNavigateToAmortization = onNavigateToAmortization,
                    onNavigateToLoanComparison = onNavigateToLoanComparison,
                    onNavigateToLoanCalcWizard = onNavigateToLoanCalcWizard,
                    viewModel = dashboardViewModel,
                )
            } else {
                HomeDashboard(
                    onNavigateToLoans = onNavigateToLoans,
                    onNavigateToBills = onNavigateToBills,
                    onNavigateToRates = onNavigateToRates,
                    onNavigateToExchangeRates = onNavigateToExchangeRates,
                    onNavigateToRateHistory = onNavigateToRateHistory,
                    onNavigateToMacro = onNavigateToMacro,
                    onNavigateToEmi = onNavigateToEmi,
                    onNavigateToAffordability = onNavigateToAffordability,
                    onNavigateToAmortization = onNavigateToAmortization,
                    onNavigateToLoanComparison = onNavigateToLoanComparison,
                    onNavigateToLoanCalcWizard = onNavigateToLoanCalcWizard,
                )
            }
            // demo:end
        }
    }
}
