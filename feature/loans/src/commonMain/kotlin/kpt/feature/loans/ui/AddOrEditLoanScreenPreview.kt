/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loans.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.submit.SubmitState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.banking.LoanKind
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier — see LoanDetailScreenPreview.kt for the
 * full rationale.
 *
 * `AddOrEditLoanScreen` is not previewed: it resolves its ViewModel through Koin. The form's field
 * primitives below are, and they are where the enabled/disabled treatment lives — the form disables
 * every field while a submit is in flight, so both states are rendered.
 */

@Preview
@Composable
internal fun LoanFormFieldsEnabledPreview() {
    KptTheme {
        Column {
            LoanKindDropdown(value = LoanKind.AUTO, onChange = {}, enabled = true)
            DoubleField(label = "Principal", value = 100_000.0, onChange = {}, enabled = true) // i18n:skip
            IntField(label = "Tenure (months)", value = 60, onChange = {}, enabled = true) // i18n:skip
            DateField(label = "First due date", value = LocalDate(2026, 10, 5), onChange = {}, enabled = true) // i18n:skip
        }
    }
}

@Preview
@Composable
internal fun LoanFormFieldsDisabledPreview() {
    // Every field is disabled while a submit is in flight. If the disabled tint is wrong the form
    // looks editable mid-submit and the user types into a field whose edits are already lost.
    KptTheme {
        Column {
            LoanKindDropdown(value = LoanKind.MORTGAGE, onChange = {}, enabled = false)
            DoubleField(label = "Principal", value = 100_000.0, onChange = {}, enabled = false) // i18n:skip
            IntField(label = "Tenure (months)", value = 60, onChange = {}, enabled = false) // i18n:skip
            DateField(label = "First due date", value = LocalDate(2026, 10, 5), onChange = {}, enabled = false) // i18n:skip
        }
    }
}

@Preview
@Composable
internal fun ComputedPreviewCardPreview() {
    KptTheme {
        ComputedPreviewCard(monthlyEmi = 2_076.0, totalInterest = 24_560.0)
    }
}

@Preview
@Composable
internal fun SubmitStatusLineSubmittingPreview() {
    KptTheme {
        SubmitStatusLine(submit = SubmitState.Submitting(), onRetry = {}, onDismiss = {})
    }
}

@Preview
@Composable
internal fun SubmitStatusLineFailedPreview() {
    // The failure arm carries the retry affordance. Rendering only the in-flight state would leave
    // the one interactive branch of this line uncovered.
    KptTheme {
        SubmitStatusLine(
            submit = SubmitState.Failed(
                error = IllegalStateException("no connection"), // i18n:skip
                category = ErrorCategory.Network,
            ),
            onRetry = {},
            onDismiss = {},
        )
    }
}
