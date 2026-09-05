/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.currencyrates.ui

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Instant

/*
 * @Preview siblings for the device-free CMP render tier — see CurrencyRatesScreenPreview.kt for
 * the full rationale.
 */

@Preview
@Composable
internal fun RateHistoryControlsPreview() {
    KptTheme {
        RateHistoryControls(
            currencies = listOf("EUR", "GBP", "INR", "JPY"), // i18n:skip
            periods = listOf(7, 30, 90),
            selectedCurrency = "EUR", // i18n:skip
            selectedPeriod = 30,
            onSelectCurrency = {},
            onSelectPeriod = {},
        )
    }
}

@Preview
@Composable
internal fun RateHistoryControlsLongestPeriodPreview() {
    // Selection drives the chip highlight. Rendering a different currency AND the last period is
    // what shows the selected-chip treatment actually tracks both inputs rather than the first.
    KptTheme {
        RateHistoryControls(
            currencies = listOf("EUR", "GBP", "INR", "JPY"), // i18n:skip
            periods = listOf(7, 30, 90),
            selectedCurrency = "JPY", // i18n:skip
            selectedPeriod = 90,
            onSelectCurrency = {},
            onSelectPeriod = {},
        )
    }
}

@Preview
@Composable
internal fun RateHistoryOfflineDataBannerPreview() {
    KptTheme {
        OfflineDataBanner(fetchedAt = Instant.fromEpochMilliseconds(1_700_000_000_000L))
    }
}

@Preview
@Composable
internal fun RateHistoryOfflineDataBannerNeverFetchedPreview() {
    KptTheme {
        OfflineDataBanner(fetchedAt = null)
    }
}
