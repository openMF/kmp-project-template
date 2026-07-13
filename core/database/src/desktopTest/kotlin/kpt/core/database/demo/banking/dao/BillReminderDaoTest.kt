/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.demo.banking.dao

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kpt.core.database.AppDatabase
import kpt.core.database.demo.banking.entity.BillReminderEntity
import kpt.core.model.demo.banking.BillCategory
import kpt.core.model.demo.banking.Recurrence
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks the [BillReminderDao] contract: CRUD + dueDay sort + observeUpcoming
 * filter semantics + enabled-only behavior.
 */
class BillReminderDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: BillReminderDao

    @BeforeTest
    fun setup() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        dao = database.billReminderDao
    }

    @AfterTest
    fun teardown() {
        database.close()
    }

    @Test
    fun emptyTableReturnsEmptyList() = runTest {
        dao.observeAll().test {
            assertEquals(emptyList(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun upsertThenGetByIdReturnsTheRow() = runTest {
        val bill = bill("B1", dueDay = 15)
        dao.upsert(bill)
        assertEquals(bill, dao.getById("B1"))
    }

    @Test
    fun upsertReplacesExistingRow() = runTest {
        dao.upsert(bill("B1", amount = 100.0))
        dao.upsert(bill("B1", amount = 250.0))
        assertEquals(250.0, dao.getById("B1")?.amount)
    }

    @Test
    fun deleteByIdRemovesTheRow() = runTest {
        dao.upsert(bill("B1"))
        dao.deleteById("B1")
        assertNull(dao.getById("B1"))
    }

    @Test
    fun observeAllOrdersByDueDayAscending() = runTest {
        dao.upsert(bill("late", dueDay = 28, createdAtMs = 1L))
        dao.upsert(bill("mid", dueDay = 15, createdAtMs = 2L))
        dao.upsert(bill("early", dueDay = 1, createdAtMs = 3L))

        val rows = dao.observeAll().first()
        assertEquals(listOf("early", "mid", "late"), rows.map { it.id })
    }

    @Test
    fun observeUpcomingFiltersByDueDays() = runTest {
        dao.upsert(bill("on-3", dueDay = 3))
        dao.upsert(bill("on-15", dueDay = 15))
        dao.upsert(bill("on-28", dueDay = 28))

        val rows = dao.observeUpcoming(setOf(3, 15)).first()
        assertEquals(setOf("on-3", "on-15"), rows.map { it.id }.toSet())
    }

    @Test
    fun observeUpcomingExcludesDisabledRows() = runTest {
        dao.upsert(bill("enabled", dueDay = 5, enabled = true))
        dao.upsert(bill("disabled", dueDay = 5, enabled = false))

        val rows = dao.observeUpcoming(setOf(5)).first()
        assertEquals(listOf("enabled"), rows.map { it.id })
    }

    @Test
    fun observeUpcomingEmptyWindowReturnsEmpty() = runTest {
        dao.upsert(bill("B1", dueDay = 15))
        val rows = dao.observeUpcoming(emptySet()).first()
        assertEquals(emptyList(), rows)
    }

    @Test
    fun observeByIdEmitsRowThenNullAfterDelete() = runTest {
        val b = bill("B1")
        dao.upsert(b)
        dao.observeById("B1").test {
            assertEquals(b, awaitItem())
            dao.deleteById("B1")
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun countReflectsInsertsAndDeletes() = runTest {
        dao.count().test {
            assertEquals(0, awaitItem())
            dao.upsert(bill("B1"))
            assertEquals(1, awaitItem())
            dao.upsert(bill("B2"))
            assertEquals(2, awaitItem())
            dao.deleteById("B1")
            assertEquals(1, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun allRecurrenceAndCategoryEnumsRoundTrip() = runTest {
        Recurrence.entries.forEach { rec ->
            BillCategory.entries.forEach { cat ->
                val id = "${rec.name}-${cat.name}"
                dao.upsert(bill(id = id, recurrence = rec, category = cat))
            }
        }
        val rows = dao.observeAll().first()
        val seenRec = rows.map { it.recurrence }.toSet()
        val seenCat = rows.map { it.category }.toSet()
        assertTrue(seenRec.containsAll(Recurrence.entries.toSet()))
        assertTrue(seenCat.containsAll(BillCategory.entries.toSet()))
    }

    private fun bill(
        id: String,
        dueDay: Int = 15,
        amount: Double = 100.0,
        recurrence: Recurrence = Recurrence.MONTHLY,
        category: BillCategory = BillCategory.UTILITIES,
        enabled: Boolean = true,
        createdAtMs: Long = 1_000L,
    ): BillReminderEntity = BillReminderEntity(
        id = id,
        name = "Test bill $id",
        amount = amount,
        dueDay = dueDay,
        recurrence = recurrence,
        category = category,
        enabled = enabled,
        reminderDaysBefore = 1,
        createdAtMs = createdAtMs,
        updatedAtMs = createdAtMs,
    )
}
