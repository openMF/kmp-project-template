/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.banking.dao

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kpt.core.database.AppDatabase
import kpt.core.database.banking.entity.LoanEntity
import kpt.core.model.banking.LoanKind
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks the [LoanDao] contract: CRUD + reactive observation + sort order.
 *
 * In-memory Room — no platform-side state leaks between tests. The fixture
 * loans below intentionally have mixed `nextDueDate` and `createdAtMs` values
 * so the ORDER BY ascending semantics are testable.
 */
class LoanDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: LoanDao

    @BeforeTest
    fun setup() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        dao = database.loanDao
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
        val loan = mortgage("L1", nextDue = LocalDate(2026, 6, 1))
        dao.upsert(loan)
        assertEquals(loan, dao.getById("L1"))
    }

    @Test
    fun upsertReplacesExistingRow() = runTest {
        val original = mortgage("L1", principal = 100_000.0)
        dao.upsert(original)
        val updated = original.copy(principal = 200_000.0, updatedAtMs = 999L)
        dao.upsert(updated)
        assertEquals(updated, dao.getById("L1"))
    }

    @Test
    fun deleteByIdRemovesTheRow() = runTest {
        dao.upsert(mortgage("L1"))
        dao.deleteById("L1")
        assertNull(dao.getById("L1"))
    }

    @Test
    fun deleteByIdAbsentIsNoop() = runTest {
        // Should not throw.
        dao.deleteById("never-existed")
    }

    @Test
    fun observeAllOrdersByNextDueDateAscending() = runTest {
        dao.upsert(mortgage("late", nextDue = LocalDate(2026, 12, 1), createdAtMs = 1L))
        dao.upsert(mortgage("early", nextDue = LocalDate(2026, 1, 1), createdAtMs = 2L))
        dao.upsert(mortgage("middle", nextDue = LocalDate(2026, 6, 1), createdAtMs = 3L))

        dao.observeAll().test {
            val rows = awaitItem()
            assertEquals(listOf("early", "middle", "late"), rows.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeAllTieBreakIsCreatedAtAscending() = runTest {
        val due = LocalDate(2026, 6, 1)
        dao.upsert(mortgage("second", nextDue = due, createdAtMs = 200L))
        dao.upsert(mortgage("first", nextDue = due, createdAtMs = 100L))

        dao.observeAll().test {
            val rows = awaitItem()
            assertEquals(listOf("first", "second"), rows.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeByIdEmitsNullWhenAbsent() = runTest {
        dao.observeById("missing").test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeByIdEmitsRowThenNullAfterDelete() = runTest {
        val loan = mortgage("L1")
        dao.upsert(loan)
        dao.observeById("L1").test {
            assertEquals(loan, awaitItem())
            dao.deleteById("L1")
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun countReflectsInsertsAndDeletes() = runTest {
        dao.count().test {
            assertEquals(0, awaitItem())
            dao.upsert(mortgage("L1"))
            assertEquals(1, awaitItem())
            dao.upsert(mortgage("L2"))
            assertEquals(2, awaitItem())
            dao.deleteById("L1")
            assertEquals(1, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun allLoanKindsRoundTripThroughTypeConverters() = runTest {
        // Locks BankingTypeConverters against future enum-rename regressions.
        LoanKind.entries.forEachIndexed { idx, kind ->
            dao.upsert(mortgage(id = "kind-$idx", kind = kind))
        }
        val rows = dao.observeAll().first()
        val storedKinds = rows.map { it.kind }.toSet()
        assertTrue(storedKinds.containsAll(LoanKind.entries.toSet()))
        assertNotNull(rows.first().kind)
    }

    private fun mortgage(
        id: String,
        kind: LoanKind = LoanKind.MORTGAGE,
        principal: Double = 250_000.0,
        nextDue: LocalDate = LocalDate(2026, 6, 1),
        createdAtMs: Long = 1_000L,
    ): LoanEntity = LoanEntity(
        id = id,
        name = "Test loan $id",
        kind = kind,
        principal = principal,
        principalRemaining = principal,
        annualRatePercent = 6.5,
        tenureMonths = 360,
        monthsRemaining = 360,
        monthlyPayment = 1_580.17,
        nextDueDate = nextDue,
        totalPaid = 0.0,
        createdAtMs = createdAtMs,
        updatedAtMs = createdAtMs,
    )
}
