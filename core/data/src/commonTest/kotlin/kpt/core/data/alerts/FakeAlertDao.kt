/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.alerts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kpt.core.database.alerts.AlertDao
import kpt.core.database.alerts.AlertEntity

/**
 * In-memory fake of [AlertDao] whose reactive reads are backed by a [MutableStateFlow], so a live
 * collector re-emits after every write — what a real Room DAO `Flow` does.
 *
 * These reads were COLD until 2026-09-17, modelling a wasmJs invalidation gap so that re-emission
 * could only come from the `RoomChangeBus`/`daoFlow`/`notifyingWrite` bridge. That bridge is gone
 * (Room 3.1.0-alpha01 measured re-emitting correctly on js and wasmJs — see
 * `core/database/src/{js,wasmJs}Test/.../WebInvalidationProbeTest.kt`), so a cold fake would now
 * assert the absence of a mechanism production depends on.
 */
internal class FakeAlertDao : AlertDao {

    private val rows = MutableStateFlow<List<AlertEntity>>(emptyList())

    override fun observeAll(): Flow<List<AlertEntity>> =
        rows.map { list -> list.sortedByDescending { it.createdAt } }

    override fun observeById(id: String): Flow<AlertEntity?> =
        rows.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun upsert(alert: AlertEntity) {
        rows.update { list -> list.filterNot { it.id == alert.id } + alert }
    }

    override suspend fun upsertAll(alerts: List<AlertEntity>) {
        val ids = alerts.map { it.id }.toSet()
        rows.update { list -> list.filterNot { it.id in ids } + alerts }
    }

    override suspend fun deleteById(id: String) {
        rows.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun deleteAll() {
        rows.update { emptyList() }
    }
}
