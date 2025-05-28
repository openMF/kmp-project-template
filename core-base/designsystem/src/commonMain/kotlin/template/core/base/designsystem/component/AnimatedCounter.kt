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
import template.core.base.designsystem.theme.KptTheme

@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    animationSpec: AnimationSpec<Float> = KptAnimationSpecs.medium,
    textStyle: TextStyle = KptTheme.typography.headlineMedium,
    prefix: String = "",
    suffix: String = ""
) {
    var currentValue by remember { mutableIntStateOf(0) }
    val animatedValue by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = animationSpec,
        label = "counter_animation"
    )

    LaunchedEffect(animatedValue) {
        currentValue = animatedValue.toInt()
    }

    Text(
        text = "$prefix$currentValue$suffix",
        style = textStyle,
        modifier = modifier.testTag("KptAnimatedCounter")
    )
}

@Composable
fun <T> KptAnimatedContentSwitcher(
    targetState: T,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = {
        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
    },
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable AnimatedContentScope.(T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier.testTag("KptAnimatedContentSwitcher"),
        transitionSpec = transitionSpec,
        contentAlignment = contentAlignment,
        label = "content_switcher",
        content = content
    )
}