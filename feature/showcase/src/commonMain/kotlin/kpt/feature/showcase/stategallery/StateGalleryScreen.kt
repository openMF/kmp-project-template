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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.base.store.screen.DataFreshness
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.DefaultErrorContent
import kpt.core.base.ui.screen.DefaultNoNetworkContent
import kpt.core.base.ui.screen.LocalScreenStateDefaults
import kpt.core.designsystem.component.state.CardLoadingSkeleton
import kpt.core.designsystem.component.state.CardStateBox
import kpt.core.designsystem.component.state.ErrorChip
import kpt.core.designsystem.component.state.InlineErrorPill
import kpt.core.designsystem.component.state.RowLoadingShimmer
import kpt.core.designsystem.theme.spacing
import kpt.core.ui.scaffold.KptScaffold

/**
 * Dev-only screen exhibiting every [ScreenState] variant and each of the 5 component-scale
 * primitives ([InlineErrorPill], [ErrorChip], [CardLoadingSkeleton], [RowLoadingShimmer],
 * [CardStateBox]). Visual smoke test for the state-UI surface area.
 *
 * Not meant for production builds — gate the entry point on `!isReleaseBuild()`.
 */
@Composable
fun StateGalleryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    val defaults = LocalScreenStateDefaults.current
    KptScaffold(
        onNavigationIconClick = onBackClick,
        title = "State Gallery (dev)",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = sp.lg, vertical = sp.md),
            verticalArrangement = Arrangement.spacedBy(sp.lg),
        ) {
            Text(
                text = "Visual smoke test for every ScreenState variant + each " +
                    "component-scale state primitive shipped by core/designsystem.",
                style = MaterialTheme.typography.bodyMedium,
            )

            SectionHeader("Component-scale primitives")

            // ── InlineErrorPill ─────────────────────────────────────────
            LabelRow("InlineErrorPill (no retry)") {
                InlineErrorPill(message = "Couldn't refresh balance")
            }
            LabelRow("InlineErrorPill (with retry)") {
                InlineErrorPill(message = "Network error", onRetry = {})
            }

            // ── ErrorChip ───────────────────────────────────────────────
            LabelRow("ErrorChip") {
                ErrorChip(message = "Failed to load", onClick = {})
            }

            // ── RowLoadingShimmer ───────────────────────────────────────
            SectionHeader("RowLoadingShimmer")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    repeat(3) {
                        RowLoadingShimmer()
                        if (it < 2) HorizontalDivider()
                    }
                }
            }

            // ── CardLoadingSkeleton ─────────────────────────────────────
            SectionHeader("CardLoadingSkeleton")
            CardLoadingSkeleton()

            // ── CardStateBox ────────────────────────────────────────────
            SectionHeader("CardStateBox — every variant")
            Text("Loading", style = MaterialTheme.typography.labelMedium)
            CardStateBox<String>(state = ScreenState.Loading) { Text(it) }
            Text("Empty", style = MaterialTheme.typography.labelMedium)
            CardStateBox<String>(state = ScreenState.Empty) { Text(it) }
            Text("Error", style = MaterialTheme.typography.labelMedium)
            CardStateBox<String>(state = ScreenState.Error(RuntimeException("balance fetch failed"))) {
                Text(it)
            }
            Text("NoNetwork", style = MaterialTheme.typography.labelMedium)
            CardStateBox<String>(state = ScreenState.NoNetwork(isCaptivePortal = false)) {
                Text(it)
            }
            Text("Content", style = MaterialTheme.typography.labelMedium)
            CardStateBox<String>(
                state = ScreenState.Content(
                    data = "Account: \$1,234.56",
                    freshness = DataFreshness.FRESH,
                ),
            ) { Card { Text(it, modifier = Modifier.padding(16.dp)) } }

            // ── Full-screen states (compact preview) ─────────────────────
            SectionHeader("Full-screen state defaults (compact preview)")

            PreviewBox(label = "Empty (defaults — title + message)") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    defaults.empty.title?.let {
                        Text(it, style = MaterialTheme.typography.titleMedium)
                    }
                    Text(defaults.empty.message, style = MaterialTheme.typography.bodyMedium)
                }
            }

            PreviewBox(label = "Error full-screen") {
                DefaultErrorContent(
                    error = RuntimeException("Demo error: server unreachable"),
                    onRetry = {},
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                )
            }

            PreviewBox(label = "NoNetwork — captive portal CTA") {
                DefaultNoNetworkContent(
                    onRetry = {},
                    isCaptivePortal = true,
                    modifier = Modifier.fillMaxWidth().height(320.dp),
                )
            }

            PreviewBox(label = "NoNetwork — plain offline") {
                DefaultNoNetworkContent(
                    onRetry = {},
                    isCaptivePortal = false,
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun LabelRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun PreviewBox(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(modifier = Modifier.fillMaxWidth()) { content() }
    }
}
