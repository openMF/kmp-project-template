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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsEvent
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsHelper
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.Param
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.ParamKeys
import io.github.mobilebytelabs.kmptoolkit.firebase.compose.rememberAnalyticsHelper

/**
 * `loans` feature tracker — DEMO-SHOWCASE. It is the template's worked example of the per-feature
 * layout: shipped and synced like template code, and deleted by `remove-demo.sh --clean` along with
 * the `feature/loans` module it tracks. **Your fork's own `kpt/core/firebase/<feature>/` is fork-owned** —
 * undeclared IS fork, so creating the directory is the whole registration step.
 *
 * Composes with the template-owned `KptAnalyticsTracker` rather than replacing it: both take the same
 * injected [AnalyticsHelper], so a screen can raise a cross-cutting `trackScreenView` and a
 * loans-specific `trackLoanCreated` through one pipeline.
 *
 * Every method here bands or omits sensitive values — see [LoansParamValues]. The banding lives in
 * this class, not at the call site, so a caller cannot accidentally pass a raw amount.
 */
class LoansAnalyticsTracker(
    private val analyticsHelper: AnalyticsHelper,
) {

    /** The list screen. [loanCount] shows whether the feature is used with 1 loan or 30. */
    fun trackListViewed(loanCount: Int) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                LoansEventTypes.LOANS_LIST_VIEWED,
                listOf(Param(LoansParamKeys.LOAN_COUNT, loanCount.toString())),
            ),
        )
    }

    /** A single loan opened. The loan id is deliberately not sent. */
    fun trackDetailViewed(kind: String) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                LoansEventTypes.LOAN_DETAIL_VIEWED,
                listOf(Param(LoansParamKeys.LOAN_KIND, kind)),
            ),
        )
    }

    /** Add/edit form opened. [step] is one of the `LoansParamValues.FORM_STEP_*` values. */
    fun trackFormOpened(editing: Boolean, step: String = LoansParamValues.FORM_STEP_DETAILS) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                LoansEventTypes.LOAN_FORM_OPENED,
                listOf(
                    Param(ParamKeys.ACTION_TYPE, if (editing) "edit" else "create"),
                    Param(LoansParamKeys.FORM_STEP, step),
                ),
            ),
        )
    }

    /**
     * The form was left without saving. Paired with [trackFormOpened] this gives an abandonment rate
     * per step, which is the question a loan form actually raises.
     */
    fun trackFormAbandoned(step: String) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                LoansEventTypes.LOAN_FORM_ABANDONED,
                listOf(Param(LoansParamKeys.FORM_STEP, step)),
            ),
        )
    }

    /**
     * A loan was created or updated. [principal] and [annualRatePercent] are banded here rather than
     * sent — the caller passes real figures and this class decides what leaves the device.
     */
    fun trackSaved(
        created: Boolean,
        kind: String,
        principal: Double,
        annualRatePercent: Double,
        tenureMonths: Int,
    ) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                if (created) LoansEventTypes.LOAN_CREATED else LoansEventTypes.LOAN_UPDATED,
                listOf(
                    Param(LoansParamKeys.LOAN_KIND, kind),
                    Param(LoansParamKeys.PRINCIPAL_BAND, principalBand(principal)),
                    Param(LoansParamKeys.RATE_BAND, rateBand(annualRatePercent)),
                    Param(LoansParamKeys.TENURE_MONTHS, tenureMonths.toString()),
                ),
            ),
        )
    }

    fun trackDeleted(kind: String) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                LoansEventTypes.LOAN_DELETED,
                listOf(Param(LoansParamKeys.LOAN_KIND, kind)),
            ),
        )
    }

    /** Reminder lifecycle, raised by `LoanReminderUseCase`. */
    fun trackReminder(action: String, leadDays: Int? = null) {
        val eventType = when (action) {
            "cancelled" -> LoansEventTypes.LOAN_REMINDER_CANCELLED
            "fired" -> LoansEventTypes.LOAN_REMINDER_FIRED
            else -> LoansEventTypes.LOAN_REMINDER_SCHEDULED
        }
        val params = mutableListOf<Param>()
        leadDays?.let { params.add(Param(LoansParamKeys.REMINDER_LEAD_DAYS, it.toString())) }

        analyticsHelper.logEvent(AnalyticsEvent(eventType, params))
    }

    private fun principalBand(principal: Double): String = when {
        principal < 1_000 -> LoansParamValues.PRINCIPAL_BAND_SMALL
        principal < 10_000 -> LoansParamValues.PRINCIPAL_BAND_MEDIUM
        principal < 100_000 -> LoansParamValues.PRINCIPAL_BAND_LARGE
        else -> LoansParamValues.PRINCIPAL_BAND_XLARGE
    }

    private fun rateBand(rate: Double): String = when {
        rate < 5.0 -> LoansParamValues.RATE_BAND_LOW
        rate < 15.0 -> LoansParamValues.RATE_BAND_MID
        else -> LoansParamValues.RATE_BAND_HIGH
    }
}

/** Composition-scoped [LoansAnalyticsTracker], sharing the ambient [AnalyticsHelper]. */
@Composable
fun rememberLoansAnalyticsTracker(): LoansAnalyticsTracker {
    val analyticsHelper = rememberAnalyticsHelper()
    return remember(analyticsHelper) { LoansAnalyticsTracker(analyticsHelper) }
}
