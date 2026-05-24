/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.loans.ui

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import template.core.base.store.screen.ScreenState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalLoansListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emptyRepositoryProducesEmptyScreenState() = runTest {
        val repo = FakeLoanRepository()
        val vm = PersonalLoansListViewModel(repo)

        vm.screenState.test {
            // Initial state is Loading; the first downstream emission produces Empty.
            assertEquals(ScreenState.Loading, awaitItem())
            assertEquals(ScreenState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loanedRepositoryProducesContentWithConsolidatedTotals() = runTest {
        val repo = FakeLoanRepository()
        repo.upsert(
            sampleLoan(
                id = "L1",
                monthlyPayment = 500.0,
                principalRemaining = 10_000.0,
                nextDueDate = LocalDate(2026, 1, 1),
            ),
        )
        repo.upsert(
            sampleLoan(
                id = "L2",
                monthlyPayment = 250.0,
                principalRemaining = 4_000.0,
                nextDueDate = LocalDate(2026, 2, 1),
            ),
        )
        repo.upsert(
            sampleLoan(
                id = "L3",
                monthlyPayment = 125.50,
                principalRemaining = 1_500.0,
                nextDueDate = LocalDate(2026, 3, 1),
            ),
        )

        val vm = PersonalLoansListViewModel(repo)

        vm.screenState.test {
            // Skip the initial Loading; await the first Content.
            var current = awaitItem()
            while (current !is ScreenState.Content<LoansListUiState>) {
                current = awaitItem()
            }
            val ui = current.data
            assertEquals(3, ui.loans.size)
            assertEquals(listOf("L1", "L2", "L3"), ui.loans.map { it.id }) // sorted by due
            assertEquals(875.50, ui.totalMonthlyEmi)
            assertEquals(15_500.0, ui.totalPrincipalRemaining)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteLoanRemovesItFromScreenState() = runTest {
        val repo = FakeLoanRepository()
        repo.upsert(sampleLoan(id = "keep"))
        repo.upsert(sampleLoan(id = "drop"))
        val vm = PersonalLoansListViewModel(repo)

        // Drive the action then advance.
        vm.onDeleteLoan("drop")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repo.current.size)
        assertEquals("keep", repo.current.first().id)
    }

    @Test
    fun observeAllSortsBySoonestDueFirst() = runTest {
        val repo = FakeLoanRepository()
        repo.upsert(sampleLoan(id = "late", nextDueDate = LocalDate(2027, 1, 1)))
        repo.upsert(sampleLoan(id = "early", nextDueDate = LocalDate(2026, 1, 1)))
        repo.upsert(sampleLoan(id = "mid", nextDueDate = LocalDate(2026, 6, 1)))

        val vm = PersonalLoansListViewModel(repo)

        vm.screenState.test {
            var current = awaitItem()
            while (current !is ScreenState.Content<LoansListUiState>) {
                current = awaitItem()
            }
            val ids = current.data.loans.map { it.id }
            assertEquals(listOf("early", "mid", "late"), ids)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun screenStateContainsFreshContentFreshness() = runTest {
        val repo = FakeLoanRepository()
        repo.upsert(sampleLoan(id = "L1"))
        val vm = PersonalLoansListViewModel(repo)

        vm.screenState.test {
            var current = awaitItem()
            while (current !is ScreenState.Content<LoansListUiState>) {
                current = awaitItem()
            }
            val content = assertIs<ScreenState.Content<LoansListUiState>>(current)
            assertTrue(content.freshness.name == "FRESH")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
