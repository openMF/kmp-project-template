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

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.economic.Country
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * The `*Screen` entry composable is never previewed: it resolves its ViewModel through Koin, so it
 * cannot render outside a running graph. Literals below are PREVIEW FIXTURE DATA — never reachable
 * from the running app — hence `// i18n:skip` rather than a translated resource.
 */

@Preview
@Composable
internal fun CountryRowPreview() {
    KptTheme {
        CountryRow(
            country = Country(code = "US", name = "United States", flagEmoji = "🇺🇸"), // i18n:skip
            onClick = {},
        )
    }
}

@Preview
@Composable
internal fun CountryRowListPreview() {
    // A long name beside a short one is where the row's name/flag alignment actually breaks.
    KptTheme {
        Column {
            CountryRow(Country("US", "United States", "🇺🇸"), onClick = {}) // i18n:skip
            CountryRow(Country("IN", "India", "🇮🇳"), onClick = {}) // i18n:skip
            CountryRow(Country("CD", "Democratic Republic of the Congo", "🇨🇩"), onClick = {}) // i18n:skip
        }
    }
}
