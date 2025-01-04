/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.shared.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import kmp_project_template.mifos_shared.generated.resources.Res
import kmp_project_template.mifos_shared.generated.resources.not_connected
import org.jetbrains.compose.resources.stringResource
import org.mifos.core.data.utils.NetworkMonitor
import org.mifos.core.data.utils.TimeZoneMonitor
import org.mifos.core.designsystem.component.MifosNavigationBar
import org.mifos.core.designsystem.component.MifosNavigationBarItem
import org.mifos.core.designsystem.component.MifosNavigationRail
import org.mifos.core.designsystem.component.MifosNavigationRailItem
import org.mifos.core.designsystem.icon.AppIcons
import org.mifos.feature.settings.navigateToNotification
import org.mifos.feature.settings.navigateToSettings
import org.mifos.shared.navigation.MifosNavHost
import org.mifos.shared.utils.TopLevelDestination

@Composable
internal fun MifosApp(
    networkMonitor: NetworkMonitor,
    timeZoneMonitor: TimeZoneMonitor,
    modifier: Modifier = Modifier,
) {
    val appState = rememberMifosAppState(
        networkMonitor = networkMonitor,
        timeZoneMonitor = timeZoneMonitor,
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val destination = appState.currentTopLevelDestination

    val isOffline by appState.isOffline.collectAsStateWithLifecycle()

    // If user is not connected to the internet show a snack bar to inform them.
    val notConnectedMessage = stringResource(Res.string.not_connected)
    LaunchedEffect(isOffline) {
        if (isOffline) {
            snackbarHostState.showSnackbar(
                message = notConnectedMessage,
                duration = Indefinite,
            )
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (appState.shouldShowBottomBar && destination != null) {
                MifosBottomBar(
                    destinations = appState.topLevelDestinations,
                    destinationsWithUnreadResources = emptySet(),
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                    currentDestination = appState.currentDestination,
                    modifier = Modifier.testTag("NiaBottomBar"),
                )
            }
        },
    ) { padding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal,
                    ),
                ),
        ) {
            if (appState.shouldShowNavRail && destination != null) {
                MifosNavRail(
                    destinations = appState.topLevelDestinations,
                    destinationsWithUnreadResources = emptySet(),
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                    currentDestination = appState.currentDestination,
                    modifier = Modifier
                        .testTag("NiaNavRail")
                        .safeDrawingPadding(),
                )
            }

            Column(Modifier.fillMaxSize()) {
                // Show the top app bar on top level destinations.
                if (destination != null) {
                    MifosAppBar(
                        title = stringResource(destination.titleText),
                        onNavigateToSettings = {
                            appState.navController.navigateToSettings()
                        },
                        onNavigateToEditProfile = {},
                        onNavigateToNotification = {
                            appState.navController.navigateToNotification()
                        },
                        destination = destination,
                    )
                }

                MifosNavHost(
                    appState = appState,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MifosAppBar(
    title: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToNotification: () -> Unit,
    destination: TopLevelDestination?,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(text = title) },
        actions = {
            Box {
                when (destination) {
                    TopLevelDestination.HOME -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            IconButton(
                                onClick = onNavigateToNotification,
                            ) {
                                Icon(
                                    imageVector = AppIcons.OutlinedNotifications,
                                    contentDescription = "view notification",
                                )
                            }

                            IconButton(
                                onClick = onNavigateToSettings,
                            ) {
                                Icon(
                                    imageVector = AppIcons.SettingsOutlined,
                                    contentDescription = "view settings",
                                )
                            }
                        }
                    }

                    TopLevelDestination.PROFILE -> {
                        IconButton(
                            onClick = onNavigateToEditProfile,
                        ) {
                            Icon(
                                imageVector = AppIcons.Edit2,
                                contentDescription = "Edit Profile",
                            )
                        }
                    }

                    else -> {}
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = modifier.testTag("mifosTopAppBar"),
    )
}

@Composable
private fun MifosNavRail(
    destinations: List<TopLevelDestination>,
    destinationsWithUnreadResources: Set<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    MifosNavigationRail(modifier = modifier) {
        destinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            val hasUnread = destinationsWithUnreadResources.contains(destination)
            MifosNavigationRailItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = destination.unselectedIcon,
                        contentDescription = null,
                    )
                },
                modifier = if (hasUnread) Modifier.notificationDot() else Modifier,
                selectedIcon = {
                    Icon(
                        imageVector = destination.selectedIcon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.iconText)) },
            )
        }
    }
}

@Composable
private fun MifosBottomBar(
    destinations: List<TopLevelDestination>,
    destinationsWithUnreadResources: Set<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    MifosNavigationBar(
        modifier = modifier,
    ) {
        destinations.forEach { destination ->
            val hasUnread = destinationsWithUnreadResources.contains(destination)
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            MifosNavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = destination.unselectedIcon,
                        contentDescription = null,
                    )
                },
                modifier = if (hasUnread) Modifier.notificationDot() else Modifier,
                selectedIcon = {
                    Icon(
                        imageVector = destination.selectedIcon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.iconText)) },
            )
        }
    }
}

private fun Modifier.notificationDot(): Modifier =
    composed {
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        drawWithContent {
            drawContent()
            drawCircle(
                tertiaryColor,
                radius = 5.dp.toPx(),
                // This is based on the dimensions of the NavigationBar's "indicator pill";
                // however, its parameters are private, so we must depend on them implicitly
                // (NavigationBarTokens.ActiveIndicatorWidth = 64.dp)
                center =
                center +
                    Offset(
                        64.dp.toPx() * .45f,
                        32.dp.toPx() * -.45f - 6.dp.toPx(),
                    ),
            )
        }
    }

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false
