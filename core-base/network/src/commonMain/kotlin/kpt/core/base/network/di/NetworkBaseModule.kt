/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.network.di

import kpt.core.base.network.RuntimeHeaderStore
import kpt.core.base.network.SupabaseClientFactory
import kpt.core.base.network.SupabaseConfigClient
import org.koin.dsl.module

/**
 * Framework network wiring — the half of the network graph a fork never configures.
 *
 * ## Why these bindings are here and not in `core/network`
 * A binding belongs in `core-base` when it touches nothing the fork generates. These two qualify:
 * `RuntimeHeaderStore` takes no arguments at all, and the client map is derived entirely through
 * `get()`. Everything still in `core/network`'s `NetworkModule` fails that test — `AccessPointRegistry`,
 * `MultiUrlConfigProvider` and `SupabaseClientFactory` each read an `App*` object that KSP generates
 * into `core/network` from `app-profile`, and `core-base` sits below that and cannot see it.
 *
 * ## Why relocating is safe without a graph test
 * `NetworkModule` includes this module unconditionally, and `KoinModules` includes `NetworkModule`
 * unconditionally. Moving a definition between two always-included modules yields the SAME container
 * with the SAME definitions — there is no ordering or visibility question to get wrong. (Koin's own
 * `verifyAll` cannot check this graph: it matches constructor parameters by type and this codebase
 * binds heavily by QUALIFIER, so it reports real bindings like `AlertsRepository` as missing.)
 *
 * ## What a fork does instead
 * Nothing here. Fork-owned network singles go in `core/network`'s `ProjectNetworkModule`, which sync
 * preserves. This file is template-owned and blind-copied.
 */
val NetworkBaseModule = module {
    // No arguments, no fork input — there was never a reason for this to sit a layer up.
    single { RuntimeHeaderStore() }

    // Derived purely through `get()`: the factory it reads is bound in core/network because IT needs
    // the generated anon keys, but this projection of it does not.
    single<Map<String, SupabaseConfigClient>> { get<SupabaseClientFactory>().clients() }
}
