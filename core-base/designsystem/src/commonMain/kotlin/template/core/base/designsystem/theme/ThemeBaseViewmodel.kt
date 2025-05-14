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

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Base ViewModel contract for managing theme-related preferences in a composable and testable way.
 *
 * This includes user preferences for dark theme behavior, dynamic color usage,
 * and color palette selection.
 */
abstract class ThemeBaseViewmodel : ViewModel() {

    /**
     * A [StateFlow] representing the current theme configuration as used by the UI layer.
     */
    abstract val themeUiState: StateFlow<ThemeData>

    /**
     * Updates the user's dark theme preference.
     *
     * @param darkThemeConfig The [DarkThemeConfig] to apply.
     */
    abstract fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    /**
     * Updates the user's dynamic color usage preference.
     *
     * @param useDynamicColor Whether to enable dynamic color.
     */
    abstract fun updateDynamicColorPreference(useDynamicColor: Boolean)

    /**
     * Updates the user's selected color palette name (e.g., brand/theme variant).
     *
     * @param paletteName The name of the palette to apply.
     */
    abstract fun updateColorPaletteName(paletteName: String)
}
