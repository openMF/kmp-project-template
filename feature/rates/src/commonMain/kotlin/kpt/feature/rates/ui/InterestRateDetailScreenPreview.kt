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
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier — see InterestRatesScreenPreview.kt for the
 * full rationale. The `previewRateSeries` fixture is shared from that file (same package).
 */

@Preview
@Composable
internal fun ChartCardRisingPreview() {
    KptTheme {
        ChartCard(series = previewRateSeries())
    }
}

@Preview
@Composable
internal fun ChartCardFlatSeriesPreview() {
    // A flat series collapses the chart's min/max range to zero — the div-by-range case that a
    // rising fixture never exercises and that renders as a degenerate plot when unguarded.
    KptTheme {
        ChartCard(
            series = previewRateSeries(current = 5.0, values = listOf(5.0, 5.0, 5.0, 5.0)),
        )
    }
}
