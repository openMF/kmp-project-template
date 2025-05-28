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

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptSwitch
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.SwitchConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptSwitchCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptSwitch Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<SwitchDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptSwitch Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = SwitchDemoType.entries.toList()) { demoType ->
                SwitchCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected switch demo
        when (selectedDemo) {
            SwitchDemoType.BASIC -> BasicSwitchExample()
            SwitchDemoType.LABEL -> LabelSwitchExample()
            SwitchDemoType.LABEL_DESCRIPTION -> LabelDescriptionSwitchExample()
            SwitchDemoType.DISABLED -> DisabledSwitchExample()
            SwitchDemoType.CUSTOM_THUMB -> CustomThumbSwitchExample()
            null -> {}
        }
    }
}

@Composable
private fun SwitchCatalogItem(
    demoType: SwitchDemoType,
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

private enum class SwitchDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic Switch", "KptSwitch with no label or description"),
    LABEL("Switch with Label", "KptSwitch with a label"),
    LABEL_DESCRIPTION("Switch with Label & Description", "KptSwitch with label and description"),
    DISABLED("Disabled Switch", "KptSwitch in a disabled state"),
    CUSTOM_THUMB("Switch with Custom Thumb", "KptSwitch with a custom thumb content"),
}

@Composable
private fun BasicSwitchExample() {
    var checked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSwitch(
            configuration = SwitchConfiguration(
                checked = checked,
                onCheckedChange = { checked = it },
            ),
        )
    }
}

@Composable
private fun LabelSwitchExample() {
    var checked by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSwitch(
            configuration = SwitchConfiguration(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Enable notifications",
            ),
        )
    }
}

@Composable
private fun LabelDescriptionSwitchExample() {
    var checked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSwitch(
            configuration = SwitchConfiguration(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Enable dark mode",
                description = "Switch between light and dark themes",
            ),
        )
    }
}

@Composable
private fun DisabledSwitchExample() {
    var checked by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSwitch(
            configuration = SwitchConfiguration(
                checked = checked,
                onCheckedChange = { /* no-op */ },
                enabled = false,
                label = "Disabled switch",
            ),
        )
    }
}

@Composable
private fun CustomThumbSwitchExample() {
    var checked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSwitch(
            configuration = SwitchConfiguration(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Custom thumb",
                thumbContent = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (checked) Color.Green else Color.Red),
                    )
                },
            ),
        )
    }
}
