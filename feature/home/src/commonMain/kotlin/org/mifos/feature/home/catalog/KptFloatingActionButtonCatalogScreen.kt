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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
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
import template.core.base.designsystem.component.FabAction
import template.core.base.designsystem.component.KptAddFloatingActionButton
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptDeleteFloatingActionButton
import template.core.base.designsystem.component.KptEditFloatingActionButton
import template.core.base.designsystem.component.KptErrorFloatingActionButton
import template.core.base.designsystem.component.KptExtendedFloatingActionButton
import template.core.base.designsystem.component.KptFavoriteFloatingActionButton
import template.core.base.designsystem.component.KptFloatingActionButton
import template.core.base.designsystem.component.KptLargeFloatingActionButton
import template.core.base.designsystem.component.KptLoadingFloatingActionButton
import template.core.base.designsystem.component.KptMultiActionFloatingActionButton
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptShareFloatingActionButton
import template.core.base.designsystem.component.KptSmallFloatingActionButton
import template.core.base.designsystem.component.KptSuccessFloatingActionButton
import template.core.base.designsystem.component.KptTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptFloatingActionButtonCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptFloatingActionButton Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<FabDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptFloatingActionButton Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = FabDemoType.entries.toList()) { demoType ->
                FabCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected FAB demo
        when (selectedDemo) {
            FabDemoType.REGULAR -> RegularFabExample()
            FabDemoType.EXTENDED -> ExtendedFabExample()
            FabDemoType.LOADING -> LoadingFabExample()
            FabDemoType.SUCCESS -> SuccessFabExample()
            FabDemoType.ERROR -> ErrorFabExample()
            FabDemoType.SMALL -> SmallFabExample()
            FabDemoType.LARGE -> LargeFabExample()
            FabDemoType.ADD -> AddFabExample()
            FabDemoType.EDIT -> EditFabExample()
            FabDemoType.DELETE -> DeleteFabExample()
            FabDemoType.SHARE -> ShareFabExample()
            FabDemoType.FAVORITE -> FavoriteFabExample()
            FabDemoType.MULTI_ACTION -> MultiActionFabExample()
            null -> {}
        }
    }
}

@Composable
private fun FabCatalogItem(
    demoType: FabDemoType,
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

private enum class FabDemoType(
    val title: String,
    val description: String,
) {
    REGULAR("Regular FAB", "KptFloatingActionButton with icon"),
    EXTENDED("Extended FAB", "KptExtendedFloatingActionButton with text and icon"),
    LOADING("Loading FAB", "KptLoadingFloatingActionButton with loading state"),
    SUCCESS("Success FAB", "KptSuccessFloatingActionButton with success state"),
    ERROR("Error FAB", "KptErrorFloatingActionButton with error state"),
    SMALL("Small FAB", "KptSmallFloatingActionButton with icon"),
    LARGE("Large FAB", "KptLargeFloatingActionButton with icon"),
    ADD("Add FAB", "KptAddFloatingActionButton for add actions"),
    EDIT("Edit FAB", "KptEditFloatingActionButton for edit actions"),
    DELETE("Delete FAB", "KptDeleteFloatingActionButton for delete actions"),
    SHARE("Share FAB", "KptShareFloatingActionButton for share actions"),
    FAVORITE("Favorite FAB", "KptFavoriteFloatingActionButton for favorite toggle"),
    MULTI_ACTION("Multi-Action FAB", "KptMultiActionFloatingActionButton with sub-actions"),
}

@Composable
private fun RegularFabExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptFloatingActionButton(
            onClick = {},
            icon = Icons.Default.Add,
        )
    }
}

@Composable
private fun ExtendedFabExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptExtendedFloatingActionButton(
            onClick = {},
            text = "Create",
            icon = Icons.Default.Add,
        )
    }
}

@Composable
private fun LoadingFabExample() {
    var loading by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptLoadingFloatingActionButton(
            onClick = { loading = !loading },
            loading = loading,
            icon = Icons.Default.Add,
        )
    }
}

@Composable
private fun SuccessFabExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSuccessFloatingActionButton(
            onClick = {},
        )
    }
}

@Composable
private fun ErrorFabExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptErrorFloatingActionButton(
            onClick = {},
        )
    }
}

@Composable
private fun SmallFabExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSmallFloatingActionButton(
            onClick = {},
            icon = Icons.Default.Add,
        )
    }
}

@Composable
private fun LargeFabExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptLargeFloatingActionButton(
            onClick = {},
            icon = Icons.Default.Add,
        )
    }
}

@Composable
private fun AddFabExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptAddFloatingActionButton(
            onClick = {},
        )
    }
}

@Composable
private fun EditFabExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptEditFloatingActionButton(
            onClick = {},
        )
    }
}

@Composable
private fun DeleteFabExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptDeleteFloatingActionButton(
            onClick = {},
        )
    }
}

@Composable
private fun ShareFabExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptShareFloatingActionButton(
            onClick = {},
        )
    }
}

@Composable
private fun FavoriteFabExample() {
    var isFavorite by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptFavoriteFloatingActionButton(
            onClick = { isFavorite = !isFavorite },
            isFavorite = isFavorite,
        )
    }
}

@Composable
private fun MultiActionFabExample() {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptMultiActionFloatingActionButton(
            expanded = expanded,
            onMainClick = { /* Main FAB action */ },
            onExpandToggle = { expanded = !expanded },
            actions = listOf(
                FabAction(Icons.Default.Add, "Add", {}),
                FabAction(Icons.Default.Edit, "Edit", {}),
                FabAction(Icons.Default.Delete, "Delete", {}),
                FabAction(Icons.Default.Share, "Share", {}),
            ),
            mainIcon = Icons.Default.Add,
        )
    }
}
