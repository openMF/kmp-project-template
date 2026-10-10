/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.affordability

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.domain.calc.AffordabilityResult
import kpt.core.domain.calc.maxAffordableLoan

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

/** The calculator's inputs. Pure local state — nothing is persisted or fetched, so the state IS the screen. */
data class AffordabilityState(
    /**
     * Gross monthly income, before tax and before [monthlyObligations] are subtracted. The default is a placeholder,
     * not a recommendation.
     */
    val monthlyIncome: Double = 5_000.0,
    /** Existing monthly commitments, subtracted before the ratio is applied. */
    val monthlyObligations: Double = 500.0,
    /** Debt-to-income ceiling as a fraction, e.g. `0.4` for 40%. */
    val dtiRatio: Double = 0.4,
    /** APR to assume, as a percentage. */
    val ratePercent: Double = 7.0,
    /**
     * Tenure to assume, in months. 240 (20 years) by default, because affordability is usually explored at mortgage
     * length rather than at a personal-loan length.
     */
    val tenureMonths: Int = 240,
)

/** One action per input, so a change is a single well-typed event rather than a whole-state replacement. */
sealed class AffordabilityAction {
    /**
     * Income changed.
     *
     * @property value the new value.
     */
    data class UpdateIncome(val value: Double) : AffordabilityAction()

    /**
     * Obligations changed.
     *
     * @property value the new value.
     */
    data class UpdateObligations(val value: Double) : AffordabilityAction()

    /**
     * The debt-to-income ceiling changed.
     *
     * @property value the new value.
     */
    data class UpdateDti(val value: Double) : AffordabilityAction()

    /**
     * The assumed rate changed.
     *
     * @property value the new value.
     */
    data class UpdateRate(val value: Double) : AffordabilityAction()

    /**
     * The assumed tenure changed.
     *
     * @property value the new value.
     */
    data class UpdateTenure(val value: Int) : AffordabilityAction()
}
