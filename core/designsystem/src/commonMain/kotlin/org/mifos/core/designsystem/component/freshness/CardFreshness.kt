/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.designsystem.component.freshness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.mifos.core.designsystem.component.FreshnessState
import template.core.base.ui.motion.kptRefreshingPulse

/**
 * Corner-badge freshness indicator for cards/tiles. Renders an 8.dp colored dot
 * pinned to the top-end of its parent [BoxScope]; visible only when [state] ≠
 * [FreshnessState.Fresh]. When state is [FreshnessState.Updating] the dot pulses
 * via [kptRefreshingPulse].
 *
 * Usage:
 * ```kotlin
 * Box {
 *     LoanSummaryCard()
 *     CardFreshness(state = uiState.freshness, modifier = Modifier.padding(8.dp))
 * }
 * ```
 *
 * Use this when the card chrome itself doesn't have a natural "header row" for an
 * inline [FreshnessIndicator] — the corner badge is unobtrusive but spottable.
 *
 * @param state Current freshness state. When [FreshnessState.Fresh], no badge renders.
 * @param modifier Additional padding/alignment for the badge container.
 */
@Composable
fun BoxScope.CardFreshness(
    state: FreshnessState,
    modifier: Modifier = Modifier,
) {
    if (state == FreshnessState.Fresh) return
    val color = resolveFreshnessColor(state)
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .then(modifier),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
                .kptRefreshingPulse(active = state == FreshnessState.Updating),
        )
    }
}
