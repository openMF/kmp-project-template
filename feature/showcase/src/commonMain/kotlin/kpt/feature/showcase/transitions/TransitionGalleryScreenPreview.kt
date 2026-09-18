/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.showcase.transitions

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier — see StateGalleryScreenPreview.kt for the
 * full rationale.
 *
 * `TransitionGalleryScreen` is a dev-only gallery with no Store and no ViewModel, so the whole
 * screen renders directly. What a still frame CANNOT capture is the motion itself — these previews
 * cover the gallery's LAYOUT (the variant list and its affordances); the transitions are verified
 * on device, not here.
 */

@Preview
@Composable
internal fun TransitionGalleryScreenPreview() {
    KptTheme {
        TransitionGalleryScreen(onNavigateToDemo = {}, onBackClick = {})
    }
}

@Preview
@Composable
internal fun TransitionGalleryScreenNarrowPreview() {
    // A narrow width is where the variant rows' labels start to wrap against their affordances.
    KptTheme {
        TransitionGalleryScreen(
            onNavigateToDemo = {},
            onBackClick = {},
            modifier = Modifier.width(320.dp),
        )
    }
}
