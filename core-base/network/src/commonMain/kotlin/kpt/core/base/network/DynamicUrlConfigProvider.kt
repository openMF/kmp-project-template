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

/**
 * Interface for providing dynamic URL configuration at runtime.
 *
 * Implementations of this interface allow the HTTP client to dynamically
 * switch between different server endpoints without recreating the client.
 *
 * This is useful for:
 * - Multi-tenant applications where users can switch between servers
 * - Development/staging/production environment switching
 * - White-label apps with different backend endpoints
 *
 * Example implementation:
 * ```kotlin
 * class MyConfigProvider(
 *     private val preferencesRepository: UserPreferencesRepository
 * ) : DynamicUrlConfigProvider {
 *     override fun getBaseUrl(): String = preferencesRepository.selectedServer.value?.url
 *         ?: "https://default.api.com"
 *
 *     override fun getLoggableHosts(): List<String> = listOf(
 *         preferencesRepository.selectedServer.value?.host ?: "default.api.com"
 *     )
 * }
 * ```
 */
interface DynamicUrlConfigProvider {
    /**
     * Returns the current base URL to use for API requests.
     * This is called for each request, allowing runtime URL switching.
     */
    fun getBaseUrl(): String

    /**
     * Returns the list of hostnames that should have HTTP logging enabled.
     * This is called dynamically, allowing the logging filter to adapt
     * to the currently selected server.
     */
    fun getLoggableHosts(): List<String>
}

/**
 * Extension of [DynamicUrlConfigProvider] for applications with multiple
 * URL types (e.g., main API, self-service API, interbank API).
 */
interface MultiUrlConfigProvider : DynamicUrlConfigProvider {
    /**
     * URL type identifier for different API endpoints.
     */
    enum class UrlType {
        /** Main API endpoint */
        MAIN,
        /** Self-service API endpoint */
        SELF_SERVICE,
        /** Interbank/third-party API endpoint */
        INTERBANK,
    }

    /**
     * Returns the base URL for the specified URL type.
     */
    fun getBaseUrl(type: UrlType): String

    /**
     * Default implementation returns MAIN URL type.
     */
    override fun getBaseUrl(): String = getBaseUrl(UrlType.MAIN)
}
