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
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.banking.LoanRepository
import kpt.core.model.demo.banking.Loan

/**
 * Read-side ViewModel for [PersonalLoansListScreen].
 *
 * Consumes the repository's offline-local [LoanRepository.loansStream]
 * ([kpt.core.base.store.screen.ScreenDataStream]) `.state` and folds in the two dashboard totals
 * (`observeTotalMonthlyEmi` / `observeTotalPrincipalRemaining`) via [combineContent] to build the
 * [LoansListUiState]. The Store decides Loading/Empty/Content (empty portfolio → Empty); the VM
 * never hand-lifts a Flow into ScreenState.
 */
class PersonalLoansListViewModel(
    private val repository: LoanRepository,
) : BaseViewModel<Unit, Nothing, LoansListAction>(Unit) {

    private val stream = repository.loansStream(viewModelScope)

    private val totals = combine(
        repository.observeTotalMonthlyEmi(),
        repository.observeTotalPrincipalRemaining(),
    ) { emi, remaining -> emi to remaining }

    val screenState: StateFlow<ScreenState<LoansListUiState>> = stream.state
        .combineContent(totals) { loans, (totalEmi, totalRemaining), _ ->
            LoansListUiState(
                loans = loans,
                totalMonthlyEmi = totalEmi,
                totalPrincipalRemaining = totalRemaining,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    /** Re-run the read (no-op refresh for the offline-local store; wired for ScreenContent). */
    fun onRetry() = stream.retry()

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
