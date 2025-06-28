/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)

package org.mifos.core.datastore

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import org.mifos.core.model.DarkThemeConfig
import org.mifos.core.model.LanguageConfig
import org.mifos.core.model.ThemeBrand
import org.mifos.core.model.UserData
import template.core.base.common.manager.DispatcherManager

private const val USER_DATA_KEY = "user_data_key"

class UserPreferencesRepositoryImpl(
    private val settings: Settings,
    private val dispatcher: DispatcherManager,
) : UserPreferencesRepository {

    private val _userData = MutableStateFlow(
        settings.decodeValue(
            key = USER_DATA_KEY,
            serializer = UserData.serializer(),
            defaultValue = settings.decodeValueOrNull(
                key = USER_DATA_KEY,
                serializer = UserData.serializer(),
            ) ?: UserData.DEFAULT,
        ),
    )

    override val userData: StateFlow<UserData>
        get() = _userData.asStateFlow()

    override val authToken: String?
        get() = null

    override val passcode: String
        get() = _userData.value.passcode

    override val observeLanguage: Flow<LanguageConfig>
        get() = _userData.map { it.appLanguage }

    override val observeDarkThemeConfig: Flow<DarkThemeConfig>
        get() = _userData.map { it.darkThemeConfig }

    override val observeDynamicColorPreference: Flow<Boolean>
        get() = _userData.map { it.useDynamicColor }

    override val observeScreenCapturePreference: Flow<Boolean>
        get() = _userData.map { it.enableScreenCapture }

    override suspend fun setLanguage(language: LanguageConfig) =
        withContext(dispatcher.io) {
            val newPreference = settings.getUserPreference().copy(appLanguage = language)
            settings.putUserPreference(newPreference)
            _userData.value = newPreference
        }

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) =
        withContext(dispatcher.io) {
            val newPreference = settings.getUserPreference().copy(themeBrand = themeBrand)
            settings.putUserPreference(newPreference)
            _userData.value = newPreference
        }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) =
        withContext(dispatcher.io) {
            val newPreference = settings.getUserPreference().copy(darkThemeConfig = darkThemeConfig)
            settings.putUserPreference(newPreference)
            _userData.value = newPreference
        }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) =
        withContext(dispatcher.io) {
            val newPreference = settings.getUserPreference().copy(useDynamicColor = useDynamicColor)
            settings.putUserPreference(newPreference)
            _userData.value = newPreference
        }

    override suspend fun setIsAuthenticated(isAuthenticated: Boolean) =
        withContext(dispatcher.io) {
            val newPreference = settings.getUserPreference().copy(isAuthenticated = isAuthenticated)
            settings.putUserPreference(newPreference)
            _userData.value = newPreference
        }

    override suspend fun setIsUnlocked(isUnlocked: Boolean) =
        withContext(dispatcher.io) {
            val newPreference = settings.getUserPreference().copy(isUnlocked = isUnlocked)
            settings.putUserPreference(newPreference)
            _userData.value = newPreference
        }

    override suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean) =
        withContext(dispatcher.io) {
            val newPreference =
                settings.getUserPreference().copy(isPasscodeEnabled = isPasscodeEnabled)
            settings.putUserPreference(newPreference)
            _userData.value = newPreference
        }

    override suspend fun setIsBiometricsEnabled(isBiometricsEnabled: Boolean) =
        withContext(dispatcher.io) {
            val newPreference =
                settings.getUserPreference().copy(isBiometricsEnabled = isBiometricsEnabled)
            settings.putUserPreference(newPreference)
            _userData.value = newPreference
        }

    override suspend fun setShowOnboarding(showOnboarding: Boolean) =
        withContext(dispatcher.io) {
            val newPreference = settings.getUserPreference().copy(showOnboarding = showOnboarding)
            settings.putUserPreference(newPreference)
            _userData.value = newPreference
        }

    override suspend fun setFirstTimeState(firstTimeState: Boolean) =
        withContext(dispatcher.io) {
            val newPreference = settings.getUserPreference().copy(firstTimeUser = firstTimeState)
            settings.putUserPreference(newPreference)
            _userData.value = newPreference
        }

    override suspend fun setPasscode(passcode: String) {
        withContext(dispatcher.io) {
            val newPreference = settings.getUserPreference().copy(passcode = passcode)
            settings.putUserPreference(newPreference)
            _userData.value = newPreference
        }
    }

    override suspend fun setScreenCapturePreference(isScreenCaptureEnabled: Boolean) =
        withContext(dispatcher.io) {
            val newPreference = settings.getUserPreference().copy(
                enableScreenCapture = isScreenCaptureEnabled,
            )
            settings.putUserPreference(newPreference)
            _userData.value = newPreference
        }

    override suspend fun clearUserData() {
        setIsAuthenticated(false)
        // TODO:: Uncomment this line when Unlocked Screen is Present
        // setIsUnlocked(false)
    }
}

private fun Settings.getUserPreference(): UserData {
    return decodeValue(
        key = USER_DATA_KEY,
        serializer = UserData.serializer(),
        defaultValue = UserData.DEFAULT,
    )
}

private fun Settings.putUserPreference(preference: UserData) {
    encodeValue(
        key = USER_DATA_KEY,
        serializer = UserData.serializer(),
        value = preference,
    )
}
