/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.amortization

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kpt.core.model.banking.Loan
import kpt.core.model.banking.LoanKind
import kpt.feature.calculators.wizard.FakeLoanRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AmortizationViewModelTest {

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
    fun inlineModePopulatesScheduleFromUserInputs() = runTest {
        val repo = FakeLoanRepository()
        val vm = AmortizationViewModel(repository = repo, loanId = null)
        vm.trySendAction(AmortizationAction.UpdatePrincipal(10_000.0))
        vm.trySendAction(AmortizationAction.UpdateRate(12.0))
        vm.trySendAction(AmortizationAction.UpdateTenure(6))
        dispatcher.scheduler.advanceUntilIdle()

        val schedule = vm.schedule.first { it.size == 6 }
        assertEquals(6, schedule.size)
    }

    @Test
    fun loanBackedModePrefillsInputsFromRepository() = runTest {
        val repo = FakeLoanRepository()
        val loan = Loan(
            id = "L42",
            name = "Home",
            kind = LoanKind.MORTGAGE,
            principal = 250_000.0,
            principalRemaining = 200_000.0,
            annualRatePercent = 6.5,
            tenureMonths = 360,
            monthsRemaining = 300,
            monthlyPayment = 1_580.17,
            nextDueDate = LocalDate(2026, 6, 1),
            totalPaid = 50_000.0,
            createdAtMs = 0L,
            updatedAtMs = 0L,
        )
        repo.upsert(loan)

        val vm = AmortizationViewModel(repository = repo, loanId = "L42")
        vm.stateFlow.test {
            // Skip the default; wait for the loan-backed update to arrive.
            val state = awaitItem().let { initial ->
                if (initial.sourceLoanId == "L42") initial else awaitItem()
            }
            assertEquals(250_000.0, state.principal)
            assertEquals(6.5, state.ratePercent)
            assertEquals(360, state.tenureMonths)
            assertEquals("Home", state.sourceLoanName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun summaryReflectsCurrentInputs() = runTest {
        val repo = FakeLoanRepository()
        val vm = AmortizationViewModel(repository = repo, loanId = null)
        vm.trySendAction(AmortizationAction.UpdatePrincipal(50_000.0))
        vm.trySendAction(AmortizationAction.UpdateRate(7.0))
        vm.trySendAction(AmortizationAction.UpdateTenure(60))
        dispatcher.scheduler.advanceUntilIdle()

        val summary = vm.summary.first { it.emi > 0.0 && it.totalPayment > 50_000.0 }
        assertTrue(summary.emi > 0.0, "EMI should be positive for valid inputs")
        assertTrue(summary.totalPayment > 50_000.0, "Total payment should exceed principal")
    }
}
