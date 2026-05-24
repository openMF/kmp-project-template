/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.calculators.affordability

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.mifos.core.domain.calc.AffordabilityResult
import org.mifos.core.domain.calc.maxAffordableLoan
import template.core.base.ui.viewmodel.BaseViewModel

/**
 * Pure-compute VM for B5 Affordability Calculator. Demonstrates the "no Store,
 * no network" archetype: input state → derived [AffordabilityResult] flow.
 */
class AffordabilityCalculatorViewModel :
    BaseViewModel<AffordabilityState, Nothing, AffordabilityAction>(AffordabilityState()) {

    /** Live affordability result computed from the current input state. */
    val affordability: StateFlow<AffordabilityResult> = stateFlow
        .map { s ->
            maxAffordableLoan(
                monthlyIncome = s.monthlyIncome,
                monthlyObligations = s.monthlyObligations,
                dtiRatio = s.dtiRatio,
                rate = s.ratePercent,
                tenureMonths = s.tenureMonths,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = maxAffordableLoan(0.0, 0.0, 0.4, 0.0, 0),
        )

    override fun handleAction(action: AffordabilityAction) = when (action) {
        is AffordabilityAction.UpdateIncome ->
            updateState { copy(monthlyIncome = action.value) }
        is AffordabilityAction.UpdateObligations ->
            updateState { copy(monthlyObligations = action.value) }
        is AffordabilityAction.UpdateDti ->
            updateState { copy(dtiRatio = action.value) }
        is AffordabilityAction.UpdateRate ->
            updateState { copy(ratePercent = action.value) }
        is AffordabilityAction.UpdateTenure ->
            updateState { copy(tenureMonths = action.value) }
    }
}

data class AffordabilityState(
    val monthlyIncome: Double = 5_000.0,
    val monthlyObligations: Double = 500.0,
    val dtiRatio: Double = 0.4,
    val ratePercent: Double = 7.0,
    val tenureMonths: Int = 240,
)

sealed class AffordabilityAction {
    data class UpdateIncome(val value: Double) : AffordabilityAction()
    data class UpdateObligations(val value: Double) : AffordabilityAction()
    data class UpdateDti(val value: Double) : AffordabilityAction()
    data class UpdateRate(val value: Double) : AffordabilityAction()
    data class UpdateTenure(val value: Int) : AffordabilityAction()
}
