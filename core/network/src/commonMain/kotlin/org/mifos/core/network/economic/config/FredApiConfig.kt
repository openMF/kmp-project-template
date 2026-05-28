/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.network.economic.config

/**
 * Runtime configuration for [org.mifos.core.network.economic.api.FredApi].
 *
 * The fork is responsible for sourcing the key — typical patterns:
 *
 * ```kotlin
 * // BuildKonfig (preferred)
 * single { FredApiConfig(apiKey = BuildKonfig.FRED_API_KEY) }
 *
 * // System env / Gradle property
 * single { FredApiConfig(apiKey = System.getenv("FRED_API_KEY")) }
 *
 * // Hardcoded for local dev (NEVER commit)
 * single { FredApiConfig(apiKey = "deadbeef...") }
 * ```
 *
 * `apiKey` is intentionally nullable so the toolkit can ship demo data when no
 * key is configured — the repository layer surfaces an explicit "FRED key not
 * configured" error rather than firing the network request with `apiKey=null`.
 */
data class FredApiConfig(
    val apiKey: String?,
    /**
     * Base URL for FRED API requests. Defaults to the public production endpoint
     * `https://api.stlouisfed.org/`. Forks can override for:
     *
     * - **Testing**: point at a MockWebServer / WireMock instance running locally
     * - **Self-hosted mirrors**: large institutional users may proxy FRED through
     *   an internal cache
     * - **Per-environment endpoints**: dev / staging / prod variants
     *
     * Override in your Koin DI:
     * ```kotlin
     * single { FredApiConfig(apiKey = "...", baseUrl = "https://staging-fred.mybank.internal/") }
     * ```
     */
    val baseUrl: String = DEFAULT_BASE_URL,
) {
    /** True when the fork has supplied a non-blank key. */
    val isConfigured: Boolean
        get() = !apiKey.isNullOrBlank()

    companion object {
        /** Default FRED public endpoint. */
        const val DEFAULT_BASE_URL: String = "https://api.stlouisfed.org/"

        /** Sentinel for forks that haven't wired the FRED key yet. */
        val Unconfigured: FredApiConfig = FredApiConfig(apiKey = null)
    }
}
