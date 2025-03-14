/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.mifos.core.datastore.UserPreferencesRepository
import org.mifos.core.model.DarkThemeConfig
import org.mifos.core.model.ThemeBrand
import org.mifos.core.model.UserData

class AppViewModel(
    settingsRepository: UserPreferencesRepository,
) : ViewModel() {
    val uiState: StateFlow<AppUiState> = settingsRepository.userData
        .onStart {
            settingsRepository.getThemeBrand(ThemeBrand.DEFAULT)
            settingsRepository.getDarkThemeConfig(DarkThemeConfig.FOLLOW_SYSTEM)
            settingsRepository.getDynamicColorPreference(false)
        }
        .map { userDate ->
            AppUiState.Success(userDate)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppUiState.Loading,
        )
}

sealed interface AppUiState {
    data object Loading : AppUiState
    data class Success(val userData: UserData) : AppUiState {
        override val shouldDisplayDynamicTheming = userData.useDynamicColor
        override val shouldUseAndroidTheme = when (userData.themeBrand) {
            ThemeBrand.DEFAULT -> false
            ThemeBrand.ANDROID -> true
        }

        override fun shouldUseDarkTheme(isSystemInDarkTheme: Boolean): Boolean =
            when (userData.darkThemeConfig) {
                DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme
                DarkThemeConfig.LIGHT -> false
                DarkThemeConfig.DARK -> true
            }
    }

    val shouldDisplayDynamicTheming: Boolean get() = true
    val shouldUseAndroidTheme: Boolean get() = false
    fun shouldUseDarkTheme(isSystemInDarkTheme: Boolean) = isSystemInDarkTheme
}
