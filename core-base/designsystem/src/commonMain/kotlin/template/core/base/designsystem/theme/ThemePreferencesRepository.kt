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

interface ThemePreferencesRepository {
    val themeData: Flow<ThemeData>

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    suspend fun getDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    suspend fun getDynamicColorPreference(useDynamicColor: Boolean)
}
