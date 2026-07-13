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

    companion object {
        // Was api.frankfurter.app — now returns HTTP 301 with a broken redirect target
        // (https://api.frankfurter.dev/v1/v1/latest?from=USD — note the duplicated /v1/v1/),
        // so following the redirect doesn't help. Switched to the new canonical host directly.
        const val BASE_URL = "https://api.frankfurter.dev/"
    }

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
