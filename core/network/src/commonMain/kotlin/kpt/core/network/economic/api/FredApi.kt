/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.economic.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import kpt.core.network.economic.dto.FredObservationsDto

/**
 * FRED (Federal Reserve Economic Data) public API v0. Base URL: [BASE_URL].
 *
 * Requires a free developer API key — sign up at
 * https://fred.stlouisfed.org/docs/api/api_key.html. Forks supply the key via
 * `FredApiConfig` (`.env.local` ➜ Koin singleton). Rate limit: 120 calls/min on
 * the free tier; the toolkit's Store5 24h TTL keeps daily usage well under
 * that ceiling.
 *
 * Reference docs: https://fred.stlouisfed.org/docs/api/fred/series_observations.html
 */
interface FredApi {

    companion object {
        const val BASE_URL = "https://api.stlouisfed.org/"
    }

    /**
     * Fetch observations for a single FRED series in the closed date interval
     * `[observationStart, observationEnd]`.
     *
     * @param seriesId FRED series identifier (e.g. `"DFF"`, `"DPRIME"`,
     *   `"MORTGAGE30US"`). Case-sensitive.
     * @param apiKey FRED developer API key. Required — FRED returns HTTP 400
     *   without a valid key.
     * @param fileType Response format. Always `"json"` for this client (FRED
     *   defaults to XML which kotlinx.serialization cannot parse).
     * @param observationStart ISO-8601 date (YYYY-MM-DD) for the inclusive start
     *   of the window.
     * @param observationEnd ISO-8601 date (YYYY-MM-DD) for the inclusive end
     *   of the window.
     */
    @GET("fred/series/observations")
    suspend fun seriesObservations(
        @Query("series_id") seriesId: String,
        @Query("api_key") apiKey: String,
        @Query("file_type") fileType: String = "json",
        @Query("observation_start") observationStart: String,
        @Query("observation_end") observationEnd: String,
    ): FredObservationsDto
}
