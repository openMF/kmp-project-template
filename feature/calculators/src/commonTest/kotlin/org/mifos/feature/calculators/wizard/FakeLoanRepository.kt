/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.calculators.wizard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.mifos.core.data.banking.LoanRepository
import org.mifos.core.model.banking.Loan

/**
 * In-memory fake of [LoanRepository] for wizard tests. Captures upserts so a
 * test can assert that the final wizard step writes through to the repository.
 */
internal class FakeLoanRepository : LoanRepository {

    private val state = MutableStateFlow<Map<String, Loan>>(emptyMap())

    /** Last successful upsert — convenient for assertions. */
    var lastUpserted: Loan? = null
        private set

    override fun observeAll(): Flow<List<Loan>> =
        state.map { it.values.toList() }

    override fun observeById(id: String): Flow<Loan?> =
        state.map { it[id] }

    override suspend fun getById(id: String): Loan? = state.value[id]

    override suspend fun upsert(loan: Loan) {
        state.value = state.value + (loan.id to loan)
        lastUpserted = loan
    }

    override suspend fun delete(id: String) {
        state.value = state.value - id
    }

    override fun observeTotalMonthlyEmi(): Flow<Double> =
        state.map { rows -> rows.values.sumOf { it.monthlyPayment } }

    override fun observeTotalPrincipalRemaining(): Flow<Double> =
        state.map { rows -> rows.values.sumOf { it.principalRemaining } }

    override fun observeCount(): Flow<Int> = state.map { it.size }
}
