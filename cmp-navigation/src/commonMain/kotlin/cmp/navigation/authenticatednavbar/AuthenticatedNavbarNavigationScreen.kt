/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.authenticatednavbar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import cmp.navigation.generated.resources.Res
import cmp.navigation.generated.resources.not_connected
import cmp.navigation.ui.KptRootScaffold
import cmp.navigation.ui.ScaffoldNavigationData
import cmp.navigation.ui.logDestinationChanged
import cmp.navigation.ui.rememberKptNavController
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.ui.NavigationItem
import org.mifos.feature.home.HomeDestination
import org.mifos.feature.home.homeGraph
import org.mifos.feature.home.navigateToHome
import org.mifos.feature.profile.navigateToProfile
import org.mifos.feature.profile.profileDestination
import template.core.base.analytics.rememberAnalyticsHelper
import template.core.base.designsystem.theme.motion
import template.core.base.ui.effects.EventsEffect
import template.core.base.ui.util.RootTransitionProviders

@Composable
internal fun AuthenticatedNavbarNavigationScreen(
    navigateToSettingsScreen: () -> Unit,
    navigateToLoans: () -> Unit,
    navigateToBills: () -> Unit,
    navigateToRates: () -> Unit,
    navigateToExchangeRates: () -> Unit,
    navigateToRateHistory: () -> Unit,
    navigateToMacro: () -> Unit,
    navigateToEmi: () -> Unit,
    navigateToAffordability: () -> Unit,
    navigateToAmortization: () -> Unit,
    navigateToLoanComparison: () -> Unit,
    navigateToLoanCalcWizard: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberKptNavController(
        name = "AuthenticatedNavbarScreen",
    ),
    viewModel: AuthenticatedNavbarNavigationViewModel = koinViewModel(),
) {
    val analyticsHelper = rememberAnalyticsHelper()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

    EventsEffect(eventFlow = viewModel.eventFlow) { event ->
        navController.apply {
            when (event) {
                AuthenticatedNavBarEvent.NavigateToHomeScreen -> {
                    analyticsHelper.logDestinationChanged(event.tab.startDestinationRoute)
                    navigateToTabOrRoot(tabToNavigateTo = event.tab) {
                        navigateToHome(navOptions = it)
                    }
                }

                AuthenticatedNavBarEvent.NavigateToProfileScreen -> {
                    analyticsHelper.logDestinationChanged(event.tab.startDestinationRoute)
                    navigateToTabOrRoot(tabToNavigateTo = event.tab) {
                        navigateToProfile(navOptions = it)
                    }
                }
            }
        }
    }

    val message = stringResource(Res.string.not_connected)
    LaunchedEffect(isOffline) {
        if (isOffline) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = Indefinite,
                )
            }
        }
    }

    AuthenticatedNavbarNavigationScreenContent(
        navController = navController,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        navigateToSettingsScreen = navigateToSettingsScreen,
        navigateToLoans = navigateToLoans,
        navigateToBills = navigateToBills,
        navigateToRates = navigateToRates,
        navigateToExchangeRates = navigateToExchangeRates,
        navigateToRateHistory = navigateToRateHistory,
        navigateToMacro = navigateToMacro,
        navigateToEmi = navigateToEmi,
        navigateToAffordability = navigateToAffordability,
        navigateToAmortization = navigateToAmortization,
        navigateToLoanComparison = navigateToLoanComparison,
        navigateToLoanCalcWizard = navigateToLoanCalcWizard,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
internal fun AuthenticatedNavbarNavigationScreenContent(
    navController: NavHostController,
    navigateToSettingsScreen: () -> Unit,
    navigateToLoans: () -> Unit,
    navigateToBills: () -> Unit,
    navigateToRates: () -> Unit,
    navigateToExchangeRates: () -> Unit,
    navigateToRateHistory: () -> Unit,
    navigateToMacro: () -> Unit,
    navigateToEmi: () -> Unit,
    navigateToAffordability: () -> Unit,
    navigateToAmortization: () -> Unit,
    navigateToLoanComparison: () -> Unit,
    navigateToLoanCalcWizard: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (AuthenticatedNavBarAction) -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val navigationItems = persistentListOf<NavigationItem>(
        AuthenticatedNavBarTabItem.HomeTab,
        AuthenticatedNavBarTabItem.ProfileTab,
    )

    KptRootScaffold(
        contentWindowInsets = WindowInsets(0.dp),
        navigationData = ScaffoldNavigationData(
            navigationItems = navigationItems,
            selectedNavigationItem = navigationItems.find {
                navBackStackEntry.isCurrentRoute(route = it.graphRoute)
            },
            onNavigationClick = { navigationItem ->
                when (navigationItem) {
                    is AuthenticatedNavBarTabItem.HomeTab -> {
                        onAction(AuthenticatedNavBarAction.HomeTabClick)
                    }

                    is AuthenticatedNavBarTabItem.ProfileTab -> {
                        onAction(AuthenticatedNavBarAction.SettingsTabClick)
                    }
                }
            },
            shouldShowNavigation = navigationItems.any {
                navBackStackEntry.isCurrentRoute(route = it.startDestinationRoute)
            },
        ),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier,
    ) {
        // Because this Scaffold has a bottom navigation bar, the NavHost will:
        // - consume the vertical navigation bar insets.
        // - consume the IME insets.
        // Snapshot motion tokens once so the non-Composable enterTransition lambdas capture
        // theme-resolved values rather than the hardcoded fallbacks.
        val motion = MaterialTheme.motion
        NavHost(
            navController = navController,
            startDestination = HomeDestination,
            // Sibling navigation (bottom-nav tab switch) uses M3 fade-through pattern.
            enterTransition = RootTransitionProviders.Mifos.Enter.fadeThrough(motion),
            exitTransition = RootTransitionProviders.Mifos.Exit.fadeThrough(motion),
            popEnterTransition = RootTransitionProviders.Mifos.Enter.fadeThrough(motion),
            popExitTransition = RootTransitionProviders.Mifos.Exit.fadeThrough(motion),
        ) {
            // TOP LEVEL DESTINATIONS
            homeGraph(
                onSettingsClick = navigateToSettingsScreen,
                onNavigateToLoans = navigateToLoans,
                onNavigateToBills = navigateToBills,
                onNavigateToRates = navigateToRates,
                onNavigateToExchangeRates = navigateToExchangeRates,
                onNavigateToRateHistory = navigateToRateHistory,
                onNavigateToMacro = navigateToMacro,
                onNavigateToEmi = navigateToEmi,
                onNavigateToAffordability = navigateToAffordability,
                onNavigateToAmortization = navigateToAmortization,
                onNavigateToLoanComparison = navigateToLoanComparison,
                onNavigateToLoanCalcWizard = navigateToLoanCalcWizard,
            )

            profileDestination()
        }
    }
}

private fun NavController.navigateToTabOrRoot(
    tabToNavigateTo: AuthenticatedNavBarTabItem,
    navigate: (NavOptions) -> Unit,
) {
    if (tabToNavigateTo.startDestinationRoute == currentDestination?.route) {
        return
    } else if (currentDestination?.parent?.route == tabToNavigateTo.graphRoute) {
        popBackStack(route = tabToNavigateTo.startDestinationRoute, inclusive = false)
    } else {
        navigate(
            navOptions {
                popUpTo(graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            },
        )
    }
}

private fun NavBackStackEntry?.isCurrentRoute(route: String): Boolean = this
    ?.destination
    ?.hierarchy
    ?.any { it.route == route } == true
