/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.designsystem.component.freshness

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import org.mifos.core.designsystem.component.FreshnessState

/**
 * Ambient [FreshnessState] propagated down the composition tree.
 *
 * Default is [FreshnessState.Fresh] so screens that don't opt in see the "all good"
 * styling. Wrap a sub-tree in [WithFreshness] to publish a different state — every
 * descendant [CardFreshness] / [ButtonFreshness] / [BannerFreshness] /
 * [SkeletonFreshness] picks it up automatically without prop drilling.
 */
val LocalFreshness = staticCompositionLocalOf { FreshnessState.Fresh }

/**
 * Publishes [state] as [LocalFreshness] for the [content] sub-tree.
 *
 * ```kotlin
 * WithFreshness(state = uiState.freshness) {
 *     LoanSummaryCard()  // CardFreshness reads LocalFreshness — no prop drilling.
 * }
 * ```
 *
 * Pair with `WithFreshness(FreshnessState.Updating) { … }` while a Store re-fetch
 * is in flight so any embedded freshness-aware widget pulses without explicit wiring.
 */
@Composable
fun WithFreshness(state: FreshnessState, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalFreshness provides state) { content() }
}
