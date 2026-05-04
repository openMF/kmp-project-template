/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.component.KptShimmerLoadingBox
import template.core.base.designsystem.theme.KptTheme
import template.core.base.store.DataFreshness
import template.core.base.store.ScreenState

/** Default size of the visual icon/illustration in the empty/error/no-network states. */
private val DefaultVisualSize = 64.dp

/**
 * Generic composable that renders any [ScreenState] with sensible defaults.
 * Integrates [DataFreshnessIndicator] for STALE/UPDATING state and animates between states
 * with a fade transition.
 *
 * Override precedence (highest first): per-call slot lambdas → [LocalScreenStateDefaults] →
 * library defaults. Pass slot lambdas to override at this call site, or wrap a subtree in
 * `CompositionLocalProvider(LocalScreenStateDefaults provides ...)` to override app-wide.
 *
 * Usage:
 * ```
 * @Composable
 * fun SavingsScreen(vm: SavingsViewModel) {
 *     val state by vm.uiState.collectAsStateWithLifecycle()
 *     ScreenContent(state = state, onRetry = vm::onRetry) { data, freshness ->
 *         SavingsList(data.accounts)
 *     }
 * }
 * ```
 */
@Composable
fun <T> ScreenContent(
    state: ScreenState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    showFreshnessIndicator: Boolean = true,
    loading: @Composable () -> Unit = { DefaultLoadingContent() },
    empty: @Composable () -> Unit = { DefaultEmptyContent() },
    noNetwork: @Composable (isCaptivePortal: Boolean) -> Unit = { captive ->
        DefaultNoNetworkContent(onRetry, isCaptivePortal = captive)
    },
    error: @Composable (Throwable) -> Unit = { DefaultErrorContent(it, onRetry) },
    content: @Composable (data: T, freshness: DataFreshness) -> Unit,
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { it.transitionKey() },
        label = "ScreenContent",
        modifier = modifier,
    ) { current ->
        when (current) {
            is ScreenState.Loading -> loading()
            is ScreenState.Empty -> empty()
            is ScreenState.NoNetwork -> noNetwork(current.isCaptivePortal)
            is ScreenState.Error -> error(current.error)
            is ScreenState.Content -> {
                Column {
                    if (showFreshnessIndicator) {
                        DataFreshnessIndicator(
                            freshness = current.freshness,
                            fetchedAt = current.fetchedAt,
                        )
                    }
                    content(current.data, current.freshness)
                }
            }
        }
    }
}

/**
 * Stable transition key used by [AnimatedContent] to detect "same kind of state" without
 * comparing data payloads. Prevents jitter when only the [ScreenState.Content.data] changes.
 */
private fun ScreenState<*>.transitionKey(): String = when (this) {
    is ScreenState.Loading -> "loading"
    is ScreenState.Empty -> "empty"
    is ScreenState.NoNetwork -> if (isCaptivePortal) "no_network_captive" else "no_network"
    is ScreenState.Error -> "error"
    is ScreenState.Content -> "content"
}

// ── Default state composables ─────────────────────────────────────────────
//
// Each Default*Content reads its config from LocalScreenStateDefaults.current by default.
// Pass an explicit `config` argument to override per-call without going through
// CompositionLocal.

@Composable
fun DefaultLoadingContent(
    modifier: Modifier = Modifier,
    config: ScreenStateLoading = LocalScreenStateDefaults.current.loading,
) {
    when (config) {
        is ScreenStateLoading.Spinner -> Box(
            modifier = modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "Loading"
                    liveRegion = LiveRegionMode.Polite
                },
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        is ScreenStateLoading.Skeleton -> SkeletonLoadingContent(
            rowCount = config.rowCount,
            modifier = modifier,
        )
        is ScreenStateLoading.Custom -> config.content()
    }
}

@Composable
private fun SkeletonLoadingContent(
    rowCount: Int,
    modifier: Modifier = Modifier,
) {
    val spacing = KptTheme.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.md)
            .semantics {
                contentDescription = "Loading"
                liveRegion = LiveRegionMode.Polite
            },
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        repeat(rowCount) {
            KptShimmerLoadingBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            )
        }
    }
}

@Composable
fun DefaultEmptyContent(
    modifier: Modifier = Modifier,
    config: ScreenStateEmpty = LocalScreenStateDefaults.current.empty,
) {
    val spacing = KptTheme.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.lg)
            .semantics {
                contentDescription = config.title?.let { "$it. ${config.message}" } ?: config.message
                liveRegion = LiveRegionMode.Polite
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ScreenStateVisualRenderer(config.visual)
        Spacer(Modifier.height(spacing.md))
        config.title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(spacing.xs))
        }
        Text(
            text = config.message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        config.cta?.let { cta ->
            Spacer(Modifier.height(spacing.lg))
            OutlinedButton(onClick = cta.onClick) {
                Text(cta.label)
            }
        }
    }
}

@Composable
fun DefaultNoNetworkContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isCaptivePortal: Boolean = false,
    config: ScreenStateNoNetwork = LocalScreenStateDefaults.current.noNetwork,
) {
    val spacing = KptTheme.spacing
    val visual = if (isCaptivePortal) config.captivePortalVisual else config.visual
    val message = if (isCaptivePortal) config.captivePortalMessage else config.message
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.lg)
            .semantics {
                contentDescription = message
                liveRegion = LiveRegionMode.Polite
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ScreenStateVisualRenderer(visual)
        Spacer(Modifier.height(spacing.md))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(spacing.lg))
        Button(onClick = onRetry) {
            Text(config.retryText)
        }
    }
}

@Composable
fun DefaultErrorContent(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    config: ScreenStateError = LocalScreenStateDefaults.current.error,
) {
    LaunchedEffect(error) { config.onShown?.invoke(error) }
    val spacing = KptTheme.spacing
    val message = config.messageFor(error)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.lg)
            .semantics {
                contentDescription = config.title?.let { "$it. $message" } ?: message
                liveRegion = LiveRegionMode.Assertive
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ScreenStateVisualRenderer(config.visual)
        Spacer(Modifier.height(spacing.md))
        config.title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(spacing.xs))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(spacing.lg))
        Button(onClick = onRetry) {
            Text(config.retryText)
        }
    }
}

// ── Visual renderer ──────────────────────────────────────────────────────

@Composable
internal fun ScreenStateVisualRenderer(
    visual: ScreenStateVisual,
    modifier: Modifier = Modifier.size(DefaultVisualSize),
) {
    when (visual) {
        is ScreenStateVisual.Vector -> Icon(
            imageVector = visual.image,
            contentDescription = null, // parent layout owns contentDescription via semantics
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is ScreenStateVisual.PainterRef -> Image(
            painter = visual.painter(),
            contentDescription = null,
            modifier = modifier,
        )
        is ScreenStateVisual.Custom -> visual.content()
    }
}
