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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kpt.core.base.store.combine.CombinedState
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.banking.LoanRepository
import kpt.core.model.demo.banking.Loan

/**
 * Read-side ViewModel for [LoanDetailScreen].
 *
 * Observes a single loan by id and projects to [ScreenState]:
 * - Loan exists → [ScreenState.Content]
 * - Loan was never saved or has been deleted → [ScreenState.Empty]
 *
 * The [loanId] is injected via Koin `parametersOf(loanId)` (see `LoansModule`) so each
 * navigation event creates a fresh VM scoped to the route argument.
 *
 * **Phase 5 archetype additions (showcase only — do not remove):**
 *
 * [loadOnceScreenState] demonstrates the **asLoadOnceStream** pattern: the stream stops
 * emitting after the first [ScreenState.Content] so in-progress user edits are never
 * overwritten by background refreshes from Room's reactive query. This is the recommended
 * pattern for any edit-capable detail screen backed by a reactive local store.
 *
 * [combinedState] demonstrates the **ScreenWithMutationStream** seam: the read-only
 * [loadOnceScreenState] is fused with a [SubmitHandler] write pipeline into one
 * [StateFlow<CombinedState<Loan, Loan>>], so the screen renders against a single value
 * covering Loading/Content/Error on the read side and Idle/Submitting/Submitted/Failed
 * on the write side. [submitEdit] is the screen-facing API to trigger a save.
 */
class LoanDetailViewModel(
    private val repository: LoanRepository,
    private val loanId: String,
) : BaseViewModel<Unit, Nothing, LoanDetailAction>(Unit) {

    /**
     * Continuous reactive stream — emits every time the loan row changes in Room. Consumes the
     * repository's per-loan [kpt.core.base.store.screen.ScreenDataStream] (absent id → Empty); the
     * screen's read-only detail view renders against `.state`.
     */
    private val detailStream = repository.loanDetailStream(loanId, viewModelScope)

    val screenState: StateFlow<ScreenState<Loan>> = detailStream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    /**
     * **Archetype: asLoadOnceStream**
     *
     * Stops after the first [ScreenState.Content] emission so background Room updates
     * (triggered by concurrent writes elsewhere in the app, or by the offline syncer)
     * do NOT overwrite in-progress user edits on the detail/edit screen.
     *
     * Semantics mirror [kpt.core.base.store.screen.asLoadOnceStream]: the upstream
     * flow is `transformWhile`-terminated on first Content — Loading and Empty continue
     * to propagate (retry surfaces remain visible) but once Content lands the collector
     * stops. This is the recommended pattern for all edit-capable screens whose source
     * is a reactive local store (Room DAOs, DataStore, etc.).
     *
     * Note: this applies [transformWhile] over the repository's per-loan `ScreenDataStream.state`
     * (terminate on first Content); the semantics mirror
     * [kpt.core.base.store.screen.asLoadOnceStream].
     */
    val loadOnceScreenState: StateFlow<ScreenState<Loan>> = detailStream.state
        .transformWhile { state ->
            emit(state)
            // Stop the upstream flow after the first Content — further Room emissions
            // are ignored so in-progress edits are not overwritten.
            state !is ScreenState.Content
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    /**
     * **Archetype: ScreenWithMutationStream**
     *
     * Fuses [loadOnceScreenState] (read side) with a [submitHandler] (write side) into
     * a single [StateFlow<CombinedState>] so the screen renders against one value:
     * - [CombinedState.read] — read-side ScreenState (Loading / Content / Empty)
     * - [CombinedState.mutation] — write-side SubmitState (Idle / Submitting / Submitted / Failed)
     *
     * The [ScreenWithMutationStream] contract pattern is the recommended way to wire a
     * read+write screen in the toolkit. This implementation uses the manual `combine`
     * approach because the loan data comes from a DAO flow rather than a Store, so
     * [kpt.core.base.store.infra.StoreFactory.createScreenWithMutation] cannot be
     * used directly. For Store-backed entities, prefer `StoreFactory.createScreenWithMutation`.
     */
    private val editSubmitHandler = viewModelScope.submitHandler<Loan>()

    val combinedState: StateFlow<CombinedState<Loan, Loan>> = combine(
        loadOnceScreenState,
        editSubmitHandler.state,
    ) { read, mutation ->
        CombinedState(read = read, mutation = mutation)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CombinedState(read = ScreenState.Loading, mutation = SubmitState.Idle),
    )

    /**
     * Submit an edit of the current loan. Transitions [combinedState.mutation] through
     * Submitting → Submitted on success, or Submitting → Failed on error.
     *
     * The actual persistence is a local Room upsert (purely offline — no network call).
     * Forks swapping in a remote API call replace the `repository.upsert(loan)` block.
     */
    fun submitEdit(loan: Loan) {
        editSubmitHandler.submit {
            repository.upsert(loan)
            loan
        }
    }

    /** Dismiss any terminal mutation state (Submitted / Failed) → resets to Idle. */
    fun dismissEditResult() {
        editSubmitHandler.reset()
    }

    override fun handleAction(action: LoanDetailAction) {
        when (action) {
            LoanDetailAction.Delete -> viewModelScope.launch {
                repository.delete(loanId)
            }
        }
    }

    /** Convenience: delete this loan (called from the screen's action button / dialog). */
    fun onDelete() {
        trySendAction(LoanDetailAction.Delete)
    }
}

/** Action sealed-hierarchy for the loan-detail MVI loop. */
sealed interface LoanDetailAction {
    /** User confirmed deletion of the currently-viewed loan. */
    data object Delete : LoanDetailAction
}
