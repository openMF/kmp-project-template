/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.showcase.stategallery

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * `StateGalleryScreen` IS directly previewable — it is a dev-only gallery that hand-builds every
 * `ScreenState` side by side and owns no Store, so it needs no Koin graph. That is also why it
 * carries a `screen_composable_excludes` waiver for the OTHER gate: delegating it to `ScreenContent`
 * would destroy the only thing it exists to do (a Store cannot be asked to produce `NoNetwork` on
 * demand). Previewing the whole screen here is therefore the honest render, not a shortcut.
 *
 * Literals are PREVIEW FIXTURE DATA — never reachable from the shipped app (this screen is gated on
 * `!isReleaseBuild()`).
 */

@Preview
@Composable
internal fun StateGalleryScreenPreview() {
    KptTheme {
        StateGalleryScreen(onBackClick = {})
    }
}

@Preview
@Composable
internal fun StateGalleryLayoutPrimitivesPreview() {
    // The three layout primitives the gallery is built from, rendered together — this is where the
    // section/label/box spacing rhythm is actually visible.
    KptTheme {
        Column {
            SectionHeader(text = "Content states") // i18n:skip
            LabelRow(label = "Loading") { Text("spinner goes here") } // i18n:skip
            PreviewBox(label = "Empty") { Text("nothing to show") } // i18n:skip
        }
    }
}
