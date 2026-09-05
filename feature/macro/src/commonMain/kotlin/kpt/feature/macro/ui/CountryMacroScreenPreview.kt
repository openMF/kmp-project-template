/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.ui

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.economic.IndicatorKind
import kpt.core.model.demo.economic.IndicatorObservation
import kpt.core.model.demo.economic.MacroIndicator
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier — see CountryPickerScreenPreview.kt for the
 * full rationale.
 *
 * `CountryMacroScreen` is the stateful wrapper (it resolves its ViewModel through Koin);
 * `CountryMacroScreenContent` is the stateless body rendered here.
 *
 * This screen is the INDEPENDENT-CARDS demo: each of the three indicators owns its own
 * `ScreenState`, so one slow or failed indicator must never blank the other two. That guarantee is
 * only visible in a MIXED render — the all-Content preview cannot show it — which is why
 * [CountryMacroScreenContentMixedCardStatesPreview] is the important one here.
 */

private fun indicator(kind: IndicatorKind, value: Double) = MacroIndicator(
    countryCode = "US", // i18n:skip
    countryName = "United States", // i18n:skip
    indicator = kind,
    observations = listOf(
        IndicatorObservation(year = 2024, value = value),
        IndicatorObservation(year = 2025, value = value * 1.02),
    ),
)

@Preview
@Composable
internal fun CountryMacroScreenContentPreview() {
    KptTheme {
        CountryMacroScreenContent(
            uiState = MacroUiState(
                countryCode = "US", // i18n:skip
                gdp = ScreenState.Content(indicator(IndicatorKind.GDP, 27_360_000_000_000.0)),
                inflation = ScreenState.Content(indicator(IndicatorKind.INFLATION_CPI, 3.1)),
                unemployment = ScreenState.Content(indicator(IndicatorKind.UNEMPLOYMENT, 3.9)),
            ),
            onBackClick = {},
            onPickCountry = {},
            onOpenIndicator = {},
            onRefreshAll = {},
            onRetryIndicator = {},
        )
    }
}

@Preview
@Composable
internal fun CountryMacroScreenContentMixedCardStatesPreview() {
    // One card loaded, one still loading, one failed — the whole point of IndependentCardLayout.
    // A regression that hoisted state to the screen would render this as a single error or a single
    // spinner, losing the two cards that are perfectly usable.
    KptTheme {
        CountryMacroScreenContent(
            uiState = MacroUiState(
                countryCode = "US", // i18n:skip
                gdp = ScreenState.Content(indicator(IndicatorKind.GDP, 27_360_000_000_000.0)),
                inflation = ScreenState.Loading,
                unemployment = ScreenState.Error(IllegalStateException("World Bank unavailable")), // i18n:skip
            ),
            onBackClick = {},
            onPickCountry = {},
            onOpenIndicator = {},
            onRefreshAll = {},
            onRetryIndicator = {},
        )
    }
}

@Preview
@Composable
internal fun CountryMacroScreenContentAllLoadingPreview() {
    // Cold start: nothing resolved yet, so DashboardProgressBar reads "0 of 3 loaded".
    KptTheme {
        CountryMacroScreenContent(
            uiState = MacroUiState(countryCode = "US"), // i18n:skip
            onBackClick = {},
            onPickCountry = {},
            onOpenIndicator = {},
            onRefreshAll = {},
            onRetryIndicator = {},
        )
    }
}
