/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mifos.core.datastore.UserPreferencesRepository

class OnboardingViewmodel(
    private val preferenceRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val totalPages = Total_Pages

    fun onNextPage() {
        if (_currentPage.value < totalPages) {
            _currentPage.value += 1
        } else {
            updateOnboardingStatus()
        }
    }

    private fun updateOnboardingStatus() {
        viewModelScope.launch {
            preferenceRepository.setFirstTimeState(false)
        }
    }
}
