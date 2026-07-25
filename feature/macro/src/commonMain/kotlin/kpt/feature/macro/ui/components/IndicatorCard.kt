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
import kpt.core.model.demo.economic.IndicatorKind
import kpt.core.model.demo.economic.MacroIndicator
import kpt.feature.macro.generated.resources.Res
import kpt.feature.macro.generated.resources.screens_macro_card_latest_year
import kpt.feature.macro.generated.resources.screens_macro_card_loading
import kpt.feature.macro.generated.resources.screens_macro_card_retry
import org.jetbrains.compose.resources.stringResource

/**
 * Persistent per-card frame for a macro indicator: the [AppCard] shell + accent stripe +
 * title, rendered identically across every [ScreenState]. Pass to
 * [IndependentCardLayout]'s `cardChrome` so the framework composable owns the
 * loading/empty/error/content dispatch while this owns the card's constant chrome.
 * [content] is the state body ([MacroLoadingBody] / [MacroContentBody] / [MacroInlineMessage]).
 */
@Composable
fun MacroIndicatorCardChrome(
    indicatorKind: IndicatorKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
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
            content()
        }
    }
}

/**
 * Maps each indicator to a distinct accent stripe colour so users can identify
 * GDP / Inflation / Unemployment at a glance without re-reading the title.
 */
@Composable
internal fun IndicatorKind.accentColor(): Color = when (this) {
    IndicatorKind.GDP -> MaterialTheme.colorScheme.secondary // emerald — growth
    IndicatorKind.INFLATION_CPI -> MaterialTheme.colorScheme.tertiary // amber — warning
    IndicatorKind.UNEMPLOYMENT -> MaterialTheme.colorScheme.error // rose — concern
    IndicatorKind.GDP_PER_CAPITA -> MaterialTheme.colorScheme.secondary
    IndicatorKind.GINI -> MaterialTheme.colorScheme.tertiary
}

@Composable
fun MacroContentBody(indicator: MacroIndicator) {
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
fun MacroLoadingBody() {
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
fun MacroInlineMessage(text: String, onRetry: () -> Unit) {
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
