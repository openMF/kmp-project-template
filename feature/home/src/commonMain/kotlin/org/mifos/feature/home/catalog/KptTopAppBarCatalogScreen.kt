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
import androidx.compose.material.icons.filled.MoreVert
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
import template.core.base.designsystem.component.KptProfileAppBar
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptSearchAppBar
import template.core.base.designsystem.component.KptSettingsAppBar
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.core.TopAppBarVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptTopAppBarCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptTopAppBar Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<TopAppBarDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptTopAppBar Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = TopAppBarDemoType.entries.toList()) { demoType ->
                TopAppBarCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected top app bar demo
        when (selectedDemo) {
            TopAppBarDemoType.BASIC -> BasicTopAppBarExample()
            TopAppBarDemoType.NAVIGATION -> NavigationTopAppBarExample()
            TopAppBarDemoType.SUBTITLE -> SubtitleTopAppBarExample()
            TopAppBarDemoType.SINGLE_ACTION -> SingleActionTopAppBarExample()
            TopAppBarDemoType.SEARCH -> SearchAppBarExample()
            TopAppBarDemoType.PROFILE -> ProfileAppBarExample()
            TopAppBarDemoType.SETTINGS -> SettingsAppBarExample()
            TopAppBarDemoType.VARIANTS -> VariantsTopAppBarExample()
            null -> {}
        }
    }
}

@Composable
private fun TopAppBarCatalogItem(
    demoType: TopAppBarDemoType,
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

private enum class TopAppBarDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic Top App Bar", "A simple top app bar with title."),
    NAVIGATION("With Navigation", "Top app bar with navigation icon and callback."),
    SUBTITLE("With Subtitle", "Top app bar with title and subtitle."),
    SINGLE_ACTION("With Single Action", "Top app bar with a single action icon."),
    SEARCH("Search App Bar", "Top app bar with search field and actions."),
    PROFILE("Profile App Bar", "Top app bar with profile action and optional subtitle."),
    SETTINGS("Settings App Bar", "Top app bar with search and more actions."),
    VARIANTS("All Variants", "Show all top app bar variants: Small, CenterAligned, Medium, Large."),
}

@Composable
private fun BasicTopAppBarExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        KptTopAppBar(title = "Basic App Bar")
    }
}

@Composable
private fun NavigationTopAppBarExample() {
    var clicked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        KptTopAppBar(
            title = if (clicked) "Back Clicked!" else "With Navigation",
            onNavigationIconClick = { clicked = !clicked },
        )
    }
}

@Composable
private fun SubtitleTopAppBarExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        KptTopAppBar(
            title = "With Subtitle",
            subtitle = "This is a subtitle",
            onNavigationIconClick = {},
        )
    }
}

@Composable
private fun SingleActionTopAppBarExample() {
    var actionClicked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        KptTopAppBar(
            title = if (actionClicked) "Action Clicked!" else "With Action",
            actionIcon = Icons.Default.MoreVert,
            onActionClick = { actionClicked = !actionClicked },
            onNavigationIconClick = {},
        )
    }
}

@Composable
private fun SearchAppBarExample() {
    var query by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        KptSearchAppBar(
            searchQuery = query,
            onSearchQueryChange = { query = it },
            onBackClick = {},
            onSearchClick = {},
        )
    }
}

@Composable
private fun ProfileAppBarExample() {
    var profileClicked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        KptProfileAppBar(
            title = if (profileClicked) "Profile Clicked!" else "Profile App Bar",
            onProfileClick = { profileClicked = !profileClicked },
            subtitle = "User subtitle",
            onNavigationIconClick = {},
        )
    }
}

@Composable
private fun SettingsAppBarExample() {
    var searchClicked by remember { mutableStateOf(false) }
    var moreClicked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        KptSettingsAppBar(
            onNavigationIconClick = {},
            onSearchClick = { searchClicked = !searchClicked },
            onMoreClick = { moreClicked = !moreClicked },
        )
        if (searchClicked) {
            Text("Search clicked!", modifier = Modifier.align(Alignment.Center))
        }
        if (moreClicked) {
            Text("More clicked!", modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
private fun VariantsTopAppBarExample() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KptTopAppBar(title = "Small Variant", variant = TopAppBarVariant.Small)
        KptTopAppBar(title = "CenterAligned Variant", variant = TopAppBarVariant.CenterAligned)
        KptTopAppBar(title = "Medium Variant", variant = TopAppBarVariant.Medium)
        KptTopAppBar(title = "Large Variant", variant = TopAppBarVariant.Large)
    }
}
