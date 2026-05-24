/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.macro.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.mifos.core.model.economic.IndicatorKind
import org.mifos.core.model.economic.MacroIndicator
import template.core.base.store.screen.ScreenState

/**
 * A single indicator's at-a-glance card on the country-macro dashboard.
 *
 * Renders one of four UI shapes based on [state]:
 * - **Loading** — placeholder rows
 * - **Content** — name, headline value, 10-year sparkline trail, latest-year subtitle
 * - **Error** — error message + per-indicator retry button (so other cards stay live)
 * - **Empty / NoNetwork / Unauthenticated** — short message + retry
 *
 * The card's [onClick] navigates to the indicator's full-history detail screen.
 */
@Composable
fun IndicatorCard(
    indicatorKind: IndicatorKind,
    state: ScreenState<MacroIndicator>,
    onRetry: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = indicatorKind.displayName(),
                style = MaterialTheme.typography.titleMedium,
            )
            when (state) {
                is ScreenState.Loading -> LoadingBody()
                is ScreenState.Content -> ContentBody(state.data)
                is ScreenState.Empty -> InlineMessage(
                    text = "No data published for this indicator.",
                    onRetry = onRetry,
                )
                is ScreenState.NoNetwork -> InlineMessage(
                    text = if (state.isCaptivePortal) {
                        "Behind a captive portal — sign in to continue."
                    } else {
                        "Offline — connect to fetch this indicator."
                    },
                    onRetry = onRetry,
                )
                is ScreenState.Error -> InlineMessage(
                    text = state.error.message ?: "Couldn't load this indicator.",
                    onRetry = onRetry,
                )
                is ScreenState.Unauthenticated -> InlineMessage(
                    text = "Authentication required.",
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun ContentBody(indicator: MacroIndicator) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.padding(end = 16.dp)) {
            Text(
                text = indicator.headlineValue(),
                style = MaterialTheme.typography.headlineSmall,
            )
            indicator.latestYear()?.let { year ->
                Text(
                    text = "Latest: $year",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Sparkline(
            values = indicator.observations.map { it.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        )
    }
}

@Composable
private fun LoadingBody() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(top = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = "Loading…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InlineMessage(text: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp).fillMaxWidth(0.7f),
        )
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}
