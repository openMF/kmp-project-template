/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.amortization.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.banking.LoanRepository
import kpt.core.model.demo.banking.AmortizationRow
import kpt.core.model.demo.banking.Loan

/**
 * ViewModel for [AmortizationScheduleScreen].
 *
 * Derives a complete month-by-month payment schedule from the current loan snapshot
 * held in [LoanRepository]. The computation is purely local — no network call,
 * no Room insert — so it uses [ScreenState] via a simple `observeById` → `map` pipeline
 * (the OFFLINE_LOCAL_ONLY archetype).
 *
 * **Why not PagingScreenStream?**
 * [kpt.core.base.store.paging.PagingScreenStream] is designed for unbounded
 * network-paginated lists. Amortization rows are finite (≤ 360 for a 30-year mortgage),
 * computed in microseconds, and never fetched from a network. `LazyColumn` handles the
 * rendering lazily at draw time — there is nothing to paginate at the data layer.
 * Forcing `PagingScreenStream` here would add network-monitor + fetched-at-repository
 * dependencies for zero benefit.
 *
 * The [loanId] is injected via Koin `parametersOf(loanId)` so each route creates a fresh
 * ViewModel scoped to the navigation entry.
 */
class AmortizationScheduleViewModel(
    private val repository: LoanRepository,
    private val loanId: String,
) : BaseViewModel<Unit, Nothing, Nothing>(Unit) {

    override fun handleAction(action: Nothing): Unit = Unit

    val screenState: StateFlow<ScreenState<List<AmortizationRow>>> =
        repository.observeById(loanId)
            .map { loan ->
                when {
                    loan == null -> ScreenState.Empty
                    loan.monthsRemaining <= 0 -> ScreenState.Empty
                    else -> ScreenState.Content(
                        data = computeSchedule(loan),
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ScreenState.Loading,
            )
}

/**
 * Standard reducing-balance amortization schedule.
 *
 * Formula per row:
 * - interest   = balance × (annualRate / 12)
 * - principal  = min(EMI − interest, balance)   [last row clamps to avoid overshoot]
 * - balance    = max(0, balance − principal)
 *
 * Zero-interest edge case: all of each payment is principal, balance reduces linearly.
 */
internal fun computeSchedule(loan: Loan): List<AmortizationRow> {
    val monthlyRate = loan.annualRatePercent / 100.0 / 12.0
    val emi = loan.monthlyPayment
    var balance = loan.principalRemaining
    return buildList {
        repeat(loan.monthsRemaining) { index ->
            val interest = if (monthlyRate > 0.0) balance * monthlyRate else 0.0
            val principal = minOf(emi - interest, balance)
            balance = maxOf(0.0, balance - principal)
            add(
                AmortizationRow(
                    month = index + 1,
                    payment = emi,
                    principal = principal,
                    interest = interest,
                    balance = balance,
                ),
            )
        }
    }
}
