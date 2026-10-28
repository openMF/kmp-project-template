/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.lwmswhoxvvoagzkqxiyd

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appconfig.dto.RemoteAppConfigDto
import kotlin.test.Test
import kotlin.test.assertEquals

/** The DTO against the LIVE `app_config` row's shape (captured 2026-10-25, public config, trimmed). */
class AppConfigRowTest {

    private val liveRows = """
        [{"id":"63514c6d-65e0-4dc5-9b1d-f2ffa5505a59","instances":[
          {"path": "/fineract-provider/api/v1/", "label": "Mifos Bank 2 Instance", "endpoint": "mifos-bank-2.mifos.community",
           "protocol": "https://", "isDefault": true, "interbankServers": [{"path": "/v1.0/vnext2/", "label": "Mifos Bank 2 Interbank",
           "endpoint": "apis.flexcore.mx", "protocol": "https://", "isDefault": true}], "platformTenantId": "mifos-bank-2"},
          {"path": "/fineract-provider/api/v1/", "label": "TT Instance", "endpoint": "tt.mifos.community", "protocol": "https://",
           "isDefault": false, "interbankServers": [], "platformTenantId": "default"}
        ],"created_at":"2026-02-09T10:55:00+00:00"}]
    """

    @Test
    fun theLiveRowDecodesAndRoundTripsAsAnInstancesDocument() {
        val row = Json { ignoreUnknownKeys = true }.decodeFromString<List<RemoteAppConfigDto>>(liveRows).single()
        val wrapped = JsonObject(mapOf("instances" to row.instances)).toString()
        assertEquals(2, Json.parseToJsonElement(wrapped).jsonObject.getValue("instances").jsonArray.size)
    }
}
