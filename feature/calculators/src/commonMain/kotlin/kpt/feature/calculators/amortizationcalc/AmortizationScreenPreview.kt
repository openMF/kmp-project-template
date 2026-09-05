/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.amortizationcalc

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
 * `AmortizationScreen` is not previewed: it resolves its ViewModel through Koin.
 */

@Preview
@Composable
internal fun AmortizationHeaderPreview() {
    KptTheme {
        AmortizationHeader()
    }
}

@Preview
@Composable
internal fun AmortizationRowItemPreview() {
    // Header plus rows together: the columns are laid out independently in each, so alignment
    // drift between them only shows when they are rendered as the screen actually stacks them.
    KptTheme {
        Column {
            AmortizationHeader()
            AmortizationRowItem(
                row = AmortizationRow(
                    month = 1,
                    payment = 2_003.79,
                    principal = 1_378.79,
                    interest = 625.0,
                    balance = 98_621.21,
                ),
            )
            AmortizationRowItem(
                row = AmortizationRow(
                    month = 60,
                    payment = 2_003.79,
                    principal = 1_991.34,
                    interest = 12.45,
                    balance = 0.0,
                ),
            )
        }
    }
}
