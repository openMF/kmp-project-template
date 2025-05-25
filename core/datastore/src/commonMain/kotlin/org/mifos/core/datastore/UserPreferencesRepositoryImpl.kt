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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.mifos.core.model.DarkThemeConfig
import org.mifos.core.model.ThemeBrand
import org.mifos.core.model.UserData
import template.core.base.datastore.repository.ReactivePreferencesRepository

/**
 * Implementation of UserPreferencesRepository using the reactive datastore.
 *
 * This implementation leverages the reactive capabilities of the datastore to provide
 * real-time updates to preferences and maintains a combined userData flow.
 */
class UserPreferencesRepositoryImpl(
    private val preferencesStore: ReactivePreferencesRepository,
) : UserPreferencesRepository {

    companion object {
        private const val THEME_BRAND_KEY = "theme_brand"
        private const val DARK_THEME_CONFIG_KEY = "dark_theme_config"
        private const val DYNAMIC_COLOR_KEY = "use_dynamic_color"

        // Default values
        private val DEFAULT_THEME_BRAND = ThemeBrand.DEFAULT
        private val DEFAULT_DARK_THEME_CONFIG = DarkThemeConfig.FOLLOW_SYSTEM
        private const val DEFAULT_DYNAMIC_COLOR = false
    }

    /**
     * Reactive userData flow that combines all user preferences.
     * Automatically updates whenever any preference changes.
     */
    override val userData: Flow<UserData> = combine(
        observeThemeBrand(),
        observeDarkThemeConfig(),
        observeDynamicColorPreference(),
    ) { themeBrand, darkThemeConfig, useDynamicColor ->
        UserData(
            themeBrand = themeBrand,
            darkThemeConfig = darkThemeConfig,
            useDynamicColor = useDynamicColor,
        )
    }.distinctUntilChanged()

    // Theme Brand Operations
    override suspend fun setThemeBrand(themeBrand: ThemeBrand): Result<Unit> {
        return preferencesStore.savePreference(THEME_BRAND_KEY, themeBrand.brandName)
    }

    override suspend fun getThemeBrand(): Result<ThemeBrand> {
        return preferencesStore.getPreference(THEME_BRAND_KEY, DEFAULT_THEME_BRAND.brandName)
            .map { brandName -> ThemeBrand.fromString(brandName) }
    }

    override fun observeThemeBrand(): Flow<ThemeBrand> {
        return preferencesStore.observePreference(THEME_BRAND_KEY, DEFAULT_THEME_BRAND.brandName)
            .map { brandName -> ThemeBrand.fromString(brandName) }
    }

    // Dark Theme Configuration Operations
    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig): Result<Unit> {
        return preferencesStore.savePreference(DARK_THEME_CONFIG_KEY, darkThemeConfig.name)
    }

    override suspend fun getDarkThemeConfig(): Result<DarkThemeConfig> {
        return preferencesStore.getPreference(DARK_THEME_CONFIG_KEY, DEFAULT_DARK_THEME_CONFIG.name)
            .map { configName -> DarkThemeConfig.fromString(configName) }
    }

    override fun observeDarkThemeConfig(): Flow<DarkThemeConfig> {
        return preferencesStore.observePreference(
            DARK_THEME_CONFIG_KEY,
            DEFAULT_DARK_THEME_CONFIG.name,
        )
            .map { configName -> DarkThemeConfig.fromString(configName) }
    }

    // Dynamic Color Preference Operations
    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean): Result<Unit> {
        return preferencesStore.savePreference(DYNAMIC_COLOR_KEY, useDynamicColor)
    }

    override suspend fun getDynamicColorPreference(): Result<Boolean> {
        return preferencesStore.getPreference(DYNAMIC_COLOR_KEY, DEFAULT_DYNAMIC_COLOR)
    }

    override fun observeDynamicColorPreference(): Flow<Boolean> {
        return preferencesStore.observePreference(DYNAMIC_COLOR_KEY, DEFAULT_DYNAMIC_COLOR)
    }

    // Batch Operations
    override suspend fun resetToDefaults(): Result<Unit> {
        return runCatching {
            setThemeBrand(DEFAULT_THEME_BRAND).getOrThrow()
            setDarkThemeConfig(DEFAULT_DARK_THEME_CONFIG).getOrThrow()
            setDynamicColorPreference(DEFAULT_DYNAMIC_COLOR).getOrThrow()
        }
    }

    override suspend fun exportPreferences(): Result<UserData> {
        return runCatching {
            UserData(
                themeBrand = getThemeBrand().getOrThrow(),
                darkThemeConfig = getDarkThemeConfig().getOrThrow(),
                useDynamicColor = getDynamicColorPreference().getOrThrow(),
            )
        }
    }

    override suspend fun importPreferences(userData: UserData): Result<Unit> {
        return runCatching {
            setThemeBrand(userData.themeBrand).getOrThrow()
            setDarkThemeConfig(userData.darkThemeConfig).getOrThrow()
            setDynamicColorPreference(userData.useDynamicColor).getOrThrow()
        }
    }
}
