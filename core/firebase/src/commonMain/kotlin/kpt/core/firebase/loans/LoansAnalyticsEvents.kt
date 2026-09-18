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
 * `loans` feature analytics keys — DEMO-SHOWCASE, deleted by `--clean` with the loans feature.
 * A fork's equivalent for its own feature is fork-owned and needs no declaration.
 *
 * This package is the reference shape for per-feature analytics: one directory per feature under
 * `kpt/core/firebase/analytics/`, holding the `Kpt`-prefixed events / tracker /
 * `KptAnalyticsExtensions.kt` scoped to that feature alone.
 *
 * ## Why here and not in the feature module
 * `feature/loans` depends on `core/firebase`, not the reverse. Keeping the keys here lets the
 * dashboards, the tracker and the crash reporter share one vocabulary without the analytics host
 * taking a dependency on every feature it can describe.
 *
 * ## Why not in the template-owned root
 * `config/analytics/KptAnalyticsEvents.kt` is full-copied by every sync. A `LOAN_*` constant there is lost
 * on the next sync and meanwhile ships to forks that have no loans. The split is the whole point:
 * template infra upgrades cleanly, fork vocabulary survives.
 *
 * Events name what the UI actually does — list, detail, add/edit, reminders — rather than a generic
 * CRUD alphabet, so a funnel can be read without consulting the code.
 */
object LoansEventTypes {
    // Browse
    const val LOANS_LIST_VIEWED = "loans_list_viewed"
    const val LOAN_DETAIL_VIEWED = "loan_detail_viewed"
    const val LOAN_AMORTIZATION_VIEWED = "loan_amortization_viewed"

    // Author — the add/edit funnel, one event per step so drop-off is visible.
    const val LOAN_FORM_OPENED = "loan_form_opened"
    const val LOAN_FORM_ABANDONED = "loan_form_abandoned"
    const val LOAN_CREATED = "loan_created"
    const val LOAN_UPDATED = "loan_updated"
    const val LOAN_DELETED = "loan_deleted"

    // Reminders — LoanReminderUseCase
    const val LOAN_REMINDER_SCHEDULED = "loan_reminder_scheduled"
    const val LOAN_REMINDER_CANCELLED = "loan_reminder_cancelled"
    const val LOAN_REMINDER_FIRED = "loan_reminder_fired"
}

/**
 * `loans` parameter keys.
 *
 * NOTE none of these carry money or identity. `principal` is bucketed by [LoansParamValues], and the
 * loan id is deliberately absent: a Firebase event is not the place to reconstruct a user's debts.
 */
object LoansParamKeys {
    const val LOAN_KIND = "loan_kind"
    const val PRINCIPAL_BAND = "principal_band"
    const val TENURE_MONTHS = "tenure_months"
    const val RATE_BAND = "rate_band"
    const val FORM_STEP = "form_step"
    const val LOAN_COUNT = "loan_count"
    const val REMINDER_LEAD_DAYS = "reminder_lead_days"
}

/**
 * `loans` parameter values.
 *
 * Bands rather than amounts. Analytics answers "do people track large loans?", which a band answers
 * and an exact figure answers at the cost of shipping a financial profile to a third party.
 */
object LoansParamValues {
    const val PRINCIPAL_BAND_SMALL = "lt_1k"
    const val PRINCIPAL_BAND_MEDIUM = "1k_10k"
    const val PRINCIPAL_BAND_LARGE = "10k_100k"
    const val PRINCIPAL_BAND_XLARGE = "gte_100k"

    const val RATE_BAND_LOW = "lt_5pct"
    const val RATE_BAND_MID = "5_15pct"
    const val RATE_BAND_HIGH = "gte_15pct"

    const val FORM_STEP_DETAILS = "details"
    const val FORM_STEP_TERMS = "terms"
    const val FORM_STEP_REVIEW = "review"
}
