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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.mifos.core.data.banking.LoanRepository
import org.mifos.core.domain.calc.computeEmi
import org.mifos.core.model.banking.Loan
import org.mifos.core.model.banking.LoanKind
import org.mifos.core.model.emi.EmiResult
import template.core.base.store.submit.SubmitOutbox
import template.core.base.ui.viewmodel.BaseDraftMutationViewModel
import kotlin.random.Random
import kotlin.time.Clock

/**
 * **Multi-step DraftSubmit showcase** — the B2 Loan Calculator Wizard.
 *
 * The wizard walks the user through five steps:
 *
 * | Step | Field           | Persistence              |
 * |------|-----------------|--------------------------|
 * | 1    | Principal       | snapshot → outbox        |
 * | 2    | Tenure (months) | snapshot → outbox        |
 * | 3    | APR             | snapshot → outbox        |
 * | 4    | Review (EMI)    | snapshot → outbox        |
 * | 5    | Name & save     | submit → LoanRepository  |
 *
 * The scenario id is the `uniqueKey` for the underlying `framework_submit_drafts`
 * row — N concurrent in-progress wizards may coexist, and resume from any device
 * session.
 *
 * On VM construction with a non-null [scenarioIdArg], the VM looks up any
 * persisted draft for that `(formKey, scenarioId)` pair and pre-fills the wizard
 * state — closing the app between steps and reopening returns to the last step
 * the user was on.
 *
 * The final step ([completeAndSave]) routes through the inherited
 * [BaseDraftMutationViewModel.onSubmit] which calls [performSubmit] →
 * [LoanRepository.upsert]. On success the draft row is automatically marked
 * SUBMITTED.
 */
class LoanCalcWizardViewModel(
    outbox: SubmitOutbox<LoanCalcScenario>,
    private val repository: LoanRepository,
    private val scenarioIdArg: String?,
) : BaseDraftMutationViewModel<LoanCalcScenario, Loan>(
    outbox = outbox,
    formKey = FORM_KEY,
    uniqueKey = scenarioIdArg ?: NEW_WIZARD_UNIQUE_KEY,
    autoSaveDraft = false,
) {

    /** Outbox kept as a VM-local handle so we can persist mid-wizard snapshots. */
    private val outboxHandle: SubmitOutbox<LoanCalcScenario> = outbox

    /** Effective unique-key used for this wizard's persisted drafts. */
    private val uniqueKey: String = scenarioIdArg ?: NEW_WIZARD_UNIQUE_KEY

    /** Wizard's in-memory form state — owns the per-step fields. */
    private val _formState = MutableStateFlow(
        LoanCalcScenario(scenarioId = scenarioIdArg ?: randomScenarioId()),
    )
    val formState: StateFlow<LoanCalcScenario> = _formState.asStateFlow()

    /**
     * Live preview of the EMI given the current inputs. Becomes meaningful at
     * step 4 (Review) once principal / rate / tenure are populated.
     */
    val preview: StateFlow<EmiResult> = _formState
        .map { s -> computeEmi(s.principal, s.ratePercent, s.tenureMonths) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            EmiResult(0.0, 0.0, 0.0),
        )

    /**
     * Becomes non-null once we've checked the outbox on first launch and found
     * a pre-existing in-progress draft to surface a "Continue / Discard?" prompt.
     */
    private val _resumeCandidate = MutableStateFlow<LoanCalcScenario?>(null)
    val resumeCandidate: StateFlow<LoanCalcScenario?> = _resumeCandidate.asStateFlow()

    init {
        // On VM creation, look up any persisted draft for the wizard's uniqueKey and
        // surface it for the screen to render the Continue / Discard prompt.
        viewModelScope.launch {
            val pending = outboxHandle.getPendingByUniqueKey(FORM_KEY, uniqueKey)
            _resumeCandidate.value = pending?.payload
        }
    }

    /**
     * User confirmed "Continue" on the resume prompt — load the persisted snapshot
     * into the form state.
     */
    fun resumeDraft() {
        _resumeCandidate.value?.let { snapshot ->
            _formState.value = snapshot
            _resumeCandidate.value = null
        }
    }

    /**
     * User chose "Discard" — drop the persisted draft so the next session starts fresh.
     */
    fun discardSavedDraft() {
        viewModelScope.launch {
            outboxHandle.deleteByUniqueKey(FORM_KEY, uniqueKey)
            _resumeCandidate.value = null
        }
    }

    fun onUpdatePrincipal(value: Double) {
        _formState.update { it.copy(principal = value) }
    }

    fun onUpdateRate(value: Double) {
        _formState.update { it.copy(ratePercent = value) }
    }

    fun onUpdateTenure(value: Int) {
        _formState.update { it.copy(tenureMonths = value) }
    }

    fun onUpdateName(value: String) {
        _formState.update { it.copy(name = value) }
    }

    /**
     * Advance to the next step and persist the snapshot for resume-anywhere.
     * Safe to call after the last step — `currentStep` is clamped at [LAST_STEP].
     */
    fun goNext() {
        val next = (_formState.value.currentStep + 1).coerceAtMost(LAST_STEP)
        _formState.update { it.copy(currentStep = next) }
        persistSnapshot()
    }

    /**
     * Return to the previous step. Persists the snapshot so back-then-close still
     * resumes at the new step.
     */
    fun goBack() {
        val prev = (_formState.value.currentStep - 1).coerceAtLeast(1)
        _formState.update { it.copy(currentStep = prev) }
        persistSnapshot()
    }

    /**
     * Final step — convert the wizard payload to a [Loan] and submit through the
     * inherited [BaseDraftMutationViewModel.onSubmit]. On success [performSubmit]
     * marks the outbox row SUBMITTED automatically.
     */
    fun completeAndSave() {
        val snapshot = _formState.value
        if (!snapshot.isReadyForSubmit()) return
        super.onSubmit(snapshot)
    }

    /** Validation for the final wizard step — all inputs must be populated and positive. */
    private fun LoanCalcScenario.isReadyForSubmit(): Boolean =
        principal > 0.0 &&
            ratePercent >= 0.0 &&
            tenureMonths > 0 &&
            name.isNotBlank()

    /** Persist the current wizard snapshot to the outbox keyed on [uniqueKey]. */
    private fun persistSnapshot() {
        val snapshot = _formState.value
        viewModelScope.launch {
            outboxHandle.saveByUniqueKey(FORM_KEY, uniqueKey, snapshot)
        }
    }

    override suspend fun performSubmit(payload: LoanCalcScenario): Loan {
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val emi = computeEmi(payload.principal, payload.ratePercent, payload.tenureMonths).emi
        val loan = Loan(
            id = payload.scenarioId,
            name = payload.name.ifBlank { "Scenario" },
            kind = LoanKind.PERSONAL,
            principal = payload.principal,
            principalRemaining = payload.principal,
            annualRatePercent = payload.ratePercent,
            tenureMonths = payload.tenureMonths,
            monthsRemaining = payload.tenureMonths,
            monthlyPayment = emi,
            // Today + 30 d is a reasonable seed — user can edit in the loan tracker.
            nextDueDate = nextDueDateSeed(),
            totalPaid = 0.0,
            createdAtMs = nowMs,
            updatedAtMs = nowMs,
        )
        repository.upsert(loan)
        return loan
    }

    private fun nextDueDateSeed(): LocalDate {
        // Trivial seed used only as initial value; not security-sensitive.
        return LocalDate(YEAR_SEED, MONTH_SEED, DAY_SEED)
    }

    companion object {
        const val FORM_KEY: String = "loan_calc_wizard"
        const val LAST_STEP: Int = 5

        /** Sentinel uniqueKey when starting a brand-new wizard with no scenarioId. */
        private const val NEW_WIZARD_UNIQUE_KEY: String = "new"

        // Plain seed values for the nextDueDate placeholder — non-cryptographic.
        private const val YEAR_SEED: Int = 2026
        private const val MONTH_SEED: Int = 6
        private const val DAY_SEED: Int = 1

        private fun randomScenarioId(): String {
            val chars = ('a'..'z') + ('0'..'9')
            return (1..16).map { chars[Random.nextInt(chars.size)] }.joinToString("")
        }
    }
}
