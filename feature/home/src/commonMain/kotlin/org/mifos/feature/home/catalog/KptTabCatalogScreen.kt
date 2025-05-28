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

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTab
import template.core.base.designsystem.component.KptTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptTabCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptTab Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<TabDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptTab Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = TabDemoType.entries.toList()) { demoType ->
                TabCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected tab demo
        when (selectedDemo) {
            TabDemoType.BASIC -> BasicTabRowExample()
            TabDemoType.CUSTOM_COLORS -> CustomColorsTabRowExample()
            TabDemoType.SCROLLABLE -> ScrollableTabRowExample()
            null -> {}
        }
    }
}

@Composable
private fun TabCatalogItem(
    demoType: TabDemoType,
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

private enum class TabDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic Tab Row", "A simple row of tabs using KptTab."),
    CUSTOM_COLORS("Tab Row with Custom Colors", "Tabs with custom selected and unselected colors."),
    SCROLLABLE("Scrollable Tab Row", "A tab row with many tabs, horizontally scrollable."),
}

@Composable
private fun BasicTabRowExample() {
    val tabs = listOf("Home", "Profile", "Settings")
    var selectedTab by remember { mutableStateOf(0) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            tabs.forEachIndexed { index, title ->
                KptTab(
                    text = title,
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                )
                if (index < tabs.lastIndex) Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun CustomColorsTabRowExample() {
    val tabs = listOf("Tab 1", "Tab 2", "Tab 3")
    var selectedTab by remember { mutableStateOf(1) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            tabs.forEachIndexed { index, title ->
                KptTab(
                    text = title,
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    selectedColor = Color(0xFF6200EE),
                    unselectedColor = Color(0xFFBB86FC),
                )
                if (index < tabs.lastIndex) Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun ScrollableTabRowExample() {
    val tabs = List(10) { "Tab ${it + 1}" }
    var selectedTab by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            tabs.forEachIndexed { index, title ->
                KptTab(
                    text = title,
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                )
                if (index < tabs.lastIndex) Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
