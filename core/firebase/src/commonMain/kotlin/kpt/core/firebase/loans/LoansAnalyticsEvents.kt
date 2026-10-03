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
    /** The loan list was opened. */
    const val LOANS_LIST_VIEWED = "loans_list_viewed"

    /** A loan's detail screen was opened. Carries [LOAN_KIND] and the principal BAND, never the loan's id or amount. */
    const val LOAN_DETAIL_VIEWED = "loan_detail_viewed"

    /** The amortization schedule was opened. */
    const val LOAN_AMORTIZATION_VIEWED = "loan_amortization_viewed"

    // Author — the add/edit funnel, one event per step so drop-off is visible.
    /** The add/edit form was opened. Paired with [LOAN_FORM_ABANDONED] this gives the form's drop-off rate. */
    const val LOAN_FORM_OPENED = "loan_form_opened"

    /** The form was left without submitting — paired with [LOAN_FORM_OPENED] this gives the drop-off rate. */
    const val LOAN_FORM_ABANDONED = "loan_form_abandoned"

    /**
     * A loan was committed for the first time. Fires on the local commit, so it counts even for a fork whose submit
     * never reaches a server.
     */
    const val LOAN_CREATED = "loan_created"

    /**
     * An existing loan was committed again. Distinct from [LOAN_CREATED] so edit frequency is measurable on its own.
     */
    const val LOAN_UPDATED = "loan_updated"

    /** A loan was deleted. Carries only [LOAN_KIND] — a deleted loan's figures have no analytic use. */
    const val LOAN_DELETED = "loan_deleted"

    // Reminders — LoanReminderUseCase
    /** A payment reminder was registered with the platform scheduler. */
    const val LOAN_REMINDER_SCHEDULED = "loan_reminder_scheduled"

    /** A payment reminder was withdrawn, because the loan was deleted or reminders were turned off. */
    const val LOAN_REMINDER_CANCELLED = "loan_reminder_cancelled"

    /** A payment reminder was delivered. Fired-minus-scheduled is how reminder loss on a given platform is spotted. */
    const val LOAN_REMINDER_FIRED = "loan_reminder_fired"
}

/**
 * `loans` parameter keys.
 *
 * NOTE none of these carry money or identity. `principal` is bucketed by [LoansParamValues], and the
 * loan id is deliberately absent: a Firebase event is not the place to reconstruct a user's debts.
 */
object LoansParamKeys {
    /** The loan's category. Low-cardinality by construction, so it is safe to break every event down by it. */
    const val LOAN_KIND = "loan_kind"

    /** Principal as a BAND, never the amount — an exact figure would make the event personal data. */
    const val PRINCIPAL_BAND = "principal_band"

    /**
     * Tenure in months. A raw number rather than a band — tenure is not identifying on its own the way an amount is.
     */
    const val TENURE_MONTHS = "tenure_months"

    /** APR as a band, for the same reason as [PRINCIPAL_BAND]. */
    const val RATE_BAND = "rate_band"

    /**
     * Which wizard step the event refers to, so abandonment can be attributed to a specific step rather than to the
     * form as a whole.
     */
    const val FORM_STEP = "form_step"

    /** How many loans the user has. */
    const val LOAN_COUNT = "loan_count"

    /** How many days before the due date the reminder is set for. */
    const val REMINDER_LEAD_DAYS = "reminder_lead_days"
}

/**
 * `loans` parameter values.
 *
 * Bands rather than amounts. Analytics answers "do people track large loans?", which a band answers
 * and an exact figure answers at the cost of shipping a financial profile to a third party.
 */
object LoansParamValues {
    /** Under 1,000. */
    const val PRINCIPAL_BAND_SMALL = "lt_1k"

    /** 1,000 to 10,000. */
    const val PRINCIPAL_BAND_MEDIUM = "1k_10k"

    /** 10,000 to 100,000. */
    const val PRINCIPAL_BAND_LARGE = "10k_100k"

    /** 100,000 and above. */
    const val PRINCIPAL_BAND_XLARGE = "gte_100k"

    /** Under 5%. */
    const val RATE_BAND_LOW = "lt_5pct"

    /** 5% to 15%. */
    const val RATE_BAND_MID = "5_15pct"

    /** 15% and above. */
    const val RATE_BAND_HIGH = "gte_15pct"

    /** Name and category. */
    const val FORM_STEP_DETAILS = "details"

    /** Principal, rate and tenure. */
    const val FORM_STEP_TERMS = "terms"

    /** Final confirmation. */
    const val FORM_STEP_REVIEW = "review"
}
