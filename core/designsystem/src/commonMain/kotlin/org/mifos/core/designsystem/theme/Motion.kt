/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared motion specs — durations, easings, and motion-pattern parameters.
 *
 * Values align with Material 3 motion guidance
 * (https://m3.material.io/styles/motion/easing-and-duration). All durations in milliseconds.
 *
 * Access from any Composable via [MaterialTheme.motion]. Sub-plan 05 builds shared-axis nav
 * transitions, fade-through, list-item-enter, and refreshing-pulse on top of these tokens —
 * changing a single value here propagates everywhere.
 */
@Immutable
data class Motion(
    // ── Durations ────────────────────────────────────────────────────────────
    /** 50ms — instant micro-interaction (chip press, icon toggle). */
    val durationShort1: Int = 50,
    /** 100ms — fast micro-interaction. */
    val durationShort2: Int = 100,
    /** 150ms — fast feedback (ripple, hover). */
    val durationShort3: Int = 150,
    /** 200ms — fast container-state change. */
    val durationShort4: Int = 200,
    /** 250ms — standard fade / cross-fade. */
    val durationMedium1: Int = 250,
    /** 300ms — standard container morph. */
    val durationMedium2: Int = 300,
    /** 350ms — standard slide. */
    val durationMedium3: Int = 350,
    /** 400ms — slow container morph. */
    val durationMedium4: Int = 400,
    /** 450ms — full-screen nav transition (forward push). */
    val durationLong1: Int = 450,
    /** 500ms — full-screen nav transition. */
    val durationLong2: Int = 500,
    /** 550ms — emphasized full-screen. */
    val durationLong3: Int = 550,
    /** 600ms — extra-long full-screen. */
    val durationLong4: Int = 600,

    // ── Easings ──────────────────────────────────────────────────────────────
    /** Standard easing — most UI elements. */
    val easingStandard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
    /** Emphasized easing — full-screen transitions, hero content. */
    val easingEmphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f),
    /** Decelerated — incoming elements (enter). */
    val easingDecelerated: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f),
    /** Accelerated — outgoing elements (exit). */
    val easingAccelerated: Easing = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f),

    // ── Pattern parameters ───────────────────────────────────────────────────
    /** Slide distance for shared-axis-X transitions. */
    val sharedAxisSlideDistance: Dp = 30.dp,
    /**
     * Duration of one full breath of the "refreshing now" pulse.
     * Consumed by sub-plan 05's `Modifier.mifosRefreshingPulse`.
     */
    val refreshingPulseDurationMs: Int = 1200,
    /**
     * Stagger delay per item when `LazyColumn` items use sub-plan 05's
     * `Modifier.mifosListItemEnter`.
     */
    val listItemEnterStaggerMs: Int = 30,
    /**
     * Cap on list-item stagger — items past this index snap into place.
     * Avoids delay-stacking on long lists.
     */
    val listItemEnterMaxAnimated: Int = 20,
)

val LocalMotion = staticCompositionLocalOf { Motion() }

/** Resolve the active [Motion] specs from composition. */
val MaterialTheme.motion: Motion
    @Composable
    @ReadOnlyComposable
    get() = LocalMotion.current
