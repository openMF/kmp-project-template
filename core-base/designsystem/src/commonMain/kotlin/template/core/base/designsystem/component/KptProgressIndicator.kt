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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.config.KptTestTags
import template.core.base.designsystem.core.KptProgressIndicatorConfiguration
import template.core.base.designsystem.core.ProgressIndicatorVariant
import template.core.base.designsystem.theme.KptTheme

// Main Enhanced Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptProgressIndicator(configuration: KptProgressIndicatorConfiguration) {
    val finalModifier = configuration.modifier
        .testTag(configuration.testTag ?: KptTestTags.PROGRESS_INDICATOR)
        .let { mod ->
            if (configuration.contentDescription != null) {
                mod.semantics { contentDescription = configuration.contentDescription }
            } else {
                mod
            }
        }

    val primaryColor = configuration.color ?: KptTheme.colorScheme.primary
    val secondaryColor = configuration.trackColor ?: KptTheme.colorScheme.surfaceVariant

    when (configuration.variant) {
        ProgressIndicatorVariant.LinearDeterminate -> {
            LinearProgressIndicator(
                progress = { configuration.progress },
                modifier = finalModifier.height(configuration.strokeWidth),
                color = primaryColor,
                trackColor = secondaryColor,
                strokeCap = configuration.strokeCap,
            )
        }

        ProgressIndicatorVariant.LinearIndeterminate -> {
            LinearProgressIndicator(
                modifier = finalModifier.height(configuration.strokeWidth),
                color = primaryColor,
                trackColor = secondaryColor,
                strokeCap = configuration.strokeCap,
            )
        }

        ProgressIndicatorVariant.CircularDeterminate -> {
            Box(
                modifier = finalModifier.size(configuration.size),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = { configuration.progress },
                    modifier = Modifier.fillMaxSize(),
                    color = primaryColor,
                    strokeWidth = configuration.strokeWidth,
                    trackColor = secondaryColor,
                    strokeCap = configuration.strokeCap,
                )

                if (configuration.showProgress) {
                    val progressText = configuration.progressFormatter?.invoke(configuration.progress)
                        ?: "${(configuration.progress * 100).toInt()}%"
                    Text(
                        text = progressText,
                        style = KptTheme.typography.labelSmall,
                        color = KptTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        ProgressIndicatorVariant.CircularIndeterminate -> {
            CircularProgressIndicator(
                modifier = finalModifier.size(configuration.size),
                color = primaryColor,
                strokeWidth = configuration.strokeWidth,
                trackColor = secondaryColor,
                strokeCap = configuration.strokeCap,
            )
        }

        ProgressIndicatorVariant.Dots -> {
            DotsProgressIndicator(configuration, finalModifier, primaryColor)
        }

        ProgressIndicatorVariant.Wave -> {
            WaveProgressIndicator(configuration, finalModifier, primaryColor)
        }

        ProgressIndicatorVariant.Pulse -> {
            PulseProgressIndicator(configuration, finalModifier, primaryColor)
        }

        ProgressIndicatorVariant.Ring -> {
            RingProgressIndicator(configuration, finalModifier, primaryColor)
        }
    }
}

@Composable
private fun DotsProgressIndicator(
    configuration: KptProgressIndicatorConfiguration,
    modifier: Modifier,
    color: Color,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_progress")
    val dotSize = configuration.size / 8
    val spacing = dotSize / 2

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(configuration.animationDuration / 3, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * configuration.animationDuration / 3),
                ),
                label = "dot_scale_$index",
            )

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .scale(scale)
                    .background(color, CircleShape),
            )
        }
    }
}

@Composable
private fun WaveProgressIndicator(
    configuration: KptProgressIndicatorConfiguration,
    modifier: Modifier,
    color: Color,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_progress")
    val barWidth = configuration.size / 8
    val barSpacing = barWidth / 2

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(barSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(5) { index ->
            val height by infiniteTransition.animateFloat(
                initialValue = (configuration.size * 0.2f).value,
                targetValue = configuration.size.value,
                animationSpec = infiniteRepeatable(
                    animation = tween(configuration.animationDuration / 2, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * configuration.animationDuration / 10),
                ),
                label = "bar_height_$index",
            )

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(height.dp)
                    .clip(RoundedCornerShape(barWidth / 2))
                    .background(color),
            )
        }
    }
}

@Composable
private fun PulseProgressIndicator(
    configuration: KptProgressIndicatorConfiguration,
    modifier: Modifier,
    color: Color,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_progress")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(configuration.animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(configuration.animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    Box(
        modifier = modifier
            .size(configuration.size)
            .scale(scale)
            .alpha(alpha)
            .background(color, CircleShape),
    )
}

@Composable
private fun RingProgressIndicator(
    configuration: KptProgressIndicatorConfiguration,
    modifier: Modifier,
    color: Color,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_progress")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(configuration.animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring_rotation",
    )

    Canvas(
        modifier = modifier.size(configuration.size),
    ) {
        val strokeWidth = configuration.strokeWidth.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Draw background ring
        drawCircle(
            color = configuration.trackColor ?: Color.Gray.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(strokeWidth, cap = configuration.strokeCap),
        )

        // Draw animated segment
        val sweepAngle = 60f
        drawArc(
            color = color,
            startAngle = rotation,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(strokeWidth, cap = configuration.strokeCap),
        )
    }
}

// 1. Simple circular progress
@Composable
fun KptCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color? = null,
    size: Dp = 40.dp,
    strokeWidth: Dp = 4.dp,
) {
    KptProgressIndicator(
        KptProgressIndicatorConfiguration(
            modifier = modifier,
            variant = ProgressIndicatorVariant.CircularIndeterminate,
            color = color,
            size = size,
            strokeWidth = strokeWidth,
        ),
    )
}

// 2. Determinate circular with progress
@Composable
fun KptCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color? = null,
    size: Dp = 40.dp,
    strokeWidth: Dp = 4.dp,
    showProgressText: Boolean = false,
) {
    KptProgressIndicator(
        KptProgressIndicatorConfiguration(
            modifier = modifier,
            variant = ProgressIndicatorVariant.CircularDeterminate,
            progress = progress,
            color = color,
            size = size,
            strokeWidth = strokeWidth,
            showProgress = showProgressText,
        ),
    )
}

// 3. Linear progress
@Composable
fun KptLinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color? = null,
    trackColor: Color? = null,
    strokeWidth: Dp = 4.dp,
) {
    KptProgressIndicator(
        KptProgressIndicatorConfiguration(
            modifier = modifier,
            variant = ProgressIndicatorVariant.LinearIndeterminate,
            color = color,
            trackColor = trackColor,
            strokeWidth = strokeWidth,
        ),
    )
}

// 4. Determinate linear with progress
@Composable
fun KptLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color? = null,
    trackColor: Color? = null,
    strokeWidth: Dp = 4.dp,
) {
    KptProgressIndicator(
        KptProgressIndicatorConfiguration(
            modifier = modifier,
            variant = ProgressIndicatorVariant.LinearDeterminate,
            progress = progress,
            color = color,
            trackColor = trackColor,
            strokeWidth = strokeWidth,
        ),
    )
}

// 5. Loading dots
@Composable
fun KptLoadingDots(
    modifier: Modifier = Modifier,
    color: Color? = null,
    size: Dp = 40.dp,
    animationDuration: Int = 1000,
) {
    KptProgressIndicator(
        KptProgressIndicatorConfiguration(
            modifier = modifier,
            variant = ProgressIndicatorVariant.Dots,
            color = color,
            size = size,
            animationDuration = animationDuration,
        ),
    )
}

// 6. Wave loading
@Composable
fun KptLoadingWave(
    modifier: Modifier = Modifier,
    color: Color? = null,
    size: Dp = 40.dp,
    animationDuration: Int = 1000,
) {
    KptProgressIndicator(
        KptProgressIndicatorConfiguration(
            modifier = modifier,
            variant = ProgressIndicatorVariant.Wave,
            color = color,
            size = size,
            animationDuration = animationDuration,
        ),
    )
}

// 7. Pulse loading
@Composable
fun KptLoadingPulse(
    modifier: Modifier = Modifier,
    color: Color? = null,
    size: Dp = 40.dp,
    animationDuration: Int = 1000,
) {
    KptProgressIndicator(
        KptProgressIndicatorConfiguration(
            modifier = modifier,
            variant = ProgressIndicatorVariant.Pulse,
            color = color,
            size = size,
            animationDuration = animationDuration,
        ),
    )
}

// 8. Progress with label
@Composable
fun KptProgressWithLabel(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    variant: ProgressIndicatorVariant = ProgressIndicatorVariant.LinearDeterminate,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = KptTheme.typography.bodyMedium,
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        KptProgressIndicator(
            KptProgressIndicatorConfiguration(
                variant = variant,
                progress = progress,
                color = color,
                modifier = Modifier.fillMaxWidth(),
            ),
        )
    }
}

// 9. Upload progress
@Composable
fun KptUploadProgress(
    progress: Float,
    fileName: String,
    fileSize: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileName,
                        style = KptTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = fileSize,
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Cancel upload",
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            KptProgressWithLabel(
                progress = progress,
                label = "Uploading...",
                variant = ProgressIndicatorVariant.LinearDeterminate,
            )
        }
    }
}

// 10. Download progress
@Composable
fun KptDownloadProgress(
    progress: Float,
    downloadSpeed: String,
    timeRemaining: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Downloading...",
                style = KptTheme.typography.bodyMedium,
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = KptTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        KptLinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = downloadSpeed,
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = timeRemaining,
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// 11. Original component compatibility
@Composable
fun KptProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color? = null,
    trackColor: Color? = null,
    strokeCap: StrokeCap? = null,
    circularStrokeWidth: Dp? = null,
    progress: Float? = null,
    variant: ProgressIndicatorVariant = ProgressIndicatorVariant.CircularIndeterminate,
    testTag: String? = null,
    contentDescription: String? = null,
) {
    KptProgressIndicator(
        KptProgressIndicatorConfiguration(
            modifier = modifier,
            variant = variant,
            progress = progress ?: 0f,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap ?: StrokeCap.Round,
            strokeWidth = circularStrokeWidth ?: 4.dp,
            testTag = testTag,
            contentDescription = contentDescription,
        ),
    )
}
