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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import template.core.base.store.DataFreshness
import template.core.base.store.ScreenState

/**
 * [ScreenContent] variant wrapped in Material 3 [PullToRefreshBox] for detail
 * pages and other single-key screens that benefit from pull-to-refresh.
 *
 * The pull spinner is driven by `state.freshness == UPDATING`, so it appears
 * automatically while a refresh-triggered fetch is in flight (and disappears
 * when fresh data lands or an error is surfaced).
 *
 * Usage with `asScreenStream`:
 * ```kotlin
 * RefreshableScreenContent(
 *     state = uiState,
 *     onRefresh = viewModel::onRetry,  // typically same as onRetry; refresh is the broader semantic
 * ) { coin, _ ->
 *     CoinDetailContent(coin)
 * }
 * ```
 *
 * For paginated screens, use [PagingScreenContent] which has built-in pull-to-refresh.
 *
 * @param onRefresh Called when the user releases the pull gesture. Should trigger a
 *   network refresh (typically `viewModel::onRetry` if your ViewModel exposes
 *   `ScreenDataStream.retry()` / `ScreenDataStream.refresh()`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> RefreshableScreenContent(
    state: ScreenState<T>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    showFreshnessIndicator: Boolean = true,
    loading: @Composable () -> Unit = { DefaultLoadingContent() },
    empty: @Composable () -> Unit = { DefaultEmptyContent() },
    noNetwork: @Composable (isCaptivePortal: Boolean) -> Unit = { captive ->
        DefaultNoNetworkContent(onRefresh, isCaptivePortal = captive)
    },
    error: @Composable (Throwable) -> Unit = { DefaultErrorContent(it, onRefresh) },
    content: @Composable (data: T, freshness: DataFreshness) -> Unit,
) {
    val isRefreshing = (state as? ScreenState.Content<*>)?.freshness == DataFreshness.UPDATING

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        ScreenContent(
            state = state,
            onRetry = onRefresh,
            modifier = Modifier.fillMaxSize(),
            showFreshnessIndicator = showFreshnessIndicator,
            loading = loading,
            empty = empty,
            noNetwork = noNetwork,
            error = error,
            content = content,
        )
    }
}
