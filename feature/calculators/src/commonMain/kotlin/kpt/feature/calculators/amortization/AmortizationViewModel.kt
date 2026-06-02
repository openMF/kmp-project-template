/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.amortization

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.banking.LoanRepository
import kpt.core.domain.calc.AmortizationRow
import kpt.core.domain.calc.amortizationSchedule
import kpt.core.domain.calc.computeEmi
import kpt.core.model.emi.EmiResult

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
 * The amortization schedule is derived from the current input state via
 * [amortizationSchedule] — no async work, no Store.
 */
class AmortizationViewModel(
    private val repository: LoanRepository,
    private val loanId: String? = null,
) : BaseViewModel<AmortizationState, Nothing, AmortizationAction>(AmortizationState()) {

    val schedule: StateFlow<List<AmortizationRow>> = stateFlow
        .map { s -> amortizationSchedule(s.principal, s.ratePercent, s.tenureMonths) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summary: StateFlow<EmiResult> = stateFlow
        .map { s -> computeEmi(s.principal, s.ratePercent, s.tenureMonths) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            EmiResult(0.0, 0.0, 0.0),
        )

    init {
        if (loanId != null) {
            repository.observeById(loanId)
                .onEach { loan ->
                    loan?.let {
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
