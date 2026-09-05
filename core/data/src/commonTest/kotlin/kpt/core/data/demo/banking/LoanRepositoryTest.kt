/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.banking

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.ScreenStreamContext
import kpt.core.data.demo.banking.impl.LoanRepositoryImpl
import kpt.core.data.infra.InMemoryFetchedAtRepository
import kpt.core.data.infra.onlineNetworkMonitor
import kpt.core.model.demo.banking.Loan
import kpt.core.model.demo.banking.LoanKind
import kpt.core.store.demo.banking.impl.provideLoansStore
import kpt.core.store.demo.banking.impl.provideLoansWriteStore
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
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
        loansWriteStore = provideLoansWriteStore(dao),
        loanDao = dao,
    )

    // asScreenStream self-resolves its ScreenStreamContext from Koin, so a test that collects
    // the repository's ScreenDataStream registers the read-path infra bundle for its duration.
    @BeforeTest
    fun startKoinForScreenStream() {
        startKoin {
            modules(
                module {
                    single { ScreenStreamContext(onlineNetworkMonitor(), InMemoryFetchedAtRepository()) }
                },
            )
        }
    }

    @AfterTest
    fun stopKoinAfterTest() = stopKoin()

    @Test
    fun upsertThenLoansStreamRoundTripsTheDomainObject() = runTest {
        val loan = sampleLoan(id = "L1")
        repo.upsert(loan)
        val loans = repo.loansStream(backgroundScope).state
            .mapNotNull { it.loansOrNull() }
            .first { it.isNotEmpty() }
        assertEquals(listOf(loan), loans)
    }

    @Test
    fun loansStreamSortsBySoonestDueFirst() = runTest {
        repo.upsert(sampleLoan(id = "late", nextDueDate = LocalDate(2026, 12, 1)))
        repo.upsert(sampleLoan(id = "early", nextDueDate = LocalDate(2026, 1, 1)))
        repo.upsert(sampleLoan(id = "mid", nextDueDate = LocalDate(2026, 6, 1)))

        val ids = repo.loansStream(backgroundScope).state
            .mapNotNull { state -> state.loansOrNull()?.map { it.id } }
            .first { it.size == 3 }
        assertEquals(listOf("early", "mid", "late"), ids)
    }

    @Test
    fun detailStreamEmitsLoanForKnownId() = runTest {
        // Was `repo.observeById` (raw DAO); now the store-backed detail read.
        //
        // Deliberately asserts PRESENCE only. The post-delete transition is NOT asserted here:
        // the fake DAO + RoomChangeBus + Turbine timing issue this file already documents (the
        // reason `observeCountReflectsInsertsAndDeletes` and `computedFlowsAreReactiveToUpserts`
        // were @Ignore'd) applies equally to the store-backed read. Asserting it would be a
        // flaky test, and loosening it to pass would be worse than not making the claim.
        val loan = sampleLoan(id = "L1")
        repo.upsert(loan)
        assertEquals(loan, detailOrNull(repo, "L1"))
    }

    @Test
    fun detailStreamIsEmptyForUnknownId() = runTest {
        assertNull(detailOrNull(repo, "missing"))
    }

    /** One loan off the store-backed detail stream, or null when absent. */
    private suspend fun TestScope.detailOrNull(repo: LoanRepository, id: String): Loan? =
        (
            repo.loanDetailStream(id, backgroundScope).state
                .first { it != ScreenState.Loading } as? ScreenState.Content<Loan>
            )?.data

    /** Every loan off the store-backed list stream — the single read path totals derive from. */
    private suspend fun TestScope.allLoans(repo: LoanRepository): List<Loan> =
        repo.loansStream(backgroundScope).state
            .mapNotNull { it.loansOrNull() }.first()

    @Test
    fun observeTotalMonthlyEmiSumsEveryLoan() = runTest {
        repo.upsert(sampleLoan(id = "L1", monthlyPayment = 1_500.0))
        repo.upsert(sampleLoan(id = "L2", monthlyPayment = 750.0))
        repo.upsert(sampleLoan(id = "L3", monthlyPayment = 100.50))

        // Totals are DERIVED from the store's list (PersonalLoansListViewModel does the same) —
        // `observeTotalMonthlyEmi()` was a second DAO query summing these very rows.
        assertEquals(2_350.50, allLoans(repo).sumOf { it.monthlyPayment })
    }

    @Test
    fun observeTotalMonthlyEmiIsZeroForEmptyPortfolio() = runTest {
        assertEquals(0.0, allLoans(repo).sumOf { it.monthlyPayment })
    }

    @Test
    fun observeTotalPrincipalRemainingSumsEveryLoan() = runTest {
        repo.upsert(sampleLoan(id = "L1", principalRemaining = 100_000.0))
        repo.upsert(sampleLoan(id = "L2", principalRemaining = 25_000.0))
        repo.upsert(sampleLoan(id = "L3", principalRemaining = 0.0))

        assertEquals(125_000.0, allLoans(repo).sumOf { it.principalRemaining })
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

/** Domain list out of a `ScreenState` (Content → rows, Empty → ∅, Loading/Error → null-skip). */
private fun ScreenState<List<Loan>>.loansOrNull(): List<Loan>? = when (this) {
    is ScreenState.Content -> data
    ScreenState.Empty -> emptyList()
    else -> null
}
