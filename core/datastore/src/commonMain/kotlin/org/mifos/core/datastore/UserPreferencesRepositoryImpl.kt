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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mifos.core.model.DarkThemeConfig
import org.mifos.core.model.ThemeBrand
import org.mifos.core.model.UserData
import org.mifos.corebase.datastore.UserPreferencesDataStore

private const val THEME_BRAND_KEY = "theme_brand"
private const val DARK_THEME_CONFIG_KEY = "dark_theme_config"
private const val DYNAMIC_COLOR_KEY = "use_dynamic_color"

class UserPreferencesRepositoryImpl(
    private val dataStore: UserPreferencesDataStore,
) : UserPreferencesRepository {

    private var _userData: MutableStateFlow<UserData> = MutableStateFlow(UserData())
    override val userData: Flow<UserData> = _userData.asStateFlow()

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        dataStore.putValue(key = THEME_BRAND_KEY, value = themeBrand.brandName)
        getThemeBrand(themeBrand)
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        dataStore.putValue(key = DARK_THEME_CONFIG_KEY, value = darkThemeConfig.name)
        getDarkThemeConfig(darkThemeConfig)
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        dataStore.putValue(key = DYNAMIC_COLOR_KEY, value = useDynamicColor)
        getDynamicColorPreference(useDynamicColor)
    }

    override suspend fun getThemeBrand(themeBrand: ThemeBrand) {
        val themeBrandString =
            dataStore.getValue(key = THEME_BRAND_KEY, default = ThemeBrand.DEFAULT.brandName)
        _userData.value = _userData.value.copy(themeBrand = ThemeBrand.fromString(themeBrandString))
    }

    override suspend fun getDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        val darkThemeConfigString =
            dataStore.getValue(
                key = DARK_THEME_CONFIG_KEY,
                default = DarkThemeConfig.FOLLOW_SYSTEM.name,
            )
        _userData.value =
            _userData.value.copy(darkThemeConfig = DarkThemeConfig.fromString(darkThemeConfigString))
    }

    override suspend fun getDynamicColorPreference(useDynamicColor: Boolean) {
        val useDynamicColorBoolean =
            dataStore.getValue(key = DYNAMIC_COLOR_KEY, default = false)
        _userData.value = _userData.value.copy(useDynamicColor = useDynamicColorBoolean)
    }
}
