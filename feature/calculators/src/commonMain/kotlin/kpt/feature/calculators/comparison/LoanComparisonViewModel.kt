/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.comparison

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.domain.demo.calc.computeEmi
import kpt.core.model.demo.emi.EmiResult

/**
 * VM for B6 Loan Comparison.
 *
 * Manages exactly 3 loan scenarios side-by-side and emits a derived analysis
 * (per-scenario [EmiResult] + index of the cheapest by total payable).
 *
 * The "save scenario" feature wires into [kpt.core.data.demo.banking.LoanRepository]
 * (see B6 plan) — left to the screen layer to bridge: convert a scenario to a
 * `Loan` and call `upsert`.
 */
class LoanComparisonViewModel :
    BaseViewModel<LoanComparisonState, Nothing, LoanComparisonAction>(LoanComparisonState()) {

    val analysis: StateFlow<LoanComparisonAnalysis> = stateFlow
        .map { s ->
            val results = s.scenarios.map { sc ->
                computeEmi(sc.principal, sc.ratePercent, sc.tenureMonths)
            }
            val bestIdx = results
                .mapIndexed { idx, r -> idx to r.totalPayment }
                // Skip zero-result scenarios (incomplete inputs) — they would otherwise
                // tie for cheapest at 0.0.
                .filter { it.second > 0.0 }
                .minByOrNull { it.second }
                ?.first ?: -1
            LoanComparisonAnalysis(results = results, cheapestIndex = bestIdx)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LoanComparisonAnalysis(
                results = List(SCENARIO_COUNT) { EmiResult(0.0, 0.0, 0.0) },
                cheapestIndex = -1,
            ),
        )

    override fun handleAction(action: LoanComparisonAction) = when (action) {
        is LoanComparisonAction.UpdateScenario -> updateState {
            copy(
                scenarios = scenarios.mapIndexed { idx, current ->
                    if (idx == action.index) action.scenario else current
                },
            )
        }
    }

    companion object {
        const val SCENARIO_COUNT: Int = 3
    }
}

data class LoanScenario(
    val principal: Double = 100_000.0,
    val ratePercent: Double = 7.5,
    val tenureMonths: Int = 60,
)

data class LoanComparisonState(
    val scenarios: List<LoanScenario> = List(LoanComparisonViewModel.SCENARIO_COUNT) {
        // Stagger defaults so the comparison is meaningful out-of-the-box.
        LoanScenario(
            principal = 100_000.0,
            ratePercent = 6.5 + it.toDouble(),
            tenureMonths = 60 + it * 60,
        )
    },
)

data class LoanComparisonAnalysis(
    val results: List<EmiResult>,
    /** Index of the cheapest scenario by total payable, or -1 if none have valid inputs. */
    val cheapestIndex: Int,
)

sealed class LoanComparisonAction {
    data class UpdateScenario(val index: Int, val scenario: LoanScenario) : LoanComparisonAction()
}
