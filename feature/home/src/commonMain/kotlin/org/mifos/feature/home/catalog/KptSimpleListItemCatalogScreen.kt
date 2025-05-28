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
import template.core.base.designsystem.component.KptSimpleListItem
import template.core.base.designsystem.component.KptTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptSimpleListItemCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptSimpleListItem Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<SimpleListItemDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptSimpleListItem Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = SimpleListItemDemoType.entries.toList()) { demoType ->
                SimpleListItemCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected simple list item demo
        when (selectedDemo) {
            SimpleListItemDemoType.BASIC -> BasicSimpleListItemExample()
            SimpleListItemDemoType.SUPPORTING -> SupportingSimpleListItemExample()
            SimpleListItemDemoType.LEADING_ICON -> LeadingIconSimpleListItemExample()
            SimpleListItemDemoType.TRAILING_ICON -> TrailingIconSimpleListItemExample()
            SimpleListItemDemoType.CLICKABLE -> ClickableSimpleListItemExample()
            null -> {}
        }
    }
}

@Composable
private fun SimpleListItemCatalogItem(
    demoType: SimpleListItemDemoType,
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

private enum class SimpleListItemDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic Simple List Item", "KptSimpleListItem with text only"),
    SUPPORTING("With Supporting Text", "KptSimpleListItem with supporting text"),
    LEADING_ICON("With Leading Icon", "KptSimpleListItem with leading icon"),
    TRAILING_ICON("With Trailing Icon", "KptSimpleListItem with trailing icon"),
    CLICKABLE("Clickable Simple List Item", "KptSimpleListItem with onClick action"),
}

@Composable
private fun BasicSimpleListItemExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSimpleListItem(text = "Simple Item")
    }
}

@Composable
private fun SupportingSimpleListItemExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSimpleListItem(text = "Simple Item", supportingText = "Supporting text")
    }
}

@Composable
private fun LeadingIconSimpleListItemExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSimpleListItem(text = "Simple Item", leadingIcon = Icons.Default.Info)
    }
}

@Composable
private fun TrailingIconSimpleListItemExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSimpleListItem(text = "Simple Item", trailingIcon = Icons.Default.Star)
    }
}

@Composable
private fun ClickableSimpleListItemExample() {
    var clicked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSimpleListItem(
            text = if (clicked) "Clicked!" else "Click me",
            onClick = { clicked = !clicked },
        )
    }
}
