/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.utils

import androidx.compose.ui.graphics.vector.ImageVector
import cmp.navigation.generated.resources.Res
import cmp.navigation.generated.resources.app_name
import cmp.navigation.generated.resources.home
import cmp.navigation.generated.resources.profile
import org.jetbrains.compose.resources.StringResource
import org.mifos.core.designsystem.icon.AppIcons

/**
 * Type for the top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composables.
 */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconText: StringResource,
    val titleText: StringResource,
) {
    HOME(
        selectedIcon = AppIcons.Home,
        unselectedIcon = AppIcons.HomeBoarder,
        iconText = Res.string.home,
        titleText = Res.string.app_name,
    ),
    PROFILE(
        selectedIcon = AppIcons.Profile,
        unselectedIcon = AppIcons.ProfileBoarder,
        iconText = Res.string.profile,
        titleText = Res.string.profile,
    ),
}
