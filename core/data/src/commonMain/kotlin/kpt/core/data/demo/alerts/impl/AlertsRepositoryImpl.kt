/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.alerts.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kpt.core.base.database.invalidation.notifyingWrite
import kpt.core.data.demo.alerts.AlertsRepository
import kpt.core.database.demo.alerts.AlertDao
import kpt.core.database.demo.alerts.AlertEntity
import kpt.core.model.demo.alerts.AlertDirection
import kpt.core.model.demo.alerts.PriceAlert
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

internal class AlertsRepositoryImpl(
    private val alertsStore: Store<Unit, List<AlertEntity>>,
    private val alertDao: AlertDao,
) : AlertsRepository {

    override fun alertsStream(): Flow<List<PriceAlert>> =
        alertsStore.stream(StoreReadRequest.cached(Unit, refresh = false))
            .filterIsInstance<StoreReadResponse.Data<List<AlertEntity>>>()
            .map { response -> response.value.map { it.toPriceAlert() } }

    override suspend fun submitAlert(alert: PriceAlert): PriceAlert {
        notifyingWrite(ALERTS_TABLE) {
            alertDao.upsert(alert.toAlertEntity())
        }
        return alert
    }

    override suspend fun deleteAlert(id: String) {
        notifyingWrite(ALERTS_TABLE) {
            alertDao.deleteById(id)
        }
    }

    private companion object {
        /** Room `@Entity(tableName = …)` for [AlertEntity]. Shared with [provideAlertsStore]'s reader. */
        const val ALERTS_TABLE = "alerts"
    }
}

/**
 * Maps a persisted [AlertEntity] to the domain [PriceAlert].
 *
 * Field-level mapping:
 * - [AlertEntity.symbol] → [PriceAlert.coinId] (both identify the watched instrument)
 * - [AlertEntity.conditionAbove] → [PriceAlert.direction] (true → ABOVE, false → BELOW)
 * - [AlertEntity.targetPrice] → [PriceAlert.targetValue]
 * - [AlertEntity.createdAt] → [PriceAlert.createdAtMs]
 * - [PriceAlert.enabled] defaults to `true` (AlertEntity has no enabled field)
 */
private fun AlertEntity.toPriceAlert(): PriceAlert = PriceAlert(
    id = id,
    coinId = symbol,
    direction = if (conditionAbove) AlertDirection.ABOVE else AlertDirection.BELOW,
    targetValue = targetPrice,
    enabled = true,
    createdAtMs = createdAt,
)

/**
 * Maps a domain [PriceAlert] to a persistable [AlertEntity].
 *
 * Field-level mapping:
 * - [PriceAlert.coinId] → [AlertEntity.symbol]
 * - [PriceAlert.direction] → [AlertEntity.conditionAbove] (ABOVE → true, BELOW/PCT_CHANGE → false)
 * - [PriceAlert.targetValue] → [AlertEntity.targetPrice]
 * - [PriceAlert.createdAtMs] → [AlertEntity.createdAt]
 */
private fun PriceAlert.toAlertEntity(): AlertEntity = AlertEntity(
    id = id,
    symbol = coinId,
    conditionAbove = direction == AlertDirection.ABOVE,
    targetPrice = targetValue,
    createdAt = createdAtMs,
)
