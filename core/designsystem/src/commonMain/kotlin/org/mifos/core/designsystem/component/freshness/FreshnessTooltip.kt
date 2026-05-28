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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mifos.core.designsystem.component.FreshnessState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * M3 [RichTooltip] anchored to [content], surfacing the underlying freshness story
 * for users who want detail beyond the [FreshnessIndicator] dot.
 *
 * Title — wall-clock of last fetch ("Updated 2m ago" / "Never fetched").
 * Body — what's happening next based on [state]:
 *  - FRESH    → "Live data."
 *  - STALE    → "Tap to refresh."
 *  - UPDATING → "Refreshing now…"
 *  - OFFLINE  → "Currently offline. Showing cached data."
 *
 * Usage:
 * ```kotlin
 * FreshnessTooltip(state = uiState.freshness, fetchedAt = uiState.fetchedAt) {
 *     FreshnessIndicator(state = uiState.freshness, label = "Loans")
 * }
 * ```
 *
 * @param state Current freshness state — drives the body copy.
 * @param fetchedAt Wall-clock instant of last successful fetch.
 * @param content The composable the tooltip is anchored to.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun FreshnessTooltip(
    state: FreshnessState,
    modifier: Modifier = Modifier,
    fetchedAt: Instant? = null,
    content: @Composable () -> Unit,
) {
    val tooltipState = rememberTooltipState()
    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                title = { Text(text = buildTitle(fetchedAt), style = MaterialTheme.typography.titleSmall) },
                text = { Text(text = buildBody(state), style = MaterialTheme.typography.bodySmall) },
            )
        },
        state = tooltipState,
        content = { content() },
    )
}

@OptIn(ExperimentalTime::class)
private fun buildTitle(fetchedAt: Instant?): String {
    if (fetchedAt == null) return "Never fetched"
    val age = kotlin.time.Clock.System.now() - fetchedAt
    return "Updated ${formatAgo(age)}"
}

private fun buildBody(state: FreshnessState): String = when (state) {
    FreshnessState.Fresh -> "Live data."
    FreshnessState.Stale -> "Tap to refresh."
    FreshnessState.Updating -> "Refreshing now…"
    FreshnessState.Offline -> "Currently offline. Showing cached data."
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
