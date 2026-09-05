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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.emi.EmiCalculatorRepository
import kpt.core.model.demo.emi.EmiResult
import kpt.core.store.demo.emi.impl.EmiParams

/**
 * EMI calculator — the `calculator_pure` combo on the DYNAMIC-KEY read shape.
 *
 * The form inputs ARE the Store key, so each distinct parameter set is its own cache entry and
 * `flatMapLatest` re-streams on every change. The result reaches the screen as a `ScreenState`
 * like every other read surface, which is what lets the screen render through `ScreenContent`
 * instead of a bespoke nullable `StateFlow` with a hand-rolled `?.let` in the composable.
 */
class EmiCalculatorViewModel(
    private val repository: EmiCalculatorRepository,
) : BaseViewModel<EmiState, Nothing, EmiAction>(EmiState()) {

    /** The stream backing the CURRENT key — retained so [onRetry] re-runs the live one. */
    private var currentStream: ScreenDataStream<EmiResult>? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val emiState: StateFlow<ScreenState<EmiResult>> = stateFlow
        .map { EmiParams(it.principal, it.ratePercent, it.tenureMonths) }
        .distinctUntilChanged()
        .flatMapLatest { params ->
            if (params.isComputable) {
                repository.emiStream(params, viewModelScope)
                    .also { currentStream = it }
                    .state
            } else {
                // Incomplete inputs are Empty, not Error — the user simply hasn't finished
                // typing. Emitting Error here would render a failure for a valid in-progress form.
                currentStream = null
                flowOf(ScreenState.Empty)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScreenState.Loading)

    fun onRetry() {
        currentStream?.retry()
    }

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
