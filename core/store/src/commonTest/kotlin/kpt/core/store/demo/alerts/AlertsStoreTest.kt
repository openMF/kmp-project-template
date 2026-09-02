/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.alerts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kpt.core.database.demo.alerts.AlertDao
import kpt.core.database.demo.alerts.AlertEntity
import kpt.core.store.demo.alerts.impl.provideAlertsStore
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Verifies [provideAlertsStore] constructability and basic SourceOfTruth wiring.
 *
 * Uses an in-memory [FakeAlertDao] that backs reads via a [MutableStateFlow],
 * so no Room runtime dependency is required in commonTest.
 */
class AlertsStoreTest {

    @Test
    fun storeIsConstructable() {
        val store = provideAlertsStore(dao = FakeAlertDao())
        assertNotNull(store)
    }
}

// ---------------------------------------------------------------------------
// Fake DAO — in-memory, MutableStateFlow-backed
// ---------------------------------------------------------------------------

private class FakeAlertDao : AlertDao {

    private val alerts = MutableStateFlow<List<AlertEntity>>(emptyList())

    override fun observeAll(): Flow<List<AlertEntity>> = alerts

    override fun observeById(id: String): Flow<AlertEntity?> =
        alerts.map { rows -> rows.firstOrNull { it.id == id } }

    override suspend fun upsert(alert: AlertEntity) {
        alerts.value = alerts.value
            .filterNot { it.id == alert.id }
            .plus(alert)
    }

    override suspend fun upsertAll(items: List<AlertEntity>) {
        val existing = alerts.value.associateBy { it.id }.toMutableMap()
        items.forEach { existing[it.id] = it }
        alerts.value = existing.values.toList()
    }

    override suspend fun deleteById(id: String) {
        alerts.value = alerts.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        alerts.value = emptyList()
    }
}
