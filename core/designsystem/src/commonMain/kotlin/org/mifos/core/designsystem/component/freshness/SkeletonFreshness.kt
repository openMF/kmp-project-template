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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.mifos.core.designsystem.component.FreshnessState
import template.core.base.designsystem.component.KptShimmerListItem
import template.core.base.ui.motion.kptRefreshingPulse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Skeleton placeholder + small "Last updated …" caption keyed to a [FreshnessState].
 * Use while [FreshnessState.Updating] / [FreshnessState.Stale] to communicate that
 * the framework is replacing this section's data, without dropping the user into a
 * full-screen loader.
 *
 * Renders [rowCount] [KptShimmerListItem] rows above a labelSmall caption showing
 * "Last updated 5m ago" (derived from [fetchedAt]). When [state] is
 * [FreshnessState.Updating] the caption row's dot pulses.
 *
 * @param state Current freshness state — drives the dot color and pulse.
 * @param fetchedAt Optional wall-clock instant of last successful fetch.
 * @param rowCount Number of shimmer rows. Defaults to 3.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun SkeletonFreshness(
    state: FreshnessState,
    modifier: Modifier = Modifier,
    fetchedAt: Instant? = null,
    rowCount: Int = 3,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(rowCount) {
            KptShimmerListItem(modifier = Modifier.fillMaxWidth())
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val color = resolveFreshnessColor(state)
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
                    .kptRefreshingPulse(active = state == FreshnessState.Updating),
            )
            Text(
                text = buildLastUpdatedText(fetchedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun buildLastUpdatedText(fetchedAt: Instant?): String {
    if (fetchedAt == null) return "Refreshing…"
    val age = kotlin.time.Clock.System.now() - fetchedAt
    return "Last updated ${formatAgo(age)}"
}

private fun formatAgo(duration: Duration): String {
    val seconds = duration.inWholeSeconds
    return when {
        seconds < 60.seconds.inWholeSeconds -> "just now"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        else -> "${seconds / 86400}d ago"
    }
}
