/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.amortizationcalc

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.banking.LoanRepository
import kpt.core.data.demo.calc.AmortizationCalcRepository
import kpt.core.model.demo.banking.Loan
import kpt.core.model.demo.calc.AmortizationBreakdown
import kpt.core.store.demo.calc.impl.AmortizationCalcParams

/**
 * VM for B3 Amortization Schedule.
 *
 * Two modes:
 * - **Loan-backed** — when [loanId] is non-null, on init the VM reads the
 *   matching loan from [LoanRepository.observeById] and pre-fills the inputs.
 *   Users can still tweak the values to model "what-if" scenarios without
 *   mutating the saved loan.
 * - **Inline** — when [loanId] is `null`, the user enters principal / rate /
 *   tenure from scratch.
 *
 * The schedule + summary come from the MEMORY_ONLY Store5 store keyed on the inputs
 * ([AmortizationCalcParams]), consumed as a `ScreenState` like every other read surface.
 */
class AmortizationViewModel(
    private val repository: LoanRepository,
    private val calcRepository: AmortizationCalcRepository,
    private val loanId: String? = null,
) : BaseViewModel<AmortizationState, Nothing, AmortizationAction>(AmortizationState()) {

    /** The stream backing the CURRENT key — retained so [onRetry] re-runs the live one. */
    private var currentStream: ScreenDataStream<AmortizationBreakdown>? = null

    /**
     * Schedule AND summary as ONE Store-backed `ScreenState`.
     *
     * They were two independently-derived `StateFlow`s off the form state; both come from the
     * same inputs, so they are one Store value now — the screen can never show a schedule from
     * one parameter set beside a summary from another, and a 240-row schedule is no longer
     * rebuilt from scratch on every keystroke that returns to a previous value.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val breakdownState: StateFlow<ScreenState<AmortizationBreakdown>> = stateFlow
        .map { AmortizationCalcParams(it.principal, it.ratePercent, it.tenureMonths) }
        .distinctUntilChanged()
        .flatMapLatest { params ->
            if (params.isComputable) {
                calcRepository.breakdownStream(params, viewModelScope)
                    .also { currentStream = it }
                    .state
            } else {
                // Incomplete inputs are Empty, not Error — the user hasn't finished typing.
                currentStream = null
                flowOf(ScreenState.Empty)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    fun onRetry() {
        currentStream?.retry()
    }

    init {
        if (loanId != null) {
            // Read the source loan THROUGH the store-backed detail stream, not a raw
            // `repository.observeById`. The store path already existed
            // (`provideLoanDetailStore` → `loanDetailStream`) and was simply not used here.
            // Only Content prefills; Loading/Empty/Error leave the user's own inputs
            // untouched — same behaviour as the previous null-guarded observeById.
            repository.loanDetailStream(loanId, viewModelScope).state
                .onEach { screenState ->
                    (screenState as? ScreenState.Content<Loan>)?.data?.let {
                        updateState {
                            copy(
                                principal = it.principal,
                                ratePercent = it.annualRatePercent,
                                tenureMonths = it.tenureMonths,
                                sourceLoanId = it.id,
                                sourceLoanName = it.name,
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    override fun handleAction(action: AmortizationAction) = when (action) {
        is AmortizationAction.UpdatePrincipal ->
            updateState { copy(principal = action.value) }
        is AmortizationAction.UpdateRate ->
            updateState { copy(ratePercent = action.value) }
        is AmortizationAction.UpdateTenure ->
            updateState { copy(tenureMonths = action.value) }
    }
}

data class AmortizationState(
    val principal: Double = 100_000.0,
    val ratePercent: Double = 8.5,
    val tenureMonths: Int = 60,
    val sourceLoanId: String? = null,
    val sourceLoanName: String? = null,
)

sealed class AmortizationAction {
    data class UpdatePrincipal(val value: Double) : AmortizationAction()
    data class UpdateRate(val value: Double) : AmortizationAction()
    data class UpdateTenure(val value: Int) : AmortizationAction()
}
