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
import template.core.base.designsystem.component.KptRadioGroup
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.RadioGroupConfiguration
import template.core.base.designsystem.component.RadioGroupOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptRadioGroupCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptRadioGroup Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<RadioGroupDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptRadioGroup Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = RadioGroupDemoType.entries.toList()) { demoType ->
                RadioGroupCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected radio group demo
        when (selectedDemo) {
            RadioGroupDemoType.BASIC -> BasicRadioGroupExample()
            RadioGroupDemoType.DESCRIPTION -> DescriptionRadioGroupExample()
            RadioGroupDemoType.DISABLED -> DisabledRadioGroupExample()
            RadioGroupDemoType.ARRANGEMENT -> ArrangementRadioGroupExample()
            null -> {}
        }
    }
}

@Composable
private fun RadioGroupCatalogItem(
    demoType: RadioGroupDemoType,
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

private enum class RadioGroupDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic Radio Group", "KptRadioGroup with simple options"),
    DESCRIPTION("Radio Group with Descriptions", "KptRadioGroup with option descriptions"),
    DISABLED("Radio Group with Disabled Option", "KptRadioGroup with a disabled option"),
    ARRANGEMENT("Custom Arrangement", "KptRadioGroup with custom vertical spacing"),
}

@Composable
private fun BasicRadioGroupExample() {
    val options = listOf(
        RadioGroupOption("a", "Option A"),
        RadioGroupOption("b", "Option B"),
        RadioGroupOption("c", "Option C"),
    )
    var selected by remember { mutableStateOf("a") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptRadioGroup(
            configuration = RadioGroupConfiguration(
                options = options,
                selectedOption = selected,
                onOptionSelected = { selected = it },
            ),
        )
    }
}

@Composable
private fun DescriptionRadioGroupExample() {
    val options = listOf(
        RadioGroupOption("a", "Option A", description = "First option"),
        RadioGroupOption("b", "Option B", description = "Second option"),
        RadioGroupOption("c", "Option C", description = "Third option"),
    )
    var selected by remember { mutableStateOf("a") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptRadioGroup(
            configuration = RadioGroupConfiguration(
                options = options,
                selectedOption = selected,
                onOptionSelected = { selected = it },
            ),
        )
    }
}

@Composable
private fun DisabledRadioGroupExample() {
    val options = listOf(
        RadioGroupOption("a", "Option A"),
        RadioGroupOption("b", "Option B", enabled = false),
        RadioGroupOption("c", "Option C"),
    )
    var selected by remember { mutableStateOf("a") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptRadioGroup(
            configuration = RadioGroupConfiguration(
                options = options,
                selectedOption = selected,
                onOptionSelected = { selected = it },
            ),
        )
    }
}

@Composable
private fun ArrangementRadioGroupExample() {
    val options = listOf(
        RadioGroupOption("a", "Option A"),
        RadioGroupOption("b", "Option B"),
        RadioGroupOption("c", "Option C"),
    )
    var selected by remember { mutableStateOf("a") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptRadioGroup(
            configuration = RadioGroupConfiguration(
                options = options,
                selectedOption = selected,
                onOptionSelected = { selected = it },
            ),
            arrangement = Arrangement.spacedBy(24.dp),
        )
    }
}
