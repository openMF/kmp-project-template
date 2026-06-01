/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.DataFreshness
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent

/**
 * Renders a vertical stack of cards where each card carries its OWN [ScreenState] —
 * so a slow card can still spin while a fast one shows content, and a single failed
 * fetch only ruins one card instead of blanking the whole dashboard.
 *
 * Each card is wrapped in [ScreenContent], so the per-card loading / empty / no-network
 * / error / content rendering is identical to a standalone screen — same defaults,
 * same captive-portal handling, same retry CTA. Pass [onDismiss] to additionally render
 * an "X" close icon next to retry, letting users hide a single failing card
 * (typical use case: a non-critical optional card in a multi-card dashboard).
 *
 * Pair with [aggregateDashboardProgress] + [DashboardProgressBar] for a top-of-screen
 * "X of Y loaded" indicator above the grid.
 *
 * The per-card content lambda receives the `(index, data, freshness)` tuple so consumers
 * can drive per-index dispatch (e.g., a different child composable per card slot).
 *
 * TODO(testing): UI tests for this composable land once Compose UI test infrastructure
 * (compose.uiTest in commonTest) is wired into core-base/ui — see Phase 13 of the
 * core-base-store-coverage epic.
 *
 * @param states List of per-card [ScreenState] — order defines the visual order of cards.
 * @param onRetry Invoked with the card's index when the user taps "Try again" on its
 *   error / no-network state.
 * @param modifier Modifier applied to the wrapping [Column].
 * @param onDismiss Optional — when non-null, each card's error / no-network state shows an
 *   "X" icon button alongside retry; the lambda receives the card index.
 * @param content Per-card content composable invoked when the card's state is
 *   [ScreenState.Content]. Receives the card index, its payload, and freshness.
 */
@Composable
fun <T> IndependentCardLayout(
    states: List<ScreenState<T>>,
    onRetry: (Int) -> Unit,
    modifier: Modifier = Modifier,
    refreshingIndicator: (@Composable () -> Unit)? = null,
    onDismiss: ((Int) -> Unit)? = null,
    content: @Composable (index: Int, data: T, freshness: DataFreshness) -> Unit,
) {
    val spacing = KptTheme.spacing
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        states.forEachIndexed { index, state ->
            ScreenContent(
                state = state,
                onRetry = { onRetry(index) },
                refreshingIndicator = refreshingIndicator,
                onDismiss = onDismiss?.let { dismiss -> { dismiss(index) } },
            ) { data, freshness ->
                content(index, data, freshness)
            }
        }
    }
}
