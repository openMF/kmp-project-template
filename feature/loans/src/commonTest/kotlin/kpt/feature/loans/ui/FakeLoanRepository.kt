/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.feature.loans.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.banking.LoanRepository
import kpt.core.model.demo.banking.Loan

/**
 * In-memory [LoanRepository] for feature-level VM tests.
 *
 * Mirrors the production sort (`nextDueDate asc, then createdAtMs asc`) so VM tests can
 * assert on ordering without standing up a real Room database.
 */
internal class FakeLoanRepository : LoanRepository {

    private val state = MutableStateFlow<List<Loan>>(emptyList())

    /** Snapshot accessor for tests. */
    val current: List<Loan> get() = state.value

    override fun observeAll(): Flow<List<Loan>> = state.map { rows ->
        rows.sortedWith(compareBy({ it.nextDueDate }, { it.createdAtMs }))
    }

    override fun loansStream(scope: CoroutineScope): ScreenDataStream<List<Loan>> =
        screenDataStreamForTesting(state.map { if (it.isEmpty()) ScreenState.Empty else ScreenState.Content(it) })

    override fun loanDetailStream(id: String, scope: CoroutineScope): ScreenDataStream<Loan> =
        screenDataStreamForTesting(
            state.map { rows -> rows.firstOrNull { it.id == id }?.let { ScreenState.Content(it) } ?: ScreenState.Empty },
        )

    override fun observeById(id: String): Flow<Loan?> = state.map { rows -> rows.firstOrNull { it.id == id } }

    override suspend fun getById(id: String): Loan? = state.value.firstOrNull { it.id == id }

    override suspend fun upsert(loan: Loan) {
        state.value = state.value.filterNot { it.id == loan.id } + loan
    }

    override suspend fun delete(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }

    override fun observeTotalMonthlyEmi(): Flow<Double> = state.map { rows -> rows.sumOf { it.monthlyPayment } }

    override fun observeTotalPrincipalRemaining(): Flow<Double> =
        state.map { rows -> rows.sumOf { it.principalRemaining } }

    override fun observeCount(): Flow<Int> = state.map { it.size }
}
