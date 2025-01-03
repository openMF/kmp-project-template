package org.mifos.feature.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.mifos.core.ui.composableWithPushTransitions
import org.mifos.core.ui.composableWithSlideTransitions

const val SETTINGS_ROUTE = "settings_route"
const val NOTIFICATION_ROUTE = "notification_route"

fun NavController.navigateToSettings(navOptions: NavOptions? = null) =
    navigate(SETTINGS_ROUTE, navOptions)

fun NavController.navigateToNotification(navOptions: NavOptions? = null) =
    navigate(NOTIFICATION_ROUTE, navOptions)

fun NavGraphBuilder.settingsScreen(
    onBackClick: () -> Unit,
) {
    composableWithPushTransitions(route = SETTINGS_ROUTE) {
        SettingsScreen(
            onBackClick = onBackClick,
        )
    }
}

fun NavGraphBuilder.notificationScreen(
    onBackClick: () -> Unit,
) {
    composableWithPushTransitions(route = NOTIFICATION_ROUTE) {
        NotificationScreen(
            onBackClick = onBackClick,
        )
    }
}