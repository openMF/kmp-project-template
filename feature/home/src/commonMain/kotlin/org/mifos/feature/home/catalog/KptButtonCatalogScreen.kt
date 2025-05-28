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
import template.core.base.designsystem.component.KptAddButton
import template.core.base.designsystem.component.KptButton
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptDeleteButton
import template.core.base.designsystem.component.KptElevatedButton
import template.core.base.designsystem.component.KptFilledButton
import template.core.base.designsystem.component.KptFullWidthButton
import template.core.base.designsystem.component.KptLargeButton
import template.core.base.designsystem.component.KptLoadingButton
import template.core.base.designsystem.component.KptOutlinedButton
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptSmallButton
import template.core.base.designsystem.component.KptTextButton
import template.core.base.designsystem.component.KptTonalButton
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.core.IconPosition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptButtonCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptButton Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedButton by remember { mutableStateOf<ButtonType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptButton Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = ButtonType.entries.toList()) { buttonType ->
                ButtonCatalogItem(
                    buttonType = buttonType,
                    onClick = { selectedButton = buttonType },
                )
            }
        }

        // Show the selected button demo
        when (selectedButton) {
            ButtonType.BASIC -> BasicButtonExample()
            ButtonType.ICON -> IconButtonExample()
            ButtonType.LOADING -> LoadingButtonExample()
            ButtonType.FULL_WIDTH -> FullWidthButtonExample()
            ButtonType.FILLED -> FilledButtonExample()
            ButtonType.OUTLINED -> OutlinedButtonExample()
            ButtonType.TEXT -> TextButtonExample()
            ButtonType.TONAL -> TonalButtonExample()
            ButtonType.ELEVATED -> ElevatedButtonExample()
            ButtonType.SMALL -> SmallButtonExample()
            ButtonType.LARGE -> LargeButtonExample()
            ButtonType.DELETE -> DeleteButtonExample()
            ButtonType.ADD -> AddButtonExample()
            null -> {}
        }
    }
}

@Composable
private fun ButtonCatalogItem(
    buttonType: ButtonType,
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
                    text = buttonType.title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buttonType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class ButtonType(
    val title: String,
    val description: String,
) {
    BASIC(
        title = "Basic Button",
        description = "Simple KptButton with text label",
    ),
    ICON(
        title = "Button with Icon",
        description = "KptButton with an icon and text",
    ),
    LOADING(
        title = "Loading Button",
        description = "KptButton with loading indicator",
    ),
    FULL_WIDTH(
        title = "Full Width Button",
        description = "KptButton that fills the width of its container",
    ),
    FILLED(
        title = "Filled Button",
        description = "KptFilledButton variant",
    ),
    OUTLINED(
        title = "Outlined Button",
        description = "KptOutlinedButton variant",
    ),
    TEXT(
        title = "Text Button",
        description = "KptTextButton variant",
    ),
    TONAL(
        title = "Tonal Button",
        description = "KptTonalButton variant",
    ),
    ELEVATED(
        title = "Elevated Button",
        description = "KptElevatedButton variant",
    ),
    SMALL(
        title = "Small Button",
        description = "KptSmallButton variant",
    ),
    LARGE(
        title = "Large Button",
        description = "KptLargeButton variant",
    ),
    DELETE(
        title = "Delete Button",
        description = "KptDeleteButton with confirmation dialog",
    ),
    ADD(
        title = "Add Button",
        description = "KptAddButton with icon only and text+icon",
    ),
}

@Composable
private fun BasicButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptButton(text = "Submit", onClick = {})
    }
}

@Composable
private fun IconButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptButton(text = "Add", icon = Icons.Default.Add, onClick = {}, iconPosition = IconPosition.Start)
    }
}

@Composable
private fun LoadingButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptLoadingButton(text = "Save", loading = true, onClick = {})
    }
}

@Composable
private fun FullWidthButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptFullWidthButton(text = "Continue", onClick = {})
    }
}

@Composable
private fun FilledButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptFilledButton(text = "Primary", onClick = {})
    }
}

@Composable
private fun OutlinedButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptOutlinedButton(text = "Secondary", onClick = {})
    }
}

@Composable
private fun TextButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptTextButton(text = "Text", onClick = {})
    }
}

@Composable
private fun TonalButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptTonalButton(text = "Tonal", onClick = {})
    }
}

@Composable
private fun ElevatedButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptElevatedButton(text = "Elevated", onClick = {})
    }
}

@Composable
private fun SmallButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptSmallButton(text = "Small", onClick = {})
    }
}

@Composable
private fun LargeButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptLargeButton(text = "Large", onClick = {})
    }
}

@Composable
private fun DeleteButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptDeleteButton(onClick = {}, confirmationRequired = true)
    }
}

@Composable
private fun AddButtonExample() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KptAddButton(onClick = {}, iconOnly = true)
        Spacer(modifier = Modifier.height(16.dp))
        KptAddButton(onClick = {}, iconOnly = false)
    }
}
