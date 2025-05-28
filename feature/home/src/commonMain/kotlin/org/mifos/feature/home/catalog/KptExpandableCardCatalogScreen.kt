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
import template.core.base.designsystem.component.KptExpandableCard
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptExpandableCardCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptExpandableCard Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<ExpandableCardDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptExpandableCard Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = ExpandableCardDemoType.entries.toList()) { demoType ->
                ExpandableCardCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected expandable card demo
        when (selectedDemo) {
            ExpandableCardDemoType.BASIC -> BasicExpandableCardExample()
            ExpandableCardDemoType.SUBTITLE -> SubtitleExpandableCardExample()
            ExpandableCardDemoType.ICON -> IconExpandableCardExample()
            null -> {}
        }
    }
}

@Composable
private fun ExpandableCardCatalogItem(
    demoType: ExpandableCardDemoType,
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

private enum class ExpandableCardDemoType(
    val title: String,
    val description: String,
) {
    BASIC(
        title = "Basic Expandable Card",
        description = "KptExpandableCard with title and expandable content",
    ),
    SUBTITLE(
        title = "Expandable Card with Subtitle",
        description = "KptExpandableCard with title, subtitle, and expandable content",
    ),
    ICON(
        title = "Expandable Card with Icon",
        description = "KptExpandableCard with icon, title, and expandable content",
    ),
}

@Composable
private fun BasicExpandableCardExample() {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptExpandableCard(
            title = "Basic Expandable Card",
            expanded = expanded,
            onExpandedChange = { expanded = it },
            expandedContent = {
                Text("This is the expanded content.")
            },
        )
    }
}

@Composable
private fun SubtitleExpandableCardExample() {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptExpandableCard(
            title = "Expandable Card with Subtitle",
            subtitle = "This is a subtitle.",
            expanded = expanded,
            onExpandedChange = { expanded = it },
            expandedContent = {
                Text("This is the expanded content with a subtitle.")
            },
        )
    }
}

@Composable
private fun IconExpandableCardExample() {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptExpandableCard(
            title = "Expandable Card with Icon",
            icon = Icons.Default.Star,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            expandedContent = {
                Text("This is the expanded content with an icon.")
            },
        )
    }
}
