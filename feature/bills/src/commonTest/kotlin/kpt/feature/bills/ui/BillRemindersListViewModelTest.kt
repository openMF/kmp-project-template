/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.bills.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.feature.bills.testing.FakeBillReminderRepository
import kpt.feature.bills.testing.FakeBillReminderScheduler
import kpt.feature.bills.testing.bill
import kpt.core.base.store.screen.ScreenState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins [BillRemindersListViewModel] behaviour:
 *  - Empty repo → `ScreenState.Empty`.
 *  - Non-empty repo → `ScreenState.Content` with `all` / `upcoming` / `totalUpcomingAmount`
 *    populated.
 *  - `onDelete` deletes + cancels the OS notification.
 *  - `onToggleEnabled` flips the row + cancels OS notification only when transitioning
 *    `enabled = true → false`.
 *  - `onMarkPaid` updates `updatedAtMs` and cancels the pending notification.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BillRemindersListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setMainDispatcher() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun resetMainDispatcher() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun emptyRepositoryProducesEmptyScreenState() = runTest {
        val viewModel = BillRemindersListViewModel(
            repository = FakeBillReminderRepository(),
            scheduler = FakeBillReminderScheduler(),
        )
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(ScreenState.Empty, viewModel.screenState.first { it !is ScreenState.Loading })
    }

    @Test
    fun nonEmptyRepositoryProducesContentWithSums() = runTest {
        val repo = FakeBillReminderRepository().apply {
            upsert(bill(id = "a", amount = 100.0, dueDay = 5))
            upsert(bill(id = "b", amount = 50.0, dueDay = 10))
        }
        val viewModel = BillRemindersListViewModel(
            repository = repo,
            scheduler = FakeBillReminderScheduler(),
        )
        dispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.screenState.first { it is ScreenState.Content<*> }
        val content = state as ScreenState.Content<BillRemindersUiState>
        assertEquals(2, content.data.all.size)
        // Sum is whatever the fake's "upcoming amount" returns; the fake aggregates ALL rows
        // for simplicity.
        assertEquals(150.0, content.data.totalUpcomingAmount)
    }

    @Test
    fun onDeleteRemovesBillAndCancelsNotification() = runTest {
        val repo = FakeBillReminderRepository().apply {
            upsert(bill(id = "b1"))
        }
        val scheduler = FakeBillReminderScheduler()
        val viewModel = BillRemindersListViewModel(repo, scheduler)

        viewModel.onDelete("b1")
        // Allow the action channel to drain (UnconfinedTestDispatcher runs eagerly, but
        // the coroutine launched in handleAction needs one round-trip).
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(repo.observeAll().first().isEmpty())
        assertTrue(scheduler.cancelledIds.contains("b1"))
    }

    @Test
    fun onToggleEnabledFlipsRowAndCancelsWhenDisabling() = runTest {
        val repo = FakeBillReminderRepository().apply {
            upsert(bill(id = "b1", enabled = true))
        }
        val scheduler = FakeBillReminderScheduler()
        val viewModel = BillRemindersListViewModel(repo, scheduler)

        viewModel.onToggleEnabled("b1")
        dispatcher.scheduler.advanceUntilIdle()

        val flipped = repo.observeAll().first().single()
        assertFalse(flipped.enabled)
        assertTrue(scheduler.cancelledIds.contains("b1"))
    }

    @Test
    fun onMarkPaidBumpsUpdatedAtAndCancelsNotification() = runTest {
        val repo = FakeBillReminderRepository().apply {
            upsert(bill(id = "b1", updatedAtMs = 1L))
        }
        val scheduler = FakeBillReminderScheduler()
        val viewModel = BillRemindersListViewModel(repo, scheduler)

        viewModel.onMarkPaid("b1")
        dispatcher.scheduler.advanceUntilIdle()

        val after = repo.observeAll().first().single()
        assertTrue(after.updatedAtMs > 1L)
        assertTrue(scheduler.cancelledIds.contains("b1"))
    }
}
