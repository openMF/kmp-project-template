/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.lwmswhoxvvoagzkqxiyd.appconfig.api

import kpt.core.network.lwmswhoxvvoagzkqxiyd.appconfig.dto.RemoteAppConfigDto

/**
 * The `app_config` table's API — the CONTRACT, with no Supabase types in sight.
 *
 * Split from its implementation for the same reason the REST points are interfaces: a consumer that
 * depends on this can be tested with a plain fake, while [AppConfigApiImpl] is the only thing that
 * needs a live `SupabaseConfigClient`. On the REST side Ktorfit generates the implementation from
 * the interface; supabase-kt has no interface-generation step, so the implementation is hand-written
 * — but the SEAM is identical, and callers should never see the difference.
 *
 * One interface per TABLE. The package path `project/appconfig/` is `{supabase-project}/{table}`,
 * so a project with five tables has five of these rather than one class that grows without bound.
 */
interface AppConfigApi {
    /** True when a real project URL + anon key are configured; false on the neutral template. */
    val isConfigured: Boolean

    /** Every `app_config` row, or an empty list when Supabase is not configured. */
    suspend fun fetchConfig(): List<RemoteAppConfigDto>

    /** The value for [key], or `null` when absent or unconfigured. */
    suspend fun fetchValue(key: String): String?
}
