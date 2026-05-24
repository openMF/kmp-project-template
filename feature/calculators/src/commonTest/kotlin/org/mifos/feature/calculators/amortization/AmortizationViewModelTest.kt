/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.calculators.amortization

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.mifos.core.model.banking.Loan
import org.mifos.core.model.banking.LoanKind
import org.mifos.feature.calculators.wizard.FakeLoanRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AmortizationViewModelTest {

    @Test
    fun inlineModePopulatesScheduleFromUserInputs() = runTest {
        val repo = FakeLoanRepository()
        val vm = AmortizationViewModel(repository = repo, loanId = null)
        vm.trySendAction(AmortizationAction.UpdatePrincipal(10_000.0))
        vm.trySendAction(AmortizationAction.UpdateRate(12.0))
        vm.trySendAction(AmortizationAction.UpdateTenure(6))

        vm.schedule.test {
            val schedule = expectMostRecentItem()
            assertEquals(6, schedule.size)
            cancelAndIgnoreRemainingEvents()
        }
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

        vm.summary.test {
            val summary = expectMostRecentItem()
            assertTrue(summary.emi > 0.0, "EMI should be positive for valid inputs")
            assertTrue(summary.totalPayment > 50_000.0, "Total payment should exceed principal")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
