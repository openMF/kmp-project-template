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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptElevatedCard
import template.core.base.designsystem.component.KptErrorCard
import template.core.base.designsystem.component.KptFilledCard
import template.core.base.designsystem.component.KptInfoCard
import template.core.base.designsystem.component.KptListItemCard
import template.core.base.designsystem.component.KptMediaCard
import template.core.base.designsystem.component.KptOutlinedCard
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptStatCard
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.KptWarningCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptCardCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptCard Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedCard by remember { mutableStateOf<CardType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptCard Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = CardType.entries.toList()) { cardType ->
                CardCatalogItem(
                    cardType = cardType,
                    onClick = { selectedCard = cardType },
                )
            }
        }

        // Show the selected card demo
        when (selectedCard) {
            CardType.FILLED -> FilledCardExample()
            CardType.ELEVATED -> ElevatedCardExample()
            CardType.OUTLINED -> OutlinedCardExample()
            CardType.INFO -> InfoCardExample()
            CardType.STAT -> StatCardExample()
            CardType.MEDIA -> MediaCardExample()
            CardType.LIST_ITEM -> ListItemCardExample()
            CardType.ERROR -> ErrorCardExample()
            CardType.WARNING -> WarningCardExample()
            null -> {}
        }
    }
}

@Composable
private fun CardCatalogItem(
    cardType: CardType,
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
                    text = cardType.title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = cardType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class CardType(
    val title: String,
    val description: String,
) {
    FILLED(
        title = "Filled Card",
        description = "KptFilledCard with content",
    ),
    ELEVATED(
        title = "Elevated Card",
        description = "KptElevatedCard with content",
    ),
    OUTLINED(
        title = "Outlined Card",
        description = "KptOutlinedCard with content",
    ),
    INFO(
        title = "Info Card",
        description = "KptInfoCard with title, description, and action",
    ),
    STAT(
        title = "Stat Card",
        description = "KptStatCard for KPIs and statistics",
    ),
    MEDIA(
        title = "Media Card",
        description = "KptMediaCard with image and actions",
    ),
    LIST_ITEM(
        title = "List Item Card",
        description = "KptListItemCard for list entries",
    ),
    ERROR(
        title = "Error Card",
        description = "KptErrorCard for error messages",
    ),
    WARNING(
        title = "Warning Card",
        description = "KptWarningCard for warnings",
    ),
}

@Composable
private fun FilledCardExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptFilledCard {
            Text("This is a filled card.")
        }
    }
}

@Composable
private fun ElevatedCardExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptElevatedCard {
            Text("This is an elevated card.")
        }
    }
}

@Composable
private fun OutlinedCardExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptOutlinedCard {
            Text("This is an outlined card.")
        }
    }
}

@Composable
private fun InfoCardExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptInfoCard(
            title = "Info Title",
            description = "This is an info card with an action.",
            actionText = "Action",
            onActionClick = {},
            icon = Icons.Default.Info,
        )
    }
}

@Composable
private fun StatCardExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptStatCard(
            title = "Total Users",
            value = "1,234",
            subtitle = "Last 30 days",
            icon = Icons.Default.Star,
            valueColor = Color(0xFF388E3C),
        )
    }
}

@Composable
private fun MediaCardExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptMediaCard(
            title = "Media Card",
            description = "This card displays media content.",
            mediaContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("[Media Placeholder]", color = Color.Gray)
                }
            },
            actions = {
                Text("Action 1", modifier = Modifier.padding(end = 8.dp))
                Text("Action 2")
            },
        )
    }
}

@Composable
private fun ListItemCardExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptListItemCard(
            title = "List Item Title",
            subtitle = "Subtitle here",
            leadingIcon = Icons.Default.Info,
            trailingIcon = Icons.Default.Star,
            onTrailingIconClick = {},
        )
    }
}

@Composable
private fun ErrorCardExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptErrorCard(
            title = "Error!",
            message = "Something went wrong.",
            actionText = "Retry",
            onActionClick = {},
        )
    }
}

@Composable
private fun WarningCardExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptWarningCard(
            title = "Warning!",
            message = "This is a warning message.",
            actionText = "Dismiss",
            onActionClick = {},
        )
    }
}
