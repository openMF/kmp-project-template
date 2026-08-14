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

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions

@Serializable
data object SettingsRoute

@Serializable
data object NotificationRoute

@Serializable
data object SyncAndDraftsRoute

fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SettingsRoute, navOptions)

fun NavController.navigateToNotification(navOptions: NavOptions? = null) = navigate(NotificationRoute, navOptions)

fun NavController.navigateToSyncAndDrafts(navOptions: NavOptions? = null) = navigate(SyncAndDraftsRoute, navOptions)

fun NavGraphBuilder.settingsDestination(
    onBackClick: () -> Unit,
    onSyncAndDraftsClick: () -> Unit,
    onTransitionGalleryClick: (() -> Unit)? = null,
    onStateGalleryClick: (() -> Unit)? = null,
) {
    composableWithPushTransitions<SettingsRoute> {
        SettingsScreen(
            onBackClick = onBackClick,
            onSyncAndDraftsClick = onSyncAndDraftsClick,
            onTransitionGalleryClick = onTransitionGalleryClick,
            onStateGalleryClick = onStateGalleryClick,
        )
    }
}

fun NavGraphBuilder.notificationDestination(onBackClick: () -> Unit) {
    composableWithPushTransitions<NotificationRoute> {
        NotificationScreen(
            onBackClick = onBackClick,
        )
    }
}

fun NavGraphBuilder.syncAndDraftsDestination(onBackClick: () -> Unit) {
    composableWithPushTransitions<SyncAndDraftsRoute> {
        SyncAndDraftsScreen(
            onBackClick = onBackClick,
        )
    }
}
