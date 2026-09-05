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
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.banking.Loan
import kpt.core.model.demo.banking.LoanKind
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * `LoanDetailScreen` is not previewed: it resolves its ViewModel through Koin.
 * Literals below are PREVIEW FIXTURE DATA — never reachable from the running app.
 */

internal fun previewLoan(
    id: String = "loan-1",
    name: String = "Car loan",
    kind: LoanKind = LoanKind.AUTO,
    principalRemaining: Double = 62_500.0,
    monthsRemaining: Int = 38,
) = Loan(
    id = id,
    name = name,
    kind = kind,
    principal = 100_000.0,
    principalRemaining = principalRemaining,
    annualRatePercent = 9.0,
    tenureMonths = 60,
    monthsRemaining = monthsRemaining,
    monthlyPayment = 2_076.0,
    nextDueDate = LocalDate(2026, 10, 5),
    totalPaid = 37_500.0,
    createdAtMs = 1_700_000_000_000L,
    updatedAtMs = 1_700_000_000_000L,
)

@Preview
@Composable
internal fun LoanDetailContentPreview() {
    KptTheme {
        LoanDetailContent(
            loan = previewLoan(),
            onEditClick = {},
            onAmortizationClick = {},
            onDeleteClick = {},
        )
    }
}

@Preview
@Composable
internal fun LoanDetailContentFullyRepaidPreview() {
    // A fully-repaid loan zeroes the remaining balance and the months-left counter — the end-state
    // the progress treatment has to handle without dividing by a zero-length remaining term.
    KptTheme {
        LoanDetailContent(
            loan = previewLoan(principalRemaining = 0.0, monthsRemaining = 0),
            onEditClick = {},
            onAmortizationClick = {},
            onDeleteClick = {},
        )
    }
}

@Preview
@Composable
internal fun MetricRowPreview() {
    KptTheme {
        Column {
            MetricRow(label = "Interest rate", value = "9.00%")
            MetricRow(label = "Next due", value = "5 Oct 2026")
        }
    }
}
