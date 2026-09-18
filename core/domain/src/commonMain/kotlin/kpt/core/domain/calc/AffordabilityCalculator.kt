/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MatchingDeclarationName", "ktlint:standard:filename")

package kpt.core.domain.calc

import kotlin.math.pow

/**
 * Affordability calculator backing the B5 screen.
 *
 * Answers: "given my monthly income, existing debts, and a DTI ceiling,
 * what additional EMI can I take on and what loan principal is that worth at
 * this rate × tenure?"
 *
 * `DTI` (Debt-to-Income) is a standard underwriting ratio. 0.40 (40 %) is a
 * conservative default — typical bands run 36 %–43 %.
 *
 * Edge cases:
 * - `dtiRatio` is clamped to `[0.0, 1.0]` so callers can wire a slider 0–100 %
 *   without separate validation.
 * - Non-positive income or tenure → zero result.
 * - Existing obligations ≥ DTI ceiling → zero result (already maxed out).
 * - Zero rate → max principal is `maxEmi · tenureMonths` (no interest).
 */
fun maxAffordableLoan(
    monthlyIncome: Double,
    monthlyObligations: Double,
    dtiRatio: Double = 0.4,
    rate: Double,
    tenureMonths: Int,
): AffordabilityResult {
    if (monthlyIncome <= 0.0 || tenureMonths <= 0) {
        return AffordabilityResult(
            maxEmi = 0.0,
            maxPrincipal = 0.0,
            rationale = "Provide a positive income and tenure to see what you can borrow.",
        )
    }
    val clampedDti = dtiRatio.coerceIn(0.0, 1.0)
    val ceiling = monthlyIncome * clampedDti
    val maxEmi = (ceiling - monthlyObligations).coerceAtLeast(0.0)
    val maxPrincipal = principalForEmi(maxEmi, rate, tenureMonths)
    val dtiPct = (clampedDti * 100).toInt()
    val rationale = buildString {
        append("At a $dtiPct% DTI ceiling on income of $monthlyIncome, ")
        append("your total monthly debt budget is $ceiling. ")
        if (monthlyObligations > 0.0) {
            append("After existing obligations of $monthlyObligations, ")
        }
        if (maxEmi <= 0.0) {
            append("you have no headroom for a new EMI.")
        } else {
            append("you can afford an EMI of up to $maxEmi.")
        }
    }
    return AffordabilityResult(
        maxEmi = maxEmi,
        maxPrincipal = maxPrincipal,
        rationale = rationale,
    )
}

/**
 * Inverse of the EMI formula — given a target monthly payment, returns the
 * principal that produces it at this rate × tenure.
 *
 * Formula: `P = EMI · ((1+r)^n − 1) / (r · (1+r)^n)`.
 * Zero-rate fallback: `P = EMI · n`.
 */
private fun principalForEmi(maxEmi: Double, annualRatePercent: Double, tenureMonths: Int): Double {
    val invalid = maxEmi <= 0.0 || tenureMonths <= 0
    val monthlyRate = annualRatePercent / 12.0 / 100.0
    return when {
        invalid -> 0.0
        monthlyRate == 0.0 -> maxEmi * tenureMonths
        else -> {
            val factor = (1.0 + monthlyRate).pow(tenureMonths.toDouble())
            maxEmi * (factor - 1.0) / (monthlyRate * factor)
        }
    }
}

/**
 * Output of [maxAffordableLoan].
 *
 * @property maxEmi Largest monthly EMI that keeps total debt within the DTI ceiling.
 * @property maxPrincipal Principal that produces [maxEmi] at the supplied rate × tenure.
 * @property rationale Human-readable explanation of the arithmetic — surfaced verbatim in the UI.
 */
data class AffordabilityResult(
    val maxEmi: Double,
    val maxPrincipal: Double,
    val rationale: String,
)
