/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.alerts.impl

import kotlinx.coroutines.flow.Flow
import org.mifos.core.data.alerts.AlertsRepository
import org.mifos.core.model.alerts.PriceAlert
import org.mifos.core.network.alerts.api.AlertsApi

internal class AlertsRepositoryImpl(
    private val api: AlertsApi,
) : AlertsRepository {

    override fun alertsStream(): Flow<List<PriceAlert>> = api.observe()

    override suspend fun submitAlert(alert: PriceAlert): PriceAlert = api.create(alert)

    override suspend fun deleteAlert(id: String) = api.delete(id)
}
