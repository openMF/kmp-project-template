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

/**
 * `loans` crash context — DEMO-SHOWCASE, deleted by `--clean` with the loans feature.
 * The same file in a fork's own feature package is fork-owned.
 *
 * The crash half of this same feature package: one directory per feature, one ownership row, so
 * a feature's analytics vocabulary and its crash breadcrumbs stay together and move together.
 *
 * ## Why a feature needs its own crash keys
 * The template-owned `KptCrashKeys` records screen, network and sync. That explains a crash in
 * the shell. It does not explain a crash while amortising a 360-month loan, which needs the shape of
 * the loan the user was editing — the tenure and the band, never the amount.
 *
 * ## Banding is not optional here
 * A crash report is more exposed than an analytics event: it carries a stack trace, and console
 * access is usually broader than analytics access. So this file reuses [LoansParamValues] bands from
 * the analytics package rather than defining its own — one vocabulary, one place to audit, and no
 * chance of the crash path leaking a precision the analytics path deliberately dropped.
 */
object LoansCrashKeys {
    /** Category of the loan being acted on. */
    const val LOAN_KIND = "loans_kind"

    /**
     * Principal as a BAND, never the amount. A crash report leaves the device, so an exact balance must not be in it.
     */
    const val PRINCIPAL_BAND = "loans_principal_band"

    /** Tenure in months — the input that drives schedule size, and so the memory a schedule crash scales with. */
    const val TENURE_MONTHS = "loans_tenure_months"

    /** How many rows the amortization schedule produced, which is what an out-of-memory crash there scales with. */
    const val SCHEDULE_ROWS = "loans_schedule_rows"

    /**
     * Which wizard step was active when the crash happened, so a step-specific crash is not averaged across the whole
     * form.
     */
    const val FORM_STEP = "loans_form_step"

    /** How many loans the user has. */
    const val LOAN_COUNT = "loans_count"
}
