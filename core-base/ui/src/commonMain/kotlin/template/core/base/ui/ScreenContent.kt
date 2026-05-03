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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import template.core.base.store.DataFreshness
import template.core.base.store.ScreenState

/**
 * Generic composable that renders any [ScreenState] with sensible defaults.
 * Integrates [DataFreshnessIndicator] for STALE/UPDATING state.
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
    when (state) {
        is ScreenState.Loading -> loading()
        is ScreenState.Empty -> empty()
        is ScreenState.NoNetwork -> noNetwork(state.isCaptivePortal)
        is ScreenState.Error -> error(state.error)
        is ScreenState.Content -> {
            Column(modifier = modifier) {
                if (showFreshnessIndicator) {
                    DataFreshnessIndicator(
                        freshness = state.freshness,
                        fetchedAt = state.fetchedAt,
                    )
                }
                content(state.data, state.freshness)
            }
        }
    }
}

@Composable
fun DefaultLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun DefaultEmptyContent(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    },
    message: String = "No data available",
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun DefaultNoNetworkContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isCaptivePortal: Boolean = false,
    icon: @Composable () -> Unit = {
        Icon(
            imageVector = if (isCaptivePortal) Icons.Default.CloudOff else Icons.Default.WifiOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    },
    message: String? = null,
    retryText: String = "Retry",
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(Modifier.height(16.dp))
        Text(
            text = message
                ?: if (isCaptivePortal) "Sign in to WiFi network" else "No internet connection",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(retryText)
        }
    }
}

@Composable
fun DefaultErrorContent(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error,
        )
    },
    message: String? = null,
    retryText: String = "Retry",
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(Modifier.height(16.dp))
        Text(
            text = message ?: error.message ?: "Something went wrong",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(retryText)
        }
    }
}
