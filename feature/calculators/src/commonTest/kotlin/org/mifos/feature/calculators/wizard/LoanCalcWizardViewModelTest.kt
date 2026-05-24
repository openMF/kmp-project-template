/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.calculators.wizard

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoanCalcWizardViewModelTest {

    @Test
    fun goNextPersistsSnapshotOnEachStep() = runTest {
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
    fun advanceThreeStepsCloseAndRecreateResumesAtSameStep() = runTest {
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

        // Session 2: recreate with same scenarioId — should find a resume candidate
        // surfaced for the screen's Continue / Discard dialog.
        val vm2 = LoanCalcWizardViewModel(outbox, repo, scenarioId)
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
    fun discardSavedDraftRemovesPersistedSnapshot() = runTest {
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
        // Resume candidate should be visible.
        assertNotNull(vm.resumeCandidate.value)
        vm.discardSavedDraft()
        assertNull(vm.resumeCandidate.value)
        val stillThere = outbox.getPendingByUniqueKey(
            LoanCalcWizardViewModel.FORM_KEY,
            scenarioId,
        )
        assertNull(stillThere, "Discard should delete the persisted draft")
    }

    @Test
    fun completeAndSavePersistsLoanViaRepository() = runTest {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val scenarioId = "wiz-4"
        val vm = LoanCalcWizardViewModel(outbox, repo, scenarioId)

        vm.onUpdatePrincipal(20_000.0)
        vm.onUpdateTenure(24)
        vm.onUpdateRate(8.0)
        vm.onUpdateName("Bike Loan")

        vm.completeAndSave()

        val saved = repo.lastUpserted
        assertNotNull(saved, "Final wizard step should upsert via LoanRepository")
        assertEquals("Bike Loan", saved.name)
        assertEquals(20_000.0, saved.principal)
        assertEquals(24, saved.tenureMonths)
        assertEquals(8.0, saved.annualRatePercent)
        assertTrue(saved.monthlyPayment > 0.0)
    }

    @Test
    fun completeAndSaveValidatesFieldsAndNoOpsOnIncompleteInput() = runTest {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val vm = LoanCalcWizardViewModel(outbox, repo, "wiz-5")

        // No principal, name still blank → must not call repository.
        vm.completeAndSave()
        assertNull(repo.lastUpserted, "Validation should refuse to upsert incomplete data")
    }

    @Test
    fun goBackClampsAtStepOne() = runTest {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val vm = LoanCalcWizardViewModel(outbox, repo, "wiz-6")
        vm.goBack()
        vm.goBack()
        assertEquals(1, vm.formState.value.currentStep)
    }

    @Test
    fun goNextClampsAtLastStep() = runTest {
        val outbox = FakeSubmitOutbox<LoanCalcScenario>()
        val repo = FakeLoanRepository()
        val vm = LoanCalcWizardViewModel(outbox, repo, "wiz-7")
        // Spam goNext beyond LAST_STEP.
        repeat(LoanCalcWizardViewModel.LAST_STEP + 3) { vm.goNext() }
        assertEquals(LoanCalcWizardViewModel.LAST_STEP, vm.formState.value.currentStep)
    }
}
