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

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset

object KptAnimationSpecs {
    val fast = tween<Float>(durationMillis = 150, easing = FastOutSlowInEasing)
    val medium = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)
    val slow = tween<Float>(durationMillis = 500, easing = FastOutSlowInEasing)

    val fastSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh,
    )

    val mediumSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    val slowSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow,
    )

    // Material Motion easing curves
    val emphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val emphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val standardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
}

@Composable
fun AnimatedVisibilityScope.slideInFromStart(
    animationSpec: FiniteAnimationSpec<IntOffset> = tween(
        300,
        easing = KptAnimationSpecs.emphasizedEasing,
    ),
): EnterTransition = slideInHorizontally(animationSpec) { -it }

@Composable
fun AnimatedVisibilityScope.slideInFromEnd(
    animationSpec: FiniteAnimationSpec<IntOffset> = tween(
        300,
        easing = KptAnimationSpecs.emphasizedEasing,
    ),
): EnterTransition = slideInHorizontally(animationSpec) { it }

@Composable
fun AnimatedVisibilityScope.slideInFromTop(
    animationSpec: FiniteAnimationSpec<IntOffset> = tween(
        300,
        easing = KptAnimationSpecs.emphasizedEasing,
    ),
): EnterTransition = slideInVertically(animationSpec) { -it }

@Composable
fun AnimatedVisibilityScope.slideInFromBottom(
    animationSpec: FiniteAnimationSpec<IntOffset> = tween(
        300,
        easing = KptAnimationSpecs.emphasizedEasing,
    ),
): EnterTransition = slideInVertically(animationSpec) { it }

