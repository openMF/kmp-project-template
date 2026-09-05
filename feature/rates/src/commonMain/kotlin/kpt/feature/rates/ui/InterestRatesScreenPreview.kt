/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.rates.ui

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.economic.InterestRateSeries
import kpt.core.model.demo.economic.RateObservation
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * The `*Screen` entry composable is never previewed: it resolves its ViewModel through Koin.
 * Literals below are PREVIEW FIXTURE DATA, never reachable from the running app — hence
 * G-SOURCE-I18N excludes `*Preview.kt` from its scan rather than asking for them to be translated.
 */

internal fun previewRateSeries(
    current: Double = 5.33,
    values: List<Double> = listOf(5.10, 5.18, 5.25, 5.33),
) = InterestRateSeries(
    seriesId = "FEDFUNDS",
    name = "Federal Funds Effective Rate",
    current = current,
    unit = "%",
    observations = values.mapIndexed { i, v ->
        RateObservation(date = LocalDate(2026, 1, i + 1), value = v)
    },
)

@Preview
@Composable
internal fun RateRowCardContentPreview() {
    KptTheme {
        RateRowCard(
            state = ScreenState.Content(previewRateSeries()),
            onRetry = {},
            onSeriesClick = {},
        )
    }
}

@Preview
@Composable
internal fun RateRowCardErrorPreview() {
    // Each row owns its OWN ScreenState (the independent-cards pattern), so one series failing
    // must render an in-place error card with a retry — not blank space, and not a whole-screen
    // error. That per-card treatment only shows up in this state.
    KptTheme {
        RateRowCard(
            state = ScreenState.Error(IllegalStateException("FRED unavailable")),
            onRetry = {},
            onSeriesClick = {},
        )
    }
}

@Preview
@Composable
internal fun RateRowContentFallingRatePreview() {
    // A falling series drives the opposite accent stripe from the rising fixture above.
    KptTheme {
        RateRowContent(
            series = previewRateSeries(current = 4.90, values = listOf(5.33, 5.20, 5.05, 4.90)),
            onClick = {},
        )
    }
}
