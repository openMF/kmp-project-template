/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.bills.ui

import androidx.compose.runtime.Composable
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.submit.SubmitState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.banking.BillCategory
import kpt.core.model.demo.banking.Recurrence
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier — see BillRemindersListScreenPreview.kt for
 * the full rationale.
 *
 * `AddOrEditBillReminderScreen` is not previewed: it resolves its ViewModel through Koin. Its form
 * sections are, and each is rendered twice — enabled and mid-submit — because the whole form
 * disables itself while a submit is in flight, and a form that only LOOKS disabled is how a user
 * ends up typing edits that are already lost.
 */

@Preview
@Composable
internal fun BasicInfoSectionPreview() {
    KptTheme {
        BasicInfoSection(
            form = BillReminderFormState(
                name = "Electricity",
                amount = 128.40,
                dueDay = 15,
            ),
            isSubmitting = false,
            onNameChange = {},
            onAmountChange = {},
            onDueDayChange = {},
        )
    }
}

@Preview
@Composable
internal fun BasicInfoSectionSubmittingPreview() {
    KptTheme {
        BasicInfoSection(
            form = BillReminderFormState(name = "Electricity", amount = 128.40, dueDay = 15),
            isSubmitting = true,
            onNameChange = {},
            onAmountChange = {},
            onDueDayChange = {},
        )
    }
}

@Preview
@Composable
internal fun RecurrenceSectionPreview() {
    KptTheme {
        RecurrenceSection(selected = Recurrence.MONTHLY, isSubmitting = false, onChange = {})
    }
}

@Preview
@Composable
internal fun RecurrenceSectionOnceSelectedPreview() {
    // ONCE is the non-repeating outlier — selecting it is what proves the chip row tracks the
    // selection rather than always highlighting MONTHLY.
    KptTheme {
        RecurrenceSection(selected = Recurrence.ONCE, isSubmitting = false, onChange = {})
    }
}

@Preview
@Composable
internal fun CategorySectionPreview() {
    KptTheme {
        CategorySection(selected = BillCategory.UTILITIES, isSubmitting = false, onChange = {})
    }
}

@Preview
@Composable
internal fun CategorySectionOtherSelectedPreview() {
    KptTheme {
        CategorySection(selected = BillCategory.OTHER, isSubmitting = false, onChange = {})
    }
}

@Preview
@Composable
internal fun ReminderSettingsSectionPreview() {
    KptTheme {
        ReminderSettingsSection(
            reminderDaysBefore = 3,
            enabled = true,
            isSubmitting = false,
            onReminderDaysBeforeChange = {},
            onEnabledChange = {},
        )
    }
}

@Preview
@Composable
internal fun ReminderSettingsSectionDisabledPreview() {
    // With reminders switched off the days-before control must read as inert — otherwise the user
    // sets a lead time that will never fire.
    KptTheme {
        ReminderSettingsSection(
            reminderDaysBefore = 1,
            enabled = false,
            isSubmitting = false,
            onReminderDaysBeforeChange = {},
            onEnabledChange = {},
        )
    }
}

@Preview
@Composable
internal fun BillSubmitStatusLineSubmittingPreview() {
    KptTheme {
        SubmitStatusLine(submit = SubmitState.Submitting(), onRetry = {}, onDismiss = {})
    }
}

@Preview
@Composable
internal fun BillSubmitStatusLineFailedPreview() {
    // This form is the DraftSubmitHandler demo: a network failure keeps the payload in the outbox,
    // so the failure line has to say so and offer a retry rather than reading as data loss.
    KptTheme {
        SubmitStatusLine(
            submit = SubmitState.Failed(
                error = IllegalStateException("no connection"),
                category = ErrorCategory.Network,
                draftSaved = true,
            ),
            onRetry = {},
            onDismiss = {},
        )
    }
}
