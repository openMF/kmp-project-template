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
import kpt.core.domain.calc.computeEmi
import kpt.core.model.emi.EmiResult

/**
 * VM for B6 Loan Comparison.
 *
 * Manages exactly 3 loan scenarios side-by-side and emits a derived analysis
 * (per-scenario [EmiResult] + index of the cheapest by total payable).
 *
 * The "save scenario" feature wires into [kpt.core.data.banking.LoanRepository]
 * (see B6 plan) — left to the screen layer to bridge: convert a scenario to a
 * `Loan` and call `upsert`.
 */
class LoanComparisonViewModel :
    BaseViewModel<LoanComparisonState, Nothing, LoanComparisonAction>(LoanComparisonState()) {

    /**
     * Derived comparison — per-scenario EMI results plus which is cheapest. Recomputed from [stateFlow], so it can
     * never disagree with the inputs on screen.
     */
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

    /** Shape of the comparison. */
    companion object {
        /** How many scenarios are compared. Fixed, because the screen lays them out side by side. */
        const val SCENARIO_COUNT: Int = 3
    }
}

/** One loan being compared. */
data class LoanScenario(
    /** Loan amount. */
    val principal: Double = 100_000.0,
    /** APR as a percentage. */
    val ratePercent: Double = 7.5,
    /**
     * Tenure in months. Staggered across the three default scenarios so the comparison shows a real difference before
     * anything is edited.
     */
    val tenureMonths: Int = 60,
)

/** The scenarios under comparison. */
data class LoanComparisonState(
    /** The scenarios, staggered by default so the screen is meaningful before anything is edited. */
    val scenarios: List<LoanScenario> = List(LoanComparisonViewModel.SCENARIO_COUNT) {
        // Stagger defaults so the comparison is meaningful out-of-the-box.
        LoanScenario(
            principal = 100_000.0,
            ratePercent = 6.5 + it.toDouble(),
            tenureMonths = 60 + it * 60,
        )
    },
)

/**
 * The computed comparison: one EMI result per scenario plus which is cheapest. Derived state — never stored, so it
 * cannot fall out of step with the scenarios on screen.
 */
data class LoanComparisonAnalysis(
    /** Per-scenario EMI results, in the same order as the scenarios. */
    val results: List<EmiResult>,
    /** Index of the cheapest scenario by total payable, or -1 if none have valid inputs. */
    val cheapestIndex: Int,
)

/** What the comparison can be asked to do. */
sealed class LoanComparisonAction {
    /**
     * Replaces one scenario. Carries the index because the scenarios are positional — the screen's columns are their
     * identity.
     *
     * @property index which column to replace — the scenarios are positional.
     * @property scenario the replacement.
     */
    data class UpdateScenario(val index: Int, val scenario: LoanScenario) : LoanComparisonAction()
}
