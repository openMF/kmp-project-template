/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.domain.demo.calc

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [maxAffordableLoan]. The function answers
 * "given my income, obligations, and a DTI ceiling, what monthly EMI fits, and
 * therefore what loan principal would that EMI buy at this rate × tenure?".
 *
 * Standard underwriting DTI ranges 36 %–43 % — the default ceiling is 0.4 (40 %).
 */
class AffordabilityCalculatorTest {

    @Test
    fun maxEmiIsTheGapBetweenDtiCeilingAndExistingObligations() {
        // 40 % of 10 000 = 4 000. Existing obligations 1 000 → headroom 3 000.
        val result = maxAffordableLoan(
            monthlyIncome = 10_000.0,
            monthlyObligations = 1_000.0,
            dtiRatio = 0.4,
            rate = 6.5,
            tenureMonths = 360,
        )
        assertCloseTo(3_000.0, result.maxEmi, tolerance = 0.01)
    }

    @Test
    fun maxEmiIsZeroWhenObligationsAlreadyExceedDtiCeiling() {
        // 50 % obligations on a 40 % DTI ceiling → no headroom for additional EMI.
        val result = maxAffordableLoan(
            monthlyIncome = 5_000.0,
            monthlyObligations = 2_500.0,
            dtiRatio = 0.4,
            rate = 7.0,
            tenureMonths = 240,
        )
        assertEquals(0.0, result.maxEmi)
        assertEquals(0.0, result.maxPrincipal)
    }

    @Test
    fun maxPrincipalRoundTripsThroughComputeEmi() {
        val rate = 6.5
        val tenure = 360
        val result = maxAffordableLoan(
            monthlyIncome = 10_000.0,
            monthlyObligations = 1_000.0,
            dtiRatio = 0.4,
            rate = rate,
            tenureMonths = tenure,
        )
        // Round-trip the principal through computeEmi — should land at ~maxEmi.
        val emiForResult = computeEmi(result.maxPrincipal, rate, tenure).emi
        assertCloseTo(result.maxEmi, emiForResult, tolerance = 0.10)
    }

    @Test
    fun rationaleSummarisesTheCeilingArithmetic() {
        val result = maxAffordableLoan(
            monthlyIncome = 8_000.0,
            monthlyObligations = 1_200.0,
            dtiRatio = 0.4,
            rate = 6.0,
            tenureMonths = 180,
        )
        assertTrue(result.rationale.isNotBlank(), "Rationale should explain the math")
    }

    @Test
    fun zeroIncomeReturnsZeroResult() {
        val result = maxAffordableLoan(
            monthlyIncome = 0.0,
            monthlyObligations = 0.0,
            dtiRatio = 0.4,
            rate = 6.0,
            tenureMonths = 120,
        )
        assertEquals(0.0, result.maxEmi)
        assertEquals(0.0, result.maxPrincipal)
    }

    @Test
    fun negativeOrZeroTenureReturnsZeroPrincipal() {
        val result = maxAffordableLoan(
            monthlyIncome = 10_000.0,
            monthlyObligations = 1_000.0,
            dtiRatio = 0.4,
            rate = 6.0,
            tenureMonths = 0,
        )
        assertEquals(0.0, result.maxPrincipal)
    }

    @Test
    fun dtiRatioIsClampedToValidRange() {
        // 1.5 should clamp to 1.0 (full income available).
        val a = maxAffordableLoan(10_000.0, 0.0, dtiRatio = 1.5, rate = 6.0, tenureMonths = 120)
        // -0.1 should clamp to 0.0 → no affordability.
        val b = maxAffordableLoan(10_000.0, 0.0, dtiRatio = -0.1, rate = 6.0, tenureMonths = 120)
        assertCloseTo(10_000.0, a.maxEmi, tolerance = 0.01)
        assertEquals(0.0, b.maxEmi)
    }

    private fun assertCloseTo(expected: Double, actual: Double, tolerance: Double) {
        assertTrue(
            abs(expected - actual) <= tolerance,
            "Expected $expected but got $actual (tolerance=$tolerance)",
        )
    }
}
