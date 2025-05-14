/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.theme

/**
 * Represents the current theme configuration used by the CMP design system.
 *
 * This data class combines color palette selection, dark theme behavior, and dynamic color support,
 * and is typically observed via [ThemePreferencesRepository.themeData].
 *
 * @param colorPaletteName The name of the currently selected color palette.
 * @param darkThemeConfig Configuration that determines when dark theme is applied.
 * Defaults to [DarkThemeConfig.FOLLOW_SYSTEM].
 * @param useDynamicColor Whether dynamic color theming is enabled based on system wallpaper or theme.
 * Defaults to `false`.
 */
data class ThemeData(
    val colorPaletteName: String,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = false,
)
