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
    const val LOAN_KIND = "loans_kind"
    const val PRINCIPAL_BAND = "loans_principal_band"
    const val TENURE_MONTHS = "loans_tenure_months"
    const val SCHEDULE_ROWS = "loans_schedule_rows"
    const val FORM_STEP = "loans_form_step"
    const val LOAN_COUNT = "loans_count"
}
