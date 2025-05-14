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

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing and observing theme-related user preferences
 * such as dark mode configuration, dynamic color usage, and color palette name.
 */
interface ThemePreferencesRepository {

    /**
     * A [Flow] that emits the current [ThemeData], representing the user's theme preferences.
     */
    val themeData: Flow<ThemeData>

    /**
     * Persists the user's selected dark theme configuration.
     *
     * @param darkThemeConfig The [DarkThemeConfig] to save.
     */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    /**
     * Persists the user's preference for enabling or disabling dynamic color.
     *
     * @param useDynamicColor Whether dynamic color should be enabled.
     */
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    /**
     * Persists the user's selected color palette name (e.g., brand or theme family).
     *
     * @param paletteName The name of the color palette (e.g., "Default", "Oceanic").
     */
    suspend fun setColorPaletteName(paletteName: String)

    /**
     * Refreshes the stored dark theme configuration and emits it via [themeData].
     */
    suspend fun refreshDarkThemeConfig()

    /**
     * Refreshes the stored dynamic color preference and emits it via [themeData].
     */
    suspend fun refreshDynamicColorPreference()

    /**
     * Refreshes the stored color palette name and emits it via [themeData].
     */
    suspend fun refreshColorPaletteName()
}
