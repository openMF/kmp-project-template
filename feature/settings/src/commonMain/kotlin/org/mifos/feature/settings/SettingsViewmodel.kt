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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mifos.core.datastore.UserPreferencesRepository
import org.mifos.core.datastore.model.AppSettings

class SettingsViewmodel(
    private val settingsRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _settingsUiState = MutableStateFlow(AppSettings.DEFAULT)
    val settingsUiState = _settingsUiState.asStateFlow()

    private suspend fun getSettings() {
        _settingsUiState.value = settingsRepository.getSettings(
            defaultValue = AppSettings.DEFAULT,
        )
    }

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings)
            getSettings()
        }
    }
}
