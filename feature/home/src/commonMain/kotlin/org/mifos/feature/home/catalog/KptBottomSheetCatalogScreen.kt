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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.component.KptBottomSheet
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptDivider
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptBottomSheetCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "Bottom Sheet Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedSheet by remember { mutableStateOf<BottomSheetType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "Bottom Sheet Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = BottomSheetType.entries.toList()) { sheetType ->
                BottomSheetCatalogItem(
                    sheetType = sheetType,
                    onClick = { selectedSheet = sheetType },
                )
            }
        }

        // Show the selected bottom sheet
        when (selectedSheet) {
            BottomSheetType.BASIC -> BasicBottomSheetExample { selectedSheet = null }
            BottomSheetType.WITH_ACTIONS -> WithActionsBottomSheetExample { selectedSheet = null }
            BottomSheetType.LIST -> ListBottomSheetExample { selectedSheet = null }
            BottomSheetType.FORM -> FormBottomSheetExample { selectedSheet = null }
            BottomSheetType.MEDIA -> MediaBottomSheetExample { selectedSheet = null }
            BottomSheetType.FULL_SCREEN -> FullScreenBottomSheetExample { selectedSheet = null }
            BottomSheetType.CUSTOM -> CustomBottomSheetExample { selectedSheet = null }
            null -> {} // No bottom sheet shown
        }
    }
}

@Composable
private fun BottomSheetCatalogItem(
    sheetType: BottomSheetType,
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
                    text = sheetType.title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sheetType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class BottomSheetType(
    val title: String,
    val description: String,
) {
    BASIC(
        title = "Basic Bottom Sheet",
        description = "Simple bottom sheet with title and content",
    ),
    WITH_ACTIONS(
        title = "With Actions",
        description = "Bottom sheet with action buttons",
    ),
    LIST(
        title = "List Content",
        description = "Bottom sheet showing a scrollable list",
    ),
    FORM(
        title = "Form Input",
        description = "Bottom sheet with form fields",
    ),
    MEDIA(
        title = "Media Content",
        description = "Bottom sheet displaying media (images/video)",
    ),
    FULL_SCREEN(
        title = "Full Screen",
        description = "Bottom sheet that expands to full screen",
    ),
    CUSTOM(
        title = "Custom Content",
        description = "Fully customizable bottom sheet",
    ),
}

// Example implementations

@Composable
private fun BasicBottomSheetExample(onDismiss: () -> Unit) {
    KptBottomSheet(
        onDismiss = onDismiss,
        sheetContent = { hideSheet ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Basic Bottom Sheet",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("This is a simple bottom sheet example")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { hideSheet() }) {
                    Text("Dismiss")
                }
            }
        },
    )
}

@Composable
private fun WithActionsBottomSheetExample(onDismiss: () -> Unit) {
    KptBottomSheet(
        onDismiss = onDismiss,
        sheetContent = { hideSheet ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = "Action Sheet",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                OutlinedButton(
                    onClick = { /* Handle action 1 */ },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Action 1")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { /* Handle action 2 */ },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Action 2")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { hideSheet() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel")
                }
            }
        },
    )
}

@Composable
private fun ListBottomSheetExample(onDismiss: () -> Unit) {
    val items = remember { List(20) { "Item ${it + 1}" } }

    KptBottomSheet(
        onDismiss = onDismiss,
        sheetContent = { hideSheet ->
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "List Content",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                ) {
                    items(items) { item ->
                        ListItem(
                            headlineContent = { Text(item) },
                            modifier = Modifier.clickable {
                                // Handle item selection
                                hideSheet()
                            },
                        )
                        KptDivider()
                    }
                }

                Button(
                    onClick = { hideSheet() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text("Close")
                }
            }
        },
    )
}

@Composable
private fun FormBottomSheetExample(onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }

    KptBottomSheet(
        onDismiss = onDismiss,
        sheetContent = { hideSheet ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Form Input",
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Enter text") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checked = it },
                    )
                    Text(
                        text = "I agree to terms",
                        modifier = Modifier.clickable { checked = !checked },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { hideSheet() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { hideSheet() },
                        modifier = Modifier.weight(1f),
                        enabled = text.isNotBlank() && checked,
                    ) {
                        Text("Submit")
                    }
                }
            }
        },
    )
}

@Composable
private fun MediaBottomSheetExample(onDismiss: () -> Unit) {
    KptBottomSheet(
        onDismiss = onDismiss,
        sheetContent = { hideSheet ->
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Media Content",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This bottom sheet shows media content with description",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { hideSheet() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Close")
                    }
                }
            }
        },
    )
}

@Composable
private fun FullScreenBottomSheetExample(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    KptBottomSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
        sheetContent = { hideSheet ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Full Screen Sheet",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    IconButton(onClick = { hideSheet() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Full screen content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("This bottom sheet expands to full screen")
                }
            }
        },
    )
}

@Composable
private fun CustomBottomSheetExample(onDismiss: () -> Unit) {
    KptBottomSheet(
        onDismiss = onDismiss,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(width = 64.dp, height = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(2.dp),
                    ),
            )
        },
        sheetContent = { hideSheet ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Custom Styled Sheet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This sheet has custom styling including shape, colors, and drag handle",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { hideSheet() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Text("Got it!")
                }
            }
        },
    )
}
