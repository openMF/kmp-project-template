/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.amortization.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.banking.AmortizationRow
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * `AmortizationScheduleScreen` is not previewed: it resolves its ViewModel through Koin.
 * Literals below are PREVIEW FIXTURE DATA — never reachable from the running app.
 */

private fun previewRows(count: Int = 4): List<AmortizationRow> {
    var balance = 100_000.0
    return (1..count).map { month ->
        val interest = balance * 0.0075
        val principal = 2_000.0 - interest
        balance -= principal
        AmortizationRow(
            month = month,
            payment = 2_000.0,
            principal = principal,
            interest = interest,
            balance = balance,
        )
    }
}

@Preview
@Composable
internal fun ScheduleTableHeaderPreview() {
    KptTheme {
        ScheduleTableHeader()
    }
}

@Preview
@Composable
internal fun ScheduleRowStripingPreview() {
    // The zebra striping is the whole point of `isEven` — a single row cannot show that the two
    // backgrounds actually differ, so both are rendered adjacently here.
    KptTheme {
        Column {
            ScheduleRow(row = previewRows().first(), isEven = true)
            ScheduleRow(row = previewRows()[1], isEven = false)
        }
    }
}

@Preview
@Composable
internal fun AmortizationTablePreview() {
    KptTheme {
        AmortizationTable(rows = previewRows(count = 6))
    }
}

@Preview
@Composable
internal fun ScheduleTotalRowPreview() {
    KptTheme {
        ScheduleTotalRow(rows = previewRows(count = 6))
    }
}

@Preview
@Composable
internal fun SummaryLinePreview() {
    KptTheme {
        Column {
            SummaryLine(label = "Total principal", value = "$100,000.00")
            SummaryLine(label = "Total interest", value = "$18,420.51")
        }
    }
}
