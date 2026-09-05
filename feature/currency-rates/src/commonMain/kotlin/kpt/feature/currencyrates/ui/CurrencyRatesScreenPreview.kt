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

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.currency.ExchangeRates
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * The `*Screen` entry composables are not previewed: they resolve their ViewModel through Koin.
 * Literals below are PREVIEW FIXTURE DATA — never reachable from the running app.
 */

internal fun previewRates() = ExchangeRates(
    base = "USD",
    date = "2026-01-01",
    rates = mapOf(
        "EUR" to 0.92,
        "GBP" to 0.79,
        "INR" to 83.12,
        "JPY" to 147.85,
    ),
)

@Preview
@Composable
internal fun CurrencyConverterCardContentPreview() {
    KptTheme {
        CurrencyConverterCard(
            amount = "100",
            targetCode = "EUR",
            spotState = ScreenState.Content(previewRates()),
            onAmountChange = {},
            onTargetChange = {},
            onRetry = {},
        )
    }
}

@Preview
@Composable
internal fun CurrencyConverterCardOfflinePreview() {
    // The converter is the CACHE_ONLY / NETWORK_ONLY archetype demo: with no network the spot
    // lookup errors and the card must offer a retry rather than silently showing a stale figure
    // as if it were live.
    KptTheme {
        CurrencyConverterCard(
            amount = "100",
            targetCode = "EUR",
            spotState = ScreenState.Error(IllegalStateException("offline"), isNetworkError = true),
            onAmountChange = {},
            onTargetChange = {},
            onRetry = {},
        )
    }
}

@Preview
@Composable
internal fun RateItemPreview() {
    KptTheme {
        Column {
            RateItem(code = "EUR", rate = 0.92)
            RateItem(code = "JPY", rate = 147.85)
        }
    }
}
