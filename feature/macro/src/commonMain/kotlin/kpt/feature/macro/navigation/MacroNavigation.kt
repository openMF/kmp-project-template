/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.core.model.demo.economic.IndicatorKind
import kpt.feature.macro.ui.CountryMacroScreen
import kpt.feature.macro.ui.CountryPickerScreen
import kpt.feature.macro.ui.MacroIndicatorDetailScreen

/** Root of the country-macro nav graph. */
@Serializable
data object MacroGraphRoute

/**
 * Dashboard entry. The default country is the United States; the picker
 * round-trips a new code via [CountryPickerRoute].
 */
@Serializable
data class CountryMacroRoute(val countryCode: String = "US")

/** Picker overlay launched from the dashboard's country chip. */
@Serializable
data object CountryPickerRoute

/**
 * Full-history detail screen for a single indicator.
 *
 * [indicatorKind] is stored as the enum's name() so Compose Navigation's
 * type-safe serializer doesn't need a registered KSerializer for the enum
 * itself — the screen converts back to the enum on bind.
 */
@Serializable
data class IndicatorDetailRoute(
    val countryCode: String,
    val indicatorKindName: String,
)

fun NavController.navigateToMacroGraph(navOptions: NavOptions? = null) {
    navigate(route = MacroGraphRoute, navOptions = navOptions)
}

/**
 * Macro feature's navigation graph.
 *
 * The host (cmp-navigation) wires this into the top-level RootNavGraph by
 * calling [macroGraph] inside its own `NavHost { ... }` builder once this
 * feature is enabled.
 */
fun NavGraphBuilder.macroGraph(navController: NavController) {
    navigation<MacroGraphRoute>(startDestination = CountryMacroRoute()) {
        composableWithPushTransitions<CountryMacroRoute> { entry ->
            val route = entry.toRoute<CountryMacroRoute>()
            CountryMacroScreen(
                countryCode = route.countryCode,
                onBackClick = { navController.popBackStackSafely() },
                onPickCountry = { navController.navigate(CountryPickerRoute) },
                onOpenIndicator = { kind ->
                    navController.navigate(
                        IndicatorDetailRoute(
                            countryCode = route.countryCode,
                            indicatorKindName = kind.name,
                        ),
                    )
                },
            )
        }

        composableWithPushTransitions<CountryPickerRoute> {
            CountryPickerScreen(
                onBackClick = { navController.popBackStackSafely() },
                onCountryPicked = { code ->
                    // Replace the dashboard's countryCode so the ViewModel
                    // hosting the new country is created cleanly. The picker
                    // pops itself; the dashboard's back-stack entry is rebuilt.
                    navController.navigate(CountryMacroRoute(code)) {
                        popUpTo(CountryMacroRoute()) { inclusive = true }
                    }
                },
            )
        }

        composableWithPushTransitions<IndicatorDetailRoute> { entry ->
            val route = entry.toRoute<IndicatorDetailRoute>()
            MacroIndicatorDetailScreen(
                countryCode = route.countryCode,
                indicatorKind = IndicatorKind.valueOf(route.indicatorKindName),
                onBackClick = { navController.popBackStackSafely() },
            )
        }
    }
}
