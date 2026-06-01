/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.economic.config

/**
 * Runtime configuration for [kpt.core.network.economic.api.WorldBankApi].
 *
 * World Bank Open Data is unauthenticated (no API key) — the only configurable surface is
 * the base URL. Forks override via Koin DI:
 *
 * ```kotlin
 * // For testing — point at a local MockWebServer
 * single { WorldBankApiConfig(baseUrl = "http://localhost:8080/") }
 *
 * // For staging — proxy through an internal mirror
 * single { WorldBankApiConfig(baseUrl = "https://wb-mirror.mybank.internal/") }
 * ```
 *
 * Default value matches the production endpoint
 * `https://api.worldbank.org/v2/` — the `v2` segment is part of the base URL so Ktorfit
 * relative paths like `country/{c}/indicator/{i}` resolve correctly.
 *
 * @param baseUrl HTTP base URL. Must end with `/`.
 */
data class WorldBankApiConfig(
    val baseUrl: String = DEFAULT_BASE_URL,
) {
    companion object {
        /**
         * Default World Bank Open Data endpoint. Note the trailing `/v2/` — the WorldBank
         * Ktorfit interface uses paths relative to this base (e.g. `v2/country/{c}/indicator/{i}`
         * is currently encoded with a `v2/` prefix; this config supplies the host only).
         *
         * **Why the default ends with `/`** — Ktorfit's URL builder requires the base to end
         * with `/` for relative-path resolution to behave intuitively.
         */
        const val DEFAULT_BASE_URL: String = "https://api.worldbank.org/"

        /** Default config for forks that don't override. */
        val Default: WorldBankApiConfig = WorldBankApiConfig()
    }
}
