/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.frankfurter.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.network.frankfurter.dto.ExchangeRatesDto
import kpt.core.network.frankfurter.dto.RateHistoryDto

/** Frankfurter open-source exchange rate API. Base URL: [BASE_URL]. */
@ApiBinding("frankfurter")
interface FrankfurterApi {

    /**
     * Latest rates for one base currency.
     *
     * @param from base currency code, e.g. `USD`.
     */
    @GET("v1/latest")
    suspend fun getLatestRates(@Query("from") from: String): ExchangeRatesDto

    /**
     * Rates for one pair over a date range.
     *
     * @param startDate first day, `YYYY-MM-DD`.
     * @param endDate last day, `YYYY-MM-DD`.
     * @param from base currency code.
     * @param to quote currency code.
     */
    @GET("v1/{startDate}..{endDate}")
    suspend fun getHistoricalRates(
        @Path("startDate") startDate: String,
        @Path("endDate") endDate: String,
        @Query("from") from: String,
        @Query("to") to: String,
    ): RateHistoryDto
}
