/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.demo.currency.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import kpt.core.network.demo.currency.dto.ExchangeRatesDto
import kpt.core.network.demo.currency.dto.RateHistoryDto

/** Frankfurter open-source exchange rate API. Base URL: [BASE_URL]. */
interface FrankfurterApi {

    @GET("v1/latest")
    suspend fun getLatestRates(@Query("from") from: String): ExchangeRatesDto

    @GET("v1/{startDate}..{endDate}")
    suspend fun getHistoricalRates(
        @Path("startDate") startDate: String,
        @Path("endDate") endDate: String,
        @Query("from") from: String,
        @Query("to") to: String,
    ): RateHistoryDto
}
