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
import template.core.base.store.screen.ScreenState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class LoanDetailViewModelTest {

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
    fun unknownLoanIdEmitsEmpty() = runTest {
        val repo = FakeLoanRepository()
        val vm = LoanDetailViewModel(repository = repo, loanId = "missing")

        vm.screenState.test {
            assertEquals(ScreenState.Loading, awaitItem())
            assertEquals(ScreenState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun knownLoanIdEmitsContentThenEmptyAfterDelete() = runTest {
        val repo = FakeLoanRepository()
        val loan = sampleLoan(id = "L1", name = "Mortgage")
        repo.upsert(loan)
        val vm = LoanDetailViewModel(repository = repo, loanId = "L1")

        vm.screenState.test {
            // Skip Loading.
            var item = awaitItem()
            while (item is ScreenState.Loading) item = awaitItem()
            val content = assertIs<ScreenState.Content<*>>(item)
            assertEquals(loan, content.data)

            vm.onDelete()
            dispatcher.scheduler.advanceUntilIdle()
            // After deletion, the stream emits Empty.
            assertEquals(ScreenState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * **Archetype showcase: asLoadOnceStream**
     *
     * [LoanDetailViewModel.loadOnceScreenState] must stop emitting after the first
     * [ScreenState.Content] so that background Room updates never overwrite in-progress
     * user edits on the detail/edit screen.
     *
     * Verifies: after Content arrives the stream completes (no further emissions), meaning
     * a subsequent Room write does NOT push a new state downstream.
     */
    @Test
    fun loadOnce_stream_is_used() = runTest {
        val repo = FakeLoanRepository()
        val loan = sampleLoan(id = "L2", name = "Car Loan")
        repo.upsert(loan)
        val vm = LoanDetailViewModel(repository = repo, loanId = "L2")

        // loadOnceScreenState is wired and observable.
        assertNotNull(vm.loadOnceScreenState)

        vm.loadOnceScreenState.test {
            // Skip Loading emission.
            var item = awaitItem()
            while (item is ScreenState.Loading) item = awaitItem()

            // First Content lands — the stream captures the loan.
            val content = assertIs<ScreenState.Content<*>>(item)
            assertEquals(loan, content.data)

            // Now simulate a background Room update (e.g. a concurrent write).
            val updated = loan.copy(name = "Updated Car Loan")
            repo.upsert(updated)
            dispatcher.scheduler.advanceUntilIdle()

            // The stream must NOT emit the updated value — asLoadOnceStream semantics
            // guarantee termination after first Content so in-progress edits are safe.
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
