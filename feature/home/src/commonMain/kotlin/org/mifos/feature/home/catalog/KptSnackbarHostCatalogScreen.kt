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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptSnackbarHost
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.SnackbarConfiguration
import template.core.base.designsystem.component.showKptSnackbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptSnackbarHostCatalogScreen(
    navigateBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedDemo by remember { mutableStateOf<SnackbarDemoType?>(null) }

    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptSnackbarHost Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
        snackbarHost = {
            KptSnackbarHost(hostState = snackbarHostState)
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptSnackbarHost Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = SnackbarDemoType.entries.toList()) { demoType ->
                SnackbarCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected snackbar demo
        when (selectedDemo) {
            SnackbarDemoType.BASIC -> BasicSnackbarExample(snackbarHostState, coroutineScope)
            SnackbarDemoType.ACTION -> ActionSnackbarExample(snackbarHostState, coroutineScope)
            SnackbarDemoType.DISMISS -> DismissSnackbarExample(snackbarHostState, coroutineScope)
            SnackbarDemoType.CUSTOM_DURATION -> CustomDurationSnackbarExample(
                snackbarHostState,
                coroutineScope,
            )

            null -> {}
        }
    }
}

@Composable
private fun SnackbarCatalogItem(
    demoType: SnackbarDemoType,
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

private enum class SnackbarDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic Snackbar", "Show a simple snackbar with a message."),
    ACTION("Snackbar with Action", "Show a snackbar with an action button."),
    DISMISS("Snackbar with Dismiss Action", "Show a snackbar with a dismiss action."),
    CUSTOM_DURATION("Snackbar with Custom Duration", "Show a snackbar with a long duration."),
}

@Composable
private fun BasicSnackbarExample(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptCard(
            onClick = {
                coroutineScope.launch {
                    snackbarHostState.showKptSnackbar(
                        SnackbarConfiguration(message = "This is a basic snackbar!"),
                    )
                }
            },
        ) {
            Text(
                text = "Show Basic Snackbar",
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}

@Composable
private fun ActionSnackbarExample(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptCard(
            onClick = {
                coroutineScope.launch {
                    snackbarHostState.showKptSnackbar(
                        SnackbarConfiguration(
                            message = "Snackbar with Action!",
                            actionLabel = "Undo",
                        ),
                    )
                }
            },
        ) {
            Text(
                text = "Show Snackbar with Action",
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}

@Composable
private fun DismissSnackbarExample(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptCard(
            onClick = {
                coroutineScope.launch {
                    snackbarHostState.showKptSnackbar(
                        SnackbarConfiguration(
                            message = "Snackbar with Dismiss Action!",
                            withDismissAction = true,
                        ),
                    )
                }
            },
        ) {
            Text(
                text = "Show Snackbar with Dismiss Action",
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}

@Composable
private fun CustomDurationSnackbarExample(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptCard(
            onClick = {
                coroutineScope.launch {
                    snackbarHostState.showKptSnackbar(
                        SnackbarConfiguration(
                            message = "Snackbar with Long Duration!",
                            duration = SnackbarDuration.Long,
                        ),
                    )
                }
            },
        ) {
            Text(
                text = "Show Snackbar with Long Duration",
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}
