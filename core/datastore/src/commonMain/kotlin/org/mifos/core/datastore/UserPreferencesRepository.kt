/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.datastore

import kotlinx.coroutines.flow.Flow
import org.mifos.core.model.DarkThemeConfig
import org.mifos.core.model.ThemeBrand
import org.mifos.core.model.UserData

interface UserPreferencesRepository {
    val userData: Flow<UserData>

    suspend fun setThemeBrand(themeBrand: ThemeBrand)
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    suspend fun getThemeBrand(themeBrand: ThemeBrand)
    suspend fun getDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    suspend fun getDynamicColorPreference(useDynamicColor: Boolean)
}
