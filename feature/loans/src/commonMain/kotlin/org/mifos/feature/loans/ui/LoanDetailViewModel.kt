/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.loans.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mifos.core.data.banking.LoanRepository
import org.mifos.core.model.banking.Loan
import template.core.base.store.screen.DataFreshness
import template.core.base.store.screen.ScreenState
import template.core.base.ui.viewmodel.BaseViewModel

/**
 * Read-side ViewModel for [LoanDetailScreen].
 *
 * Observes a single loan by id and projects to [ScreenState]:
 * - Loan exists → [ScreenState.Content]
 * - Loan was never saved or has been deleted → [ScreenState.Empty]
 *
 * The [loanId] is injected via Koin `parametersOf(loanId)` (see `LoansModule`) so each
 * navigation event creates a fresh VM scoped to the route argument.
 */
class LoanDetailViewModel(
    private val repository: LoanRepository,
    private val loanId: String,
) : BaseViewModel<Unit, Nothing, LoanDetailAction>(Unit) {

    val screenState: StateFlow<ScreenState<Loan>> = repository.observeById(loanId)
        .map { loan ->
            if (loan == null) {
                ScreenState.Empty
            } else {
                ScreenState.Content(data = loan, freshness = DataFreshness.FRESH)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

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
