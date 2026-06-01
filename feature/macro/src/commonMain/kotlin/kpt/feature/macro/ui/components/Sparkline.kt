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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lightweight inline sparkline. Plots [values] horizontally as a polyline
 * inside [modifier]'s bounds, mapping min..max to bottom..top.
 *
 * Renders no axis, no labels, no grid. Use for the IndicatorCard's 10-year
 * trend hint — the dashboard's detail screen surfaces full series with a
 * Text-based table since no charting primitive exists in this template.
 *
 * Missing values (null) split the path so the line "skips" gaps cleanly
 * rather than dropping to zero — World Bank data is famously sparse for
 * small economies.
 *
 * When fewer than 2 non-null values are present, the canvas renders an
 * em-dash centred in the area to signal "not enough data" rather than
 * crashing on a degenerate path.
 *
 * @param values Y-axis values, ordered ascending by their X position (year).
 * @param modifier Compose modifier; should provide a finite size (typically a
 *   row in an [IndicatorCard] with a fixed height).
 * @param color Stroke color; defaults to the Material 3 primary.
 * @param strokeWidth Line thickness.
 * @param height Fixed height for the sparkline canvas. Defaults to 48dp.
 */
@Composable
fun Sparkline(
    values: List<Double?>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp,
    height: Dp = 48.dp,
) {
    // Wrap in a single Box so detekt's ModifierReused doesn't fire on the
    // two conditional render paths — the caller's modifier is consumed once,
    // and the body composes the right child against fillMaxSize.
    Box(modifier = modifier.height(height)) {
        if (values.count { it != null } < 2) {
            SparklineEmpty()
        } else {
            SparklineCanvas(values, color, strokeWidth)
        }
    }
}

@Composable
private fun SparklineEmpty() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "—",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SparklineCanvas(
    values: List<Double?>,
    color: Color,
    strokeWidth: Dp,
) {
    val finiteValues = values.mapNotNull { it }
    val minValue = finiteValues.min()
    val maxValue = finiteValues.max()
    // Guard a horizontal "flat" series so the divide-by-zero in normalization
    // doesn't NaN out and the path collapses to a single straight line at the
    // canvas midline.
    val spread = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0
    val stroke = Stroke(width = strokeWidth.value)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val widthPx = size.width
        val heightPx = size.height
        if (widthPx <= 0f || heightPx <= 0f) return@Canvas

        val stepX = if (values.size > 1) widthPx / (values.size - 1) else 0f
        val path = Path()
        var penDown = false
        values.forEachIndexed { i, raw ->
            if (raw == null) {
                penDown = false
                return@forEachIndexed
            }
            val x = i * stepX
            val normalized = ((raw - minValue) / spread).toFloat() // 0..1
            // Compose canvas Y grows downward — flip so larger values draw higher.
            val y = heightPx - (normalized * heightPx)
            if (!penDown) {
                path.moveTo(x, y)
                penDown = true
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(path = path, color = color, style = stroke)
        // Tail dot so single-segment series have a visible terminator.
        val lastIdx = values.indexOfLast { it != null }
        if (lastIdx >= 0) {
            val raw = values[lastIdx]!!
            val x = lastIdx * stepX
            val normalized = ((raw - minValue) / spread).toFloat()
            val y = heightPx - (normalized * heightPx)
            drawCircle(color = color, radius = strokeWidth.value * 1.5f, center = Offset(x, y))
        }
    }
}
