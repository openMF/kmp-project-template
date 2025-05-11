/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.designsystem.component

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch
import org.mifos.designsystem.component.variant.ProgressIndicatorVariant

@Composable
fun CMPProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color? = null,
    trackColor: Color? = null,
    strokeCap: StrokeCap? = null,
    circularStrokeWidth: Dp? = null,
    gapSize: Dp? = null,
    determinateLinearDrawStopIndicator: (DrawScope.() -> Unit)? = null,
    updateDeterminateProgress: suspend ((Float) -> Unit) -> Unit = {},
    variant: ProgressIndicatorVariant = ProgressIndicatorVariant.INDETERMINATE_CIRCULAR,
) {
    var currentProgress by remember { mutableStateOf(0f) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    if (!loading) return

    when (variant) {
        ProgressIndicatorVariant.DETERMINATE_LINEAR -> DeterminateLinearIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap,
            gapSize = gapSize,
            progress = currentProgress,
            drawStopIndicator = determinateLinearDrawStopIndicator,
        )

        ProgressIndicatorVariant.DETERMINATE_CIRCULAR -> DeterminateCircularIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap,
            strokeWidth = circularStrokeWidth,
            gapSize = gapSize,
            progress = currentProgress,
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

    if (
        variant == ProgressIndicatorVariant.DETERMINATE_LINEAR ||
        variant == ProgressIndicatorVariant.DETERMINATE_CIRCULAR
    ) {
        scope.launch {
            updateDeterminateProgress { progress ->
                currentProgress = progress
            }
            loading = false
        }
    }
}

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
