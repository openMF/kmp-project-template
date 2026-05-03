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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import template.core.base.store.DataFreshness
import template.core.base.store.PagingScreenStream
import template.core.base.store.ScreenState

/**
 * [ScreenContent] variant for paginated lists.
 * Integrates [LoadMoreFooter] that handles load-more progress, errors, and end-of-list.
 *
 * Usage:
 * ```
 * PagingScreenContent(
 *     pagingStream = viewModel.pagingStream,
 *     onRetry = viewModel::retry,
 * ) { items, freshness ->
 *     LazyColumn {
 *         items(items) { item -> ItemRow(item) }
 *         item { LoadMoreFooter(pagingStream = viewModel.pagingStream) }
 *     }
 * }
 * ```
 */
@Composable
fun <T : Any> PagingScreenContent(
    pagingStream: PagingScreenStream<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    showFreshnessIndicator: Boolean = true,
    loading: @Composable () -> Unit = { DefaultLoadingContent() },
    empty: @Composable () -> Unit = { DefaultEmptyContent() },
    noNetwork: @Composable (isCaptivePortal: Boolean) -> Unit = { captive ->
        DefaultNoNetworkContent(onRetry, isCaptivePortal = captive)
    },
    error: @Composable (Throwable) -> Unit = { DefaultErrorContent(it, onRetry) },
    content: @Composable (data: List<T>, freshness: DataFreshness) -> Unit,
) {
    val state by pagingStream.state.collectAsState(ScreenState.Loading)

    ScreenContent(
        state = state,
        onRetry = onRetry,
        modifier = modifier,
        showFreshnessIndicator = showFreshnessIndicator,
        loading = loading,
        empty = empty,
        noNetwork = noNetwork,
        error = error,
        content = content,
    )
}
