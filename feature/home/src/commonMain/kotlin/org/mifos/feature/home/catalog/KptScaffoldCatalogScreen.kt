/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package org.mifos.feature.home.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptFloatingActionButton
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.rememberPullToRefreshStateData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptScaffoldCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptScaffold Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<ScaffoldDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptScaffold Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = ScaffoldDemoType.entries.toList()) { demoType ->
                ScaffoldCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected scaffold demo
        when (selectedDemo) {
            ScaffoldDemoType.BASIC -> BasicScaffoldExample()
            ScaffoldDemoType.BOTTOM_BAR -> BottomBarScaffoldExample()
            ScaffoldDemoType.FAB -> FabScaffoldExample()
            ScaffoldDemoType.SNACKBAR -> SnackbarScaffoldExample()
            ScaffoldDemoType.PULL_TO_REFRESH -> PullToRefreshScaffoldExample()
            null -> {}
        }
    }
}

@Composable
private fun ScaffoldCatalogItem(
    demoType: ScaffoldDemoType,
    onClick: () -> Unit,
) {
    KptCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = demoType.title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = demoType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class ScaffoldDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic Scaffold", "KptScaffold with top bar and content"),
    BOTTOM_BAR("Scaffold with Bottom Bar", "KptScaffold with a bottom bar"),
    FAB("Scaffold with FAB", "KptScaffold with a floating action button"),
    SNACKBAR("Scaffold with Snackbar", "KptScaffold with a snackbar host"),
    PULL_TO_REFRESH("Scaffold with Pull-to-Refresh", "KptScaffold with pull-to-refresh support"),
}

@Composable
private fun BasicScaffoldExample() {
    KptScaffold(
        topBar = {
            KptTopAppBar(title = "Basic Scaffold", onNavigationIconClick = {})
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("This is a basic scaffold.")
        }
    }
}

@Composable
private fun BottomBarScaffoldExample() {
    KptScaffold(
        topBar = {
            KptTopAppBar(title = "Scaffold with Bottom Bar", onNavigationIconClick = {})
        },
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Bottom Bar")
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("This scaffold has a bottom bar.")
        }
    }
}

@Composable
private fun FabScaffoldExample() {
    KptScaffold(
        topBar = {
            KptTopAppBar(title = "Scaffold with FAB", onNavigationIconClick = {})
        },
        floatingActionButton = {
            KptFloatingActionButton(onClick = {}, icon = Icons.Default.Add)
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("This scaffold has a FAB.")
        }
    }
}

@Composable
private fun SnackbarScaffoldExample() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    KptScaffold(
        topBar = {
            KptTopAppBar(title = "Scaffold with Snackbar", onNavigationIconClick = {})
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Hello from Snackbar!")
                    }
                },
            ) {
                Text("Show Snackbar")
            }
        }
    }
}

@Composable
private fun PullToRefreshScaffoldExample() {
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    KptScaffold(
        topBar = {
            KptTopAppBar(title = "Scaffold with Pull-to-Refresh", onNavigationIconClick = {})
        },
        rememberPullToRefreshStateData = rememberPullToRefreshStateData(
            isEnabled = true,
            isRefreshing = refreshing,
            onRefresh = {
                refreshing = true
                // Simulate refresh
                scope.launch {
                    delay(1500)
                    refreshing = false
                }
            },
        ),
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (refreshing) "Refreshing..." else "Pull down to refresh.")
        }
    }
}
