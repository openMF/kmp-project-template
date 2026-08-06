/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.feature.bills.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.banking.BillReminderRepository
import kpt.core.model.demo.banking.BillCategory
import kpt.core.model.demo.banking.BillReminder
import kpt.core.model.demo.banking.Recurrence

/**
 * In-memory [BillReminderRepository] for ViewModel-level unit tests.
 *
 * "Upcoming" semantics are intentionally simpler than production — we treat the entire
 * row set as upcoming so tests can assert the VM's aggregation behaviour without
 * recomputing day-of-month windows. The production repository's window math is
 * exercised separately by `BillReminderRepositoryTest` in `core/data`.
 */
internal class FakeBillReminderRepository : BillReminderRepository {

    private val state = MutableStateFlow<List<BillReminder>>(emptyList())

    /** Current snapshot — useful for raw assertions. */
    val current: List<BillReminder> get() = state.value

    override fun billRemindersStream(scope: CoroutineScope): ScreenDataStream<List<BillReminder>> =
        screenDataStreamForTesting(state.map { if (it.isEmpty()) ScreenState.Empty else ScreenState.Content(it) })

    override fun observeAll(): Flow<List<BillReminder>> = state.map { rows ->
        rows.sortedBy { it.dueDay }
    }

    override fun observeUpcoming(maxDays: Int): Flow<List<BillReminder>> = state.map { rows ->
        if (maxDays < 0) emptyList() else rows.filter { it.enabled }
    }

    override fun observeById(id: String): Flow<BillReminder?> = state.map { rows ->
        rows.firstOrNull { it.id == id }
    }

    override suspend fun getById(id: String): BillReminder? = state.value.firstOrNull { it.id == id }

    override suspend fun upsert(bill: BillReminder) {
        state.value = state.value.filterNot { it.id == bill.id } + bill
    }

    override suspend fun delete(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }

    override fun observeTotalUpcomingAmount(maxDays: Int): Flow<Double> = state.map { rows ->
        if (maxDays < 0) 0.0 else rows.filter { it.enabled }.sumOf { it.amount }
    }

    override fun observeCount(): Flow<Int> = state.map { it.size }
}

/**
 * Builder for a [BillReminder] with sensible test defaults.
 *
 * Used across feature/bills test classes — central so every test gets the same
 * "boring bill" shape unless they override a single field.
 */
internal fun bill(
    id: String = "B1",
    name: String = "Test bill",
    amount: Double = 100.0,
    dueDay: Int = 15,
    recurrence: Recurrence = Recurrence.MONTHLY,
    category: BillCategory = BillCategory.UTILITIES,
    enabled: Boolean = true,
    reminderDaysBefore: Int = 1,
    createdAtMs: Long = 1_700_000_000_000L,
    updatedAtMs: Long = 1_700_000_000_000L,
): BillReminder = BillReminder(
    id = id,
    name = name,
    amount = amount,
    dueDay = dueDay,
    recurrence = recurrence,
    category = category,
    enabled = enabled,
    reminderDaysBefore = reminderDaysBefore,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs,
)
