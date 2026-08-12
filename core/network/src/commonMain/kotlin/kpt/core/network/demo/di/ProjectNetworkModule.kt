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

import kpt.core.network.BuildKonfig
import kpt.core.base.network.restApi
import kpt.core.network.demo.cloudtodo.api.createJsonPlaceholderApi
import kpt.core.network.demo.crypto.api.createCoinGeckoApi
import kpt.core.network.demo.currency.api.createFrankfurterApi
import kpt.core.network.demo.economic.api.createFredApi
import kpt.core.network.demo.economic.api.createWorldBankApi
import kpt.core.network.demo.economic.config.FredApiConfig
import org.koin.dsl.module

/**
 * ProjectNetworkModule — the FORK-OWNED API-client wiring. This is where a fork wires its API
 * interfaces to the app's network **access points**.
 *
 * Every server the app talks to is declared ONCE in `app-profile/app.yaml#network.access_points`
 * (→ `AccessPointRegistry` via `syncForkConfig`). This module wires each API in ONE line via
 * `restApi("<id>") { it.createXApi() }` — `core/network` auto-builds the Ktor client + Ktorfit
 * (base URL, logging, proxy) from the access point, so there is NO per-API Ktorfit boilerplate and NO
 * hardcoded URL here. To add an API: declare its endpoint in app.yaml, run `syncForkConfig`, write the
 * Ktorfit interface + its `createXApi()`, and add one `restApi(...)` line.
 *
 * Ownership: fork-owned `demo/` package (customization-surface.yaml); installed via
 * `FeatureRegistry.featureKoinModules`; the customizer `--clean` strips it. The infra aggregator
 * [kpt.core.network.di.NetworkModule] stays demo-free `owner: template`.
 */
val ProjectNetworkModule = module {
    // FRED's API key is a request-time @Query parameter (not client setup), so its config stays here
    // (InterestRateSeriesStore injects it); the base URL + proxy come from the "fred" access point.
    single<FredApiConfig> {
        FredApiConfig(apiKey = BuildKonfig.FRED_API_KEY.takeIf { it.isNotBlank() })
    }

    // Every demo server — base URL + logging (+ FRED's proxy) resolved from its app-profile access point.
    restApi("frankfurter") { it.createFrankfurterApi() }
    restApi("coingecko") { it.createCoinGeckoApi() }
    restApi("fred") { it.createFredApi() }
    restApi("worldbank") { it.createWorldBankApi() }
    // jsonplaceholder — the WRITABLE demo backend for the Store5 MUTABLE (offline-write) archetype.
    restApi("jsonplaceholder") { it.createJsonPlaceholderApi() }
}
