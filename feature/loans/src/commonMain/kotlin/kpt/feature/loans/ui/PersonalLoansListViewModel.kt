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
import kotlinx.coroutines.launch
import kpt.core.base.store.screen.DataFreshness
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.banking.LoanRepository
import kpt.core.model.banking.Loan

/**
 * Read-side ViewModel for [PersonalLoansListScreen].
 *
 * Combines `observeAll()`, `observeTotalMonthlyEmi()`, and `observeTotalPrincipalRemaining()`
 * from [LoanRepository] into a single [LoansListUiState] surface, projected as a
 * [ScreenState]. Empty portfolio → [ScreenState.Empty]; any loans → [ScreenState.Content].
 *
 * `DataFreshness.FRESH` is used unconditionally because the data is purely local — there is
 * no network fetch to be STALE about. Future remote-sync forks should swap to a Store5-backed
 * stream that carries real freshness.
 */
class PersonalLoansListViewModel(
    private val repository: LoanRepository,
) : BaseViewModel<Unit, Nothing, LoansListAction>(Unit) {

    val screenState: StateFlow<ScreenState<LoansListUiState>> = combine(
        repository.observeAll(),
        repository.observeTotalMonthlyEmi(),
        repository.observeTotalPrincipalRemaining(),
    ) { loans, totalEmi, totalRemaining ->
        if (loans.isEmpty()) {
            ScreenState.Empty
        } else {
            ScreenState.Content(
                data = LoansListUiState(
                    loans = loans,
                    totalMonthlyEmi = totalEmi,
                    totalPrincipalRemaining = totalRemaining,
                ),
                freshness = DataFreshness.FRESH,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    override fun handleAction(action: LoansListAction) {
        when (action) {
            is LoansListAction.DeleteLoan -> viewModelScope.launch {
                repository.delete(action.id)
            }
        }
    }

    /** Convenience: delete a loan from the screen without dropping into the action channel. */
    fun onDeleteLoan(id: String) {
        trySendAction(LoansListAction.DeleteLoan(id))
    }
}

/**
 * Aggregated read-model for the loans list screen: the user-visible loans plus the consolidated
 * totals tile rendered at the top of the screen.
 */
data class LoansListUiState(
    val loans: List<Loan>,
    val totalMonthlyEmi: Double,
    val totalPrincipalRemaining: Double,
)

/** Action sealed-hierarchy for the loans-list MVI loop. */
sealed interface LoansListAction {
    /** User confirmed deletion of [id] (typically via long-press / swipe-to-dismiss). */
    data class DeleteLoan(val id: String) : LoansListAction
}
