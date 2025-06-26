/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.component.KptTopAppBar

/**
 * A composable scaffold layout for the KPT design system that extends
 * the Material3 [Scaffold] by optionally supporting pull-to-refresh
 * functionality.
 *
 * Use this as a base layout structure with customizable top bar,
 * bottom bar, FAB, and optional pull-to-refresh support via
 * [rememberPullToRefreshStateData].
 *
 * @param modifier Modifier applied to the scaffold container. *(Default:
 *    [Modifier])*
 * @param topBar Composable displayed at the top of the screen. *(Optional;
 *    Default: empty lambda)*
 * @param bottomBar Composable displayed at the bottom of the screen.
 *    *(Optional; Default: empty lambda)*
 * @param snackbarHost Composable used to display snackbars. *(Optional;
 *    Default: empty lambda)*
 * @param floatingActionButton Composable FAB displayed in the scaffold.
 *    *(Optional; Default: empty lambda)*
 * @param floatingActionButtonPosition Position of the FAB. *(Default:
 *    [FabPosition.End])*
 * @param containerColor Background color of the scaffold. *(Default:
 *    [MaterialTheme.colorScheme.background])*
 * @param contentColor Foreground content color. *(Default:
 *    [contentColorFor(containerColor)])*
 * @param contentWindowInsets Insets to apply around the scaffold content.
 *    (Default: [ScaffoldDefaults.contentWindowInsets])*
 * @param rememberPullToRefreshStateData Optional state used to enable
 *    pull-to-refresh functionality. When non-null, wraps the content in a
 *    [PullToRefreshBox]. *(Optional)*
 * @param testTag Tag for UI testing. *(Optional)*
 * @param contentDescription Content description for accessibility.
 *    *(Optional)*
 * @param content The main content of the scaffold. Receives
 *    [PaddingValues] that should be respected to avoid overlap with bars
 *    or FAB.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    rememberPullToRefreshStateData: PullToRefreshStateData? = null,
    testTag: String? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        modifier = modifier.testTag(testTag ?: "KptScaffold"),
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
    ) { paddingValues ->
        if (rememberPullToRefreshStateData != null) {
            val pullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = rememberPullToRefreshStateData.isRefreshing,
                onRefresh = rememberPullToRefreshStateData.onRefresh,
                modifier = Modifier.padding(paddingValues),
            ) {
                content(PaddingValues(0.dp))
            }
        } else {
            content(paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptScaffold(
    onNavigationIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    floatingActionButtonContent: FloatingActionButtonContent? = null,
    pullToRefreshState: PullToRefreshStateData = rememberPullToRefreshStateData(),
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit = {},
) {
    Scaffold(
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                )
            }
        },
        floatingActionButton = {
            floatingActionButtonContent?.let { content ->
                FloatingActionButton(
                    onClick = content.onClick,
                    contentColor = content.contentColor,
                    content = content.content,
                )
            }
        },
        snackbarHost = snackbarHost,
        containerColor = Color.Transparent,
        content = { paddingValues ->
            val internalPullToRefreshState = rememberPullToRefreshState()
            Box(
                modifier = Modifier.pullToRefresh(
                    state = internalPullToRefreshState,
                    isRefreshing = pullToRefreshState.isRefreshing,
                    onRefresh = pullToRefreshState.onRefresh,
                    enabled = pullToRefreshState.isEnabled,
                ),
            ) {
                content(paddingValues)

                PullToRefreshDefaults.Indicator(
                    modifier = Modifier
                        .padding(paddingValues)
                        .align(Alignment.TopCenter),
                    isRefreshing = pullToRefreshState.isRefreshing,
                    state = internalPullToRefreshState,
                )
            }
        },
        modifier = modifier
            .testTag("KptScaffold")
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding(),
    )
}

data class FloatingActionButtonContent(
    val onClick: (() -> Unit),
    val contentColor: Color,
    val content: (@Composable () -> Unit),
)

/**
 * Data class representing the pull-to-refresh state and behavior.
 *
 * @param isRefreshing Indicates whether the content is currently being
 *    refreshed.
 * @param onRefresh Callback triggered when a pull-to-refresh gesture is
 *    performed.
 */
data class PullToRefreshStateData(
    val isEnabled: Boolean,
    val isRefreshing: Boolean,
    val onRefresh: () -> Unit,
)

/**
 * Remembers and returns a [PullToRefreshStateData] instance.
 *
 * @param isRefreshing Whether the refresh animation should be shown.
 *    *(Default: `false`)*
 * @param onRefresh Callback to execute on pull-to-refresh. *(Default:
 *    empty lambda)*
 */
@Composable
fun rememberPullToRefreshStateData(
    isEnabled: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = { },
) = remember(isEnabled, isRefreshing, onRefresh) {
    PullToRefreshStateData(
        isEnabled = isEnabled,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    )
}
