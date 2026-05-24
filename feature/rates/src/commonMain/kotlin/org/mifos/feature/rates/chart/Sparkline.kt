/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.rates.chart

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Minimal Canvas-based sparkline — a one-pass connected polyline whose y-axis is
 * normalized to the series's own `[min, max]` range. No external chart dependency.
 *
 * Drawn purely with [Canvas] + [Path.lineTo]; the geometry computation lives in
 * [SparklineGeometry] for unit-testability.
 *
 * Per plan Phase 1: the toolkit chose **option B — custom Canvas** to avoid taking
 * on a third-party chart library while still showing a 365-day rate trend at row-
 * card scale. Visual density and visual quality decisions can be revisited later
 * by swapping in a richer primitive (e.g. Vico) without touching call sites.
 *
 * @param values Ordered time series (oldest → newest). Empty list renders nothing.
 * @param modifier Layout modifier — caller supplies `Modifier.size(...)` or `.fillMaxWidth().height(...)`.
 * @param color Stroke color. Defaults to the primary brand color.
 * @param strokeWidth Stroke thickness.
 */
@Composable
fun Sparkline(
    values: List<Double>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 1.5.dp,
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas

        val points = SparklineGeometry.normalize(
            values = values,
            width = size.width,
            height = size.height,
        )
        if (points.isEmpty()) return@Canvas

        val path = Path().apply {
            moveTo(points.first().first, points.first().second)
            points.drop(1).forEach { (x, y) -> lineTo(x, y) }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth.toPx()),
        )
    }
}
