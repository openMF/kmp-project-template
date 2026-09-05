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

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.banking.BillCategory
import kpt.core.model.demo.banking.BillReminder
import kpt.core.model.demo.banking.Recurrence
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * The `*Screen` entry composables are not previewed: they resolve their ViewModel through Koin.
 * Literals below are PREVIEW FIXTURE DATA — never reachable from the running app.
 */

internal fun previewBill(
    id: String = "bill-1", // i18n:skip
    name: String = "Electricity", // i18n:skip
    dueDay: Int = 15,
    category: BillCategory = BillCategory.UTILITIES,
    enabled: Boolean = true,
) = BillReminder(
    id = id,
    name = name,
    amount = 128.40,
    dueDay = dueDay,
    recurrence = Recurrence.MONTHLY,
    category = category,
    enabled = enabled,
    reminderDaysBefore = 3,
    createdAtMs = 1_700_000_000_000L,
    updatedAtMs = 1_700_000_000_000L,
)

@Preview
@Composable
internal fun UpcomingSummaryHeroPreview() {
    KptTheme {
        UpcomingSummaryHero(totalAmount = 412.75, upcomingCount = 3)
    }
}

@Preview
@Composable
internal fun UpcomingSummaryHeroNothingDuePreview() {
    // Zero upcoming bills is the good news case, and it takes a different copy path from "3 due".
    // A totals-only fixture would never render it.
    KptTheme {
        UpcomingSummaryHero(totalAmount = 0.0, upcomingCount = 0)
    }
}

@Preview
@Composable
internal fun BillReminderRowPreview() {
    KptTheme {
        BillReminderRow(bill = previewBill(), today = 10, onMarkPaid = {}, onClick = {})
    }
}

@Preview
@Composable
internal fun BillReminderRowOverdueAndDisabledPreview() {
    // `today` vs `dueDay` drives the overdue treatment, and a disabled reminder must read as muted
    // rather than simply vanishing. Both are states the default fixture cannot show.
    KptTheme {
        Column {
            BillReminderRow(bill = previewBill(dueDay = 3), today = 20, onMarkPaid = {}, onClick = {})
            BillReminderRow(
                bill = previewBill(id = "bill-2", name = "Gym", enabled = false), // i18n:skip
                today = 10,
                onMarkPaid = {},
                onClick = {},
            )
        }
    }
}
