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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import template.core.base.designsystem.component.DividerOrientation
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptDivider
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptDividerCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptDivider Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<DividerDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptDivider Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = DividerDemoType.entries.toList()) { demoType ->
                DividerCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected divider demo
        when (selectedDemo) {
            DividerDemoType.HORIZONTAL -> HorizontalDividerExample()
            DividerDemoType.VERTICAL -> VerticalDividerExample()
            DividerDemoType.THICKNESS -> CustomThicknessDividerExample()
            DividerDemoType.COLOR -> CustomColorDividerExample()
            null -> {}
        }
    }
}

@Composable
private fun DividerCatalogItem(
    demoType: DividerDemoType,
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

private enum class DividerDemoType(
    val title: String,
    val description: String,
) {
    HORIZONTAL(
        title = "Horizontal Divider",
        description = "KptDivider with horizontal orientation",
    ),
    VERTICAL(
        title = "Vertical Divider",
        description = "KptDivider with vertical orientation",
    ),
    THICKNESS(
        title = "Custom Thickness Divider",
        description = "KptDivider with custom thickness",
    ),
    COLOR(
        title = "Custom Color Divider",
        description = "KptDivider with custom color",
    ),
}

@Composable
private fun HorizontalDividerExample() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Above Divider")
        KptDivider(
            modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
            orientation = DividerOrientation.Horizontal,
        )
        Text("Below Divider")
    }
}

@Composable
private fun VerticalDividerExample() {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text("Left")
        KptDivider(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxHeight().width(2.dp),
            orientation = DividerOrientation.Vertical,
        )
        Text("Right")
    }
}

@Composable
private fun CustomThicknessDividerExample() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Thin Divider")
        KptDivider(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            thickness = 1.dp,
        )
        Text("Thick Divider")
        KptDivider(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            thickness = 8.dp,
        )
    }
}

@Composable
private fun CustomColorDividerExample() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Default Color Divider")
        KptDivider(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        )
        Text("Red Color Divider")
        KptDivider(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            color = Color.Red,
        )
        Text("Blue Color Divider")
        KptDivider(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            color = Color.Blue,
        )
    }
}
