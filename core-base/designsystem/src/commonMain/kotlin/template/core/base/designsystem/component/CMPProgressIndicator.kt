/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.component

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import template.core.base.designsystem.component.variant.ProgressIndicatorVariant

/**
 * A unified, customizable progress indicator for the CMP design system that supports both
 * determinate and indeterminate variants in linear and circular styles.
 *
 * The rendered indicator depends on the [variant] value, which maps to Material3's
 * [LinearProgressIndicator] and [CircularProgressIndicator] implementations.
 *
 * @param modifier Modifier applied to the progress indicator container. *(Default: [Modifier])*
 * @param color The color of the progress bar or circle. *(Optional; defaults to variant's theme color)*
 * @param trackColor The color of the background track behind the progress. *(Optional)*
 * @param strokeCap The shape of the line ends for linear indicators or circular arcs. *(Optional)*
 * @param circularStrokeWidth The stroke width for circular indicators. *(Optional)*
 * @param gapSize The visual gap between segments for linear and circular indicators. *(Optional)*
 * @param determinateLinearDrawStopIndicator Optional custom drawing logic for the
 * stop indicator in [ProgressIndicatorVariant.DETERMINATE_LINEAR]. *(Optional)*
 * @param progress Current progress value, expected between `0f` and `1f`.
 * Required for [ProgressIndicatorVariant.DETERMINATE_LINEAR] and [ProgressIndicatorVariant.DETERMINATE_CIRCULAR].*
 * @param variant Determines the style and behavior of the indicator.
 * (Default: [ProgressIndicatorVariant.INDETERMINATE_CIRCULAR])*
 */
@Composable
fun CMPProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color? = null,
    trackColor: Color? = null,
    strokeCap: StrokeCap? = null,
    circularStrokeWidth: Dp? = null,
    gapSize: Dp? = null,
    determinateLinearDrawStopIndicator: (DrawScope.() -> Unit)? = null,
    progress: Float? = null,
    variant: ProgressIndicatorVariant = ProgressIndicatorVariant.INDETERMINATE_CIRCULAR,
) {
    when (variant) {
        ProgressIndicatorVariant.DETERMINATE_LINEAR -> DeterminateLinearIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap,
            gapSize = gapSize,
            progress = progress ?: 0F,
            drawStopIndicator = determinateLinearDrawStopIndicator,
        )

        ProgressIndicatorVariant.DETERMINATE_CIRCULAR -> DeterminateCircularIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap,
            strokeWidth = circularStrokeWidth,
            gapSize = gapSize,
            progress = progress ?: 0F,
        )

        ProgressIndicatorVariant.INDETERMINATE_LINEAR -> IndeterminateLinearIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap,
            gapSize = gapSize,
        )

        ProgressIndicatorVariant.INDETERMINATE_CIRCULAR -> IndeterminateCircularIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap,
            strokeWidth = circularStrokeWidth,
        )
    }
}

/**
 * Internal implementation of a determinate linear progress indicator with optional custom stop indicator.
 *
 * @param color Foreground progress color. *(Optional)*
 * @param trackColor Background track color. *(Optional)*
 * @param strokeCap The shape of the line ends. *(Optional)*
 * @param gapSize Gap between progress segments. *(Optional)*
 * @param progress Current progress value between 0 and 1.
 * @param drawStopIndicator Optional custom drawing logic for the stop indicator. *(Optional)*
 * @param modifier Modifier applied to the indicator. *(Default: [Modifier])*
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeterminateLinearIndicator(
    color: Color?,
    trackColor: Color?,
    strokeCap: StrokeCap?,
    gapSize: Dp?,
    progress: Float,
    drawStopIndicator: (DrawScope.() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val finalColor = color ?: ProgressIndicatorDefaults.linearColor
    val defaultDrawStopIndicator: DrawScope.() -> Unit = {
        ProgressIndicatorDefaults.drawStopIndicator(
            drawScope = this,
            stopSize = ProgressIndicatorDefaults.LinearTrackStopIndicatorSize,
            color = finalColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }

    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier,
        color = finalColor,
        trackColor = trackColor ?: ProgressIndicatorDefaults.linearTrackColor,
        strokeCap = strokeCap ?: ProgressIndicatorDefaults.LinearStrokeCap,
        gapSize = gapSize ?: ProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
        drawStopIndicator = drawStopIndicator ?: defaultDrawStopIndicator,
    )
}

/**
 * Internal implementation of a determinate circular progress indicator.
 *
 * @param color Foreground progress color. *(Optional)*
 * @param trackColor Background track color. *(Optional)*
 * @param strokeCap Arc cap style. *(Optional)*
 * @param strokeWidth Width of the arc stroke. *(Optional)*
 * @param gapSize Gap in the circular arc. *(Optional)*
 * @param progress Current progress value between 0 and 1.
 * @param modifier Modifier applied to the indicator. *(Default: [Modifier])*
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeterminateCircularIndicator(
    color: Color?,
    trackColor: Color?,
    strokeCap: StrokeCap?,
    strokeWidth: Dp?,
    gapSize: Dp?,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    CircularProgressIndicator(
        progress = { progress },
        modifier = modifier,
        color = color ?: ProgressIndicatorDefaults.circularColor,
        trackColor = trackColor ?: ProgressIndicatorDefaults.circularDeterminateTrackColor,
        strokeWidth = strokeWidth ?: ProgressIndicatorDefaults.CircularStrokeWidth,
        strokeCap = strokeCap ?: ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
        gapSize = gapSize ?: ProgressIndicatorDefaults.CircularIndicatorTrackGapSize,
    )
}

/**
 * Internal implementation of an indeterminate linear progress indicator.
 *
 * @param color Foreground color. *(Optional)*
 * @param trackColor Track color. *(Optional)*
 * @param strokeCap End shape of the line segments. *(Optional)*
 * @param gapSize Gap between animated segments. *(Optional)*
 * @param modifier Modifier applied to the indicator. *(Default: [Modifier])*
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IndeterminateLinearIndicator(
    color: Color?,
    trackColor: Color?,
    strokeCap: StrokeCap?,
    gapSize: Dp?,
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        modifier = modifier,
        color = color ?: ProgressIndicatorDefaults.linearColor,
        trackColor = trackColor ?: ProgressIndicatorDefaults.linearTrackColor,
        strokeCap = strokeCap ?: ProgressIndicatorDefaults.LinearStrokeCap,
        gapSize = gapSize ?: ProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
    )
}

/**
 * Internal implementation of an indeterminate circular progress indicator.
 *
 * @param color Foreground arc color. *(Optional)*
 * @param trackColor Background arc color. *(Optional)*
 * @param strokeCap Cap style for arc ends. *(Optional)*
 * @param strokeWidth Stroke width of the arc. *(Optional)*
 * @param modifier Modifier applied to the indicator. *(Default: [Modifier])*
 */
@Composable
private fun IndeterminateCircularIndicator(
    color: Color?,
    trackColor: Color?,
    strokeCap: StrokeCap?,
    strokeWidth: Dp?,
    modifier: Modifier = Modifier,
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color ?: ProgressIndicatorDefaults.circularColor,
        trackColor = trackColor ?: ProgressIndicatorDefaults.circularIndeterminateTrackColor,
        strokeWidth = strokeWidth ?: ProgressIndicatorDefaults.CircularStrokeWidth,
        strokeCap = strokeCap ?: ProgressIndicatorDefaults.CircularIndeterminateStrokeCap,
    )
}
