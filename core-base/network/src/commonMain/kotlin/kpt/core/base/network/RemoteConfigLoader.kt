/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kpt.core.base.common.DataState

/**
 * Generic interface for loading remote configuration.
 *
 * This interface abstracts the remote config loading mechanism, allowing
 * implementations for different backends like:
 * - Supabase
 * - Firebase Remote Config
 * - Custom REST API
 * - Local file system (for fallback)
 *
 * @param T The type of configuration data to load.
 */
interface RemoteConfigLoader<T> {
    /**
     * Fetches the configuration from the remote source.
     *
     * @return [DataState] containing the configuration or an error.
     */
    suspend fun fetchConfig(): DataState<T>

    /**
     * Observes configuration changes as a Flow.
     *
     * @return [Flow] of [DataState] containing the configuration.
     */
    fun observeConfig(): Flow<DataState<T>>
}

/**
 * Abstract base class for remote config loaders with common functionality.
 *
 * Provides:
 * - Automatic fallback to local config on failure
 * - Flow-based observation with proper dispatcher
 * - Error handling
 *
 * @param T The type of configuration data to load.
 * @param ioDispatcher The dispatcher to use for IO operations.
 */
abstract class BaseRemoteConfigLoader<T>(
    private val ioDispatcher: CoroutineDispatcher,
) : RemoteConfigLoader<T> {

    /**
     * Fetches configuration from the primary remote source.
     * Override this method to implement the actual fetch logic.
     *
     * @return [DataState] containing the configuration.
     */
    protected abstract suspend fun fetchFromRemote(): DataState<T>

    /**
     * Loads fallback configuration when remote fetch fails.
     * Override this method to provide fallback logic (e.g., from local files).
     *
     * @return [DataState] containing the fallback configuration.
     */
    protected abstract suspend fun loadFallbackConfig(): DataState<T>

    /**
     * Checks if the remote source is properly configured.
     * If not configured, fallback config will be used directly.
     *
     * @return true if the remote source is configured, false otherwise.
     */
    protected open fun isRemoteConfigured(): Boolean = true

    override suspend fun fetchConfig(): DataState<T> {
        if (!isRemoteConfigured()) {
            return loadFallbackConfig()
        }
        return try {
            fetchFromRemote()
        } catch (e: Exception) {
            loadFallbackConfig()
        }
    }

    override fun observeConfig(): Flow<DataState<T>> = flow {
        emit(fetchConfig())
    }.flowOn(ioDispatcher)
}

/**
 * Configuration holder for remote config credentials.
 *
 * This is a generic data class that can be used to store
 * credentials for various remote config services.
 */
data class RemoteConfigCredentials(
    val url: String,
    val apiKey: String,
    val tableName: String = "app_config",
) {
    /**
     * Checks if the credentials are properly configured.
     */
    val isConfigured: Boolean
        get() = url.isNotBlank() &&
            apiKey.isNotBlank() &&
            !url.contains("YOUR_") &&
            !apiKey.contains("YOUR_")
}
