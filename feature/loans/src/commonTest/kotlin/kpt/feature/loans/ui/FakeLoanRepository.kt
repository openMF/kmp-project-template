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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
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

    // NOT an interface member any more: LoanRepository dropped observeAll() (it duplicated
    // the store-backed loansStream read path). Kept as a plain test helper.
    fun observeAll(): Flow<List<Loan>> = state.map { rows ->
        rows.sortedWith(compareBy({ it.nextDueDate }, { it.createdAtMs }))
    }

    override fun loansStream(scope: CoroutineScope): ScreenDataStream<List<Loan>> =
        // Mirror production: the DAO sorts `nextDueDate ASC, createdAtMs ASC`, so the stream must too.
        screenDataStreamForTesting(
            state.map { rows ->
                if (rows.isEmpty()) {
                    ScreenState.Empty
                } else {
                    ScreenState.Content(rows.sortedWith(compareBy({ it.nextDueDate }, { it.createdAtMs })))
                }
            },
        )

    override fun loanDetailStream(id: String, scope: CoroutineScope): ScreenDataStream<Loan> =
        screenDataStreamForTesting(
            state.map { rows ->
                rows.firstOrNull { it.id == id }?.let { ScreenState.Content(it) } ?: ScreenState.Empty
            },
        )

    fun observeById(id: String): Flow<Loan?> = state.map { rows -> rows.firstOrNull { it.id == id } }

    suspend fun getById(id: String): Loan? = state.value.firstOrNull { it.id == id }

    override suspend fun upsert(loan: Loan) {
        state.value = state.value.filterNot { it.id == loan.id } + loan
    }

    override suspend fun delete(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }

    fun observeTotalMonthlyEmi(): Flow<Double> = state.map { rows -> rows.sumOf { it.monthlyPayment } }

    fun observeTotalPrincipalRemaining(): Flow<Double> =
        state.map { rows -> rows.sumOf { it.principalRemaining } }

    fun observeCount(): Flow<Int> = state.map { it.size }
}
