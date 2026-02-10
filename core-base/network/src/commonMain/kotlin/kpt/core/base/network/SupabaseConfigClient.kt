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

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import kpt.core.base.common.DataState

/**
 * Generic Supabase configuration client for fetching remote configuration.
 *
 * This class provides a reusable Supabase client setup with:
 * - Lazy initialization of Supabase client
 * - Configurable logging level
 * - Generic fetch methods for any configuration type
 * - Built-in error handling with DataState
 *
 * ## Usage
 *
 * ```kotlin
 * // Create credentials (typically from build config or secrets)
 * object MySupabaseCredentials : SupabaseCredentials {
 *     override val url = BuildConfig.SUPABASE_URL
 *     override val anonKey = BuildConfig.SUPABASE_ANON_KEY
 * }
 *
 * // Create client
 * val configClient = SupabaseConfigClient(
 *     credentials = MySupabaseCredentials,
 *     logLevel = LogLevel.DEBUG,
 * )
 *
 * // Fetch configuration
 * val result = configClient.fetchSingle<MyConfig>("app_config")
 * when (result) {
 *     is DataState.Success -> println("Config: ${result.data}")
 *     is DataState.Error -> println("Error: ${result.error}")
 *     else -> {}
 * }
 * ```
 *
 * @param credentials The Supabase credentials (URL and anon key).
 * @param logLevel The logging level for Supabase client. Defaults to INFO.
 */
class SupabaseConfigClient(
    private val credentials: SupabaseCredentials,
    private val logLevel: LogLevel = LogLevel.INFO,
) {
    /**
     * Lazily initialized Supabase client.
     * Only created when first accessed.
     */
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = credentials.url,
            supabaseKey = credentials.anonKey,
        ) {
            defaultLogLevel = logLevel
            install(Postgrest)
        }
    }

    /**
     * Checks if Supabase is properly configured.
     *
     * @return true if credentials are valid, false otherwise.
     */
    val isConfigured: Boolean
        get() = credentials.isConfigured

    /**
     * Gets the Postgrest query builder for direct table access.
     *
     * @param tableName The name of the table to query.
     * @return PostgrestQueryBuilder for the specified table.
     */
    fun from(tableName: String): PostgrestQueryBuilder =
        client.postgrest.from(tableName)

    /**
     * Fetches a single record from a table.
     *
     * @param T The type to decode the result into.
     * @param tableName The name of the table to query.
     * @return DataState containing the decoded result or error.
     */
    suspend inline fun <reified T : Any> fetchSingle(
        tableName: String,
    ): DataState<T> {
        if (!isConfigured) {
            return DataState.Error(
                error = IllegalStateException("Supabase is not configured"),
            )
        }
        return try {
            val result = from(tableName)
                .select()
                .decodeSingle<T>()
            DataState.Success(result)
        } catch (e: Exception) {
            DataState.Error(error = e)
        }
    }

    /**
     * Fetches a list of records from a table.
     *
     * @param T The type to decode each record into.
     * @param tableName The name of the table to query.
     * @return DataState containing the list of decoded results or error.
     */
    suspend inline fun <reified T : Any> fetchList(
        tableName: String,
    ): DataState<List<T>> {
        if (!isConfigured) {
            return DataState.Error(
                error = IllegalStateException("Supabase is not configured"),
            )
        }
        return try {
            val result = from(tableName)
                .select()
                .decodeList<T>()
            DataState.Success(result)
        } catch (e: Exception) {
            DataState.Error(error = e)
        }
    }

    /**
     * Fetches records with a custom query.
     *
     * @param T The type to decode each record into.
     * @param tableName The name of the table to query.
     * @param query Lambda to customize the query (filters, ordering, etc.).
     * @return DataState containing the list of decoded results or error.
     */
    suspend inline fun <reified T : Any> fetchWithQuery(
        tableName: String,
        crossinline query: PostgrestQueryBuilder.() -> Unit,
    ): DataState<List<T>> {
        if (!isConfigured) {
            return DataState.Error(
                error = IllegalStateException("Supabase is not configured"),
            )
        }
        return try {
            val builder = from(tableName).apply(query)
            val result = builder.select().decodeList<T>()
            DataState.Success(result)
        } catch (e: Exception) {
            DataState.Error(error = e)
        }
    }
}

/**
 * Interface for Supabase credentials.
 *
 * Implement this interface to provide Supabase URL and anon key
 * from your project's build configuration or secrets.
 *
 * ## Example Implementation
 *
 * ```kotlin
 * object MySupabaseCredentials : SupabaseCredentials {
 *     override val url: String = BuildConfig.SUPABASE_URL
 *     override val anonKey: String = BuildConfig.SUPABASE_ANON_KEY
 * }
 * ```
 *
 * ## Generated Implementation
 *
 * If using the SupabaseConfigConventionPlugin, credentials are
 * automatically generated:
 *
 * ```kotlin
 * // Auto-generated SupabaseCredentials object
 * object SupabaseCredentials : kpt.core.base.network.SupabaseCredentials {
 *     override val url = "https://your-project.supabase.co"
 *     override val anonKey = "your-anon-key"
 * }
 * ```
 */
interface SupabaseCredentials {
    /**
     * The Supabase project URL.
     * Example: "https://your-project.supabase.co"
     */
    val url: String

    /**
     * The Supabase anon (public) key.
     * This key is safe to use in client-side code.
     */
    val anonKey: String

    /**
     * Checks if the credentials are properly configured.
     * Returns false if URL or key contain placeholder values.
     */
    val isConfigured: Boolean
        get() = url.isNotBlank() &&
            anonKey.isNotBlank() &&
            !url.contains("YOUR_") &&
            !anonKey.contains("YOUR_") &&
            url.startsWith("https://")
}
