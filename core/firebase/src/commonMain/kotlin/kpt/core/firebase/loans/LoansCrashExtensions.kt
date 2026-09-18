/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.firebase.loans

import io.github.mobilebytelabs.kmptoolkit.firebase.crashlytics.CrashReporter

/**
 * `loans` [CrashReporter] breadcrumbs — DEMO-SHOWCASE, deleted by `--clean` with the loans feature.
 * The same file in a fork's own feature package is fork-owned.
 *
 * ## Banding is not optional here
 * A crash report is more exposed than an analytics event: it carries a stack trace, and console
 * access is usually broader than analytics access. So this file reuses [LoansParamValues] bands from
 * the analytics package rather than defining its own — one vocabulary, one place to audit, and no
 * chance of the crash path leaking a precision the analytics path deliberately dropped.
 */

/**
 * The loan being edited or viewed when a crash occurs.
 *
 * [principal] is banded on the way in — the caller passes the real figure and this function decides
 * what reaches the report, exactly as `LoansAnalyticsTracker` does.
 */
fun CrashReporter.setLoanContext(
    kind: String,
    principal: Double,
    tenureMonths: Int,
) {
    setCustomKey(LoansCrashKeys.LOAN_KIND, kind)
    setCustomKey(LoansCrashKeys.PRINCIPAL_BAND, principalBand(principal))
    setCustomKey(LoansCrashKeys.TENURE_MONTHS, tenureMonths.toString())
    log("loans -> $kind / ${tenureMonths}mo")
}

/**
 * Amortisation is the one place in this feature that builds an unbounded list — one row per month —
 * so the row count is the first thing worth knowing about an OOM or a jank report here.
 */
fun CrashReporter.setAmortizationContext(scheduleRows: Int) {
    setCustomKey(LoansCrashKeys.SCHEDULE_ROWS, scheduleRows.toString())
}

/** Which step of the add/edit form was open. Pairs with the analytics funnel of the same name. */
fun CrashReporter.setLoanFormStep(step: String) {
    setCustomKey(LoansCrashKeys.FORM_STEP, step)
}

/** How many loans the user holds — list-rendering crashes scale with this. */
fun CrashReporter.setLoanCount(count: Int) {
    setCustomKey(LoansCrashKeys.LOAN_COUNT, count.toString())
}

private fun principalBand(principal: Double): String = when {
    principal < 1_000 -> LoansParamValues.PRINCIPAL_BAND_SMALL
    principal < 10_000 -> LoansParamValues.PRINCIPAL_BAND_MEDIUM
    principal < 100_000 -> LoansParamValues.PRINCIPAL_BAND_LARGE
    else -> LoansParamValues.PRINCIPAL_BAND_XLARGE
}
