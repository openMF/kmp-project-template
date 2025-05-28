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
import androidx.compose.material3.CheckboxDefaults
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
import template.core.base.designsystem.component.CheckboxConfiguration
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptCheckbox
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptCheckboxCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptCheckbox Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedCheckbox by remember { mutableStateOf<CheckboxType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptCheckbox Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = CheckboxType.entries.toList()) { checkboxType ->
                CheckboxCatalogItem(
                    checkboxType = checkboxType,
                    onClick = { selectedCheckbox = checkboxType },
                )
            }
        }

        // Show the selected checkbox demo
        when (selectedCheckbox) {
            CheckboxType.BASIC -> BasicCheckboxExample()
            CheckboxType.LABEL -> LabelCheckboxExample()
            CheckboxType.LABEL_DESCRIPTION -> LabelDescriptionCheckboxExample()
            CheckboxType.DISABLED -> DisabledCheckboxExample()
            CheckboxType.CUSTOM_COLOR -> CustomColorCheckboxExample()
            null -> {}
        }
    }
}

@Composable
private fun CheckboxCatalogItem(
    checkboxType: CheckboxType,
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
                    text = checkboxType.title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = checkboxType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class CheckboxType(
    val title: String,
    val description: String,
) {
    BASIC(
        title = "Basic Checkbox",
        description = "Simple KptCheckbox with no label",
    ),
    LABEL(
        title = "Checkbox with Label",
        description = "KptCheckbox with a label",
    ),
    LABEL_DESCRIPTION(
        title = "Checkbox with Label & Description",
        description = "KptCheckbox with a label and description",
    ),
    DISABLED(
        title = "Disabled Checkbox",
        description = "KptCheckbox in a disabled state",
    ),
    CUSTOM_COLOR(
        title = "Custom Color Checkbox",
        description = "KptCheckbox with custom colors",
    ),
}

@Composable
private fun BasicCheckboxExample() {
    var checked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptCheckbox(
            configuration = CheckboxConfiguration(
                checked = checked,
                onCheckedChange = { checked = it },
            ),
        )
    }
}

@Composable
private fun LabelCheckboxExample() {
    var checked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptCheckbox(
            configuration = CheckboxConfiguration(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Accept Terms",
            ),
        )
    }
}

@Composable
private fun LabelDescriptionCheckboxExample() {
    var checked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptCheckbox(
            configuration = CheckboxConfiguration(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Subscribe",
                description = "Receive updates and newsletters",
            ),
        )
    }
}

@Composable
private fun DisabledCheckboxExample() {
    var checked by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptCheckbox(
            configuration = CheckboxConfiguration(
                checked = checked,
                onCheckedChange = null,
                label = "Disabled Option",
                enabled = false,
            ),
        )
    }
}

@Composable
private fun CustomColorCheckboxExample() {
    var checked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptCheckbox(
            configuration = CheckboxConfiguration(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Custom Color",
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF6200EE),
                    uncheckedColor = Color(0xFFBDBDBD),
                    checkmarkColor = Color.White,
                ),
            ),
        )
    }
}
