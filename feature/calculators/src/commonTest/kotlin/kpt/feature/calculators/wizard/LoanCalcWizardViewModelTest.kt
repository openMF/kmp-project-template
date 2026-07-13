/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.wizard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.model.demo.banking.LoanCalcScenario
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoanCalcWizardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        // viewModelScope (Dispatchers.Main.immediate) must be replaced by a test
        // dispatcher so .launch{} blocks actually run during runTest.
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun goNextPersistsSnapshotOnEachStep() = runTest(testDispatcher) {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val scenarioId = "wiz-1"
        val vm = LoanCalcWizardViewModel(
            outbox = outbox,
            repository = repo,
            scenarioIdArg = scenarioId,
        )

        vm.onUpdatePrincipal(50_000.0)
        vm.goNext() // now step 2
        advanceUntilIdle()

        // The wizard's saveByUniqueKey suspends; runTest drains.
        val pending = outbox.getPendingByUniqueKey(
            LoanCalcWizardViewModel.FORM_KEY,
            scenarioId,
        )
        assertNotNull(pending, "Wizard should persist a draft after first goNext()")
        assertEquals(50_000.0, pending.payload.principal)
        assertEquals(2, pending.payload.currentStep)
    }

    @Test
    fun advanceThreeStepsCloseAndRecreateResumesAtSameStep() = runTest(testDispatcher) {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val scenarioId = "wiz-2"

        // Session 1: advance principal → tenure → rate.
        val vm1 = LoanCalcWizardViewModel(outbox, repo, scenarioId)
        vm1.onUpdatePrincipal(75_000.0)
        vm1.goNext()
        vm1.onUpdateTenure(60)
        vm1.goNext()
        vm1.onUpdateRate(6.0)
        vm1.goNext() // → step 4
        advanceUntilIdle()

        // Session 2: recreate with same scenarioId — should find a resume candidate
        // surfaced for the screen's Continue / Discard dialog.
        val vm2 = LoanCalcWizardViewModel(outbox, repo, scenarioId)
        advanceUntilIdle()
        val candidate = vm2.resumeCandidate.value
            ?: error("Expected resume candidate after recreating VM with same scenarioId")
        assertEquals(75_000.0, candidate.principal)
        assertEquals(60, candidate.tenureMonths)
        assertEquals(6.0, candidate.ratePercent)
        assertEquals(4, candidate.currentStep)

        // After the user taps "Continue", formState mirrors the persisted snapshot.
        vm2.resumeDraft()
        val resumed = vm2.formState.value
        assertEquals(4, resumed.currentStep)
        assertEquals(75_000.0, resumed.principal)
        assertNull(vm2.resumeCandidate.value, "Resume candidate cleared after consumption")
    }

    @Test
    fun discardSavedDraftRemovesPersistedSnapshot() = runTest(testDispatcher) {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val scenarioId = "wiz-3"

        // Pre-populate a saved draft for this scenarioId.
        outbox.saveByUniqueKey(
            LoanCalcWizardViewModel.FORM_KEY,
            scenarioId,
            LoanCalcScenario(
                scenarioId = scenarioId,
                principal = 1_000.0,
                currentStep = 2,
            ),
        )

        val vm = LoanCalcWizardViewModel(outbox, repo, scenarioId)
        advanceUntilIdle()
        // Resume candidate should be visible.
        assertNotNull(vm.resumeCandidate.value)
        vm.discardSavedDraft()
        advanceUntilIdle()
        assertNull(vm.resumeCandidate.value)
        val stillThere = outbox.getPendingByUniqueKey(
            LoanCalcWizardViewModel.FORM_KEY,
            scenarioId,
        )
        assertNull(stillThere, "Discard should delete the persisted draft")
    }

    @Test
    fun completeAndSavePersistsLoanViaRepository() = runTest(testDispatcher) {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val scenarioId = "wiz-4"
        val vm = LoanCalcWizardViewModel(outbox, repo, scenarioId)

        vm.onUpdatePrincipal(20_000.0)
        vm.onUpdateTenure(24)
        vm.onUpdateRate(8.0)
        vm.onUpdateName("Bike Loan")

        vm.completeAndSave()
        advanceUntilIdle()

        val saved = repo.lastUpserted
        assertNotNull(saved, "Final wizard step should upsert via LoanRepository")
        assertEquals("Bike Loan", saved.name)
        assertEquals(20_000.0, saved.principal)
        assertEquals(24, saved.tenureMonths)
        assertEquals(8.0, saved.annualRatePercent)
        assertTrue(saved.monthlyPayment > 0.0)
    }

    @Test
    fun completeAndSaveValidatesFieldsAndNoOpsOnIncompleteInput() = runTest(testDispatcher) {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val vm = LoanCalcWizardViewModel(outbox, repo, "wiz-5")

        // No principal, name still blank → must not call repository.
        vm.completeAndSave()
        advanceUntilIdle()
        assertNull(repo.lastUpserted, "Validation should refuse to upsert incomplete data")
    }

    // ── Regression guard for "Save as Loan does nothing" bug (2026-05-25) ─────────
    //
    // The wizard's "Save as Loan" button on step 5/5 always tapped without effect when
    // the user hadn't filled the Scenario Name. completeAndSave() silently returned
    // due to `name.isNotBlank()` failing isReadyForSubmit(). UX fix: disable the
    // button visually; tests below pin the validation contract so a future change
    // can't accidentally make the button enable without backing logic.

    @Test
    fun completeAndSaveNoOpsWhenNameIsBlank() = runTest(testDispatcher) {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val vm = LoanCalcWizardViewModel(outbox, repo, "wiz-blank-name")

        // Everything filled EXCEPT name — the most common "Save does nothing" case
        // because the user filled steps 1-4 and tapped Save before typing the name.
        vm.onUpdatePrincipal(10_000.0)
        vm.onUpdateTenure(12)
        vm.onUpdateRate(7.5)
        // name intentionally left blank
        vm.completeAndSave()
        advanceUntilIdle()

        assertNull(repo.lastUpserted, "Blank scenario name must prevent the save")
    }

    @Test
    fun completeAndSaveNoOpsWhenPrincipalIsZero() = runTest(testDispatcher) {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val vm = LoanCalcWizardViewModel(outbox, repo, "wiz-zero-principal")

        vm.onUpdateName("No Money")
        vm.onUpdateTenure(12)
        vm.onUpdateRate(5.0)
        // principal stays 0.0
        vm.completeAndSave()
        advanceUntilIdle()

        assertNull(repo.lastUpserted, "Zero principal must prevent the save")
    }

    @Test
    fun completeAndSaveNoOpsWhenTenureIsZero() = runTest(testDispatcher) {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val vm = LoanCalcWizardViewModel(outbox, repo, "wiz-zero-tenure")

        vm.onUpdateName("Endless")
        vm.onUpdatePrincipal(10_000.0)
        vm.onUpdateRate(5.0)
        // tenureMonths stays 0
        vm.completeAndSave()
        advanceUntilIdle()

        assertNull(repo.lastUpserted, "Zero tenure must prevent the save")
    }

    @Test
    fun completeAndSaveClearsTheOutboxDraftAndResetsForm() = runTest(testDispatcher) {
        // Regression guard for the 2026-05-25 "Save as Loan does nothing on second
        // wizard entry" bug. After a successful save the wizard's PENDING outbox row
        // MUST be deleted, otherwise the next wizard entry (same uniqueKey="new")
        // hydrates from a stale draft pinned to currentStep=5 with name="" and the
        // validator-gated Save button stays disabled — looking like a dead button.
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val scenarioId = "wiz-clears-draft"
        val vm = LoanCalcWizardViewModel(outbox, repo, scenarioId)

        // Advance through the wizard so a draft gets persisted.
        vm.onUpdatePrincipal(5_000.0)
        vm.goNext()
        vm.onUpdateTenure(12)
        vm.goNext()
        vm.onUpdateRate(7.0)
        vm.goNext()
        vm.goNext() // step 5
        vm.onUpdateName("Bike")
        advanceUntilIdle()
        assertNotNull(
            outbox.getPendingByUniqueKey(LoanCalcWizardViewModel.FORM_KEY, scenarioId),
            "Sanity: a draft must exist before the save fires",
        )

        // Save.
        vm.completeAndSave()
        advanceUntilIdle()

        // After save: the loan is upserted AND the draft is cleared AND the form is reset.
        assertNotNull(repo.lastUpserted, "Loan must persist")
        assertNull(
            outbox.getPendingByUniqueKey(LoanCalcWizardViewModel.FORM_KEY, scenarioId),
            "Draft MUST be deleted after successful save — otherwise the next wizard " +
                "entry rehydrates step 5 with a stale snapshot and Save looks dead.",
        )
        assertEquals(
            1,
            vm.formState.value.currentStep,
            "Form must reset to step 1 so the screen reflects 'fresh wizard' rather than " +
                "continuing to render the just-saved review fields.",
        )
        assertEquals("", vm.formState.value.name, "Form name must reset to empty")
        assertEquals(0.0, vm.formState.value.principal, "Form principal must reset to 0")
    }

    @Test
    fun completeAndSavePersistsLoanIdMatchesScenarioId() = runTest(testDispatcher) {
        // Pins the contract that the saved Loan's `id` mirrors the wizard's
        // `scenarioId` so a resumed wizard updates the same Loan row rather than
        // creating duplicates on each save.
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val scenarioId = "wiz-pin-id"
        val vm = LoanCalcWizardViewModel(outbox, repo, scenarioId)

        vm.onUpdatePrincipal(1_000.0)
        vm.onUpdateTenure(6)
        vm.onUpdateRate(4.0)
        vm.onUpdateName("Same Id")
        vm.completeAndSave()
        advanceUntilIdle()

        val saved = repo.lastUpserted
        assertNotNull(saved)
        assertEquals(scenarioId, saved.id, "Loan id must equal the wizard's scenarioId")
    }

    @Test
    fun completeAndSaveComputesMonthlyEmiFromInputs() = runTest(testDispatcher) {
        // Pins the contract that the saved Loan's monthly payment is computed from
        // (principal, rate, tenure) — not left at 0.0 or pulled from anywhere else.
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val vm = LoanCalcWizardViewModel(outbox, repo, "wiz-emi-calc")

        // Round numbers picked for an easy sanity check: principal $1200 at 0% over
        // 12 months → exactly $100/month.
        vm.onUpdatePrincipal(1_200.0)
        vm.onUpdateTenure(12)
        vm.onUpdateRate(0.0)
        vm.onUpdateName("Zero Interest")
        vm.completeAndSave()
        advanceUntilIdle()

        val saved = repo.lastUpserted
        assertNotNull(saved)
        // Allow small floating-point slack from computeEmi's internal math.
        assertTrue(
            kotlin.math.abs(saved.monthlyPayment - 100.0) < 0.01,
            "Expected EMI ≈ \$100; got \${saved.monthlyPayment}",
        )
    }

    @Test
    fun goBackClampsAtStepOne() = runTest(testDispatcher) {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val vm = LoanCalcWizardViewModel(outbox, repo, "wiz-6")
        vm.goBack()
        vm.goBack()
        advanceUntilIdle()
        assertEquals(1, vm.formState.value.currentStep)
    }

    @Test
    fun goNextClampsAtLastStep() = runTest(testDispatcher) {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val vm = LoanCalcWizardViewModel(outbox, repo, "wiz-7")
        // Spam goNext beyond LAST_STEP.
        repeat(LoanCalcWizardViewModel.LAST_STEP + 3) { vm.goNext() }
        advanceUntilIdle()
        assertEquals(LoanCalcWizardViewModel.LAST_STEP, vm.formState.value.currentStep)
    }
}
