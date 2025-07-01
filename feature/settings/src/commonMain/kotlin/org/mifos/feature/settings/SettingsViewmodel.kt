/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mifos.core.data.repository.UserDataRepository
import org.mifos.core.model.DarkThemeConfig
import org.mifos.core.model.ThemeBrand
import template.core.base.analytics.AnalyticsHelper

class SettingsViewmodel(
    private val settingsRepository: UserDataRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {
    val settingsUiState: StateFlow<SettingsUiState> = settingsRepository.userData
        .map { userDate ->
            SettingsUiState.Success(
                settings = UserEditableSettings(
                    brand = userDate.themeBrand,
                    useDynamicColor = userDate.useDynamicColor,
                    darkThemeConfig = userDate.darkThemeConfig,
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState.Loading,
        )

    fun updateThemeBrand(themeBrand: ThemeBrand) {
        viewModelScope.launch {
            analyticsHelper.logThemeBrandChanged(themeBrand)
            settingsRepository.setThemeBrand(themeBrand)
        }
    }

    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            analyticsHelper.logThemeChanged(darkThemeConfig)
            settingsRepository.setDarkThemeConfig(darkThemeConfig)
        }
    }

    fun updateDynamicColorPreference(useDynamicColor: Boolean) {
        viewModelScope.launch {
            analyticsHelper.logDynamicColorPreferences(useDynamicColor)
            settingsRepository.setDynamicColorPreference(useDynamicColor)
        }
    }
}

data class UserEditableSettings(
    val brand: ThemeBrand,
    val useDynamicColor: Boolean,
    val darkThemeConfig: DarkThemeConfig,
)

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val settings: UserEditableSettings) : SettingsUiState
}

private fun AnalyticsHelper.logThemeBrandChanged(themeBrand: ThemeBrand) {
    logEvent(
        type = "theme_brand_changed",
        params = mapOf(
            "theme_brand" to themeBrand.name,
        ),
    )
}

private fun AnalyticsHelper.logDynamicColorPreferences(useDynamicColor: Boolean) {
    logEvent(
        type = "dynamic_color_preference_changed",
        params = mapOf(
            "use_dynamic_color" to useDynamicColor.toString(),
        ),
    )
}

private fun AnalyticsHelper.logThemeChanged(themeConfig: DarkThemeConfig) {
    logEvent(
        type = "dark_theme_config_changed",
        params = mapOf(
            "dark_theme_config" to themeConfig.name,
        ),
    )
}
