/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.component.AppCard
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.demo.economic.IndicatorKind
import kpt.core.model.demo.economic.MacroIndicator
import kpt.feature.macro.generated.resources.Res
import kpt.feature.macro.generated.resources.screens_macro_card_auth_required
import kpt.feature.macro.generated.resources.screens_macro_card_captive_portal
import kpt.feature.macro.generated.resources.screens_macro_card_empty
import kpt.feature.macro.generated.resources.screens_macro_card_generic_error
import kpt.feature.macro.generated.resources.screens_macro_card_latest_year
import kpt.feature.macro.generated.resources.screens_macro_card_loading
import kpt.feature.macro.generated.resources.screens_macro_card_offline
import kpt.feature.macro.generated.resources.screens_macro_card_retry
import org.jetbrains.compose.resources.stringResource

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
    AppCard(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        accentColor = indicatorKind.accentColor(),
    ) {
        Column {
            Text(
                text = indicatorKind.displayName(),
                style = MaterialTheme.typography.titleMedium,
            )
            when (state) {
                is ScreenState.Loading -> LoadingBody()
                is ScreenState.Content -> ContentBody(state.data)
                is ScreenState.Empty -> InlineMessage(
                    text = stringResource(Res.string.screens_macro_card_empty),
                    onRetry = onRetry,
                )
                is ScreenState.NoNetwork -> InlineMessage(
                    text = if (state.isCaptivePortal) {
                        stringResource(Res.string.screens_macro_card_captive_portal)
                    } else {
                        stringResource(Res.string.screens_macro_card_offline)
                    },
                    onRetry = onRetry,
                )
                is ScreenState.Error -> InlineMessage(
                    text = state.error.message
                        ?: stringResource(Res.string.screens_macro_card_generic_error),
                    onRetry = onRetry,
                )
                is ScreenState.Unauthenticated -> InlineMessage(
                    text = stringResource(Res.string.screens_macro_card_auth_required),
                    onRetry = onRetry,
                )
            }
        }
    }
}

/**
 * Maps each indicator to a distinct accent stripe colour so users can identify
 * GDP / Inflation / Unemployment at a glance without re-reading the title.
 */
@Composable
private fun IndicatorKind.accentColor(): Color = when (this) {
    IndicatorKind.GDP -> MaterialTheme.colorScheme.secondary // emerald — growth
    IndicatorKind.INFLATION_CPI -> MaterialTheme.colorScheme.tertiary // amber — warning
    IndicatorKind.UNEMPLOYMENT -> MaterialTheme.colorScheme.error // rose — concern
    IndicatorKind.GDP_PER_CAPITA -> MaterialTheme.colorScheme.secondary
    IndicatorKind.GINI -> MaterialTheme.colorScheme.tertiary
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
                    text = stringResource(Res.string.screens_macro_card_latest_year, year.toString()),
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
            text = stringResource(Res.string.screens_macro_card_loading),
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
        TextButton(onClick = onRetry) { Text(stringResource(Res.string.screens_macro_card_retry)) }
    }
}
