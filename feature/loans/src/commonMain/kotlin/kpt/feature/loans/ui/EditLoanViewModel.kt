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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.SubmitOutbox
import kpt.core.base.ui.viewmodel.BaseMutationViewModel
import kpt.core.base.ui.viewmodel.MutationMode
import kpt.core.data.demo.banking.LoanRepository
import kpt.core.model.demo.banking.Loan
import kpt.core.model.demo.banking.LoanKind
import kotlin.random.Random
import kotlin.time.Clock

/**
 * **The canonical multi-formKey [BaseMutationViewModel] (`MutationMode.Draft`) showcase.**
 *
 * Each instance is scoped to a single loan via `uniqueKey = loanId`. The user can edit three
 * loans offline → three independent PENDING drafts coexist in `framework_submit_drafts`. On
 * the next online transition, [kpt.core.base.store.submit.OfflineSubmitSyncer] commits
 * them serially — none lost, none overwritten.
 *
 * `formKey = "loan"` groups every loan draft under one form-type for outbox queries (e.g.
 * "show me all pending loan drafts on the dashboard"). `uniqueKey` partitions inside the
 * form-type so concurrent drafts coexist.
 *
 * **Add vs Edit modes:**
 * - `loanId == null` → fresh add. `uniqueKey = "new"` (singleton add slot).
 * - `loanId != null` → edit existing. Form pre-populated via [hydrateFromExisting].
 *
 * The "submit" target is `repository.upsert(loan)` — purely local. The DraftSubmitHandler
 * polish (Saving… / Saved ✓ / Saved offline) still applies, which is the whole point of
 * the showcase: forks see the framework's offline-resilience UX even when their submit is
 * a local commit, and can swap in a remote API call later with zero ViewModel changes.
 */
class EditLoanViewModel(
    private val repository: LoanRepository,
    outbox: SubmitOutbox<Loan>,
    val loanId: String?,
) : BaseMutationViewModel<Loan, Loan>(
    MutationMode.Draft(
        outbox = outbox,
        formKey = FORM_KEY,
        uniqueKey = loanId ?: NEW_LOAN_KEY,
        autoSaveDraft = true,
    ),
) {

    private val _formState = MutableStateFlow(LoanFormState())
    val formState: StateFlow<LoanFormState> = _formState.asStateFlow()

    init {
        // `mutableScreenState` only gates "form ready" — Loading while an existing loan hydrates
        // (edit), then Content(snapshot); Content(snapshot) immediately for create. Without this the
        // screen would sit at ScreenState.Loading forever (MutationScreenContent renders the read
        // gate). The content slot ignores the read payload (the form renders `_formState`), so the
        // snapshot is a "ready" sentinel, not display data.
        if (loanId != null) {
            viewModelScope.launch {
                repository.getById(loanId)?.let { hydrateFromExisting(it) }
                mutableScreenState.value = ScreenState.Content(formSnapshot())
            }
        } else {
            mutableScreenState.value = ScreenState.Content(formSnapshot())
        }
    }

    /**
     * Three-case Resume: pre-fill the form from the saved draft surfaced in
     * [uiState]`.resumableDraft`, then dismiss the prompt (the inherited [onResumeDraft]).
     * Wired to [kpt.core.base.ui.draft.DraftResolutionPrompt]'s Resume action.
     */
    fun onResume() {
        uiState.value.resumableDraft?.let { hydrateFromExisting(it) }
        onResumeDraft()
    }

    /** A [Loan] snapshot of the current form — the "form ready" sentinel for the screen state. */
    private fun formSnapshot(): Loan {
        val form = _formState.value
        val now = Clock.System.now().toEpochMilliseconds()
        return Loan(
            id = loanId ?: NEW_LOAN_KEY,
            name = form.name,
            kind = form.kind,
            principal = form.principal,
            principalRemaining = form.principalRemaining,
            annualRatePercent = form.annualRatePercent,
            tenureMonths = form.tenureMonths,
            monthsRemaining = form.monthsRemaining,
            monthlyPayment = computeMonthlyEmi(
                principal = form.principal,
                annualRatePercent = form.annualRatePercent,
                tenureMonths = form.tenureMonths,
            ),
            nextDueDate = form.nextDueDate,
            totalPaid = form.totalPaid,
            createdAtMs = now,
            updatedAtMs = now,
        )
    }

    /** Pre-populate the form with the existing loan's fields. Idempotent. */
    private fun hydrateFromExisting(loan: Loan) {
        _formState.value = LoanFormState(
            name = loan.name,
            kind = loan.kind,
            principal = loan.principal,
            principalRemaining = loan.principalRemaining,
            annualRatePercent = loan.annualRatePercent,
            tenureMonths = loan.tenureMonths,
            monthsRemaining = loan.monthsRemaining,
            nextDueDate = loan.nextDueDate,
            totalPaid = loan.totalPaid,
        )
    }

    fun onNameChange(value: String) {
        _formState.update { it.copy(name = value) }
    }

    fun onKindChange(value: LoanKind) {
        _formState.update { it.copy(kind = value) }
    }

    fun onPrincipalChange(value: Double) {
        _formState.update {
            it.copy(
                principal = value,
                // For new loans, keep remaining in sync with principal until the user edits it.
                principalRemaining = if (loanId == null) value else it.principalRemaining,
            )
        }
    }

    fun onPrincipalRemainingChange(value: Double) {
        _formState.update { it.copy(principalRemaining = value) }
    }

    fun onAnnualRatePercentChange(value: Double) {
        _formState.update { it.copy(annualRatePercent = value) }
    }

    fun onTenureMonthsChange(value: Int) {
        _formState.update {
            it.copy(
                tenureMonths = value,
                monthsRemaining = if (loanId == null) value else it.monthsRemaining,
            )
        }
    }

    fun onMonthsRemainingChange(value: Int) {
        _formState.update { it.copy(monthsRemaining = value) }
    }

    fun onNextDueDateChange(value: LocalDate) {
        _formState.update { it.copy(nextDueDate = value) }
    }

    fun onTotalPaidChange(value: Double) {
        _formState.update { it.copy(totalPaid = value) }
    }

    /**
     * Snapshot the current form into a [Loan] payload and submit through the draft handler.
     *
     * Blank name or non-positive principal → no-op. Computes monthly EMI from
     * (principal, annualRatePercent, tenureMonths) so the persisted row carries an
     * accurate dashboard value even before the user logs any payments.
     */
    fun onSubmit() {
        val form = _formState.value
        if (!form.isValid) return
        val now = Clock.System.now().toEpochMilliseconds()
        val monthly = computeMonthlyEmi(
            principal = form.principal,
            annualRatePercent = form.annualRatePercent,
            tenureMonths = form.tenureMonths,
        )
        val payload = Loan(
            id = loanId ?: randomId(),
            name = form.name.trim(),
            kind = form.kind,
            principal = form.principal,
            principalRemaining = form.principalRemaining,
            annualRatePercent = form.annualRatePercent,
            tenureMonths = form.tenureMonths,
            monthsRemaining = form.monthsRemaining,
            monthlyPayment = monthly,
            nextDueDate = form.nextDueDate,
            totalPaid = form.totalPaid,
            createdAtMs = now,
            updatedAtMs = now,
        )
        super.onSubmit(payload)
    }

    /** Local commit. With the syncer wired, this also runs during offline-draft replay. */
    override suspend fun performSubmit(payload: Loan): Loan {
        repository.upsert(payload)
        return payload
    }

    /** Reset both submit state (super) and form (VM-local). */
    override fun onDismissResult() {
        super.onDismissResult()
        _formState.value = LoanFormState()
    }

    private fun randomId(): String {
        val chars = ('a'..'z') + ('0'..'9')
        return (1..16).map { chars[Random.nextInt(chars.size)] }.joinToString("")
    }

    companion object {
        /** Stable form-type identifier — groups every loan draft in the outbox. */
        const val FORM_KEY: String = "loan"

        /**
         * Sentinel [MutationMode.Draft.uniqueKey] for the "add new loan" slot when
         * no [loanId] is supplied. Per-loan edit drafts use the loan id itself, so they never
         * collide with the add slot.
         */
        const val NEW_LOAN_KEY: String = "new"
    }
}

/**
 * Multi-field form state for the add/edit loan screen.
 *
 * Default values render a sensible "blank form" — kind defaults to PERSONAL, dates default
 * to a stable epoch so the form is deterministic in tests. The Compose layer translates
 * 0.0 → empty text-field, "Pick date" → date picker prompt.
 */
data class LoanFormState(
    val name: String = "",
    val kind: LoanKind = LoanKind.PERSONAL,
    val principal: Double = 0.0,
    val principalRemaining: Double = 0.0,
    val annualRatePercent: Double = 0.0,
    val tenureMonths: Int = 0,
    val monthsRemaining: Int = 0,
    val nextDueDate: LocalDate = DEFAULT_DUE_DATE,
    val totalPaid: Double = 0.0,
) {
    /** Minimum validity for the Save button. */
    val isValid: Boolean
        get() = name.isNotBlank() &&
            principal > 0.0 &&
            tenureMonths > 0 &&
            annualRatePercent >= 0.0

    /** Live preview of the monthly payment — surfaced read-only on the form. */
    val previewMonthlyEmi: Double
        get() = computeMonthlyEmi(principal, annualRatePercent, tenureMonths)

    /** Live preview of total interest over the loan tenure — surfaced read-only on the form. */
    val previewTotalInterest: Double
        get() = computeTotalInterest(principal, annualRatePercent, tenureMonths)

    companion object {
        /** Stable placeholder so the form is deterministic before the user picks a real date. */
        val DEFAULT_DUE_DATE: LocalDate = LocalDate(2026, 1, 1)
    }
}
