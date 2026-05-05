/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.network.di

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.dsl.module
import org.mifos.core.network.fintech.CoinGeckoApi
import org.mifos.core.network.fintech.FintechApiClient
import org.mifos.core.network.fintech.FrankfurterApi
import template.core.base.network.httpClient
import template.core.base.network.setupDefaultHttpClient

val NetworkModule = module {

    single {
        FintechApiClient(
            frankfurterKtorfit = Ktorfit.Builder()
                .httpClient(
                    client = httpClient(
                        setupDefaultHttpClient(
                            baseUrl = "https://api.frankfurter.dev/",
                            loggableHosts = listOf("api.frankfurter.dev"),
                        ),
                    ),
                )
                .build(),
            coinGeckoKtorfit = Ktorfit.Builder()
                .httpClient(
                    client = httpClient(
                        setupDefaultHttpClient(
                            baseUrl = "https://api.coingecko.com/",
                            loggableHosts = listOf("api.coingecko.com"),
                        ),
                    ),
                )
                .build(),
        )
    }

    single<FrankfurterApi> { get<FintechApiClient>().frankfurterApi }
    single<CoinGeckoApi> { get<FintechApiClient>().coinGeckoApi }
}
