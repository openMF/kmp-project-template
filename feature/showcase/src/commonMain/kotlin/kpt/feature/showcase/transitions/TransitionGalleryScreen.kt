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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kpt.core.designsystem.theme.spacing
import kpt.core.ui.scaffold.KptScaffold
import kpt.feature.showcase.TestTags
import kpt.feature.showcase.generated.resources.Res
import kpt.feature.showcase.generated.resources.screens_showcase_transition_gallery_intro
import org.jetbrains.compose.resources.stringResource

/**
 * Dev-only screen exhibiting every transition factory. Each row tile, when
 * tapped, navigates to a destination using a specific transition, then the
 * destination auto-pops after 2s. Visual smoke test for the duration-symmetry
 * invariants enforced by `SharedAxisSpec` and `TransitionPushSpec`.
 *
 * Not meant for production builds — gate the entry point on debug.
 */
@Composable
fun TransitionGalleryScreen(
    onNavigateToDemo: (TransitionVariant) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    KptScaffold(
        onNavigationIconClick = onBackClick,
        title = "Transition Gallery (dev)",
        modifier = modifier.testTag(TestTags.TransitionGallery.SCREEN),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = sp.lg),
        ) {
            Text(
                text = stringResource(Res.string.screens_showcase_transition_gallery_intro),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = sp.md),
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(sp.sm),
            ) {
                items(TransitionVariant.entries) { variant ->
                    Button(
                        onClick = { onNavigateToDemo(variant) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(variant.displayName)
                    }
                }
            }
        }
    }
}

/**
 * The transition factories the gallery exercises.
 *
 * @property displayName what the gallery lists it as.
 */
enum class TransitionVariant(val displayName: String) {
    /** Shared-axis, moving forward/deeper. */
    SharedAxisForward("Shared-axis push forward"),

    /** Shared-axis reversed, for a pop. Mirrors [SharedAxisForward] so a push and its pop read as one movement. */
    SharedAxisBack("Shared-axis pop back"),

    /** Fade-through, for a sibling swap where neither side moves. */
    FadeThrough("Fade-through (siblings)"),

    /** Both sides slide left — the arriving screen enters from the right as the leaving one exits left. */
    PushLeft("Push left (enter+exit)"),

    /** Both sides slide right, the mirror of [PushLeft]. */
    PushRight("Push right (enter+exit)"),

    /** Arrives from the bottom edge — the sheet-like entry. */
    SlideUp("Slide up"),

    /** Arrives from the top edge. */
    SlideDown("Slide down"),

    /** Hold in place while the sibling animates. */
    Stay("Stay (no-op)"),

    /** No animation. */
    None("None"),
}
