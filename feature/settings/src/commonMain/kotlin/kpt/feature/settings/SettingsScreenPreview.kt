/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * Reference @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` via `verifyRoborazziDesktop` — no emulator, no Robolectric. Every fork's
 * `kmp-screen-gen` emits these per screen; this is the template's demonstrator.
 *
 * `SettingsScreen` itself is not previewed: it resolves its ViewModel through Koin. Literals below
 * are PREVIEW FIXTURE DATA — never reachable from the running app — hence `// i18n:skip` rather
 * than a translated string resource.
 */

@Preview
@Composable
internal fun SettingsScreenContentPreview() {
    KptTheme {
        SettingsScreenContent(
            onBackClick = {},
            onThemeCardClick = {},
            onLanguageCardClick = {},
            onSyncAndDraftsClick = {},
        )
    }
}

@Preview
@Composable
internal fun SettingsRowsPreview() {
    // The two shipped rows side by side. Each passes its own accent colour into the shared
    // `SettingsRowCard`, so rendering them together is what shows the accents actually differ
    // rather than both falling back to one theme colour.
    KptTheme {
        Column {
            LanguageCard(onClick = {})
            SyncAndDraftsCard(onClick = {})
        }
    }
}

@Preview
@Composable
internal fun SettingsRowCardLongTitlePreview() {
    // The row primitive on its own, with a title long enough to wrap — the case the two shipped
    // rows (both short) never exercise, and where icon/title/chevron alignment breaks first.
    KptTheme {
        SettingsRowCard(
            icon = AppIcons.Language,
            title = "Change the application display language and region format", // i18n:skip
            contentDescription = "Opens the language picker", // i18n:skip
            accentColor = MaterialTheme.colorScheme.tertiary,
            onClick = {},
        )
    }
}
