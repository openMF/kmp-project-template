/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.core

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import kotlin.reflect.KClass

/**
 * The base contract every `Kpt*` component satisfies: a test tag, a content description and a
caller-supplied [Modifier].
 *
 * Uniform on purpose — a UI test can locate any component the same way, and no component invents
 * its own accessibility story.
 */
interface KptComponent {
    val testTag: String?
    val contentDescription: String?
    val modifier: Modifier
}

/**
 * Mixed into components that respond to a tap.
 *
 * [interactionSource] is exposed so a caller can hoist ripple/press state — a component that owns
 * it privately cannot participate in a parent's interaction handling.
 */
interface Clickable {
    val onClick: () -> Unit
    val enabled: Boolean
    val interactionSource: MutableInteractionSource?
}

/**
 * Mixed into components whose colors, shape and elevation can be overridden at the call site.
 *
 * Every member is nullable: null means "inherit from the theme", which keeps a component themed by
 * default and overridable only where a screen genuinely differs.
 */
interface Styleable {
    val colors: ComponentColors?
    val shape: Shape?
    val elevation: ComponentElevation?
}

/**
 * Mixed into components that accept a whole [ComponentTheme] rather than individual style slots.
 */
interface Themeable {
    val theme: ComponentTheme?
}

/**
 * Marker for a component's color set. Each component defines its own slots; the marker exists so
 * [Styleable] can carry them without knowing the shape.
 */
interface ComponentColors

/**
 * Marker for a component's elevation set, per interaction state (resting, pressed, focused).
 */
interface ComponentElevation

/**
 * Marker for a complete component theme — colors, shape and elevation resolved together.
 */
interface ComponentTheme

/**
 * Resolves the [ComponentTheme] for a component, letting a fork swap the whole theming rule rather
 * than overriding components one at a time.
 */
interface ThemeStrategy {
    fun applyTheme(component: KptComponent): ComponentTheme
}

/**
 * Builds a component of type [T] from a [ComponentConfiguration] — the seam that lets components be
 * constructed from data (a registry, a server-driven layout) instead of only from Kotlin call sites.
 */
interface ComponentFactory<T : KptComponent> {
    fun create(configuration: ComponentConfiguration): T
}

/**
 * A component's declarative description, convertible to the component itself via [build].
 */
interface ComponentConfiguration {
    fun build(): KptComponent
}

/**
 * Observable holder for one component's mutable value.
 *
 * `@Stable` so Compose can skip recomposition when the reference is unchanged; mutate through
 * [update] rather than replacing the holder, or that guarantee is lost.
 */
@Stable
interface ComponentState<T> {
    val value: T
    fun update(newValue: T)
}

/**
 * A named visual variant of a component (filled, outlined, tonal, …).
 *
 * Sealed so the variant set is closed and exhaustively handled at each render site.
 */
sealed interface ComponentVariant {
    val name: String
    val isEnabled: Boolean get() = true
}

/**
 * Renders a list of components as one composition — used where a screen's content is assembled from
 * data rather than written out.
 */
interface ComponentComposer {
    @Composable
    fun compose(components: List<KptComponent>): Unit
}

/**
 * Mixed into components with a tunable transition. See `theme/Motion.kt` for the shared durations;
 * overriding per component is what makes an app's motion feel inconsistent.
 */
interface Animatable {
    val animationDuration: Long
    val animationEasing: androidx.compose.animation.core.Easing?
}

/**
 * Supplies a component's semantics — description, role and any extra properties.
 *
 * Separate from [KptComponent] so a component can delegate accessibility to a wrapper rather than
 * re-declaring it.
 */
interface AccessibilityProvider {
    val semantics: androidx.compose.ui.semantics.SemanticsPropertyReceiver.() -> Unit
    val contentDescription: String?
    val role: androidx.compose.ui.semantics.Role?
}

/**
 * The whole design language in one object: colors, typography, shapes, spacing and elevation.
 *
 * A fork supplies its own and every component follows, which is the point of the indirection.
 */
interface KptThemeProvider {
    val colors: KptColorScheme
    val typography: KptTypography
    val shapes: KptShapes
    val spacing: KptSpacing
    val elevation: KptElevation
}

/**
 * The full Material 3 color role set.
 *
 * Roles, not literal colors — a component asks for `onSurfaceVariant`, never a hex value, so light
 * and dark themes and a fork's palette all work without touching the component.
 */
@Stable
interface KptColorScheme {
    val primary: Color
    val onPrimary: Color
    val primaryContainer: Color
    val onPrimaryContainer: Color
    val inversePrimary: Color
    val secondary: Color
    val onSecondary: Color
    val secondaryContainer: Color
    val onSecondaryContainer: Color
    val tertiary: Color
    val onTertiary: Color
    val tertiaryContainer: Color
    val onTertiaryContainer: Color
    val background: Color
    val onBackground: Color
    val surface: Color
    val onSurface: Color
    val surfaceVariant: Color
    val onSurfaceVariant: Color
    val surfaceTint: Color
    val inverseSurface: Color
    val inverseOnSurface: Color
    val error: Color
    val onError: Color
    val errorContainer: Color
    val onErrorContainer: Color
    val outline: Color
    val outlineVariant: Color
    val scrim: Color
    val surfaceBright: Color
    val surfaceDim: Color
    val surfaceContainer: Color
    val surfaceContainerHigh: Color
    val surfaceContainerHighest: Color
    val surfaceContainerLow: Color
    val surfaceContainerLowest: Color
    val primaryFixed: Color
    val primaryFixedDim: Color
    val onPrimaryFixed: Color
    val onPrimaryFixedVariant: Color
    val secondaryFixed: Color
    val secondaryFixedDim: Color
    val onSecondaryFixed: Color
    val onSecondaryFixedVariant: Color
    val tertiaryFixed: Color
    val tertiaryFixedDim: Color
    val onTertiaryFixed: Color
    val onTertiaryFixedVariant: Color
}

/**
 * The Material 3 type scale — display through label, each in three sizes.
 */
@Stable
interface KptTypography {
    val displayLarge: androidx.compose.ui.text.TextStyle
    val displayMedium: androidx.compose.ui.text.TextStyle
    val displaySmall: androidx.compose.ui.text.TextStyle
    val headlineLarge: androidx.compose.ui.text.TextStyle
    val headlineMedium: androidx.compose.ui.text.TextStyle
    val headlineSmall: androidx.compose.ui.text.TextStyle
    val titleLarge: androidx.compose.ui.text.TextStyle
    val titleMedium: androidx.compose.ui.text.TextStyle
    val titleSmall: androidx.compose.ui.text.TextStyle
    val bodyLarge: androidx.compose.ui.text.TextStyle
    val bodyMedium: androidx.compose.ui.text.TextStyle
    val bodySmall: androidx.compose.ui.text.TextStyle
    val labelLarge: androidx.compose.ui.text.TextStyle
    val labelMedium: androidx.compose.ui.text.TextStyle
    val labelSmall: androidx.compose.ui.text.TextStyle
}

/**
 * The corner-shape scale, from `extraSmall` to `extraLarge`, applied by component size rather than
 * chosen per call site.
 */
@Stable
interface KptShapes {
    val extraSmall: CornerBasedShape
    val small: CornerBasedShape
    val medium: CornerBasedShape
    val large: CornerBasedShape
    val extraLarge: CornerBasedShape
}

/**
 * The spacing scale every layout measures with.
 *
 * Components reference these rather than literal `.dp` values so density stays uniform and a fork
 * can retune the whole app's rhythm in one place.
 */
@Stable
interface KptSpacing {
    val xs: Dp
    val sm: Dp
    val md: Dp
    val lg: Dp
    val xl: Dp
    val xxl: Dp
}

/**
 * The elevation scale, in Material 3 levels 0–5.
 */
@Stable
interface KptElevation {
    val level0: Dp
    val level1: Dp
    val level2: Dp
    val level3: Dp
    val level4: Dp
    val level5: Dp
}

/**
 * Renders component type [T]. Registered in a [ComponentRegistry] so a data-driven layout can resolve
 * a renderer by type at runtime.
 */
interface ComponentRenderer<T : KptComponent> {
    @Composable
    fun render(component: T)
}

/**
 * Maps component types to their renderers and factories — the lookup a [ComponentComposer] uses.
 */
interface ComponentRegistry {
    fun <T : KptComponent> register(type: KClass<T>, renderer: ComponentRenderer<T>)
    fun <T : KptComponent> getRenderer(type: KClass<T>): ComponentRenderer<T>?
}

/**
 * DSL marker for the component-configuration builders.
 *
 * Stops an inner builder from implicitly seeing an outer scope's receivers, which is how nested
 * DSL blocks silently configure the wrong component.
 */
@DslMarker
annotation class ComponentDsl

/**
 * Receiver for the component-configuration DSL, scoped by [ComponentDsl].
 */
@ComponentDsl
interface ComponentConfigurationScope {
    var testTag: String?
    var contentDescription: String?
    var enabled: Boolean
    var modifier: Modifier
}
