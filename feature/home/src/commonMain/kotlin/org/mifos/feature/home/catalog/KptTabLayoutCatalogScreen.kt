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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import template.core.base.designsystem.component.KptTabLayout
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.TabItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptTabLayoutCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptTabLayout Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<TabLayoutDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptTabLayout Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = TabLayoutDemoType.entries.toList()) { demoType ->
                TabLayoutCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected tab layout demo
        when (selectedDemo) {
            TabLayoutDemoType.BASIC -> BasicTabLayoutExample()
            TabLayoutDemoType.WITH_ICONS -> TabLayoutWithIconsExample()
            TabLayoutDemoType.DYNAMIC_CONTENT -> TabLayoutDynamicContentExample()
            null -> {}
        }
    }
}

@Composable
private fun TabLayoutCatalogItem(
    demoType: TabLayoutDemoType,
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

private enum class TabLayoutDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic Tab Layout", "A simple tab layout with text tabs."),
    WITH_ICONS("Tab Layout with Icons", "Tabs with icons and text."),
    DYNAMIC_CONTENT(
        "Tab Layout with Dynamic Content",
        "Tabs with content that changes based on tab selection.",
    ),
}

@Composable
private fun BasicTabLayoutExample() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        TabItem(title = "Tab 1", content = { CenteredText("Content for Tab 1") }),
        TabItem(title = "Tab 2", content = { CenteredText("Content for Tab 2") }),
        TabItem(title = "Tab 3", content = { CenteredText("Content for Tab 3") }),
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        KptTabLayout(
            tabs = tabs,
            selectedTabIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        )
    }
}

@Composable
private fun TabLayoutWithIconsExample() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        TabItem(
            title = "Home",
            icon = Icons.Default.Home,
            content = { CenteredText("Home Content") },
        ),
        TabItem(
            title = "Profile",
            icon = Icons.Default.Person,
            content = { CenteredText("Profile Content") },
        ),
        TabItem(
            title = "Settings",
            icon = Icons.Default.Settings,
            content = { CenteredText("Settings Content") },
        ),
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        KptTabLayout(
            tabs = tabs,
            selectedTabIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        )
    }
}

@Composable
private fun TabLayoutDynamicContentExample() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        TabItem(
            title = "Numbers",
            content = {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    repeat(3) { Text("Number: ${it + 1}") }
                }
            },
        ),
        TabItem(
            title = "Letters",
            content = {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    listOf("A", "B", "C").forEach { Text("Letter: $it") }
                }
            },
        ),
        TabItem(
            title = "Symbols",
            content = {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    listOf("@", "#", "$", "%").forEach { Text("Symbol: $it") }
                }
            },
        ),
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        KptTabLayout(
            tabs = tabs,
            selectedTabIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        )
    }
}

@Composable
private fun CenteredText(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
    }
}
