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

abstract class ThemeBaseViewmodel : ViewModel() {
    abstract val themeUiState: StateFlow<ThemeData>
    abstract fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    abstract fun updateDynamicColorPreference(useDynamicColor: Boolean)
    abstract fun getDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    abstract fun getDynamicColorPreference(useDynamicColor: Boolean)
}
