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

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SettingsListener
import com.russhwolf.settings.coroutines.getLongFlow
import com.russhwolf.settings.coroutines.getLongStateFlow
import com.russhwolf.settings.long
import com.russhwolf.settings.serialization.containsValue
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import com.russhwolf.settings.serialization.removeValue
import com.russhwolf.settings.serialization.serializedValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import org.mifos.core.datastore.model.SampleUser

private const val USER_KEY = "sample_user"
private const val LAST_LOGIN_KEY = "last_login"

class UserPreferencesDataStore(
    private val settings: Settings,
    private val dispatcher: CoroutineDispatcher,
    scope: CoroutineScope,
) {

    // --- Basic Operations ---

    // Store primitive value
    fun putLastLogin(timeStamp: Long) {
        settings.putLong(LAST_LOGIN_KEY, timeStamp)
    }

    // Retrieve primitive value with default
    fun getLastLogin(default: Long = 0): Long {
        return settings.getLong(LAST_LOGIN_KEY, default)
    }

    // Retrieve nullable primitive
    fun getLastLoginOrNull(): Long? {
        return settings.getLongOrNull(LAST_LOGIN_KEY)
    }

    // Property delegate for primitive
    val lastLogin: Long by settings.long(LAST_LOGIN_KEY, defaultValue = 0)

    // Check key existence
    fun hasLastLogin(): Boolean {
        return settings.hasKey(LAST_LOGIN_KEY)
    }

    // Remove key
    fun removeLastLogin() {
        settings.remove(LAST_LOGIN_KEY)
    }

    // Clear all
    suspend fun clearAll() {
        withContext(dispatcher) {
            settings.clear()
        }
    }

    // Get all keys and size
    val allKeys: Set<String> get() = settings.keys
    val size: Int get() = settings.size

    // --- Serialization Operations ---

    // Store serialized object
    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
    suspend fun saveUser(user: SampleUser) {
        withContext(dispatcher) {
            settings.encodeValue(
                serializer = SampleUser.serializer(),
                key = USER_KEY,
                value = user,
            )
            _userFlow.value = user
        }
    }

    // Retrieve serialized object with default
    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
    fun getUser(): SampleUser {
        return settings.decodeValue(
            serializer = SampleUser.serializer(),
            key = USER_KEY,
            defaultValue = SampleUser.DEFAULT,
        )
    }

    // Retrieve nullable serialized object
    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
    fun getUserOrNull(): SampleUser? {
        return settings.decodeValueOrNull(
            serializer = SampleUser.serializer(),
            key = USER_KEY,
        )
    }

    // Property delegate for serialized object
    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
    val userProperty: SampleUser by settings.serializedValue(
        serializer = SampleUser.serializer(),
        key = USER_KEY,
        defaultValue = SampleUser.DEFAULT,
    )

    // Check serialized object existence
    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
    fun hasUser(): Boolean {
        return settings.containsValue(
            serializer = SampleUser.serializer(),
            key = USER_KEY,
        )
    }

    // Remove serialized object
    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
    suspend fun removeUser() {
        withContext(dispatcher) {
            settings.removeValue(
                serializer = SampleUser.serializer(),
                key = USER_KEY,
            )
            _userFlow.value = SampleUser.DEFAULT
        }
    }

    // --- Listener Operations ---
    private val observableSettings: ObservableSettings = settings as ObservableSettings
    fun observeAgeChange(onChange: (Int) -> Int): SettingsListener {
        return observableSettings.addIntListener(key = "user_age", defaultValue = 0) { value ->
            onChange(value)
        }
    }

    // --- Coroutine/Flow Operations ---
    private val _userFlow = MutableStateFlow(getUser())

    // Flow for serialized user
    val userFlow: StateFlow<SampleUser> get() = _userFlow.asStateFlow()

    // Flow for primitive (requires ObservableSettings)
    @OptIn(ExperimentalSettingsApi::class)
    val lastLoginFlow: Flow<Long> = observableSettings.getLongFlow(
        key = LAST_LOGIN_KEY,
        defaultValue = 0L,
    )

    // StateFlow for primitive
    @OptIn(ExperimentalSettingsApi::class)
    val lastLoginStateFlow: StateFlow<Long> = observableSettings.getLongStateFlow(
        key = LAST_LOGIN_KEY,
        coroutineScope = scope,
        defaultValue = 0L,
    )

    // Update specific field (example with serialization)
    suspend fun updateUserName(newName: String) {
        withContext(dispatcher) {
            val currentUser = getUser()
            val updatedUser = currentUser.copy(name = newName)
            saveUser(updatedUser)
        }
    }
}
