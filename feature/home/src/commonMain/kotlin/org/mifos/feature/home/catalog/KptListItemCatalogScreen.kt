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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import template.core.base.designsystem.component.KptListItem
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.ListItemConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptListItemCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptListItem Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<ListItemDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptListItem Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = ListItemDemoType.entries.toList()) { demoType ->
                ListItemCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected list item demo
        when (selectedDemo) {
            ListItemDemoType.BASIC -> BasicListItemExample()
            ListItemDemoType.SUPPORTING -> SupportingListItemExample()
            ListItemDemoType.LEADING_ICON -> LeadingIconListItemExample()
            ListItemDemoType.TRAILING_ICON -> TrailingIconListItemExample()
            ListItemDemoType.OVERLINE -> OverlineListItemExample()
            ListItemDemoType.CLICKABLE -> ClickableListItemExample()
            null -> {}
        }
    }
}

@Composable
private fun ListItemCatalogItem(
    demoType: ListItemDemoType,
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

private enum class ListItemDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic List Item", "KptListItem with headline only"),
    SUPPORTING("List Item with Supporting Content", "KptListItem with headline and supporting content"),
    LEADING_ICON("List Item with Leading Icon", "KptListItem with leading icon and headline"),
    TRAILING_ICON("List Item with Trailing Icon", "KptListItem with headline and trailing icon"),
    OVERLINE("List Item with Overline", "KptListItem with overline, headline, and supporting content"),
    CLICKABLE("Clickable List Item", "KptListItem with onClick action"),
}

@Composable
private fun BasicListItemExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptListItem(
            configuration = ListItemConfiguration(
                headlineContent = { Text("Headline") },
            ),
        )
    }
}

@Composable
private fun SupportingListItemExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptListItem(
            configuration = ListItemConfiguration(
                headlineContent = { Text("Headline") },
                supportingContent = { Text("Supporting content goes here.") },
            ),
        )
    }
}

@Composable
private fun LeadingIconListItemExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptListItem(
            configuration = ListItemConfiguration(
                headlineContent = { Text("Headline") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
            ),
        )
    }
}

@Composable
private fun TrailingIconListItemExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptListItem(
            configuration = ListItemConfiguration(
                headlineContent = { Text("Headline") },
                trailingContent = { Icon(Icons.Default.Star, contentDescription = null) },
            ),
        )
    }
}

@Composable
private fun OverlineListItemExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptListItem(
            configuration = ListItemConfiguration(
                overlineContent = { Text("Overline") },
                headlineContent = { Text("Headline") },
                supportingContent = { Text("Supporting content goes here.") },
            ),
        )
    }
}

@Composable
private fun ClickableListItemExample() {
    var clicked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptListItem(
            configuration = ListItemConfiguration(
                headlineContent = { Text(if (clicked) "Clicked!" else "Click me") },
                onClick = { clicked = !clicked },
            ),
        )
    }
}
