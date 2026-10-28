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

import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kpt.core.base.network.SupabaseConfigClient
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appconfig.api.AppConfigApi
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appconfig.dto.RemoteAppConfigDto

/**
 * Supabase implementation of [AppConfigApi] — a typed facade over `client.postgrest`.
 *
 * **Inert without a key.** With no anon key [SupabaseConfigClient.isConfigured] is false and the call
 * returns null rather than throwing; construction never touches the network (`client` is `by lazy`).
 */
@ApiBinding("lwmswhoxvvoagzkqxiyd")
class AppConfigApiImpl(
    private val supabase: SupabaseConfigClient,
) : AppConfigApi {

    override val isConfigured: Boolean get() = supabase.isConfigured

    override suspend fun fetchInstancesConfig(): String? {
        if (!isConfigured) return null
        val row = supabase.data.from(TABLE).select(Columns.list("id", "instances")).decodeList<RemoteAppConfigDto>()
            .firstOrNull()
        return row?.let { JsonObject(mapOf("instances" to it.instances)).toString() }
    }

    private companion object {
        const val TABLE = "app_config"
    }
}
