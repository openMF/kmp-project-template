/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.calculators.comparison

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.emi.EmiResult
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * `LoanComparisonScreen` is not previewed: it resolves its ViewModel through Koin.
 * Literals are PREVIEW FIXTURE DATA — never reachable from the running app.
 */

private fun result(total: Double) = EmiResult(
    emi = total / 60.0,
    totalPayment = total,
    totalInterest = total - 100_000.0,
)

@Preview
@Composable
internal fun ComparisonHeroPreview() {
    KptTheme {
        ComparisonHero(
            analysis = LoanComparisonAnalysis(
                results = listOf(result(118_000.0), result(126_500.0), result(134_900.0)),
                cheapestIndex = 0,
            ),
        )
    }
}

@Preview
@Composable
internal fun ComparisonHeroNoValidInputsPreview() {
    // `cheapestIndex = -1` is the documented "no scenario has valid inputs" case. The hero has to
    // render a zeroed savings figure rather than index into an empty list.
    KptTheme {
        ComparisonHero(
            analysis = LoanComparisonAnalysis(results = emptyList(), cheapestIndex = -1),
        )
    }
}

@Preview
@Composable
internal fun ScenarioCardCheapestPreview() {
    KptTheme {
        ScenarioCard(
            index = 0,
            scenario = LoanScenario(principal = 100_000.0, ratePercent = 6.5, tenureMonths = 60),
            result = result(118_000.0),
            isCheapest = true,
            onChange = {},
        )
    }
}

@Preview
@Composable
internal fun ScenarioCardNotYetComputedPreview() {
    // `result = null` is the pre-computation state — the card must still render its inputs rather
    // than collapsing, and it must not wear the cheapest highlight.
    KptTheme {
        ScenarioCard(
            index = 1,
            scenario = LoanScenario(principal = 100_000.0, ratePercent = 8.5, tenureMonths = 120),
            result = null,
            isCheapest = false,
            onChange = {},
        )
    }
}

@Preview
@Composable
internal fun ResultStripHighlightedPreview() {
    KptTheme {
        ResultStrip(result = result(118_000.0), highlight = true)
    }
}

@Preview
@Composable
internal fun ResultStripPlainPreview() {
    // `highlight` swaps both container and content colours — the pair is the only way to see the
    // contrast actually holds in each.
    KptTheme {
        ResultStrip(result = result(134_900.0), highlight = false)
    }
}

@Preview
@Composable
internal fun ResultCellPreview() {
    KptTheme {
        ResultCell(label = "Total payable", value = "$118,000.00")
    }
}
