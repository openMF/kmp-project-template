package template.core.base.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun BounceAnimation(
    targetValue: Float = 1.1f,
    durationMillis: Int = 100,
    content: @Composable (scale: Float) -> Unit,
) {
    var triggered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (triggered) targetValue else 1f,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        finishedListener = { triggered = false },
        label = "bounce_animation",
    )

    LaunchedEffect(Unit) {
        triggered = true
    }

    content(scale)
}

@Composable
fun RevealAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = KptAnimationSpecs.medium,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec) + scaleIn(animationSpec, initialScale = 0.8f),
        exit = fadeOut(animationSpec) + scaleOut(animationSpec, targetScale = 0.8f),
    ) {
        content()
    }
}

@Composable
fun <T> StaggeredAnimation(
    items: List<T>,
    modifier: Modifier = Modifier,
    staggerDelayMillis: Long = 100,
    content: @Composable (item: T, index: Int) -> Unit,
) {
    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            var visible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(index * staggerDelayMillis)
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300, easing = KptAnimationSpecs.emphasizedEasing),
                ) + fadeIn(animationSpec = tween(300)),
            ) {
                content(item, index)
            }
        }
    }
}
