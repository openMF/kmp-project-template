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
import org.mifos.core.network.currency.config.FrankfurterApiConfig
import org.mifos.core.network.economic.api.FredApi
import org.mifos.core.network.economic.api.WorldBankApi
import org.mifos.core.network.economic.config.FredApiConfig
import org.mifos.core.network.economic.config.WorldBankApiConfig
import template.core.base.network.httpClient
import template.core.base.network.setupDefaultHttpClient

// NOTE: Backend URLs are sourced from Koin-injected config classes (FredApiConfig,
// FrankfurterApiConfig, WorldBankApiConfig), each carrying a `baseUrl: String` field that
// defaults to the public production endpoint. Forks override these `single { ... }` bindings
// at their app-module level to swap in mocks / mirrors / per-environment endpoints.
//
// TODO: If/when BuildKonfig is added to the project for FRED_API_KEY (the canonical Plan 10
// strategy), thread BuildKonfig.FRED_BASE_URL / FRANKFURTER_BASE_URL / WORLDBANK_BASE_URL
// through here. Today (no BuildKonfig in-tree), the default-param approach on each config class
// provides the same fork-customisation surface without the buildscript complexity.
val NetworkModule = module {

    // Default FRED config — not configured. Forks override this single() in
    // their own DI module to thread in the actual key (BuildKonfig / env / etc.).
    single<FredApiConfig> { FredApiConfig.Unconfigured }
    single<FrankfurterApiConfig> { FrankfurterApiConfig.Default }
    single<WorldBankApiConfig> { WorldBankApiConfig.Default }

    single {
        FintechApiClient(
            frankfurterKtorfit = Ktorfit.Builder()
                .httpClient(
                    client = httpClient(
                        setupDefaultHttpClient(
                            baseUrl = get<FrankfurterApiConfig>().baseUrl,
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
                            baseUrl = get<FredApiConfig>().baseUrl,
                            loggableHosts = listOf("api.stlouisfed.org"),
                        ),
                    ),
                )
                .build(),
            worldBankKtorfit = Ktorfit.Builder()
                .httpClient(
                    client = httpClient(
                        setupDefaultHttpClient(
                            baseUrl = get<WorldBankApiConfig>().baseUrl,
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
