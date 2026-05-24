/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.network.economic.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import org.mifos.core.network.economic.dto.WorldBankResponseDto

/**
 * World Bank Open Data API v2. Base URL: [BASE_URL]. **No authentication required.**
 *
 * Reference docs: https://datatopics.worldbank.org/world-development-indicators/
 *
 * Wire-format quirk: every response is a two-element JSON array
 * `[metadata, observations[]]` rather than a normal object. The
 * [WorldBankResponseDto] class wraps a custom serializer that lifts both halves
 * into named fields — endpoint consumers see a regular data class.
 */
interface WorldBankApi {

    companion object {
        const val BASE_URL = "https://api.worldbank.org/"
    }

    /**
     * Fetch annual observations of a single indicator for a single country.
     *
     * @param countryCode ISO 3166-1 alpha-2 (e.g. `"US"`) or alpha-3 (e.g.
     *   `"USA"`). Both work — World Bank accepts either; the toolkit standardises
     *   on alpha-2.
     * @param indicator World Bank indicator code, e.g. `"NY.GDP.MKTP.CD"` for
     *   GDP. Sourced from [org.mifos.core.model.economic.IndicatorKind.worldBankCode].
     * @param format Response format. Always `"json"` — World Bank defaults to
     *   XML which kotlinx.serialization cannot parse.
     * @param perPage Pagination size. Default 50 is enough for ~50 years of
     *   annual data; raise for very long history pulls.
     * @param dateRange `YYYY:YYYY` inclusive year span, e.g. `"2000:2024"`.
     */
    @GET("v2/country/{countryCode}/indicator/{indicator}")
    suspend fun indicator(
        @Path("countryCode") countryCode: String,
        @Path("indicator") indicator: String,
        @Query("format") format: String = "json",
        @Query("per_page") perPage: Int = 50,
        @Query("date") dateRange: String,
    ): WorldBankResponseDto
}
