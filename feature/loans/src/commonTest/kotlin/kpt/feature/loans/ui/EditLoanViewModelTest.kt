/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loans.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.model.banking.Loan
import kpt.core.base.store.submit.SubmitOutboxStatus
import kpt.core.base.store.submit.SubmitState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * **The multi-formKey killer-feature contract.**
 *
 * Locks the per-loan independence of [EditLoanViewModel.uniqueKey] against the framework's
 * [kpt.core.base.store.submit.SubmitOutbox] — three concurrent VMs editing three distinct
 * loans must produce three distinct PENDING outbox entries, none overwriting the others.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EditLoanViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── T1: happy-path edit success ──────────────────────────────────────────

    @Test
    fun submitOnlineCommitsLoanAndReachesSubmittedState() = runTest {
        val repo = FakeLoanRepository()
        val outbox = InMemorySubmitOutbox<Loan>()
        val vm = EditLoanViewModel(repository = repo, outbox = outbox, loanId = null)

        vm.onNameChange("New car loan")
        vm.onPrincipalChange(20_000.0)
        vm.onTenureMonthsChange(48)
        vm.onAnnualRatePercentChange(7.5)
        vm.onSubmit()

        dispatcher.scheduler.advanceUntilIdle()

        assertIs<SubmitState.Submitted<*>>(vm.uiState.value.submit)
        assertEquals(1, repo.current.size)
        // No PENDING draft after a successful submit.
        val pending = outbox.entries.filter { it.status == SubmitOutboxStatus.PENDING }
        assertTrue(pending.isEmpty(), "Successful submit must not leave a PENDING draft")
    }

    // ─── T2: invalid form → no-op ────────────────────────────────────────────

    @Test
    fun submitWithInvalidFormIsNoOp() = runTest {
        val repo = FakeLoanRepository()
        val outbox = InMemorySubmitOutbox<Loan>()
        val vm = EditLoanViewModel(repository = repo, outbox = outbox, loanId = null)

        // Name blank, principal == 0 → !isValid.
        vm.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(SubmitState.Idle, vm.uiState.value.submit)
        assertTrue(repo.current.isEmpty())
        assertTrue(outbox.entries.isEmpty())
    }

    // ─── T3: edit mode hydrates form from repository ─────────────────────────

    @Test
    fun editModeHydratesFormFromExistingLoan() = runTest {
        val repo = FakeLoanRepository()
        val existing = sampleLoan(id = "L1", name = "Old Name", principal = 12_345.67)
        repo.upsert(existing)
        val outbox = InMemorySubmitOutbox<Loan>()

        val vm = EditLoanViewModel(repository = repo, outbox = outbox, loanId = "L1")
        dispatcher.scheduler.advanceUntilIdle()

        val form = vm.formState.value
        assertEquals("Old Name", form.name)
        assertEquals(12_345.67, form.principal)
    }

    // ─── T4: THE killer scenario — three concurrent edits, three PENDING ────

    @Test
    fun threeConcurrentLoanEditsProduceThreeIndependentPendingDrafts() = runTest {
        val repo = FakeLoanRepository()
        val l1 = sampleLoan(id = "L1", name = "Mortgage", principalRemaining = 100_000.0)
        val l2 = sampleLoan(id = "L2", name = "Car", principalRemaining = 8_000.0)
        val l3 = sampleLoan(id = "L3", name = "Student", principalRemaining = 22_000.0)
        repo.upsert(l1)
        repo.upsert(l2)
        repo.upsert(l3)

        // Use a shared outbox — this is the production wiring (single SubmitOutbox<Loan>).
        val outbox = InMemorySubmitOutbox<Loan>()

        // Override repo.upsert to throw a Network error so the draft handler routes to the
        // offline auto-save path. We do this with a shadow repo for each VM.
        val offlineRepo = OfflineFailingRepository(repo)

        val vm1 = EditLoanViewModel(repository = offlineRepo, outbox = outbox, loanId = "L1")
        val vm2 = EditLoanViewModel(repository = offlineRepo, outbox = outbox, loanId = "L2")
        val vm3 = EditLoanViewModel(repository = offlineRepo, outbox = outbox, loanId = "L3")
        dispatcher.scheduler.advanceUntilIdle()

        // User edits each loan's principalRemaining field, then submits.
        vm1.onPrincipalRemainingChange(99_000.0)
        vm1.onSubmit()
        vm2.onPrincipalRemainingChange(7_500.0)
        vm2.onSubmit()
        vm3.onPrincipalRemainingChange(21_000.0)
        vm3.onSubmit()

        dispatcher.scheduler.advanceUntilIdle()

        // All 3 must be in Failed(draftSaved=true) — the silent auto-save path.
        val s1 = assertIs<SubmitState.Failed>(vm1.uiState.value.submit)
        val s2 = assertIs<SubmitState.Failed>(vm2.uiState.value.submit)
        val s3 = assertIs<SubmitState.Failed>(vm3.uiState.value.submit)
        assertTrue(s1.draftSaved, "L1 draft must have auto-saved on network failure")
        assertTrue(s2.draftSaved, "L2 draft must have auto-saved on network failure")
        assertTrue(s3.draftSaved, "L3 draft must have auto-saved on network failure")

        // THE LOCK: 3 distinct PENDING entries, one per uniqueKey, all under formKey="loan".
        val pending = outbox.entries.filter { it.status == SubmitOutboxStatus.PENDING }
        assertEquals(3, pending.size, "Each loan must occupy its own PENDING slot")
        val uniqueKeys = pending.mapNotNull { it.uniqueKey }.toSet()
        assertEquals(setOf("L1", "L2", "L3"), uniqueKeys)
        assertTrue(pending.all { it.formKey == EditLoanViewModel.FORM_KEY })

        // The newest payload values were persisted, not overwritten.
        assertEquals(
            99_000.0,
            pending.first { it.uniqueKey == "L1" }.payload.principalRemaining,
        )
        assertEquals(
            7_500.0,
            pending.first { it.uniqueKey == "L2" }.payload.principalRemaining,
        )
        assertEquals(
            21_000.0,
            pending.first { it.uniqueKey == "L3" }.payload.principalRemaining,
        )
    }

    // ─── T5: addNew uses NEW_LOAN_KEY, doesn't collide with an existing edit ─

    @Test
    fun addNewLoanDraftIsDistinctFromExistingLoanEditDraft() = runTest {
        val repo = FakeLoanRepository()
        repo.upsert(sampleLoan(id = "L1"))
        val outbox = InMemorySubmitOutbox<Loan>()
        val offlineRepo = OfflineFailingRepository(repo)

        val addVm = EditLoanViewModel(repository = offlineRepo, outbox = outbox, loanId = null)
        val editVm = EditLoanViewModel(repository = offlineRepo, outbox = outbox, loanId = "L1")
        dispatcher.scheduler.advanceUntilIdle()

        addVm.onNameChange("Brand new")
        addVm.onPrincipalChange(5_000.0)
        addVm.onTenureMonthsChange(12)
        addVm.onSubmit()
        editVm.onPrincipalRemainingChange(50.0)
        editVm.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        val pending = outbox.entries.filter { it.status == SubmitOutboxStatus.PENDING }
        assertEquals(2, pending.size)
        val keys = pending.mapNotNull { it.uniqueKey }.toSet()
        assertEquals(setOf(EditLoanViewModel.NEW_LOAN_KEY, "L1"), keys)

        // Both share the same formKey.
        assertTrue(pending.all { it.formKey == EditLoanViewModel.FORM_KEY })
    }

    // ─── T6: re-submit on the SAME uniqueKey upserts, doesn't duplicate ──────

    @Test
    fun resubmitOnSameLoanIdUpdatesTheSamePendingDraft() = runTest {
        val repo = FakeLoanRepository()
        repo.upsert(sampleLoan(id = "L1"))
        val outbox = InMemorySubmitOutbox<Loan>()
        val offlineRepo = OfflineFailingRepository(repo)

        val vm = EditLoanViewModel(repository = offlineRepo, outbox = outbox, loanId = "L1")
        dispatcher.scheduler.advanceUntilIdle()

        vm.onPrincipalRemainingChange(100.0)
        vm.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onPrincipalRemainingChange(200.0)
        vm.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        val pending = outbox.entries.filter { it.status == SubmitOutboxStatus.PENDING }
        assertEquals(1, pending.size, "Same (formKey, uniqueKey) must upsert, not duplicate")
        assertEquals(200.0, pending.first().payload.principalRemaining)
        assertEquals("L1", pending.first().uniqueKey)
    }

    // ─── T7: dismiss after submit success clears the form ────────────────────

    @Test
    fun dismissResultClearsFormFields() = runTest {
        val repo = FakeLoanRepository()
        val outbox = InMemorySubmitOutbox<Loan>()
        val vm = EditLoanViewModel(repository = repo, outbox = outbox, loanId = null)

        vm.onNameChange("Holiday Loan")
        vm.onPrincipalChange(2_000.0)
        vm.onTenureMonthsChange(6)
        vm.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertIs<SubmitState.Submitted<*>>(vm.uiState.value.submit)
        assertNotNull(repo.current.firstOrNull { it.name == "Holiday Loan" })

        vm.onDismissResult()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("", vm.formState.value.name)
        assertEquals(0.0, vm.formState.value.principal)
    }

    @Test
    fun submitComputesMonthlyPaymentFromFormInputs() = runTest {
        val repo = FakeLoanRepository()
        val outbox = InMemorySubmitOutbox<Loan>()
        val vm = EditLoanViewModel(repository = repo, outbox = outbox, loanId = null)

        vm.onNameChange("EMI Test")
        vm.onPrincipalChange(10_000.0)
        vm.onAnnualRatePercentChange(12.0)
        vm.onTenureMonthsChange(12)
        vm.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        val persisted = repo.current.first()
        // computeMonthlyEmi(10_000, 12, 12) ≈ 888.49 — assert it's in the right ballpark.
        assertTrue(persisted.monthlyPayment > 800.0 && persisted.monthlyPayment < 900.0)
    }

    // For T3 hydration, also double-check that the form does NOT auto-populate when
    // loanId is null (add mode).
    @Test
    fun addModeStartsWithBlankForm() = runTest {
        val repo = FakeLoanRepository()
        repo.upsert(sampleLoan(id = "L1", name = "Should-Not-Hydrate"))
        val outbox = InMemorySubmitOutbox<Loan>()
        val vm = EditLoanViewModel(repository = repo, outbox = outbox, loanId = null)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("", vm.formState.value.name)
        // No leakage from any existing loan.
        assertNull(vm.formState.value.name.ifEmpty { null })
    }
}

/**
 * Wraps a [FakeLoanRepository] and makes `upsert` throw a [java.io.IOException]-like network
 * exception so the [kpt.core.base.store.submit.DraftSubmitHandler] routes to the
 * silent auto-save path. Reads still delegate to the underlying repo.
 */
private class OfflineFailingRepository(
    private val delegate: FakeLoanRepository,
) : kpt.core.data.banking.LoanRepository by delegate {

    override suspend fun upsert(loan: Loan) {
        throw OfflineIOException("network unavailable")
    }
}

/**
 * Throwable whose simple class name contains both `Offline` and `IOException`, so the
 * framework's `categorize()` (in `core-base/store/error/ErrorCategory.kt`) classifies it
 * as [kpt.core.base.store.error.ErrorCategory.Network] — which is the trigger
 * required for [kpt.core.base.store.submit.DraftSubmitHandler]'s silent auto-save path.
 */
private class OfflineIOException(message: String) : RuntimeException(message)
