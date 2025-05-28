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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.core.KptProgressIndicatorConfiguration
import template.core.base.designsystem.core.ProgressIndicatorVariant
import template.core.base.designsystem.theme.KptTheme

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

@Composable
fun AnimationExamples() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Animation Examples", style = KptTheme.typography.headlineMedium)

        // Loading indicators
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            KptProgressIndicator(
                KptProgressIndicatorConfiguration(variant = ProgressIndicatorVariant.CircularIndeterminate),
            )
            KptProgressIndicator(
                KptProgressIndicatorConfiguration(variant = ProgressIndicatorVariant.Dots),
            )
            KptProgressIndicator(
                KptProgressIndicatorConfiguration(variant = ProgressIndicatorVariant.Wave),
            )
        }

        // Animated counter
        var counterValue by remember { mutableIntStateOf(0) }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedCounter(
                targetValue = counterValue,
                prefix = "$",
                suffix = ".00",
            )
            Button(onClick = { counterValue = (0..999).random() }) {
                Text("Random")
            }
        }

        // Shimmer loading
        KptShimmerListItem(modifier = Modifier.fillMaxWidth())

        // Staggered animation
        var showItems by remember { mutableStateOf(false) }
        Button(onClick = { showItems = !showItems }) {
            Text("Toggle Staggered Animation")
        }

        if (showItems) {
            StaggeredAnimation(
                items = listOf("Item 1", "Item 2", "Item 3", "Item 4"),
            ) { item, _ ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}