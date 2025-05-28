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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptSlideTransition
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.SlideDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptSlideTransitionCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptSlideTransition Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<SlideTransitionDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptSlideTransition Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = SlideTransitionDemoType.entries.toList()) { demoType ->
                SlideTransitionCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected slide transition demo
        when (selectedDemo) {
            SlideTransitionDemoType.LEFT -> SlideTransitionExample(direction = SlideDirection.Left)
            SlideTransitionDemoType.RIGHT -> SlideTransitionExample(direction = SlideDirection.Right)
            SlideTransitionDemoType.UP -> SlideTransitionExample(direction = SlideDirection.Up)
            SlideTransitionDemoType.DOWN -> SlideTransitionExample(direction = SlideDirection.Down)
            null -> {}
        }
    }
}

@Composable
private fun SlideTransitionCatalogItem(
    demoType: SlideTransitionDemoType,
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

private enum class SlideTransitionDemoType(
    val title: String,
    val description: String,
) {
    LEFT("Slide from Left", "KptSlideTransition with SlideDirection.Left"),
    RIGHT("Slide from Right", "KptSlideTransition with SlideDirection.Right"),
    UP("Slide from Up", "KptSlideTransition with SlideDirection.Up"),
    DOWN("Slide from Down", "KptSlideTransition with SlideDirection.Down"),
}

@Composable
private fun SlideTransitionExample(direction: SlideDirection) {
    var visible by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { visible = !visible }) {
                Text(if (visible) "Hide" else "Show")
            }
            Spacer(modifier = Modifier.height(24.dp))
            KptSlideTransition(visible = visible, direction = direction) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(120.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    KptCard(
                        modifier = Modifier.fillMaxSize(),
                        onClick = {},
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Sliding ${direction.name}",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}
