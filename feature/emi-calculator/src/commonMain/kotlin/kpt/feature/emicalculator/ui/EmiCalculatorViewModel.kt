/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.emicalculator.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.domain.demo.emi.calculateEmi
import kpt.core.model.demo.emi.EmiResult

class EmiCalculatorViewModel : BaseViewModel<EmiState, Nothing, EmiAction>(EmiState()) {

    val emiResult: StateFlow<EmiResult?> = stateFlow.map { state ->
        if (state.principal > 0 && state.ratePercent > 0 && state.tenureMonths > 0) {
            calculateEmi(state.principal, state.ratePercent, state.tenureMonths)
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    override fun handleAction(action: EmiAction) = when (action) {
        is EmiAction.UpdatePrincipal -> updateState { copy(principal = action.value) }
        is EmiAction.UpdateRate -> updateState { copy(ratePercent = action.value) }
        is EmiAction.UpdateTenure -> updateState { copy(tenureMonths = action.value) }
    }
}

data class EmiState(
    val principal: Double = 100000.0,
    val ratePercent: Double = 8.5,
    val tenureMonths: Int = 12,
)

sealed class EmiAction {
    data class UpdatePrincipal(val value: Double) : EmiAction()
    data class UpdateRate(val value: Double) : EmiAction()
    data class UpdateTenure(val value: Int) : EmiAction()
}
