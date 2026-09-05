/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.affordability

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.core.domain.demo.calc.AffordabilityResult
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * `AffordabilityCalculatorScreen` is the stateful wrapper (it resolves its ViewModel through Koin);
 * `AffordabilityCalculatorScreenContent` is the stateless body rendered here.
 *
 * Literals are PREVIEW FIXTURE DATA — never reachable from the running app — hence `// i18n:skip`.
 */

@Preview
@Composable
internal fun AffordabilityCalculatorScreenContentPreview() {
    KptTheme {
        AffordabilityCalculatorScreenContent(
            state = AffordabilityState(),
            result = AffordabilityResult(
                maxEmi = 1_500.0,
                maxPrincipal = 193_500.0,
                rationale = "40% of $5,000 income minus $500 obligations", // i18n:skip
            ),
            onBackClick = {},
            onIncomeChange = {},
            onObligationsChange = {},
            onDtiPercentChange = {},
            onRateChange = {},
            onTenureChange = {},
        )
    }
}

@Preview
@Composable
internal fun AffordabilityCalculatorScreenContentNothingAffordablePreview() {
    // Obligations at or above the DTI allowance zero the headroom. The calculator has to say so
    // with a real rationale rather than rendering a $0 hero that reads like a loading state.
    KptTheme {
        AffordabilityCalculatorScreenContent(
            state = AffordabilityState(monthlyIncome = 2_000.0, monthlyObligations = 2_000.0),
            result = AffordabilityResult(
                maxEmi = 0.0,
                maxPrincipal = 0.0,
                rationale = "Existing obligations already exceed the debt-to-income allowance", // i18n:skip
            ),
            onBackClick = {},
            onIncomeChange = {},
            onObligationsChange = {},
            onDtiPercentChange = {},
            onRateChange = {},
            onTenureChange = {},
        )
    }
}
