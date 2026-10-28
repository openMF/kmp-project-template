/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.lwmswhoxvvoagzkqxiyd.appconfig.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

/**
 * One row of the Mifos Supabase `app_config` table — the SHAPE THE LIVE TABLE HAS (read 2026-10-25):
 * `{id, instances, created_at}`, one row whose `instances` column is the shared server-instance list
 * every Mifos app reads (mifos-pay's `InstancesConfig`). Each instance names its `supportedApps`.
 *
 * NOT the template's original `{key, value}` rows: that contract never matched this project, so a
 * key/value read decoded nothing and the app silently stayed on its bundled list.
 */
@Serializable
data class RemoteAppConfigDto(
    /** The row's id. */
    val id: String? = null,
    /**
     * Kept as raw JSON: core/network's `parseInstances` owns the instance shape, for this row and the
     * bundled list alike.
     */
    val instances: JsonArray = JsonArray(emptyList()),
)
