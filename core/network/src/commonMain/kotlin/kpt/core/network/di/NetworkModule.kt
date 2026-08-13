/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.di

import kpt.core.base.network.AccessPointRegistry
import kpt.core.base.network.MultiUrlConfigProvider
import kpt.core.base.network.SupabaseConfigClient
import kpt.core.base.network.SupabaseCredentials
import kpt.core.network.config.AppAccessPoints
import kpt.core.network.config.AppMultiUrlConfigProvider
import org.koin.dsl.module
import kpt.core.network.config.SupabaseCredentials as GeneratedSupabaseCredentials

// NOTE: Backend base URLs are NOT hardcoded here or in config classes anymore — every server is a
// declared access point in app-profile/app.yaml#network.access_points (→ AccessPointRegistry via
// syncForkConfig). The demo APIs are wired in ProjectNetworkModule via `restApi("<id>") { … }`, which
// auto-builds each transport from its access point. Fork-customisation = edit app.yaml + syncForkConfig.
// FRED's API key remains a per-request @Query param (a vault secret: mifos-x-fred-api-key → BuildKonfig).
// Dynamic server config (consumer-facing, from core-base/network):
//   - SupabaseConfigClient is registered below so forks can fetch runtime server config from a
//     Supabase `app_config`-style table. Its credentials are generated from the gitignored
//     `secrets/live/supabase/supabaseCredentialsFile.json` (SupabaseConfigConventionPlugin); absent that file the
//     creds are empty, so the client stays inert (isConfigured == false). Forks drop in the file, or
//     override the `single<SupabaseCredentials>` binding.
//   - DynamicBaseUrlPlugin is an opt-in of setupDefaultHttpClient(...): a fork implements
//     DynamicUrlConfigProvider (reads its selected server, keyed by AppUrlTypes) and passes it as
//     `dynamicUrlProvider = get()` on any client that should switch base URL at runtime. The
//     toolkit's own fixed-URL APIs (FRED / World Bank / CoinGecko / Frankfurter) don't use it.
// INFRA-ONLY, owner: template (E1 / C2). The demo API configs + FintechApiClient + demo API bindings
// relocated to the fork-owned [kpt.core.network.demo.di.ProjectNetworkModule]; this aggregator carries
// ZERO `kpt.core.*.demo.*` imports so a template sync can blind-copy it without re-introducing demo
// wiring a fork already stripped.
val NetworkModule = module {

    // The fork's generated access points, wrapped by the framework registry mechanism (core-base/network).
    // The restApi("<id>") DSL and AppMultiUrlConfigProvider both resolve transports/base-URLs from this.
    single { AccessPointRegistry(AppAccessPoints.points) }

    // Unified access-point provider — resolves every named UrlType to its REST base URL from the
    // AccessPointRegistry. Clients thread it via
    // setupDefaultHttpClient(multiUrlProvider = get(), urlType = AppUrlTypes.<NAME>).
    single<MultiUrlConfigProvider> { AppMultiUrlConfigProvider(get()) }

    // Supabase-backed dynamic config — overridable by forks. The credentials object is generated
    // from secrets/live/supabase/supabaseCredentialsFile.json by SupabaseConfigConventionPlugin (empty when the
    // file is absent, so the client stays inert until a fork provides a project).
    single<SupabaseCredentials> { GeneratedSupabaseCredentials }
    single { SupabaseConfigClient(credentials = get()) }
}
