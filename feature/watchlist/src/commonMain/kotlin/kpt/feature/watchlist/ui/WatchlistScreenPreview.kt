/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.watchlist.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.watchlist.WatchlistItem
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * `WatchlistScreen` is not previewed: it resolves its ViewModel through Koin.
 * Literals are PREVIEW FIXTURE DATA — never reachable from the running app.
 */

@Preview
@Composable
internal fun WatchlistRowPreview() {
    KptTheme {
        WatchlistRow(
            item = WatchlistItem(coinId = "bitcoin", addedAtMs = 1_700_000_000_000L),
            onRemove = {},
        )
    }
}

@Preview
@Composable
internal fun WatchlistRowListPreview() {
    // Coin ids vary a lot in length; stacking a short one against a long one is where the row's
    // id/remove-affordance alignment breaks first.
    KptTheme {
        Column {
            WatchlistRow(WatchlistItem("btc", 1_700_000_000_000L), onRemove = {})
            WatchlistRow(WatchlistItem("ethereum-classic", 1_700_000_000_000L), onRemove = {})
        }
    }
}
