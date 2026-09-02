/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.alerts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kpt.core.database.demo.alerts.AlertDao
import kpt.core.database.demo.alerts.AlertEntity

/**
 * In-memory fake of [AlertDao] whose [observeAll] is a **cold snapshot** — each subscription
 * emits the current rows once, then completes.
 *
 * Models the wasmJs failure mode the invalidation bridge absorbs (Room 3 alpha05's
 * `InvalidationTracker` does not fan out to a live collector after a write), so the ONLY
 * re-emit source is `daoFlow(ALERTS_TABLE) { }` re-subscribing on the `RoomChangeBus` signal
 * published by `notifyingWrite(ALERTS_TABLE) { }`. Cold — not a hot `MutableStateFlow` — so
 * there is no Turbine race (the reason the banking hot-fake reactive tests are `@Ignore`d).
 */
internal class FakeAlertDao : AlertDao {

    private val rows = mutableListOf<AlertEntity>()

    override fun observeAll(): Flow<List<AlertEntity>> =
        flow { emit(rows.sortedByDescending { it.createdAt }) }

    override fun observeById(id: String): Flow<AlertEntity?> =
        flow { emit(rows.firstOrNull { it.id == id }) }

    override suspend fun upsert(alert: AlertEntity) {
        rows.removeAll { it.id == alert.id }
        rows.add(alert)
    }

    override suspend fun upsertAll(alerts: List<AlertEntity>) {
        alerts.forEach { a -> rows.removeAll { it.id == a.id } }
        rows.addAll(alerts)
    }

    override suspend fun deleteById(id: String) {
        rows.removeAll { it.id == id }
    }

    override suspend fun deleteAll() {
        rows.clear()
    }
}
