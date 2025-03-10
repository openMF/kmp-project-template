/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.corebase.datastore

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer

/**
 * A data store class for managing user preferences using a settings storage mechanism.
 * This class provides methods to store, retrieve, and manage key-value pairs in a thread-safe manner
 * using coroutines and a specified dispatcher.
 *
 * @property settings The underlying settings storage implementation used to persist preferences.
 * @property dispatcher The [CoroutineDispatcher] used to execute storage operations, ensuring thread safety.
 */
class UserPreferencesDataStore(
    private val settings: Settings,
    private val dispatcher: CoroutineDispatcher,
) {

    /**
     * Stores a value associated with the specified key in the data store.
     * Supports primitive types directly and custom types via a provided [KSerializer].
     *
     * @param T The type of the value to store.
     * @param key The unique key associated with the value.
     * @param value The value to store.
     * @param serializer The [KSerializer] used to serialize custom types; required for non-primitive types.
     * @throws IllegalArgumentException If a custom type is provided without a serializer.
     */
    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
    suspend fun <T> putValue(
        key: String,
        value: T,
        serializer: KSerializer<T>? = null,
    ) {
        withContext(dispatcher) {
            when (value) {
                is Int -> settings.putInt(key, value)
                is Long -> settings.putLong(key, value)
                is Float -> settings.putFloat(key, value)
                is Double -> settings.putDouble(key, value)
                is String -> settings.putString(key, value)
                is Boolean -> settings.putBoolean(key, value)
                else -> {
                    require(serializer != null) { "Unsupported type or no serializer provided for ${value!!::class}" }
                    settings.encodeValue(
                        serializer = serializer,
                        value = value,
                        key = key,
                    )
                }
            }
        }
    }

    /**
     * Retrieves a value associated with the specified key from the data store.
     * Returns the stored value or the provided default if the key does not exist.
     * Supports primitive types directly and custom types via a provided [KSerializer].
     *
     * @param T The type of the value to retrieve.
     * @param key The unique key associated with the value.
     * @param default The default value to return if the key is not found.
     * @param serializer The [KSerializer] used to deserialize custom types; required for non-primitive types.
     * @return The stored value or the default value if the key is not found.
     * @throws IllegalArgumentException If a custom type is requested without a serializer.
     */
    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
    suspend fun <T> getValue(
        key: String,
        default: T,
        serializer: KSerializer<T>? = null,
    ): T {
        return withContext(dispatcher) {
            when (default) {
                is Int -> settings.getInt(key, default) as T
                is Long -> settings.getLong(key, default) as T
                is Float -> settings.getFloat(key, default) as T
                is Double -> settings.getDouble(key, default) as T
                is String -> settings.getString(key, default) as T
                is Boolean -> settings.getBoolean(key, default) as T
                else -> {
                    require(serializer != null) { "Unsupported type or no serializer provided for ${default!!::class}" }
                    settings.decodeValue(
                        serializer = serializer,
                        key = key,
                        defaultValue = default,
                    )
                }
            }
        }
    }

    /**
     * Checks if the specified [key] exists in the data store.
     *
     * @param key The key to check.
     * @return `true` if the key exists, `false` otherwise.
     */
    suspend fun hasKey(key: String): Boolean {
        return settings.hasKey(key)
    }

    /**
     * Removes the value associated with the specified [key] from the data store.
     *
     * @param key The key whose value should be removed.
     */
    fun removeValue(key: String) {
        settings.remove(key)
    }

    /**
     * Clears all stored preferences in the data store.
     */
    fun clearAll() {
        settings.clear()
    }

    /**
     * Retrieves all keys currently stored in the data store.
     *
     * @return A [Set] containing all keys in the data store.
     */
    fun getAllKeys(): Set<String> {
        return settings.keys
    }

    /**
     * Returns the total number of key-value pairs stored in the data store.
     *
     * @return The number of stored preferences.
     */
    fun getSize(): Int {
        return settings.size
    }
}
