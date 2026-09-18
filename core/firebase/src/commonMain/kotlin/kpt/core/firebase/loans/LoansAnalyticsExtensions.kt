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

import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsEvent
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsHelper
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.Param
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.ParamKeys

/**
 * `loans` [AnalyticsHelper] extensions — DEMO-SHOWCASE, deleted by `--clean` with the loans feature.
 * The same file in a fork's own feature package is fork-owned.
 *
 * The extension form exists for callers that already hold an [AnalyticsHelper] and should not build a
 * tracker for one event — a use-case, a Koin-injected repository, a navigation side-effect. Screens
 * with several events should take [LoansAnalyticsTracker] instead.
 */

/**
 * One step of the add/edit funnel. Emitting a step event rather than only start/finish is what makes
 * "where do people give up entering a loan?" answerable.
 */
fun AnalyticsHelper.trackLoanFormStep(step: String, completed: Boolean) {
    logEvent(
        AnalyticsEvent(
            if (completed) LoansEventTypes.LOAN_FORM_OPENED else LoansEventTypes.LOAN_FORM_ABANDONED,
            listOf(
                Param(LoansParamKeys.FORM_STEP, step),
                Param(ParamKeys.SUCCESS, completed.toString()),
            ),
        ),
    )
}

/**
 * The amortization schedule opened for a loan. Lives here rather than in the `amortization` feature
 * because the schedule is a projection OF a loan — the funnel it belongs to is the loans funnel.
 */
fun AnalyticsHelper.trackAmortizationViewed(kind: String, tenureMonths: Int) {
    logEvent(
        AnalyticsEvent(
            LoansEventTypes.LOAN_AMORTIZATION_VIEWED,
            listOf(
                Param(LoansParamKeys.LOAN_KIND, kind),
                Param(LoansParamKeys.TENURE_MONTHS, tenureMonths.toString()),
            ),
        ),
    )
}

/** A reminder scheduled from `LoanReminderUseCase`, where no tracker is in scope. */
fun AnalyticsHelper.trackLoanReminderScheduled(leadDays: Int) {
    logEvent(
        AnalyticsEvent(
            LoansEventTypes.LOAN_REMINDER_SCHEDULED,
            listOf(Param(LoansParamKeys.REMINDER_LEAD_DAYS, leadDays.toString())),
        ),
    )
}
