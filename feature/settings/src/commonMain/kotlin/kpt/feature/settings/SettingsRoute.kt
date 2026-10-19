/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions

/** Route for the settings screen. */
@Serializable
data object SettingsRoute

/** Route for notification settings. */
@Serializable
data object NotificationRoute

/** Route for the sync-and-drafts screen — pending writes and saved drafts. */
@Serializable
data object SyncAndDraftsRoute

/**
 * Navigates to settings.
 *
 * @param navOptions optional nav options.
 */
fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SettingsRoute, navOptions)

/**
 * Navigates to notification settings.
 *
 * @param navOptions optional nav options.
 */
fun NavController.navigateToNotification(navOptions: NavOptions? = null) = navigate(NotificationRoute, navOptions)

/**
 * Navigates to the sync-and-drafts screen.
 *
 * @param navOptions optional nav options.
 */
fun NavController.navigateToSyncAndDrafts(navOptions: NavOptions? = null) = navigate(SyncAndDraftsRoute, navOptions)

/**
 * The settings backbone destination. [settingsBody] is the fork-owned inner content (default supplied by
 * `cmp-navigation`'s `BackboneRegistry.settingsBody`, which wires the back / sync-and-drafts / dev-menu
 * callbacks into the demo body); this template graph forwards the opaque body. (WS01 base-feature seam,
 * epic AC7 — mirrors the `home` shell/seam split.)
 */
fun NavGraphBuilder.settingsDestination(settingsBody: @Composable () -> Unit = {}) {
    composableWithPushTransitions<SettingsRoute> {
        settingsBody()
    }
}

/**
 * Registers the notification-settings destination.
 *
 * @param onBackClick invoked to leave the screen.
 */
fun NavGraphBuilder.notificationDestination(onBackClick: () -> Unit) {
    composableWithPushTransitions<NotificationRoute> {
        NotificationScreen(
            onBackClick = onBackClick,
        )
    }
}

/**
 * Registers the sync-and-drafts destination.
 *
 * @param onBackClick invoked to leave the screen.
 */
fun NavGraphBuilder.syncAndDraftsDestination(onBackClick: () -> Unit) {
    composableWithPushTransitions<SyncAndDraftsRoute> {
        SyncAndDraftsScreen(
            onBackClick = onBackClick,
        )
    }
}
