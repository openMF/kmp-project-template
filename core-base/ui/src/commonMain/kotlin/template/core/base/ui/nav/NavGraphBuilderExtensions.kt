/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.ui.nav

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KType
import template.core.base.designsystem.theme.Motion
import template.core.base.designsystem.theme.MotionSnapshot
import template.core.base.ui.util.TransitionProviders

/**
 * A wrapper around [NavGraphBuilder.composable] that supplies slide up/down transitions.
 *
 * Theme-aware since 2026-05-26 — durations resolve from `motion.durationLong1`. Suitable for
 * modal-style destinations (bottom sheets, full-screen dialogs) where the new screen slides
 * up from the bottom on push and slides down on pop.
 *
 * Two overloads: the **no-arg** variant pulls motion from [MotionSnapshot.current] (updated
 * on every `MaterialTheme.motion` read upstream), so call sites stay clean. The
 * **explicit** variant takes a [Motion] parameter — useful for tests or when you need
 * deterministic transitions regardless of theme state.
 */
inline fun <reified T : Any> NavGraphBuilder.composableWithSlideTransitions(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composableWithSlideTransitions<T>(MotionSnapshot.current, typeMap, deepLinks, content)

inline fun <reified T : Any> NavGraphBuilder.composableWithSlideTransitions(
    motion: Motion,
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = TransitionProviders.Kpt.Enter.slideUp(motion),
        exitTransition = TransitionProviders.Kpt.Exit.stay(motion),
        popEnterTransition = TransitionProviders.Kpt.Enter.stay(motion),
        popExitTransition = TransitionProviders.Kpt.Exit.slideDown(motion),
        sizeTransform = null,
        content = content,
    )
}

/**
 * A wrapper around [NavGraphBuilder.composable] that supplies "stay" transitions.
 *
 * Theme-aware since 2026-05-26 — holds the screen visible for `motion.durationLong1` while
 * the other side of the transition animates. Uses `0.99f` target alpha so Compose doesn't
 * optimize the held side away.
 *
 * Two overloads — see [composableWithSlideTransitions] for the snapshot vs. explicit
 * trade-off.
 */
inline fun <reified T : Any> NavGraphBuilder.composableWithStayTransitions(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composableWithStayTransitions<T>(MotionSnapshot.current, typeMap, deepLinks, content)

inline fun <reified T : Any> NavGraphBuilder.composableWithStayTransitions(
    motion: Motion,
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = TransitionProviders.Kpt.Enter.stay(motion),
        exitTransition = TransitionProviders.Kpt.Exit.stay(motion),
        popEnterTransition = TransitionProviders.Kpt.Enter.stay(motion),
        popExitTransition = TransitionProviders.Kpt.Exit.stay(motion),
        sizeTransform = null,
        content = content,
    )
}

/**
 * A wrapper around [NavGraphBuilder.composable] that supplies push transitions.
 *
 * Theme-aware since 2026-05-26 — push semantics preserved (only one side moves at a time:
 * entering screen slides; exiting screen stays; popping screen slides back; revealed
 * screen stays). Forks that override the `Motion` data class (slide distance, durations,
 * easing) see those changes flow through every push-transitioning screen automatically.
 *
 * Two overloads — see [composableWithSlideTransitions] for the snapshot vs. explicit
 * trade-off.
 *
 * This is suitable for screens deeper within a hierarchy that uses push transitions; the root
 * screen of such a hierarchy should use [composableWithRootPushTransitions].
 */
inline fun <reified T : Any> NavGraphBuilder.composableWithPushTransitions(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composableWithPushTransitions<T>(MotionSnapshot.current, typeMap, deepLinks, content)

inline fun <reified T : Any> NavGraphBuilder.composableWithPushTransitions(
    motion: Motion,
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = TransitionProviders.Kpt.Enter.sharedAxisForward(motion),
        exitTransition = TransitionProviders.Exit.stay,
        popEnterTransition = TransitionProviders.Enter.stay,
        popExitTransition = TransitionProviders.Kpt.Exit.sharedAxisBack(motion),
        sizeTransform = null,
        content = content,
    )
}

/**
 * A wrapper around [NavGraphBuilder.composable] that supplies push transitions to the root screen
 * in a nested graph that uses push transitions.
 *
 * Theme-aware since 2026-05-26 — see [composableWithPushTransitions].
 */
inline fun <reified T : Any> NavGraphBuilder.composableWithRootPushTransitions(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composableWithRootPushTransitions<T>(MotionSnapshot.current, typeMap, deepLinks, content)

inline fun <reified T : Any> NavGraphBuilder.composableWithRootPushTransitions(
    motion: Motion,
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = TransitionProviders.Enter.stay,
        exitTransition = TransitionProviders.Kpt.Exit.sharedAxisForward(motion),
        popEnterTransition = TransitionProviders.Kpt.Enter.sharedAxisBack(motion),
        popExitTransition = TransitionProviders.Exit.fadeOut,
        sizeTransform = null,
        content = content,
    )
}
