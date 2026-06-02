/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.currency.config

/**
 * Runtime configuration for [kpt.core.network.currency.api.FrankfurterApi].
 *
 * Frankfurter is an unauthenticated open-source exchange-rates API (no API key). The only
 * configurable surface is the base URL. Forks override via Koin DI:
 *
 * ```kotlin
 * // For testing — point at a local MockWebServer
 * single { FrankfurterApiConfig(baseUrl = "http://localhost:8080/") }
 *
 * // For self-hosting — Frankfurter is open-source and self-hostable
 * single { FrankfurterApiConfig(baseUrl = "https://fx.mybank.internal/") }
 * ```
 *
 * Default value matches the production canonical host
 * `https://api.frankfurter.dev/` (the older `api.frankfurter.app` returns a broken
 * redirect and should not be used).
 *
 * @param baseUrl HTTP base URL. Must end with `/`.
 */
data class FrankfurterApiConfig(
    val baseUrl: String = DEFAULT_BASE_URL,
) {
    companion object {
        /**
         * Default Frankfurter endpoint. The older `api.frankfurter.app` returns HTTP 301
         * with a broken redirect target (duplicated path segment) — use the new canonical
         * host directly.
         */
        const val DEFAULT_BASE_URL: String = "https://api.frankfurter.dev/"

        /** Default config for forks that don't override. */
        val Default: FrankfurterApiConfig = FrankfurterApiConfig()
    }
}
