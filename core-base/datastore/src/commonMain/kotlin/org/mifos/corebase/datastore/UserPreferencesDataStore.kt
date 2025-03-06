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
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.SuspendSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * A data store implementation for managing user preferences with support for primitive and serialized types.
 * This class provides suspend functions for storing and retrieving values, as well as a flow-based API for observing
 * changes to specific keys. It leverages [SuspendSettings] for synchronous operations and [FlowSettings] for
 * reactive streams, enabling both one-time reads/writes and real-time updates.
 *
 * @param suspendSettings An instance of [SuspendSettings] for performing suspend-based read/write operations.
 * @param flowSettings An instance of [FlowSettings] for observing preference changes as a [Flow].
 */
@OptIn(ExperimentalSettingsApi::class)
class UserPreferencesDataStore(
    private val suspendSettings: SuspendSettings,
    private val flowSettings: FlowSettings,
) {

    /**
     * Retrieves a value associated with the specified [key], returning the [default] value if the key is not found.
     * Supports primitive types (e.g., [Int], [Double], [String]) natively and custom types via serialization.
     *
     * @param key The key under which the value is stored.
     * @param default The default value to return if the key does not exist or retrieval fails.
     * @param serializer An optional [KSerializer] for deserializing custom types.
     * Required if [default] is not a primitive type.
     * @return The stored value of type [T], or [default] if the key is not found.
     * @throws IllegalArgumentException If [default] is not a supported primitive type and [serializer] is null.
     */
    suspend fun <T> getValue(
        key: String,
        default: T,
        serializer: KSerializer<T>? = null,
    ): T {
        return when (default) {
            is Int -> suspendSettings.getInt(key, default) as T
            is Long -> suspendSettings.getLong(key, default) as T
            is Float -> suspendSettings.getFloat(key, default) as T
            is Double -> suspendSettings.getDouble(key, default) as T
            is String -> suspendSettings.getString(key, default) as T
            is Boolean -> suspendSettings.getBoolean(key, default) as T
            else -> {
                require(serializer != null) { "Unsupported type or no serializer provided for ${default!!::class}" }
                getSerializedData(
                    key = key,
                    defaultValue = default,
                    serializer = serializer,
                )
            }
        }
    }

    /**
     * Stores a [value] under the specified [key]. Supports primitive types natively and custom types via serialization.
     *
     * @param key The key under which to store the [value].
     * @param value The value to store, of type [T].
     * @param serializer An optional [KSerializer] for serializing custom types.
     * Required if [value] is not a primitive type.
     * @throws IllegalArgumentException If [value] is not a supported primitive type and [serializer] is null.
     */
    suspend fun <T> putValue(
        key: String,
        value: T,
        serializer: KSerializer<T>? = null,
    ) {
        when (value) {
            is Int -> suspendSettings.putInt(key, value)
            is Long -> suspendSettings.putLong(key, value)
            is Float -> suspendSettings.putFloat(key, value)
            is Double -> suspendSettings.putDouble(key, value)
            is String -> suspendSettings.putString(key, value)
            is Boolean -> suspendSettings.putBoolean(key, value)
            else -> {
                require(serializer != null) { "Unsupported type or no serializer provided for ${value!!::class}" }
                putSerializableData(
                    key = key,
                    value = value,
                    serializer = serializer,
                )
            }
        }
    }

    /**
     * Observes changes to the value associated with the specified [key] as a [Flow].
     * Emits the current value and any subsequent updates.
     * Supports primitive types natively and custom types via serialization.
     *
     * @param key The key to observe.
     * @param defaultValue The default value to use if the key does not exist initially.
     * @param serializer An optional [KSerializer] for deserializing custom types.
     * Required if [defaultValue] is not a primitive type.
     * @return A [Flow] emitting values of type [T] whenever the key's value changes.
     * @throws IllegalArgumentException If [defaultValue] is not a supported primitive type and [serializer] is null.
     */
    fun <T> observeKeyFlow(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>? = null,
    ): Flow<T> {
        return when (defaultValue) {
            is Int -> flowSettings.getIntFlow(key, defaultValue as Int) as Flow<T>
            is Long -> flowSettings.getLongFlow(key, defaultValue as Long) as Flow<T>
            is Float -> flowSettings.getFloatFlow(key, defaultValue as Float) as Flow<T>
            is Double -> flowSettings.getDoubleFlow(key, defaultValue as Double) as Flow<T>
            is String -> flowSettings.getStringFlow(key, defaultValue as String) as Flow<T>
            is Boolean -> flowSettings.getBooleanFlow(key, defaultValue as Boolean) as Flow<T>
            else -> {
                require(serializer != null) {
                    "Unsupported type or no serializer provided for ${defaultValue!!::class}"
                }
                flowSettings.getStringFlow(key, Json.encodeToString(serializer, defaultValue))
                    .map { jsonString ->
                        Json.decodeFromString(serializer, jsonString)
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
        return suspendSettings.hasKey(key)
    }

    /**
     * Removes the value associated with the specified [key] from the data store.
     *
     * @param key The key whose value should be removed.
     */
    suspend fun removeValue(key: String) {
        suspendSettings.remove(key)
    }

    /**
     * Clears all stored preferences in the data store.
     */
    suspend fun clearAll() {
        suspendSettings.clear()
    }

    /**
     * Retrieves all keys currently stored in the data store.
     *
     * @return A [Set] containing all keys in the data store.
     */
    suspend fun getAllKeys(): Set<String> {
        return suspendSettings.keys()
    }

    /**
     * Returns the total number of key-value pairs stored in the data store.
     *
     * @return The number of stored preferences.
     */
    suspend fun getSize(): Int {
        return suspendSettings.size()
    }

    /**
     * Serializes and stores a custom type [value] as a JSON string under the specified [key].
     *
     * @param key The key under which to store the serialized value.
     * @param value The value to serialize and store.
     * @param serializer The [KSerializer] used to serialize [value] into a JSON string.
     */
    private suspend fun <T> putSerializableData(key: String, value: T, serializer: KSerializer<T>) {
        val json = Json.encodeToString(
            serializer = serializer,
            value = value,
        )
        suspendSettings.putString(key = key, value = json)
    }

    /**
     * Retrieves and deserializes a value associated with the specified [key], returning [defaultValue] if not found.
     *
     * @param key The key whose value should be retrieved.
     * @param defaultValue The default value to return if the key does not exist or deserialization fails.
     * @param serializer The [KSerializer] used to deserialize the stored JSON string into type [T].
     * @return The deserialized value of type [T], or [defaultValue] if the key is not found.
     */
    private suspend fun <T> getSerializedData(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
    ): T {
        val json = suspendSettings.getStringOrNull(key = key) ?: return defaultValue
        return Json.decodeFromString(
            deserializer = serializer,
            string = json,
        )
    }
}
