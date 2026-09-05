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

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.banking.LoanKind
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier — see LoanDetailScreenPreview.kt for the
 * full rationale. The `previewLoan` fixture is shared from that file (same package).
 */

@Preview
@Composable
internal fun SummaryHeroPreview() {
    KptTheme {
        SummaryHero(
            ui = LoansListUiState(
                loans = listOf(
                    previewLoan(),
                    previewLoan(id = "loan-2", name = "Home loan", kind = LoanKind.MORTGAGE), // i18n:skip
                ),
                totalMonthlyEmi = 4_152.0,
                totalPrincipalRemaining = 125_000.0,
            ),
        )
    }
}

@Preview
@Composable
internal fun SummaryHeroNoLoansPreview() {
    // Zero loans still renders the hero — with zeroed totals rather than a blank card. This is the
    // first thing a new user sees, and it is the state a totals-only fixture never covers.
    KptTheme {
        SummaryHero(
            ui = LoansListUiState(
                loans = emptyList(),
                totalMonthlyEmi = 0.0,
                totalPrincipalRemaining = 0.0,
            ),
        )
    }
}
