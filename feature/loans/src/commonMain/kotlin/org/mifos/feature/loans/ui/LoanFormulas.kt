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

import kotlin.math.pow

/**
 * Standard amortizing-loan monthly payment ("EMI").
 *
 * `EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)` where
 * - `P` = principal
 * - `r` = monthly interest rate (annualRatePercent / 100 / 12)
 * - `n` = tenure in months
 *
 * Edge cases:
 * - Zero or negative principal/tenure → returns `0.0` (caller validates).
 * - Zero interest rate → returns principal / tenureMonths (simple division).
 */
internal fun computeMonthlyEmi(
    principal: Double,
    annualRatePercent: Double,
    tenureMonths: Int,
): Double = when {
    principal <= 0.0 || tenureMonths <= 0 -> 0.0
    annualRatePercent <= 0.0 -> principal / tenureMonths
    else -> {
        val r = annualRatePercent / 100.0 / 12.0
        val factor = (1.0 + r).pow(tenureMonths)
        principal * r * factor / (factor - 1.0)
    }
}

/**
 * Total interest paid over the lifetime of an amortizing loan.
 *
 * `totalInterest = (EMI * n) - P`. Returns `0.0` if inputs are non-positive.
 */
internal fun computeTotalInterest(
    principal: Double,
    annualRatePercent: Double,
    tenureMonths: Int,
): Double {
    if (principal <= 0.0 || tenureMonths <= 0) return 0.0
    val emi = computeMonthlyEmi(principal, annualRatePercent, tenureMonths)
    return (emi * tenureMonths) - principal
}
