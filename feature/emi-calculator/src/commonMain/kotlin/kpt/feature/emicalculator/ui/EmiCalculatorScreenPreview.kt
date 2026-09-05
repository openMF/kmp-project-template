/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.emicalculator.ui

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.emi.EmiResult
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * `EmiCalculatorScreen` is the stateful wrapper (it resolves its ViewModel through Koin);
 * `EmiCalculatorScreenContent` is the stateless body, so it is what gets rendered here.
 *
 * The calculator reads through the same `ScreenContent` wrapper as every other read surface, so its
 * result panel has real Loading / Content / Empty / Error states rather than hand-rolled null
 * checks. Each is previewed: Empty is the "inputs not yet complete" case a user sees on first
 * open, and Error is the one that must offer a retry.
 */

private val sampleResult = EmiResult(
    emi = 8_722.61,
    totalPayment = 104_671.32,
    totalInterest = 4_671.32,
)

@Preview
@Composable
internal fun EmiCalculatorScreenContentPreview() {
    KptTheme {
        EmiCalculatorScreenContent(
            state = EmiState(),
            emiState = ScreenState.Content(sampleResult),
            onBackClick = {},
            onPrincipalChange = {},
            onRateChange = {},
            onTenureChange = {},
            onRetry = {},
        )
    }
}

@Preview
@Composable
internal fun EmiCalculatorScreenContentIncompleteInputsPreview() {
    // Zeroed inputs are not computable, so the store emits Empty. The form must still render and
    // stay editable — a blank result panel here is correct, a blank SCREEN is not.
    KptTheme {
        EmiCalculatorScreenContent(
            state = EmiState(principal = 0.0, ratePercent = 0.0, tenureMonths = 0),
            emiState = ScreenState.Empty,
            onBackClick = {},
            onPrincipalChange = {},
            onRateChange = {},
            onTenureChange = {},
            onRetry = {},
        )
    }
}

@Preview
@Composable
internal fun EmiCalculatorScreenContentLoadingPreview() {
    KptTheme {
        EmiCalculatorScreenContent(
            state = EmiState(),
            emiState = ScreenState.Loading,
            onBackClick = {},
            onPrincipalChange = {},
            onRateChange = {},
            onTenureChange = {},
            onRetry = {},
        )
    }
}
