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
import androidx.compose.material3.RadioButtonDefaults
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
import template.core.base.designsystem.component.KptRadioButton
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.RadioButtonConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptRadioButtonCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptRadioButton Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<RadioButtonDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptRadioButton Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = RadioButtonDemoType.entries.toList()) { demoType ->
                RadioButtonCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected radio button demo
        when (selectedDemo) {
            RadioButtonDemoType.BASIC -> BasicRadioButtonExample()
            RadioButtonDemoType.LABEL -> LabelRadioButtonExample()
            RadioButtonDemoType.LABEL_DESCRIPTION -> LabelDescriptionRadioButtonExample()
            RadioButtonDemoType.DISABLED -> DisabledRadioButtonExample()
            RadioButtonDemoType.GROUP -> RadioButtonGroupExample()
            null -> {}
        }
    }
}

@Composable
private fun RadioButtonCatalogItem(
    demoType: RadioButtonDemoType,
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

private enum class RadioButtonDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic Radio Button", "KptRadioButton with no label"),
    LABEL("Radio Button with Label", "KptRadioButton with a label"),
    LABEL_DESCRIPTION(
        "Radio Button with Label & Description",
        "KptRadioButton with a label and description",
    ),
    DISABLED("Disabled Radio Button", "KptRadioButton in a disabled state"),
    GROUP("Radio Button Group", "A group of KptRadioButtons for selection"),
}

@Composable
private fun BasicRadioButtonExample() {
    var selected by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptRadioButton(
            configuration = RadioButtonConfiguration(
                selected = selected,
                onClick = { selected = !selected },
            ),
        )
    }
}

@Composable
private fun LabelRadioButtonExample() {
    var selected by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptRadioButton(
            configuration = RadioButtonConfiguration(
                selected = selected,
                onClick = { selected = !selected },
                label = "Option A",
            ),
        )
    }
}

@Composable
private fun LabelDescriptionRadioButtonExample() {
    var selected by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptRadioButton(
            configuration = RadioButtonConfiguration(
                selected = selected,
                onClick = { selected = !selected },
                label = "Subscribe",
                description = "Receive updates and newsletters",
            ),
        )
    }
}

@Composable
private fun DisabledRadioButtonExample() {
    var selected by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptRadioButton(
            configuration = RadioButtonConfiguration(
                selected = selected,
                onClick = null,
                label = "Disabled Option",
                enabled = false,
            ),
        )
    }
}

@Composable
private fun RadioButtonGroupExample() {
    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedIndex by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        options.forEachIndexed { index, label ->
            KptRadioButton(
                configuration = RadioButtonConfiguration(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    label = label,
                    colors = if (selectedIndex == index) {
                        RadioButtonDefaults.colors(
                            selectedColor = Color(
                                0xFF6200EE,
                            ),
                        )
                    } else {
                        null
                    },
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
