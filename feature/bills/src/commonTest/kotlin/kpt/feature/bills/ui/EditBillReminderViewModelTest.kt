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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.submit.SubmitOutboxStatus
import kpt.core.base.store.submit.SubmitState
import kpt.core.data.banking.BillReminderRepository
import kpt.core.model.banking.BillCategory
import kpt.core.model.banking.BillReminder
import kpt.core.model.banking.Recurrence
import kpt.feature.bills.testing.FakeBillReminderRepository
import kpt.feature.bills.testing.FakeBillReminderScheduler
import kpt.feature.bills.testing.InMemorySubmitOutbox
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Locks the **second multi-formKey draft showcase** semantics for [EditBillReminderViewModel]:
 *
 *  - Three bills edited **offline** → three independent PENDING drafts in the shared
 *    `formKey = "bill"` outbox, discriminated by `uniqueKey = billId`. The offline flag
 *    comes from `OfflineFailingRepository`, which throws on `upsert` so the framework's
 *    auto-save path persists the payload as PENDING.
 *  - `performSubmit` writes the bill to the repository AND hands a [BillReminderSchedule]
 *    to the gateway when `enabled = true`.
 *  - Disabled bills still persist but trigger a `cancel(billId)` instead of `schedule(...)`.
 *  - Form validation gates submit when name is blank or amount is non-positive.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EditBillReminderViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setMainDispatcher() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun threeIndependentBillEditsProduceThreeDistinctPendingDrafts() = runTest {
        // Single shared outbox across all three VMs — like the production wiring.
        val outbox = InMemorySubmitOutbox<BillReminder>()
        val repo = FakeBillReminderRepository()
        val offline = OfflineFailingRepository(repo)
        val scheduler = FakeBillReminderScheduler()

        val billA = "bill-a"
        val billB = "bill-b"
        val billC = "bill-c"
        val vmA = newVm(offline, scheduler, outbox, billA)
        val vmB = newVm(offline, scheduler, outbox, billB)
        val vmC = newVm(offline, scheduler, outbox, billC)

        // Each VM fills in a different name + amount, then submits.
        vmA.onNameChange("Electricity")
        vmA.onAmountChange(80.0)
        vmA.onSubmit()
        vmB.onNameChange("Internet")
        vmB.onAmountChange(60.0)
        vmB.onSubmit()
        vmC.onNameChange("Streaming")
        vmC.onAmountChange(15.0)
        vmC.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        // THE LOCK: 3 distinct PENDING entries, one per uniqueKey, all under formKey="bill".
        val pending = outbox.entries.filter { it.status == SubmitOutboxStatus.PENDING }
        assertEquals(3, pending.size, "Each bill must occupy its own PENDING slot")
        val uniqueKeys = pending.mapNotNull { it.uniqueKey }.toSet()
        assertEquals(setOf(billA, billB, billC), uniqueKeys)
        assertTrue(pending.all { it.formKey == EditBillReminderViewModel.FORM_KEY })

        // Each VM's payload was preserved verbatim — no cross-contamination.
        assertEquals("Electricity", pending.first { it.uniqueKey == billA }.payload.name)
        assertEquals("Internet", pending.first { it.uniqueKey == billB }.payload.name)
        assertEquals("Streaming", pending.first { it.uniqueKey == billC }.payload.name)
    }

    @Test
    fun successfulSubmitSchedulesNotificationWhenEnabled() = runTest {
        val outbox = InMemorySubmitOutbox<BillReminder>()
        val repo = FakeBillReminderRepository()
        val scheduler = FakeBillReminderScheduler()
        val vm = newVm(repo, scheduler, outbox, "bill-x")

        vm.onNameChange("Phone")
        vm.onAmountChange(40.0)
        vm.onDueDayChange(28)
        vm.onRecurrenceChange(Recurrence.MONTHLY)
        vm.onCategoryChange(BillCategory.UTILITIES)
        vm.onReminderDaysBeforeChange(3)
        vm.onEnabledChange(true)
        vm.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertIs<SubmitState.Submitted<*>>(vm.uiState.value.submit)
        assertEquals(1, scheduler.scheduled.size)
        assertEquals("bill-x", scheduler.scheduled.single().billId)
        assertEquals("Phone", scheduler.scheduled.single().title)
        assertTrue(scheduler.scheduled.single().triggerAtMs > 0L)
        // Successful submit leaves no PENDING draft.
        val pending = outbox.entries.filter { it.status == SubmitOutboxStatus.PENDING }
        assertTrue(pending.isEmpty(), "Successful submit must not leave a PENDING draft")
    }

    @Test
    fun successfulSubmitCancelsNotificationWhenDisabled() = runTest {
        val outbox = InMemorySubmitOutbox<BillReminder>()
        val repo = FakeBillReminderRepository()
        val scheduler = FakeBillReminderScheduler()
        val vm = newVm(repo, scheduler, outbox, "bill-disabled")

        vm.onNameChange("Old subscription")
        vm.onAmountChange(20.0)
        vm.onEnabledChange(false)
        vm.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, scheduler.scheduled.size)
        assertTrue(scheduler.cancelledIds.contains("bill-disabled"))
    }

    @Test
    fun submitIgnoresWhenNameIsBlank() = runTest {
        val outbox = InMemorySubmitOutbox<BillReminder>()
        val repo = FakeBillReminderRepository()
        val scheduler = FakeBillReminderScheduler()
        val vm = newVm(repo, scheduler, outbox, "bill-blank")

        vm.onNameChange("   ")
        vm.onAmountChange(50.0)
        vm.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        // The submit gate refuses — no outbox entry, no repo row, no schedule call.
        assertEquals(0, outbox.entries.size)
        assertEquals(0, repo.observeAll().first().size)
        assertEquals(0, scheduler.scheduled.size)
    }

    @Test
    fun submitIgnoresWhenAmountIsZero() = runTest {
        val outbox = InMemorySubmitOutbox<BillReminder>()
        val repo = FakeBillReminderRepository()
        val scheduler = FakeBillReminderScheduler()
        val vm = newVm(repo, scheduler, outbox, "bill-zero")

        vm.onNameChange("Free trial")
        vm.onAmountChange(0.0)
        vm.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, outbox.entries.size)
    }

    @Test
    fun successfulSubmitReachesSubmittedTerminalState() = runTest {
        val outbox = InMemorySubmitOutbox<BillReminder>()
        val repo = FakeBillReminderRepository()
        val scheduler = FakeBillReminderScheduler()
        val vm = newVm(repo, scheduler, outbox, "bill-ok")

        vm.onNameChange("Rent")
        vm.onAmountChange(1200.0)
        vm.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        val submitted = vm.uiState.value.submit
        assertIs<SubmitState.Submitted<*>>(submitted)
        assertNotNull(submitted.result)
    }

    @Test
    fun formKeyIsBillForEveryInstance() {
        // The whole multi-formKey showcase rests on this invariant.
        assertEquals("bill", EditBillReminderViewModel.FORM_KEY)
    }

    private fun newVm(
        repo: BillReminderRepository,
        scheduler: FakeBillReminderScheduler,
        outbox: InMemorySubmitOutbox<BillReminder>,
        billId: String?,
    ): EditBillReminderViewModel = EditBillReminderViewModel(
        repository = repo,
        scheduler = scheduler,
        billId = billId,
        outbox = outbox,
        // Pin a deterministic clock so reminderTriggerMs() math is reproducible — without
        // this, today's calendar date interacts with the test's dueDay to make the
        // reminder either in the past (no schedule) or in the future (schedule), flaking
        // the assertion. Pin to 2026-01-15 00:00 UTC so May/Aug/etc. due dates always
        // produce a positive triggerAtMs.
        clock = object : kotlin.time.Clock {
            override fun now(): kotlin.time.Instant =
                kotlin.time.Instant.fromEpochMilliseconds(1_736_899_200_000L)
        },
        timeZone = kotlinx.datetime.TimeZone.UTC,
    )
}

/**
 * Wraps a [FakeBillReminderRepository] and makes `upsert` throw a network-like exception so
 * the `DraftSubmitHandler` routes to the silent auto-save path. Mirrors the loans test fake.
 */
private class OfflineFailingRepository(
    private val delegate: FakeBillReminderRepository,
) : BillReminderRepository by delegate {

    override suspend fun upsert(bill: BillReminder) {
        throw OfflineIOException("network unavailable")
    }
}

/**
 * Throwable whose simple class name contains both `Offline` and `IOException`, so the
 * framework's `categorize()` (in `core-base/store/error/ErrorCategory.kt`) classifies it as
 * `ErrorCategory.Network` — the trigger required for `DraftSubmitHandler`'s auto-save path.
 */
private class OfflineIOException(message: String) : RuntimeException(message)
