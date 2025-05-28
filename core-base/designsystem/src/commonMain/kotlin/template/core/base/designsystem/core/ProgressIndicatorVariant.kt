/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.core

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Enhanced ProgressIndicator Variant System
sealed interface ProgressIndicatorVariant: ComponentVariant {
    override val name: String

    data object LinearDeterminate : ProgressIndicatorVariant {
        override val name: String = "linear_determinate"
    }

    data object LinearIndeterminate : ProgressIndicatorVariant {
        override val name: String = "linear_indeterminate"
    }

    data object CircularDeterminate : ProgressIndicatorVariant {
        override val name: String = "circular_determinate"
    }

    data object CircularIndeterminate : ProgressIndicatorVariant {
        override val name: String = "circular_indeterminate"
    }

    data object Dots : ProgressIndicatorVariant {
        override val name: String = "dots"
    }

    data object Wave : ProgressIndicatorVariant {
        override val name: String = "wave"
    }

    data object Pulse : ProgressIndicatorVariant {
        override val name: String = "pulse"
    }

    data object Ring : ProgressIndicatorVariant {
        override val name: String = "ring"
    }
}

// Progress Style
enum class ProgressStyle {
    Rounded, Sharp, Gradient
}

// Configuration class
@Immutable
data class KptProgressIndicatorConfiguration(
    override val modifier: Modifier = Modifier,
    val variant: ProgressIndicatorVariant = ProgressIndicatorVariant.CircularIndeterminate,
    val progress: Float = 0f,
    val color: Color? = null,
    val trackColor: Color? = null,
    val backgroundColor: Color? = null,
    val strokeWidth: Dp = 4.dp,
    val strokeCap: StrokeCap = StrokeCap.Round,
    val size: Dp = 40.dp,
    val animationDuration: Int = 1000,
    val showProgress: Boolean = false,
    val progressFormatter: ((Float) -> String)? = null,
    val style: ProgressStyle = ProgressStyle.Rounded,
    override val testTag: String? = null,
    override val contentDescription: String? = null,
): KptComponent

// DSL Builder
@DslMarker
annotation class ProgressIndicatorDsl

@ProgressIndicatorDsl
class KptProgressIndicatorBuilder {
    var modifier: Modifier = Modifier
    var variant: ProgressIndicatorVariant =
        ProgressIndicatorVariant.CircularIndeterminate
    var progress: Float = 0f
    var color: Color? = null
    var trackColor: Color? = null
    var backgroundColor: Color? = null
    var strokeWidth: Dp = 4.dp
    var strokeCap: StrokeCap = StrokeCap.Round
    var size: Dp = 40.dp
    var animationDuration: Int = 1000
    var showProgress: Boolean = false
    var progressFormatter: ((Float) -> String)? = null
    var style: ProgressStyle = ProgressStyle.Rounded
    var testTag: String? = null
    var contentDescription: String? = null

    fun build(): KptProgressIndicatorConfiguration = KptProgressIndicatorConfiguration(
        modifier = modifier,
        variant = variant,
        progress = progress,
        color = color,
        trackColor = trackColor,
        backgroundColor = backgroundColor,
        strokeWidth = strokeWidth,
        strokeCap = strokeCap,
        size = size,
        animationDuration = animationDuration,
        showProgress = showProgress,
        progressFormatter = progressFormatter,
        style = style,
        testTag = testTag,
        contentDescription = contentDescription,
    )
}

// DSL Function
fun kptProgressIndicator(
    block: KptProgressIndicatorBuilder.() -> Unit,
): KptProgressIndicatorConfiguration {
    return KptProgressIndicatorBuilder().apply(block).build()
}

