# `core-base/designsystem`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_DESIGNSYSTEM.md`
> **Measured:** 36 Kotlin files, 1 test files

**Defines annotations:** `@ComponentDsl`, `@TopAppBarDsl`

**Consumes contracts:** `@ComponentDsl`, `@TopAppBarDsl`

## Principal types

`AccessibilityProvider`, `Animatable`, `BarGeometry`, `BreakpointConfiguration`, `Clickable`, `ComponentColors`, `ComponentComposer`, `ComponentConfiguration`, `ComponentConfigurationScope`, `ComponentDsl`, `ComponentElevation`, `ComponentFactory`, `ComponentRegistry`, `ComponentRenderer`  …and 54 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/designsystem sha=ddef521cde7ed6cfc7c0a4f15a29a9308d55e04f -->
## API reference

_Generated from `core-base/designsystem` at tree `ddef521cde7e` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/chart/BarGeometry.kt`

```kotlin
object BarGeometry
```
Pure-function math for bar chart composables. Normalizes each bar to a fraction in `[0f, 1f]` against the series' max. **Degenerate-input contracts**: - Empty list → empty result.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptBarChart.kt:74</code></summary>

```kotlin
) {
    val palette = ChartTokens.multiSeriesColors()
    val fractions = remember(data) { BarGeometry.normalizedHeights(data.map { it.value }) }
    val animatedFraction by animateFloatAsState(
        targetValue = if (data.isEmpty()) 0f else 1f,
        animationSpec = animationSpec,
        label = "kptBarFraction",
```

</details>

- `fun normalizedHeights(values: List<Float>): List<Float>` — Per-bar height as a fraction `[0, 1]` of the canvas height.
- `val max = values.maxOrNull() ?: 0f`

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/chart/DonutGeometry.kt`

```kotlin
object DonutGeometry
```
Pure-function math for donut chart composables. Extracted from the Composable so sweep-angle correctness can be tested without a Compose test rule. **Degenerate-input contracts**: - Empty list → empty result.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptDonutChart.kt:67</code></summary>

```kotlin
) {
    val palette = ChartTokens.multiSeriesColors()
    val sweeps = remember(slices) { DonutGeometry.sweepAngles(slices.map { it.value }) }
    // Animate the per-slice sweep total. Once at-rest, sweeps[i] is the per-slice arc;
    // during transition, we scale by the animatedFraction so all arcs grow together.
    val animatedFraction by animateFloatAsState(
        targetValue = if (sweeps.any { it > 0f }) 1f else 0f,
```

</details>

- `fun sweepAngles(values: List<Float>): List<Float>` — Per-slice sweep angle in degrees, in the same order as `values`.
- `val total = values.sum()`

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/chart/SparklineGeometry.kt`

```kotlin
object SparklineGeometry
```
Pure-function path geometry for sparkline / area chart composables. Extracted from the `Canvas { drawPath() }` block so the math is unit-testable without a Compose test rule.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptSparkline.kt:50</code></summary>

```kotlin
        if (values.isEmpty()) return@Canvas

        val points = SparklineGeometry.normalize(
            values = values,
            width = size.width,
            height = size.height,
        )
```

</details>

- `fun normalize(values: List<Double>, width: Float, height: Float): List<Pair<Float, Float>>` — Normalize `values` to canvas coordinates fitting `[0, width] × [0, height]`.
- `val result: List<Pair<Float, Float>> = when`
- `val min = values.min()`
- `val max = values.max()`
- `val range = max - min`
- `val stepX = width / (values.size - 1)`
- `val x = index * stepX`
- `val y = if (range == 0.0)`
- `val normalized = ((value - min) / range).toFloat()`

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/AppCard.kt`

```kotlin
fun AppCard(
```
Material 3 elevated card for grouping related content (loan rows, form sections, dashboard tiles).

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailScreen.kt:192</code></summary>

```kotlin
        }

        AppCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(sp.md),
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/BounceAnimation.kt`

```kotlin
fun BounceAnimation(
```
A composable that briefly enlarges the content to create a bounce effect when triggered.

```kotlin
fun RevealAnimation(
```
A composable that reveals or hides content with a combination of fade and scale animations.

```kotlin
fun <T> StaggeredAnimation(
```
Animates a list of items with a staggered vertical slide-in and fade-in effect.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/HeroCard.kt`

```kotlin
fun HeroCard(
```
Hero card — the dashboard's first impression.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailScreen.kt:176</code></summary>

```kotlin
        }

        HeroCard {
            AmountDisplay(
                amountText = formatMoney(loan.principalRemaining),
                label = stringResource(Res.string.screens_loans_detail_principal_remaining_label),
                supporting = {
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/KptAnimationSpecs.kt`

```kotlin
object KptAnimationSpecs
```
Centralized animation specifications following Material Motion design guidelines. This object provides consistent animation timing and easing curves throughout the KPT design system.

<details><summary>Example</summary>

```kotlin
// For simple property animations
val animatedAlpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = KptAnimationSpecs.medium
)

// For spring-based animations
val animatedScale by animateFloatAsState(
    targetValue = if (pressed) 0.95f else 1f,
    animationSpec = KptAnimationSpecs.fastSpring
)
```

</details>

- `val fast = tween<Float>(durationMillis = 150, easing = FastOutSlowInEasing)` — Fast tween animation (150ms) for quick transitions like state changes. Best used for: button states, small UI element appearances/disappearances.
- `val medium = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)` — Medium tween animation (300ms) for standard UI transitions. Best used for: screen transitions, modal appearances, content changes.
- `val slow = tween<Float>(durationMillis = 500, easing = FastOutSlowInEasing)` — Slow tween animation (500ms) for complex or large-scale transitions. Best used for: page transitions, complex layout changes, dramatic effects.
- `val fastSpring = spring<Float>(` — Fast spring animation with medium bounce for responsive interactions. Best used for: button presses, interactive feedback, quick selections.
- `val mediumSpring = spring<Float>(` — Medium spring animation with low bounce for smooth transitions. Best used for: drawer openings, sheet expansions, smooth scrolling effects.
- `val slowSpring = spring<Float>(` — Slow spring animation with no bounce for stable, smooth animations. Best used for: large content movements, settling animations, smooth stops.
- `val emphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)` — Emphasized easing for important transitions that should draw attention. Creates a slow start with a quick finish.
- `val emphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)` — Emphasized accelerate easing for elements leaving the screen. Quick start that maintains momentum.
- `val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)` — Emphasized decelerate easing for elements entering the screen. Maintains momentum then slows to a smooth stop.
- `val standardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)` — Standard easing for general purpose animations. Provides a balanced, natural feeling motion.

```kotlin
fun AnimatedVisibilityScope.slideInFromStart(
```
Slide in animation from the start edge of the screen (left in LTR, right in RTL). This extension function provides a convenient way to create slide-in animations that respect the current layout direction.

```kotlin
fun AnimatedVisibilityScope.slideInFromEnd(
```
Slide in animation from the end edge of the screen (right in LTR, left in RTL). This extension function provides a convenient way to create slide-in animations that respect the current layout direction.

```kotlin
fun AnimatedVisibilityScope.slideInFromTop(
```
Slide in animation from the top edge of the screen. Creates a smooth vertical slide-in effect commonly used for notifications, drop-down menus, or top-anchored content.

```kotlin
fun AnimatedVisibilityScope.slideInFromBottom(
```
Slide in animation from the bottom edge of the screen. Creates a smooth vertical slide-in effect commonly used for bottom sheets, action panels, or bottom-anchored content.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/KptButton.kt`

```kotlin
fun KptButton(
```
Primary filled button following the KPT design system. Wraps `Button` with project-standard defaults. Prefer this over importing Material3 `Button` directly so design-system tokens can be applied in one place.

<details><summary>Example</summary>

```kotlin
KptButton(onClick = viewModel::onSubmit, enabled = uiState.canInteract) {
    Text("Save")
}
```

</details>

```kotlin
fun KptOutlinedButton(
```
Outlined variant of `KptButton`.

```kotlin
fun KptTextButton(
```
Text (flat) variant of `KptButton`.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/KptShimmerLoadingBox.kt`

```kotlin
fun KptShimmerLoadingBox(
```
Animated shimmer placeholder box. **Future migration:** new call sites should prefer `KptProgress(KptProgress.Shimmer(...))` from `kpt.core.base.designsystem.component.progress` — the unified progress family.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/RowLoadingShimmer.kt:42</code></summary>

```kotlin
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KptShimmerLoadingBox(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
        )
        Column(
```

</details>

```kotlin
fun KptShimmerListItem(
```
_No KDoc at source._

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/KptToastHost.kt`

```kotlin
fun KptToastHost(
```
The app's transient-message host. Place once near the root of the UI. Replaces `KptSnackbarHost`, which wrapped Material3's `SnackbarHost`. Two reasons it went: 1.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/KptTopAppBar.kt`

```kotlin
fun KptTopAppBar(configuration: KptTopAppBarConfiguration)
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptScaffold.kt:62</code></summary>

```kotlin
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
```

</details>

```kotlin
fun KptTopAppBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptScaffold.kt:62</code></summary>

```kotlin
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
```

</details>

```kotlin
fun KptTopAppBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptScaffold.kt:62</code></summary>

```kotlin
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
```

</details>

```kotlin
fun KptTopAppBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptScaffold.kt:62</code></summary>

```kotlin
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
```

</details>

```kotlin
fun KptTopAppBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptScaffold.kt:62</code></summary>

```kotlin
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
```

</details>

```kotlin
fun KptTopAppBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptScaffold.kt:62</code></summary>

```kotlin
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
```

</details>

```kotlin
fun KptSearchAppBar(
```
_No KDoc at source._

```kotlin
fun KptProfileAppBar(
```
_No KDoc at source._

```kotlin
fun KptSettingsAppBar(
```
_No KDoc at source._

```kotlin
fun KptSmallTopAppBar(
```
_No KDoc at source._

```kotlin
fun KptCenterAlignedTopAppBar(
```
_No KDoc at source._

```kotlin
fun KptMediumTopAppBar(
```
_No KDoc at source._

```kotlin
fun KptLargeTopAppBar(
```
_No KDoc at source._

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/progress/KptProgress.kt`

```kotlin
sealed interface KptProgress
```
Sealed family of "something is in progress" UI variants.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/progress/KptProgressRenderer.kt`

```kotlin
fun KptProgress(
```
Single dispatch composable for every "something is in progress" UI in the toolkit. Pick a `KptProgress` variant; this renderer wires it to the right primitive at the right size.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/progress/ProgressSizeSpec.kt`

```kotlin
enum class ProgressSize { Xs, Sm, Md, Lg }
```
T-shirt sizes for `KptProgress` variants. Maps to (diameter, stroke) dp pairs via `ProgressSizeSpec.dpFor` — keeps every project-wide progress indicator on a single set of rhythm-aligned dimensions.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/component/SlideTransition.kt`

```kotlin
fun KptSlideTransition(
```
_No KDoc at source._

```kotlin
enum class SlideDirection { Left, Right, Up, Down }
```
_No KDoc at source._

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/core/ComponentStateHolder.kt`

```kotlin
class ComponentStateHolder<T>(initialValue: T) : ComponentState<T>
```
A concrete implementation of `ComponentState` that holds and manages component state. This class provides a thread-safe way to hold and update state values within components, with automatic recomposition when the state changes.

<details><summary>Example</summary>

```kotlin
class MyComponentState(initialExpanded: Boolean) {
    private val _expanded = ComponentStateHolder(initialExpanded)
    val expanded: ComponentState<Boolean> = _expanded

    fun toggleExpanded() {
        _expanded.update(!_expanded.value)
    }
}
```

</details>

```kotlin
fun <T> rememberComponentState(initialValue: T): ComponentState<T>
```
Remembers a `ComponentState` instance across recompositions.

<details><summary>Example</summary>

```kotlin
@Composable
fun ExpandableCard() {
    val expandedState = rememberComponentState(initialValue = false)

    Card(
        modifier = Modifier.clickable {
            expandedState.update(!expandedState.value)
        }
    ) {
        if (expandedState.value) {
            DetailContent()
        } else {
            SummaryContent()
        }
    }
}
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/core/KptComponent.kt`

```kotlin
interface KptComponent
```
The base contract every `Kpt*` component satisfies: a test tag, a content description and a caller-supplied `Modifier`.

- `val testTag: String?`
- `val contentDescription: String?`
- `val modifier: Modifier`

```kotlin
interface Clickable
```
Mixed into components that respond to a tap. `interactionSource` is exposed so a caller can hoist ripple/press state — a component that owns it privately cannot participate in a parent's interaction handling.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanRowCard.kt:59</code></summary>

```kotlin

    AppCard(
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongPress),
        accentColor = loanKindAccent(loan.kind),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
```

</details>

- `val onClick: () -> Unit`
- `val enabled: Boolean`
- `val interactionSource: MutableInteractionSource?`

```kotlin
interface Styleable
```
Mixed into components whose colors, shape and elevation can be overridden at the call site.

- `val colors: ComponentColors?`
- `val shape: Shape?`
- `val elevation: ComponentElevation?`

```kotlin
interface Themeable
```
Mixed into components that accept a whole `ComponentTheme` rather than individual style slots.

- `val theme: ComponentTheme?`

```kotlin
interface ComponentColors
```
Marker for a component's color set. Each component defines its own slots; the marker exists so `Styleable` can carry them without knowing the shape.

```kotlin
interface ComponentElevation
```
Marker for a component's elevation set, per interaction state (resting, pressed, focused).

```kotlin
interface ComponentTheme
```
Marker for a complete component theme — colors, shape and elevation resolved together.

```kotlin
interface ThemeStrategy
```
Resolves the `ComponentTheme` for a component, letting a fork swap the whole theming rule rather than overriding components one at a time.

- `fun applyTheme(component: KptComponent): ComponentTheme`

```kotlin
interface ComponentFactory<T : KptComponent>
```
Builds a component of type `T` from a `ComponentConfiguration` — the seam that lets components be constructed from data (a registry, a server-driven layout) instead of only from Kotlin call sites.

- `fun create(configuration: ComponentConfiguration): T`

```kotlin
interface ComponentConfiguration
```
A component's declarative description, convertible to the component itself via `build`.

- `fun build(): KptComponent`

```kotlin
interface ComponentState<T>
```
Observable holder for one component's mutable value. `@Stable` so Compose can skip recomposition when the reference is unchanged; mutate through `update` rather than replacing the holder, or that guarantee is lost.

- `val value: T`
- `fun update(newValue: T)`

```kotlin
sealed interface ComponentVariant
```
A named visual variant of a component (filled, outlined, tonal, …). Sealed so the variant set is closed and exhaustively handled at each render site.

```kotlin
interface ComponentComposer
```
Renders a list of components as one composition — used where a screen's content is assembled from data rather than written out.

- `fun compose(components: List<KptComponent>): Unit`

```kotlin
interface Animatable
```
Mixed into components with a tunable transition. See `theme/Motion.kt` for the shared durations; overriding per component is what makes an app's motion feel inconsistent.

- `val animationDuration: Long`
- `val animationEasing: androidx.compose.animation.core.Easing?`

```kotlin
interface AccessibilityProvider
```
Supplies a component's semantics — description, role and any extra properties. Separate from `KptComponent` so a component can delegate accessibility to a wrapper rather than re-declaring it.

- `val semantics: androidx.compose.ui.semantics.SemanticsPropertyReceiver.() -> Unit`
- `val contentDescription: String?`
- `val role: androidx.compose.ui.semantics.Role?`

```kotlin
interface KptThemeProvider
```
The whole design language in one object: colors, typography, shapes, spacing and elevation. A fork supplies its own and every component follows, which is the point of the indirection.

- `val colors: KptColorScheme`
- `val typography: KptTypography`
- `val shapes: KptShapes`
- `val spacing: KptSpacing`
- `val elevation: KptElevation`

```kotlin
interface KptColorScheme
```
The full Material 3 color role set. Roles, not literal colors — a component asks for `onSurfaceVariant`, never a hex value, so light and dark themes and a fork's palette all work without touching the component.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:126</code></summary>

```kotlin
        androidTheme -> if (darkTheme) darkColorScheme() else lightColorScheme()
        else -> if (darkTheme) darkScheme else lightScheme
    }.toKptColorScheme()

    val mifosTypography = Typography().toKptTypography(fontFamily)

    val themeProvider = KptThemeProviderImpl(
```

</details>

- `val primary: Color`
- `val onPrimary: Color`
- `val primaryContainer: Color`
- `val onPrimaryContainer: Color`
- `val inversePrimary: Color`
- `val secondary: Color`
- `val onSecondary: Color`
- `val secondaryContainer: Color`
- `val onSecondaryContainer: Color`
- `val tertiary: Color`
- `val onTertiary: Color`
- `val tertiaryContainer: Color`
- `val onTertiaryContainer: Color`
- `val background: Color`
  _…more members; read the file._

```kotlin
interface KptTypography
```
The Material 3 type scale — display through label, each in three sizes.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:128</code></summary>

```kotlin
    }.toKptColorScheme()

    val mifosTypography = Typography().toKptTypography(fontFamily)

    val themeProvider = KptThemeProviderImpl(
        colors = colorScheme,
        typography = mifosTypography,
```

</details>

- `val displayLarge: androidx.compose.ui.text.TextStyle`
- `val displayMedium: androidx.compose.ui.text.TextStyle`
- `val displaySmall: androidx.compose.ui.text.TextStyle`
- `val headlineLarge: androidx.compose.ui.text.TextStyle`
- `val headlineMedium: androidx.compose.ui.text.TextStyle`
- `val headlineSmall: androidx.compose.ui.text.TextStyle`
- `val titleLarge: androidx.compose.ui.text.TextStyle`
- `val titleMedium: androidx.compose.ui.text.TextStyle`
- `val titleSmall: androidx.compose.ui.text.TextStyle`
- `val bodyLarge: androidx.compose.ui.text.TextStyle`
- `val bodyMedium: androidx.compose.ui.text.TextStyle`
- `val bodySmall: androidx.compose.ui.text.TextStyle`
- `val labelLarge: androidx.compose.ui.text.TextStyle`
- `val labelMedium: androidx.compose.ui.text.TextStyle`
  _…more members; read the file._

```kotlin
interface KptShapes
```
The corner-shape scale, from `extraSmall` to `extraLarge`, applied by component size rather than chosen per call site.

- `val extraSmall: CornerBasedShape`
- `val small: CornerBasedShape`
- `val medium: CornerBasedShape`
- `val large: CornerBasedShape`
- `val extraLarge: CornerBasedShape`

```kotlin
interface KptSpacing
```
The spacing scale every layout measures with. Components reference these rather than literal `.dp` values so density stays uniform and a fork can retune the whole app's rhythm in one place.

- `val xs: Dp`
- `val sm: Dp`
- `val md: Dp`
- `val lg: Dp`
- `val xl: Dp`
- `val xxl: Dp`

```kotlin
interface KptElevation
```
The elevation scale, in Material 3 levels 0–5.

- `val level0: Dp`
- `val level1: Dp`
- `val level2: Dp`
- `val level3: Dp`
- `val level4: Dp`
- `val level5: Dp`

```kotlin
interface ComponentRenderer<T : KptComponent>
```
Renders component type `T`. Registered in a `ComponentRegistry` so a data-driven layout can resolve a renderer by type at runtime.

- `fun render(component: T)`

```kotlin
interface ComponentRegistry
```
Maps component types to their renderers and factories — the lookup a `ComponentComposer` uses.

- `fun <T : KptComponent> register(type: KClass<T>, renderer: ComponentRenderer<T>)`
- `fun <T : KptComponent> getRenderer(type: KClass<T>): ComponentRenderer<T>?`

```kotlin
annotation class ComponentDsl
```
DSL marker for the component-configuration builders. Stops an inner builder from implicitly seeing an outer scope's receivers, which is how nested DSL blocks silently configure the wrong component.

```kotlin
interface ComponentConfigurationScope
```
Receiver for the component-configuration DSL, scoped by `ComponentDsl`.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/core/KptTopAppBarConfiguration.kt`

```kotlin
sealed interface TopAppBarVariant : ComponentVariant
```
Defines the visual variants available for the KPT top app bar. Each variant corresponds to a different Material3 top app bar style with different visual characteristics and use cases.

```kotlin
data class KptTopAppBarConfiguration(
```
Configuration class that defines all properties for a KPT top app bar. This immutable data class encapsulates all the customization options for the top app bar, providing a clean API for complex configurations.

<details><summary>Example</summary>

```kotlin
val config = KptTopAppBarConfiguration(
    title = "My Screen",
    variant = TopAppBarVariant.Large,
    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
    onNavigationIonClick = { navController.navigateUp() },
    actions = listOf(
        TopAppBarAction(
            icon = Icons.Default.Search,
            contentDescription = "Search",
            onClick = { openSearch() }
        )
    ),
    subtitle = "Optional subtitle"
)
```

</details>

```kotlin
data class TopAppBarAction(
```
Represents an action button in the top app bar. Action buttons are displayed on the right side of the top app bar and provide quick access to common functions like search, menu, or other contextual actions.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptScaffold.kt:56</code></summary>

```kotlin
        .only(WindowInsetsSides.Horizontal),
    snackbarHost: @Composable () -> Unit = {},
    actions: List<TopAppBarAction> = emptyList(),
    content: @Composable () -> Unit = {},
) {
    Scaffold(
        topBar = {
```

</details>

```kotlin
annotation class TopAppBarDsl
```
_No KDoc at source._

```kotlin
class KptTopAppBarBuilder
```
DSL builder class for creating `KptTopAppBarConfiguration` instances. This builder provides a fluent API for configuring top app bars with a clean, readable syntax. All properties have sensible defaults and can be customized as needed.

<details><summary>Example</summary>

```kotlin
val config = kptTopAppBar {
    title = "Settings"
    variant = TopAppBarVariant.Large
    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
    onNavigationClick = { navController.navigateUp() }

    action(Icons.Default.Search, "Search") { openSearch() }
    action(Icons.Default.MoreVert, "More options") { showMenu() }

    subtitle = "Customize your experience"
    testTag = "SettingsTopAppBar"
}
```

</details>

```kotlin
fun kptTopAppBar(block: KptTopAppBarBuilder.() -> Unit): KptTopAppBarConfiguration
```
DSL function for creating a `KptTopAppBarConfiguration` using a builder pattern. This function provides a convenient way to configure top app bars with a clean, type-safe DSL syntax. All configuration is done within the builder block.

<details><summary>Example</summary>

```kotlin
val topAppBarConfig = kptTopAppBar {
    title = "My Screen"
    variant = TopAppBarVariant.Medium
    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
    onNavigationClick = { navController.navigateUp() }

    action(Icons.Default.Search, "Search") {
        // Handle search
    }

    action(Icons.Default.Favorite, "Add to favorites") {
        // Handle favorite
    }
}

KptTopAppBar(topAppBarConfig)
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptMaterialTheme.kt`

```kotlin
fun KptMaterialTheme(
```
KptMaterialTheme provides Material3 integration for KptTheme. This composable applies KptTheme values to MaterialTheme automatically, making all Material3 components use KptTheme design tokens.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:138</code></summary>

```kotlin
    val financeColors = if (darkTheme) darkFinanceColors() else lightFinanceColors()

    KptMaterialTheme(theme = themeProvider) {
        // Provide the design-system token CompositionLocals app-wide so every widget
        // built on `core/designsystem/component/`, `chart/`, and `motion/` resolves
        // semantic finance colors, motion specs, spacing scale, and elevation tiers
        // without per-call wiring. Forks override any subset via
```

</details>

```kotlin
fun KptMaterialTheme(
```
KptMaterialTheme with dark theme support. Provides automatic light/dark theme switching with Material3 integration.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:138</code></summary>

```kotlin
    val financeColors = if (darkTheme) darkFinanceColors() else lightFinanceColors()

    KptMaterialTheme(theme = themeProvider) {
        // Provide the design-system token CompositionLocals app-wide so every widget
        // built on `core/designsystem/component/`, `chart/`, and `motion/` resolves
        // semantic finance colors, motion specs, spacing scale, and elevation tiers
        // without per-call wiring. Forks override any subset via
```

</details>

```kotlin
fun KptMaterialTheme(
```
DSL builder for creating KptMaterialTheme with custom configuration

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:138</code></summary>

```kotlin
    val financeColors = if (darkTheme) darkFinanceColors() else lightFinanceColors()

    KptMaterialTheme(theme = themeProvider) {
        // Provide the design-system token CompositionLocals app-wide so every widget
        // built on `core/designsystem/component/`, `chart/`, and `motion/` resolves
        // semantic finance colors, motion specs, spacing scale, and elevation tiers
        // without per-call wiring. Forks override any subset via
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptTheme.kt`

```kotlin
fun KptTheme(
```
KptTheme provides the core theming composable that makes all KPT design tokens available to child components through Composition Locals.

<details><summary>Example</summary>

```kotlin
KptTheme {
    // All child components can now access:
    // KptTheme.colorScheme
    // KptTheme.typography
    // KptTheme.shapes
    // KptTheme.spacing
    // KptTheme.elevation
    MyScreen()
}
```

</details>

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/AddOrEditLoanScreenPreview.kt:33</code></summary>

```kotlin
@Composable
internal fun LoanFormFieldsEnabledPreview() {
    KptTheme {
        Column {
            LoanKindDropdown(value = LoanKind.AUTO, onChange = {}, enabled = true)
            DoubleField(label = "Principal", value = 100_000.0, onChange = {}, enabled = true)
            IntField(label = "Tenure (months)", value = 60, onChange = {}, enabled = true)
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptThemeExtensions.kt`

```kotlin
fun KptSpacing.paddingValues(
```
Creates `PaddingValues` using KPT spacing tokens with horizontal and vertical values. This extension function provides a convenient way to create consistent padding using the design system's spacing scale.

<details><summary>Example</summary>

```kotlin
Box(
    modifier = Modifier.padding(
        KptTheme.spacing.paddingValues(
            horizontal = KptTheme.spacing.lg,
            vertical = KptTheme.spacing.md
        )
    )
)
```

</details>

```kotlin
fun KptSpacing.paddingValues(
```
Creates `PaddingValues` using KPT spacing tokens with individual edge values. This extension function provides fine-grained control over padding for each edge while maintaining consistency with the design system's spacing scale.

<details><summary>Example</summary>

```kotlin
Card(
    modifier = Modifier.padding(
        KptTheme.spacing.paddingValues(
            start = KptTheme.spacing.lg,
            top = KptTheme.spacing.md,
            end = KptTheme.spacing.lg,
            bottom = KptTheme.spacing.xl
        )
    )
)
```

</details>

```kotlin
fun KptTypography.toMaterial3Typography(fontFamily: FontFamily? = FontFamily.Default): Typography
```
_No KDoc at source._

```kotlin
fun Typography.toKptTypography(fontFamily: FontFamily? = FontFamily.Default): KptTypography =
```
_No KDoc at source._

```kotlin
fun KptTypography.toMaterial3Typography(): Typography
```
Extension function to convert KptTypography to Material3 Typography This ensures that all Material3 components automatically use KptTheme typography

```kotlin
fun Typography.toKptTypography(): KptTypography = KptTypographyImpl(
```
_No KDoc at source._

```kotlin
fun KptColorScheme.toMaterial3ColorScheme(): ColorScheme
```
Extension function to convert KptColorScheme to Material3 ColorScheme This ensures that all Material3 components automatically use KptTheme colors

```kotlin
fun ColorScheme.toKptColorScheme(): KptColorScheme = KptColorSchemeImpl(
```
_No KDoc at source._

```kotlin
fun KptShapes.toMaterial3Shapes(): Shapes
```
Extension function to convert KptShapes to Material3 Shapes This ensures that all Material3 components automatically use KptTheme shapes

```kotlin
fun Shapes.toKptShapes(): KptShapes = KptShapesImpl(
```
_No KDoc at source._

```kotlin
fun KptElevation.cardElevation(
```
Get CardDefaults.cardElevation using KptTheme elevation

```kotlin
object KptSpacingDefaults
```
Predefined spacing combinations for common UI patterns. This object provides convenient access to commonly used padding configurations that follow design system best practices.

<details><summary>Example</summary>

```kotlin
// Apply standard screen padding
Column(
    modifier = Modifier.padding(KptSpacingDefaults.screenPadding())
) {
    // Screen content
}

// Apply card content padding
Card {
    Column(
        modifier = Modifier.padding(KptSpacingDefaults.cardPadding())
    ) {
        // Card content
    }
}
```

</details>

- `fun screenPadding() = KptTheme.spacing.paddingValues(` — Standard padding for screen-level content.
- `fun cardPadding() = KptTheme.spacing.paddingValues(` — Standard padding for card content.
- `fun buttonPadding() = KptTheme.spacing.paddingValues(` — Standard padding for button content.

```kotlin
object KptElevationDefaults
```
Predefined elevation configurations for common UI patterns. This object provides semantically meaningful elevation presets that follow Material Design elevation guidelines.

<details><summary>Example</summary>

```kotlin
// Standard card elevation
Card(elevation = KptElevationDefaults.card()) {
    // Card content
}

// Prominent card for important content
Card(elevation = KptElevationDefaults.raisedCard()) {
    // Important content
}
```

</details>

- `fun card() = KptTheme.elevation.cardElevation(` — Standard card elevation for normal content.
- `fun raisedCard() = KptTheme.elevation.cardElevation(` — Elevated card for prominent content.
- `fun dialogCard() = KptTheme.elevation.cardElevation(` — High elevation for modal content.

```kotlin
val KptColorScheme.containerColors: ContainerColors
```
Provides convenient access to container color combinations. This extension property groups related container colors and their corresponding content colors for easy access.

<details><summary>Example</summary>

```kotlin
val colors = KptTheme.colorScheme.containerColors

Card(
    colors = CardDefaults.cardColors(
        containerColor = colors.primary,
        contentColor = colors.onPrimary
    )
) {
    // Card content
}
```

</details>

```kotlin
data class ContainerColors(
```
A collection of container colors and their corresponding content colors.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/AdaptiveListDetailPaneScaffold.kt`

```kotlin
fun AdaptiveListDetailPaneScaffold(
```
A layout scaffold for adaptive list-detail navigation using Material 3's `ListDetailPaneScaffold`. This composable allows you to build responsive UIs with two primary panes: a main (list) pane and a detail pane, with an optional third pane.

<details><summary>Example</summary>

```kotlin
AdaptiveListDetailPaneScaffold(
    mainPaneContent = { navigateToDetail ->
        LazyColumn {
            items(itemsList) { item ->
                ListItem(
                    headlineText = { Text(item.title) },
                    modifier = Modifier.clickable { navigateToDetail() }
                )
            }
        }
    },
    detailPaneContent = { navigateBack ->
        Column {
            Text("Detail view")
            Button(onClick = navigateBack) { Text("Back") }
        }
    }
)
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/AdaptiveNavigableListDetailScaffold.kt`

```kotlin
fun <T : PaneScaffoldItem<*>> AdaptiveNavigableListDetailPaneScaffold(
```
_No KDoc at source._

```kotlin
sealed interface SelectionVisibilityState
```
Describes the current selection state for the list pane within an adaptive layout. Used to determine how list items should behave (clickable vs. selectable) and how they are styled.

```kotlin
interface PaneScaffoldItem<T : Any>
```
_No KDoc at source._

- `val id: T`

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/AdaptiveNavigableSupportingPaneScaffold.kt`

```kotlin
fun AdaptiveNavigableSupportingPaneScaffold(
```
A composable layout for adaptive UIs that implements a navigable two-pane structure using Material 3's `SupportingPaneScaffold`.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/AdaptiveNavigationSuiteScaffold.kt`

```kotlin
fun AdaptiveNavigationSuiteScaffold(
```
A responsive scaffold that adapts the navigation UI (drawer, rail, or bottom bar) based on the current window size and device posture.

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptFlowColumn.kt`

```kotlin
fun KptFlowColumn(
```
_No KDoc at source._

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptFlowRow.kt`

```kotlin
fun KptFlowRow(
```
_No KDoc at source._

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptGrid.kt`

```kotlin
fun KptGrid(
```
_No KDoc at source._

```kotlin
interface GridScope
```
_No KDoc at source._

- `fun Modifier.gridItem(span: Int = 1): Modifier`

```kotlin
data class GridConfiguration(
```
_No KDoc at source._

```kotlin
data class BreakpointConfiguration(
```
_No KDoc at source._

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptMasonryGrid.kt`

```kotlin
fun KptMasonryGrid(
```
_No KDoc at source._

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptResponsiveLayout.kt`

```kotlin
fun KptResponsiveLayout(
```
A responsive layout composable that adapts content based on screen size breakpoints.

<details><summary>Example</summary>

```kotlin
KptResponsiveLayout(
    compact = {
        // Single column layout for phones
        LazyColumn {
            items(data) { item -> ItemCard(item) }
        }
    },
    medium = {
        // Two column grid for tablets
        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            items(data) { item -> ItemCard(item) }
        }
    },
    expanded = {
        // Three column layout with sidebar for desktop
        Row {
            Sidebar(modifier = Modifier.width(240.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f)
            ) {
                items(data) { item -> ItemCard(item) }
            }
        }
    }
)
```

</details>

```kotlin
class ResponsiveLayoutInfo(
```
Contains information about the current screen size and responsive breakpoints. This class provides both the raw screen dimensions and convenience boolean flags for determining which breakpoint the current screen size falls into.

```kotlin
fun rememberResponsiveLayoutInfo(): ResponsiveLayoutInfo
```
Remembers and provides responsive layout information based on the current window size.

<details><summary>Example</summary>

```kotlin
@Composable
fun MyScreen() {
    val layoutInfo = rememberResponsiveLayoutInfo()

    when {
        layoutInfo.isCompact -> CompactLayout()
        layoutInfo.isMedium -> MediumLayout()
        layoutInfo.isExpanded -> ExpandedLayout()
    }
}
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptSidebarLayout.kt`

```kotlin
fun KptSidebarLayout(
```
_No KDoc at source._

```kotlin
data class SidebarConfiguration(
```
_No KDoc at source._

```kotlin
enum class SidebarPosition { Start, End }
```
_No KDoc at source._

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptSplitPane.kt`

```kotlin
fun KptSplitPane(
```
_No KDoc at source._

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/layout/KptStack.kt`

```kotlin
fun KptStack(
```
_No KDoc at source._

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/theme/KptColorSchemeImpl.kt`

```kotlin
data class KptColorSchemeImpl(
```
Default `KptColorScheme` — the Material 3 baseline palette. `@Immutable` so Compose can skip recomposition when the instance is unchanged. A fork overrides only the roles it brands and inherits the rest, rather than restating all fifty.

```kotlin
data class KptTypographyImpl(
```
Default `KptTypography` — the Material 3 type scale at its standard sizes and weights.

```kotlin
data class KptShapesImpl(
```
Default `KptShapes` — the Material 3 corner scale, 4dp through 28dp.

```kotlin
data class KptSpacingImpl(
```
Default `KptSpacing` — a 4dp-based scale. Components reference these rather than literal `.dp`, so retuning density is one edit here instead of a sweep through every layout.

```kotlin
data class KptElevationImpl(
```
Default `KptElevation` — Material 3 levels 0–5.

```kotlin
data class KptThemeProviderImpl(
```
Default `KptThemeProvider`, composing the five default scales into one design language.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:130</code></summary>

```kotlin
    val mifosTypography = Typography().toKptTypography(fontFamily)

    val themeProvider = KptThemeProviderImpl(
        colors = colorScheme,
        typography = mifosTypography,
    )
```

</details>

```kotlin
val LocalKptColors = staticCompositionLocalOf<KptColorScheme> { KptColorSchemeImpl() }
```
CompositionLocal carrying the active `KptColorScheme`. `static` because the theme changes rarely — a read does not subscribe, so a palette swap recomposes the subtree rather than every reader.

```kotlin
val LocalKptTypography = staticCompositionLocalOf<KptTypography> { KptTypographyImpl() }
```
CompositionLocal carrying the active `KptTypography`.

```kotlin
val LocalKptShapes = staticCompositionLocalOf<KptShapes> { KptShapesImpl() }
```
CompositionLocal carrying the active `KptShapes`.

```kotlin
val LocalKptSpacing = staticCompositionLocalOf<KptSpacing> { KptSpacingImpl() }
```
CompositionLocal carrying the active `KptSpacing`.

```kotlin
val LocalKptElevation = staticCompositionLocalOf<KptElevation> { KptElevationImpl() }
```
CompositionLocal carrying the active `KptElevation`.

```kotlin
class KptThemeBuilder
```
DSL builder for a complete `KptThemeProvider`. Entry point: `kptTheme`.

```kotlin
class KptColorSchemeBuilder
```
DSL builder for a `KptColorScheme`; unset roles keep their defaults.

```kotlin
class KptTypographyBuilder
```
DSL builder for a `KptTypography`; unset styles keep their defaults.

```kotlin
class KptShapesBuilder
```
DSL builder for a `KptShapes`; unset corners keep their defaults.

```kotlin
class KptSpacingBuilder
```
DSL builder for a `KptSpacing`; unset steps keep their defaults.

```kotlin
class KptElevationBuilder
```
DSL builder for a `KptElevation`; unset levels keep their defaults.

```kotlin
object KptTheme
```
Composition-local accessor for the active design language — `KptTheme.colors`, `.typography`, `.shapes`, `.spacing`, `.elevation`. The read side of the theme; `kptTheme` is the write side.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/AddOrEditLoanScreenPreview.kt:33</code></summary>

```kotlin
@Composable
internal fun LoanFormFieldsEnabledPreview() {
    KptTheme {
        Column {
            LoanKindDropdown(value = LoanKind.AUTO, onChange = {}, enabled = true)
            DoubleField(label = "Principal", value = 100_000.0, onChange = {}, enabled = true)
            IntField(label = "Tenure (months)", value = 60, onChange = {}, enabled = true)
```

</details>

- `val colorScheme: KptColorScheme`
- `val typography: KptTypography`
- `val shapes: KptShapes`
- `val spacing: KptSpacing`
- `val elevation: KptElevation`

```kotlin
fun kptTheme(block: KptThemeBuilder.() -> Unit): KptThemeProvider
```
Builds a `KptThemeProvider` with the DSL, overriding only what a fork brands: ```kotlin val theme = kptTheme { colors { primary = BrandPurple } } ```

<details><summary>Example</summary>

```kotlin
val theme = kptTheme { colors { primary = BrandPurple } }
```

</details>

### `core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/theme/Motion.kt`

```kotlin
data class Motion(
```
Shared motion specs — durations, easings, and motion-pattern parameters. Values align with Material 3 motion guidance (https://m3.material.io/styles/motion/easing-and-duration). All durations in milliseconds.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:151</code></summary>

```kotlin
        CompositionLocalProvider(
            LocalFinanceColors provides financeColors,
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
        ) {
```

</details>

```kotlin
val LocalMotion = staticCompositionLocalOf { Motion() }
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:151</code></summary>

```kotlin
        CompositionLocalProvider(
            LocalFinanceColors provides financeColors,
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
        ) {
```

</details>

```kotlin
val MaterialTheme.motion: Motion
```
Resolve the active `Motion` specs from composition.

```kotlin
object MotionSnapshot
```
Last-read snapshot of the active `Motion`. Updated as a side effect whenever any `@Composable` site reads `MaterialTheme.motion`.

---

_64 type(s), 179 function(s)/property(ies); 115 carry KDoc at source; 17 authored example(s); 24 live call site(s)._
<!-- api-docs:end -->
