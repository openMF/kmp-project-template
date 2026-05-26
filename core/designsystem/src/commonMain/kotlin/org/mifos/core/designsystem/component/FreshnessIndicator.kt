/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.mifos.core.designsystem.theme.finance
import template.core.base.ui.motion.mifosRefreshingPulse

/**
 * Tiny dot + label showing how fresh the displayed data is. Useful inline below
 * dashboard amounts or next to list headers so users understand whether they're
 * looking at live data, a stale cache, or an offline snapshot.
 *
 * When [state] == [FreshnessState.Updating], the dot pulses using
 * [MaterialTheme.motion.refreshingPulseDurationMs].
 */
@Composable
fun FreshnessIndicator(
    state: FreshnessState,
    label: String,
    modifier: Modifier = Modifier,
) {
    val color = resolveFreshnessColor(state)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FreshnessDot(color = color, pulsing = state == FreshnessState.Updating)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FreshnessDot(color: Color, pulsing: Boolean) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
            .mifosRefreshingPulse(active = pulsing),
    )
}

@Composable
private fun resolveFreshnessColor(state: FreshnessState): Color {
    val f = MaterialTheme.finance
    return when (state) {
        FreshnessState.Fresh -> f.freshnessFresh
        FreshnessState.Stale -> f.freshnessStale
        FreshnessState.Updating -> f.freshnessUpdating
        FreshnessState.Offline -> f.freshnessOffline
    }
}
