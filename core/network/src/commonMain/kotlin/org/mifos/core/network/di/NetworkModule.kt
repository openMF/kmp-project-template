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
import org.mifos.core.network.client.FintechApiClient
import org.mifos.core.network.crypto.api.CoinGeckoApi
import org.mifos.core.network.currency.api.FrankfurterApi
import org.mifos.core.network.economic.api.FredApi
import org.mifos.core.network.economic.api.WorldBankApi
import org.mifos.core.network.economic.config.FredApiConfig
import template.core.base.network.httpClient
import template.core.base.network.setupDefaultHttpClient

val NetworkModule = module {

    // Default FRED config — not configured. Forks override this single() in
    // their own DI module to thread in the actual key (BuildKonfig / env / etc.).
    single<FredApiConfig> { FredApiConfig.Unconfigured }

    single {
        FintechApiClient(
            frankfurterKtorfit = Ktorfit.Builder()
                .httpClient(
                    client = httpClient(
                        setupDefaultHttpClient(
                            baseUrl = FrankfurterApi.BASE_URL,
                            loggableHosts = listOf("api.frankfurter.dev"),
                        ),
                    ),
                )
                .build(),
            coinGeckoKtorfit = Ktorfit.Builder()
                .httpClient(
                    client = httpClient(
                        setupDefaultHttpClient(
                            baseUrl = CoinGeckoApi.BASE_URL,
                            loggableHosts = listOf("api.coingecko.com"),
                        ),
                    ),
                )
                .build(),
            fredKtorfit = Ktorfit.Builder()
                .httpClient(
                    client = httpClient(
                        setupDefaultHttpClient(
                            baseUrl = FredApi.BASE_URL,
                            loggableHosts = listOf("api.stlouisfed.org"),
                        ),
                    ),
                )
                .build(),
            worldBankKtorfit = Ktorfit.Builder()
                .httpClient(
                    client = httpClient(
                        setupDefaultHttpClient(
                            baseUrl = WorldBankApi.BASE_URL,
                            loggableHosts = listOf("api.worldbank.org"),
                        ),
                    ),
                )
                .build(),
        )
    }

    single<FrankfurterApi> { get<FintechApiClient>().frankfurterApi }
    single<CoinGeckoApi> { get<FintechApiClient>().coinGeckoApi }
    single<FredApi> { get<FintechApiClient>().fredApi }
    single<WorldBankApi> { get<FintechApiClient>().worldBankApi }
}
