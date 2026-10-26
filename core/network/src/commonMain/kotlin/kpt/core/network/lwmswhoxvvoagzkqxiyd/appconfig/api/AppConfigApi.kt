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

/**
 * The Mifos `app_config` table's API — the CONTRACT, with no Supabase types in sight, so a consumer is
 * tested with a plain fake while [impl.AppConfigApiImpl] is the only thing that needs a live client.
 */
interface AppConfigApi {
    /** True when a real project URL + anon key are configured. */
    val isConfigured: Boolean

    /**
     * The shared instance list as `{"instances": [...]}` JSON — the first `app_config` row — or null
     * when unconfigured or the table has no row. Every app's instances are in it; the caller filters.
     */
    suspend fun fetchInstancesConfig(): String?
}
