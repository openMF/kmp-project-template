/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.crypto.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * `CoinDetailScreen` itself is not previewed: it resolves its ViewModel through Koin, so rendering
 * it outside a running graph would fail. The stateless pieces below are what carry the visuals.
 *
 * The literals here are PREVIEW FIXTURE DATA, not shipped copy — they exist only to give the
 * renderer something to lay out and are never reachable from the running app, so they carry
 * not translated into every locale — G-SOURCE-I18N excludes `*Preview.kt` from its scan
 * (RULE-IMPL-NO-HARDCODED-STRING-001).
 */

@Preview
@Composable
internal fun StatRowPreview() {
    KptTheme {
        StatRow(label = "Market cap rank", value = "#1")
    }
}

@Preview
@Composable
internal fun StatRowGroupPreview() {
    // The stat block as the detail screen actually stacks it — a long value beside a short one is
    // where label/value column balance breaks, and a single row never shows that.
    KptTheme {
        Column {
            StatRow(label = "Market cap", value = "$1,284,000,000,000")
            StatRow(label = "24h high", value = "$51,000.00")
            StatRow(label = "24h low", value = "$49,000.00")
            StatRow(label = "Circulating supply", value = "19,000,000 BTC")
        }
    }
}
