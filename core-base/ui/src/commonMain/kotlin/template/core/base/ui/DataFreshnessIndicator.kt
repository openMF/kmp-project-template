/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import template.core.base.store.DataFreshness
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Compact staleness banner shown above content when data is not fresh.
 *
 * - **FRESH** → hidden (no banner)
 * - **STALE** → "Offline · Updated 5m ago" with cloud-off icon
 * - **UPDATING** → thin progress bar + "Refreshing..." label
 *
 * @param freshness Current data freshness state.
 * @param fetchedAt Wall-clock instant of last successful fetch for human-readable timestamp.
 * @param staleLabel Custom stale message. Null uses default with timestamp.
 * @param updatingLabel Custom updating message. Null uses "Refreshing...".
 */
@OptIn(ExperimentalTime::class)
@Composable
fun DataFreshnessIndicator(
    freshness: DataFreshness,
    modifier: Modifier = Modifier,
    fetchedAt: Instant? = null,
    staleLabel: String? = null,
    updatingLabel: String? = null,
) {
    AnimatedVisibility(
        visible = freshness != DataFreshness.FRESH,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
        modifier = modifier,
    ) {
        when (freshness) {
            DataFreshness.STALE -> StaleBanner(
                fetchedAt = fetchedAt,
                customLabel = staleLabel,
            )
            DataFreshness.UPDATING -> UpdatingBanner(
                fetchedAt = fetchedAt,
                customLabel = updatingLabel,
            )
            DataFreshness.FRESH -> { /* hidden */ }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun StaleBanner(
    fetchedAt: Instant?,
    customLabel: String?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = customLabel ?: buildStaleText(fetchedAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun UpdatingBanner(
    fetchedAt: Instant?,
    customLabel: String?,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = customLabel ?: buildUpdatingText(fetchedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
internal fun buildStaleText(fetchedAt: Instant?): String {
    if (fetchedAt == null) return "Offline"
    val age = kotlin.time.Clock.System.now() - fetchedAt
    return "Offline \u00b7 Updated ${formatDurationAgo(age)}"
}

@OptIn(ExperimentalTime::class)
internal fun buildUpdatingText(fetchedAt: Instant?): String {
    if (fetchedAt == null) return "Refreshing\u2026"
    val age = kotlin.time.Clock.System.now() - fetchedAt
    return "Refreshing \u00b7 Last updated ${formatDurationAgo(age)}"
}

internal fun formatDurationAgo(duration: Duration): String {
    val seconds = duration.inWholeSeconds
    return when {
        seconds < 60 -> "just now"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        else -> "${seconds / 86400}d ago"
    }
}
