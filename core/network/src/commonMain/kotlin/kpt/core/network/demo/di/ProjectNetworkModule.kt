/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.demo.di

import de.jensklingenberg.ktorfit.Ktorfit
import kpt.core.base.network.httpClient
import kpt.core.base.network.setupDefaultHttpClient
import kpt.core.network.BuildKonfig
import kpt.core.network.demo.FintechApiClient
import kpt.core.network.demo.cloudtodo.api.JsonPlaceholderApi
import kpt.core.network.demo.cloudtodo.api.createJsonPlaceholderApi
import kpt.core.network.demo.crypto.api.CoinGeckoApi
import kpt.core.network.demo.currency.api.FrankfurterApi
import kpt.core.network.demo.currency.config.FrankfurterApiConfig
import kpt.core.network.demo.economic.api.FredApi
import kpt.core.network.demo.economic.api.WorldBankApi
import kpt.core.network.demo.economic.config.FredApiConfig
import kpt.core.network.demo.economic.config.WorldBankApiConfig
import org.koin.dsl.module

/**
 * ProjectNetworkModule — the FORK-OWNED demo API-client wiring for the toolkit showcase
 * (FRED / World Bank / CoinGecko / Frankfurter / JsonPlaceholder).
 *
 * Relocated out of the infra aggregator [kpt.core.network.di.NetworkModule] (E1 / C2, epic
 * pure-white-label-store5-network) so that aggregator becomes an infra-only full-copy `owner:
 * template` file that carries ZERO `kpt.core.*.demo.*` imports — eliminating the sync-fragility
 * defect class.
 *
 * NOTE: Backend URLs are sourced from Koin-injected config classes (FredApiConfig,
 * FrankfurterApiConfig, WorldBankApiConfig), each carrying a `baseUrl: String` field that
 * defaults to the public production endpoint. Forks override these `single { ... }` bindings
 * at their app-module level to swap in mocks / mirrors / per-environment endpoints.
 *
 * Ownership: the `demo/` package is fork-owned in customization-surface.yaml. Installed into the app Koin graph
 * via the fork-owned `cmp-navigation/registry/FeatureRegistry.featureKoinModules` demo block; the
 * customizer `--clean` deletes this whole `demo/` package + empties that registry block together.
 */
val ProjectNetworkModule = module {
    single<FredApiConfig> {
        FredApiConfig(apiKey = BuildKonfig.FRED_API_KEY.takeIf { it.isNotBlank() })
    }
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
                            proxiedHosts = listOf("api.stlouisfed.org"),
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

    // cloud-todo — jsonplaceholder is the only WRITABLE demo backend (POST/PUT accepted), used to
    // showcase the Store5 MUTABLE (offline-write) archetype (`provideCloudTodoStore`).
    single<JsonPlaceholderApi> {
        Ktorfit.Builder()
            .httpClient(
                client = httpClient(
                    setupDefaultHttpClient(
                        baseUrl = "https://jsonplaceholder.typicode.com/",
                        loggableHosts = listOf("jsonplaceholder.typicode.com"),
                    ),
                ),
            )
            .build()
            .createJsonPlaceholderApi()
    }
}
