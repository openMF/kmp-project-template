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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import org.jetbrains.compose.ui.tooling.preview.Preview
import template.core.base.designsystem.theme.KptTheme

/**
 * A composable that animates an integer counter transition smoothly.
 *
 * @param targetValue The target number the counter should animate to.
 * @param modifier [Modifier] to be applied to the text displaying the
 *    counter.
 * @param animationSpec The animation specification to use for the
 *    transition.
 * @param textStyle The [TextStyle] applied to the counter text.
 * @param prefix A string to display before the number.
 * @param suffix A string to display after the number.
 * @sample AnimatedCounterPreview
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    animationSpec: AnimationSpec<Float> = KptAnimationSpecs.medium,
    textStyle: TextStyle = KptTheme.typography.headlineMedium,
    prefix: String = "",
    suffix: String = "",
) {
    var currentValue by remember { mutableIntStateOf(0) }
    val animatedValue by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = animationSpec,
        label = "counter_animation",
    )

    LaunchedEffect(animatedValue) {
        currentValue = animatedValue.toInt()
    }

    Text(
        text = "$prefix$currentValue$suffix",
        style = textStyle,
        modifier = modifier.testTag("KptAnimatedCounter"),
    )
}

/**
 * A generic animated content switcher for composables that transition
 * between states with a horizontal slide effect.
 *
 * @param targetState The state that determines which composable content to
 *    show.
 * @param modifier [Modifier] to be applied to the content.
 * @param transitionSpec Defines how the transition animation should
 *    behave.
 * @param contentAlignment The alignment for the content inside the
 *    animation container.
 * @param content A composable block that renders the UI based on the
 *    target state.
 * @sample KptAnimatedContentSwitcherPreview
 */
@Composable
fun <T> KptAnimatedContentSwitcher(
    targetState: T,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = {
        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
    },
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable AnimatedContentScope.(T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier.testTag("KptAnimatedContentSwitcher"),
        transitionSpec = transitionSpec,
        contentAlignment = contentAlignment,
        label = "content_switcher",
        content = content,
    )
}

@Preview
@Composable
private fun AnimatedCounterPreview() {
    KptTheme {
        AnimatedCounter(targetValue = 42, prefix = "Count: ")
    }
}

@Preview
@Composable
private fun KptAnimatedContentSwitcherPreview() {
    KptTheme {
        var state = 0
        KptAnimatedContentSwitcher(targetState = state) {
            Text(text = "Page $it", style = KptTheme.typography.bodyLarge)
        }
    }
}
