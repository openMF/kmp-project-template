/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.banking

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.database.banking.dao.BillReminderDao
import kpt.core.database.banking.entity.BillReminderEntity
import kpt.core.store.banking.impl.provideBillRemindersStore
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Verifies [provideBillRemindersStore] constructability with an in-memory
 * [FakeBillReminderDao].
 */
class BillRemindersStoreTest {

    @Test
    fun storeIsConstructable() {
        val store = provideBillRemindersStore(dao = FakeBillReminderDao())
        assertNotNull(store)
    }
}

// ---------------------------------------------------------------------------
// Fake DAO — in-memory, MutableStateFlow-backed
// ---------------------------------------------------------------------------

private class FakeBillReminderDao : BillReminderDao {

    private val reminders = MutableStateFlow<List<BillReminderEntity>>(emptyList())

    override fun observeAll(): Flow<List<BillReminderEntity>> = reminders

    override fun observeUpcoming(dueDays: Set<Int>): Flow<List<BillReminderEntity>> =
        MutableStateFlow(
            reminders.value.filter { it.enabled && it.dueDay in dueDays },
        )

    override fun observeById(id: String): Flow<BillReminderEntity?> =
        MutableStateFlow(reminders.value.firstOrNull { it.id == id })

    override suspend fun getById(id: String): BillReminderEntity? =
        reminders.value.firstOrNull { it.id == id }

    override fun count(): Flow<Int> = MutableStateFlow(reminders.value.size)

    override suspend fun upsert(entity: BillReminderEntity) {
        reminders.value = reminders.value
            .filterNot { it.id == entity.id }
            .plus(entity)
    }

    override suspend fun deleteById(id: String) {
        reminders.value = reminders.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        reminders.value = emptyList()
    }
}
