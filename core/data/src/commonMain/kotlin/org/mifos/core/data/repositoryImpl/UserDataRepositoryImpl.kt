/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.repositoryImpl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.mifos.core.data.repository.UserDataRepository
import org.mifos.core.datastore.UserPreferencesRepository
import org.mifos.core.model.DarkThemeConfig
import org.mifos.core.model.LanguageConfig
import org.mifos.core.model.ThemeBrand
import org.mifos.core.model.UserData

class UserDataRepositoryImpl(
    private val preferencesRepository: UserPreferencesRepository,
) : UserDataRepository {
    override val userData: StateFlow<UserData>
        get() = preferencesRepository.userData

    override val authToken: String?
        get() = preferencesRepository.authToken

    override val passcode: String
        get() = preferencesRepository.passcode

    override val observeLanguage: Flow<LanguageConfig>
        get() = preferencesRepository.observeLanguage

    override val observeDarkThemeConfig: Flow<DarkThemeConfig>
        get() = preferencesRepository.observeDarkThemeConfig

    override val observeDynamicColorPreference: Flow<Boolean>
        get() = preferencesRepository.observeDynamicColorPreference

    override val observeScreenCapturePreference: Flow<Boolean>
        get() = preferencesRepository.observeScreenCapturePreference

    override suspend fun setLanguage(language: LanguageConfig) = preferencesRepository.setLanguage(language)

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) = preferencesRepository.setThemeBrand(themeBrand)

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) =
        preferencesRepository.setDarkThemeConfig(darkThemeConfig)

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) =
        preferencesRepository.setDynamicColorPreference(useDynamicColor)

    override suspend fun setIsAuthenticated(isAuthenticated: Boolean) =
        preferencesRepository.setIsAuthenticated(isAuthenticated)

    override suspend fun setIsUnlocked(isUnlocked: Boolean) = preferencesRepository.setIsUnlocked(isUnlocked)

    override suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean) =
        preferencesRepository.setIsPasscodeEnabled(isPasscodeEnabled)

    override suspend fun setIsBiometricsEnabled(isBiometricsEnabled: Boolean) =
        preferencesRepository.setIsBiometricsEnabled(isBiometricsEnabled)

    override suspend fun setShowOnboarding(showOnboarding: Boolean) =
        preferencesRepository.setShowOnboarding(showOnboarding)

    override suspend fun setFirstTimeState(firstTimeState: Boolean) =
        preferencesRepository.setFirstTimeState(firstTimeState)

    override suspend fun setPasscode(passcode: String) = preferencesRepository.setPasscode(passcode)

    override suspend fun clearUserData() = preferencesRepository.clearUserData()
}
