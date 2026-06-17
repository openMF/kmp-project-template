/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.banking

import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kpt.core.data.banking.impl.LoanRepositoryImpl
import kpt.core.model.banking.Loan
import kpt.core.model.banking.LoanKind
import kpt.core.store.banking.impl.provideLoansStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Locks the [LoanRepository] contract:
 * - DAO round-trip preserves all fields.
 * - Flow projections (observe* methods) emit the domain type.
 * - Computed flows ([LoanRepository.observeTotalMonthlyEmi],
 *   [LoanRepository.observeTotalPrincipalRemaining]) reduce across all loans.
 */
class LoanRepositoryTest {

    private val dao = FakeLoanDao()
    private val repo: LoanRepository = LoanRepositoryImpl(
        loansStore = provideLoansStore(dao),
        loanDao = dao,
    )

    @Test
    fun upsertThenObserveAllRoundTripsTheDomainObject() = runTest {
        val loan = sampleLoan(id = "L1")
        repo.upsert(loan)
        assertEquals(listOf(loan), repo.observeAll().first())
    }

    @Test
    fun observeAllSortsBySoonestDueFirst() = runTest {
        repo.upsert(sampleLoan(id = "late", nextDueDate = LocalDate(2026, 12, 1)))
        repo.upsert(sampleLoan(id = "early", nextDueDate = LocalDate(2026, 1, 1)))
        repo.upsert(sampleLoan(id = "mid", nextDueDate = LocalDate(2026, 6, 1)))

        val ids = repo.observeAll().first().map { it.id }
        assertEquals(listOf("early", "mid", "late"), ids)
    }

    @Test
    fun observeByIdEmitsLoanThenNullAfterDelete() = runTest {
        val loan = sampleLoan(id = "L1")
        repo.upsert(loan)
        repo.observeById("L1").test {
            assertEquals(loan, awaitItem())
            repo.delete("L1")
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getByIdReturnsNullForUnknown() = runTest {
        assertNull(repo.getById("missing"))
    }

    @Test
    fun observeTotalMonthlyEmiSumsEveryLoan() = runTest {
        repo.upsert(sampleLoan(id = "L1", monthlyPayment = 1_500.0))
        repo.upsert(sampleLoan(id = "L2", monthlyPayment = 750.0))
        repo.upsert(sampleLoan(id = "L3", monthlyPayment = 100.50))

        assertEquals(2_350.50, repo.observeTotalMonthlyEmi().first())
    }

    @Test
    fun observeTotalMonthlyEmiIsZeroForEmptyPortfolio() = runTest {
        assertEquals(0.0, repo.observeTotalMonthlyEmi().first())
    }

    @Test
    fun observeTotalPrincipalRemainingSumsEveryLoan() = runTest {
        repo.upsert(sampleLoan(id = "L1", principalRemaining = 100_000.0))
        repo.upsert(sampleLoan(id = "L2", principalRemaining = 25_000.0))
        repo.upsert(sampleLoan(id = "L3", principalRemaining = 0.0))

        assertEquals(125_000.0, repo.observeTotalPrincipalRemaining().first())
    }

    @Test
    fun observeCountReflectsInsertsAndDeletes() = runTest {
        repo.observeCount().test {
            assertEquals(0, awaitItem())
            repo.upsert(sampleLoan(id = "L1"))
            assertEquals(1, awaitItem())
            repo.upsert(sampleLoan(id = "L2"))
            assertEquals(2, awaitItem())
            repo.delete("L1")
            assertEquals(1, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun computedFlowsAreReactiveToUpserts() = runTest {
        repo.observeTotalPrincipalRemaining().test {
            assertEquals(0.0, awaitItem())
            repo.upsert(sampleLoan(id = "L1", principalRemaining = 50_000.0))
            assertEquals(50_000.0, awaitItem())
            // Update L1 to a lower balance — sum recomputes.
            repo.upsert(sampleLoan(id = "L1", principalRemaining = 30_000.0))
            assertEquals(30_000.0, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun sampleLoan(
        id: String,
        kind: LoanKind = LoanKind.MORTGAGE,
        principal: Double = 250_000.0,
        principalRemaining: Double = 200_000.0,
        monthlyPayment: Double = 1_580.17,
        nextDueDate: LocalDate = LocalDate(2026, 6, 1),
    ): Loan = Loan(
        id = id,
        name = "Loan $id",
        kind = kind,
        principal = principal,
        principalRemaining = principalRemaining,
        annualRatePercent = 6.5,
        tenureMonths = 360,
        monthsRemaining = 300,
        monthlyPayment = monthlyPayment,
        nextDueDate = nextDueDate,
        totalPaid = principal - principalRemaining,
        createdAtMs = 1_700_000_000_000L,
        updatedAtMs = 1_700_000_000_000L,
    )
}
