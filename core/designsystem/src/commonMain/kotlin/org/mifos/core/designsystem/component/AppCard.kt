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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mifos.core.designsystem.theme.elevation
import org.mifos.core.designsystem.theme.spacing

/**
 * Opinionated card surface for grouping related content (loan rows, bill rows, dashboard
 * sections). Uses [MaterialTheme.colorScheme.surfaceContainerHigh] for tonal lift over the
 * page background, [MaterialTheme.elevation.low] shadow, and [MaterialTheme.spacing.lg]
 * internal padding by default.
 *
 * Prefer [AppCard] over raw [Card] / [Surface] — it enforces consistent lift, padding, and
 * shape so screens look unified without per-call wiring.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.spacing.lg),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = MaterialTheme.elevation.low,
        ),
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
