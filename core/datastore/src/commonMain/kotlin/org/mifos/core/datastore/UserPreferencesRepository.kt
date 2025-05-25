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

/**
 * Repository interface for managing user preferences with reactive capabilities.
 *
 * This interface provides reactive access to user preferences including theme settings,
 * dark mode configuration, and dynamic color preferences.
 */
interface UserPreferencesRepository {

    /**
     * Reactive stream of current user data combining all preferences.
     * Emits whenever any preference changes.
     */
    val userData: Flow<UserData>

    // Theme Brand Operations
    suspend fun setThemeBrand(themeBrand: ThemeBrand): Result<Unit>
    suspend fun getThemeBrand(): Result<ThemeBrand>
    fun observeThemeBrand(): Flow<ThemeBrand>

    // Dark Theme Configuration Operations
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig): Result<Unit>
    suspend fun getDarkThemeConfig(): Result<DarkThemeConfig>
    fun observeDarkThemeConfig(): Flow<DarkThemeConfig>

    // Dynamic Color Preference Operations
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean): Result<Unit>
    suspend fun getDynamicColorPreference(): Result<Boolean>
    fun observeDynamicColorPreference(): Flow<Boolean>

    // Batch Operations
    suspend fun resetToDefaults(): Result<Unit>
    suspend fun exportPreferences(): Result<UserData>
    suspend fun importPreferences(userData: UserData): Result<Unit>
}
