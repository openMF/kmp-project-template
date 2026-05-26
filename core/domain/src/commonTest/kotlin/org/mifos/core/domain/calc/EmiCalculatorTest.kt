/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.domain.calc

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Golden-input/output tests for [computeEmi] and [amortizationSchedule].
 *
 * The toolkit's calculators present EMIs at 2 dp via standard `Double`. The
 * formula is the standard reducing-balance EMI:
 * `EMI = P × r × (1+r)^n / ((1+r)^n − 1)`.
 *
 * Edge cases covered:
 * - 0 % interest → simple division of principal across tenure.
 * - Non-positive tenure / principal → empty schedule + zero result.
 */
class EmiCalculatorTest {

    @Test
    fun computeEmiMatchesStandardFormulaForTypicalMortgage() {
        // $250,000 @ 6.5 % APR for 30 years (360 months)
        // EMI ≈ 1580.17
        val result = computeEmi(principal = 250_000.0, annualRatePercent = 6.5, tenureMonths = 360)
        assertCloseTo(1_580.17, result.emi, tolerance = 0.01)
        assertCloseTo(568_861.22, result.totalPayment, tolerance = 0.50)
        assertCloseTo(318_861.22, result.totalInterest, tolerance = 0.50)
    }

    @Test
    fun computeEmiFallsBackToFlatDivisionWhenRateIsZero() {
        // 0 % rate → EMI = principal / months, total interest = 0.
        val result = computeEmi(principal = 12_000.0, annualRatePercent = 0.0, tenureMonths = 12)
        assertEquals(1_000.0, result.emi, "EMI must be principal / months when rate=0")
        assertEquals(12_000.0, result.totalPayment)
        assertEquals(0.0, result.totalInterest)
    }

    @Test
    fun computeEmiReturnsZeroResultForNonPositiveTenure() {
        val result = computeEmi(principal = 50_000.0, annualRatePercent = 8.0, tenureMonths = 0)
        assertEquals(0.0, result.emi)
        assertEquals(0.0, result.totalPayment)
        assertEquals(0.0, result.totalInterest)
    }

    @Test
    fun computeEmiReturnsZeroResultForNonPositivePrincipal() {
        val result = computeEmi(principal = 0.0, annualRatePercent = 8.0, tenureMonths = 24)
        assertEquals(0.0, result.emi)
    }

    @Test
    fun amortizationScheduleHasOneRowPerMonth() {
        val schedule = amortizationSchedule(
            principal = 10_000.0,
            annualRatePercent = 12.0,
            tenureMonths = 6,
        )
        assertEquals(6, schedule.size)
        assertEquals(1, schedule.first().installmentNumber)
        assertEquals(6, schedule.last().installmentNumber)
    }

    @Test
    fun amortizationScheduleSumOfPrincipalEqualsLoanPrincipal() {
        val principal = 100_000.0
        val schedule = amortizationSchedule(principal, annualRatePercent = 9.0, tenureMonths = 60)
        val totalPrincipalPaid = schedule.sumOf { it.principalPaid }
        // Allow ¢-level rounding drift from the closing balance.
        assertCloseTo(principal, totalPrincipalPaid, tolerance = 0.10)
    }

    @Test
    fun amortizationScheduleSumOfInterestMatchesTotalInterestFromComputeEmi() {
        val emi = computeEmi(50_000.0, 7.5, 24)
        val sched = amortizationSchedule(50_000.0, 7.5, 24)
        val totalInterestFromSchedule = sched.sumOf { it.interestPaid }
        assertCloseTo(emi.totalInterest, totalInterestFromSchedule, tolerance = 0.10)
    }

    @Test
    fun amortizationScheduleClosingBalanceIsZero() {
        val schedule = amortizationSchedule(25_000.0, 6.0, 12)
        // Final remaining balance rounds to ≤ ¢-level.
        assertTrue(
            abs(schedule.last().balanceRemaining) < 0.01,
            "Closing balance should be ~0, was ${schedule.last().balanceRemaining}",
        )
    }

    @Test
    fun amortizationScheduleIsEmptyForNonPositiveTenure() {
        assertEquals(emptyList(), amortizationSchedule(10_000.0, 5.0, 0))
        assertEquals(emptyList(), amortizationSchedule(10_000.0, 5.0, -3))
    }

    @Test
    fun amortizationScheduleHandlesZeroRate() {
        val schedule = amortizationSchedule(1_200.0, 0.0, 12)
        assertEquals(12, schedule.size)
        // Every installment is identical, only principal — no interest.
        schedule.forEach {
            assertEquals(0.0, it.interestPaid)
            assertEquals(100.0, it.principalPaid)
        }
        assertCloseTo(0.0, schedule.last().balanceRemaining, tolerance = 0.01)
    }

    private fun assertCloseTo(expected: Double, actual: Double, tolerance: Double) {
        assertTrue(
            abs(expected - actual) <= tolerance,
            "Expected $expected but got $actual (tolerance=$tolerance)",
        )
    }
}
