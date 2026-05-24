/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.banking.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mifos.core.data.banking.LoanRepository
import org.mifos.core.database.banking.dao.LoanDao
import org.mifos.core.database.banking.entity.LoanEntity
import org.mifos.core.model.banking.Loan

internal class LoanRepositoryImpl(
    private val dao: LoanDao,
) : LoanRepository {

    override fun observeAll(): Flow<List<Loan>> = dao.observeAll().map { rows ->
        rows.map { it.toDomain() }
    }

    override fun observeById(id: String): Flow<Loan?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): Loan? = dao.getById(id)?.toDomain()

    override suspend fun upsert(loan: Loan) {
        dao.upsert(loan.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    override fun observeTotalMonthlyEmi(): Flow<Double> = dao.observeAll().map { rows ->
        rows.sumOf { it.monthlyPayment }
    }

    override fun observeTotalPrincipalRemaining(): Flow<Double> = dao.observeAll().map { rows ->
        rows.sumOf { it.principalRemaining }
    }

    override fun observeCount(): Flow<Int> = dao.count()
}

private fun LoanEntity.toDomain(): Loan = Loan(
    id = id,
    name = name,
    kind = kind,
    principal = principal,
    principalRemaining = principalRemaining,
    annualRatePercent = annualRatePercent,
    tenureMonths = tenureMonths,
    monthsRemaining = monthsRemaining,
    monthlyPayment = monthlyPayment,
    nextDueDate = nextDueDate,
    totalPaid = totalPaid,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs,
)

private fun Loan.toEntity(): LoanEntity = LoanEntity(
    id = id,
    name = name,
    kind = kind,
    principal = principal,
    principalRemaining = principalRemaining,
    annualRatePercent = annualRatePercent,
    tenureMonths = tenureMonths,
    monthsRemaining = monthsRemaining,
    monthlyPayment = monthlyPayment,
    nextDueDate = nextDueDate,
    totalPaid = totalPaid,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs,
)
