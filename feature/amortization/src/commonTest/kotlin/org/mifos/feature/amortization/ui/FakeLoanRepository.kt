/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.amortization.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.mifos.core.data.banking.LoanRepository
import org.mifos.core.model.banking.Loan

internal class FakeLoanRepository : LoanRepository {

    private val state = MutableStateFlow<List<Loan>>(emptyList())

    fun seed(vararg loans: Loan) {
        state.value = loans.toList()
    }

    override fun observeAll(): Flow<List<Loan>> = state

    override fun observeById(id: String): Flow<Loan?> =
        state.map { rows -> rows.firstOrNull { it.id == id } }

    override suspend fun getById(id: String): Loan? =
        state.value.firstOrNull { it.id == id }

    override suspend fun upsert(loan: Loan) {
        state.value = state.value.filterNot { it.id == loan.id } + loan
    }

    override suspend fun delete(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }

    override fun observeTotalMonthlyEmi(): Flow<Double> =
        state.map { rows -> rows.sumOf { it.monthlyPayment } }

    override fun observeTotalPrincipalRemaining(): Flow<Double> =
        state.map { rows -> rows.sumOf { it.principalRemaining } }

    override fun observeCount(): Flow<Int> = state.map { it.size }
}
