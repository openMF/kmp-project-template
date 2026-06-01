/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.banking

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kpt.core.database.banking.dao.BillReminderDao
import kpt.core.database.banking.entity.BillReminderEntity

/**
 * In-memory fake of [BillReminderDao] for repository-level unit tests.
 *
 * `observeUpcoming` matches the production DAO contract: only enabled rows
 * whose `dueDay` is in the supplied set; ordered by `dueDay` asc then `createdAtMs`.
 */
internal class FakeBillReminderDao : BillReminderDao {

    private val state = MutableStateFlow<List<BillReminderEntity>>(emptyList())

    override fun observeAll(): Flow<List<BillReminderEntity>> = state.map { rows ->
        rows.sortedWith(compareBy({ it.dueDay }, { it.createdAtMs }))
    }

    override fun observeUpcoming(dueDays: Set<Int>): Flow<List<BillReminderEntity>> =
        state.map { rows ->
            rows
                .filter { it.enabled && it.dueDay in dueDays }
                .sortedWith(compareBy({ it.dueDay }, { it.createdAtMs }))
        }

    override fun observeById(id: String): Flow<BillReminderEntity?> =
        state.map { rows -> rows.firstOrNull { it.id == id } }

    override suspend fun getById(id: String): BillReminderEntity? =
        state.value.firstOrNull { it.id == id }

    override fun count(): Flow<Int> = state.map { it.size }

    override suspend fun upsert(entity: BillReminderEntity) {
        state.value = state.value.filterNot { it.id == entity.id } + entity
    }

    override suspend fun deleteById(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}
