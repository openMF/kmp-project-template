/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.network.fintech

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import org.mifos.core.network.fintech.model.ExchangeRatesDto
import org.mifos.core.network.fintech.model.RateHistoryDto

interface FrankfurterApi {

    @GET("v1/latest")
    suspend fun getLatestRates(
        @Query("from") from: String,
    ): ExchangeRatesDto

    @GET("v1/{startDate}..{endDate}")
    suspend fun getHistoricalRates(
        @Path("startDate") startDate: String,
        @Path("endDate") endDate: String,
        @Query("from") from: String,
        @Query("to") to: String,
    ): RateHistoryDto
}
