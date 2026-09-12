/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.lwmswhoxvvoagzkqxiyd.appconfig.api.impl

import kpt.core.base.network.SupabaseConfigClient
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appconfig.api.AppConfigApi
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appconfig.dto.RemoteAppConfigDto

/**
 * Supabase implementation of [AppConfigApi] — a typed facade over `client.postgrest`.
 *
 * `@ApiBinding` goes on the IMPLEMENTATION because the implementation is what gets constructed, but
 * the generated Koin binding is emitted as `supabaseApi<AppConfigApi>("project") { AppConfigApiImpl(it) }`
 * — the processor resolves the single supertype and binds THAT. So injecting `AppConfigApi` works and
 * nothing outside this file names the impl. The single-arg constructor taking [SupabaseConfigClient]
 * is the whole contract `supabaseApi<T>` requires.
 *
 * **Inert by default.** The template declares the `project` endpoint but ships no Supabase project
 * and no anon key, so [SupabaseConfigClient.isConfigured] is false and every call returns empty
 * rather than throwing — an unconfigured fork stays on the "no remote config, use defaults" path
 * instead of crashing at start-up. Construction never touches the network (`client` is `by lazy`),
 * so Koin can build the binding on an unconfigured fork safely.
 */
@ApiBinding("lwmswhoxvvoagzkqxiyd")
class AppConfigApiImpl(
    private val supabase: SupabaseConfigClient,
) : AppConfigApi {

    override val isConfigured: Boolean get() = supabase.isConfigured

    override suspend fun fetchConfig(): List<RemoteAppConfigDto> {
        if (!isConfigured) return emptyList()
        return supabase.data.from(TABLE).select().decodeList<RemoteAppConfigDto>()
    }

    override suspend fun fetchValue(key: String): String? =
        fetchConfig().firstOrNull { it.key == key }?.value

    private companion object {
        const val TABLE = "app_config"
    }
}
