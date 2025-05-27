package org.mifos.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mifos.core.designsystem.icon.AppIcons
import template.core.base.designsystem.component.BasicDialogState
import template.core.base.designsystem.component.KptAlertDialog
import template.core.base.designsystem.component.KptBasicDialog
import template.core.base.designsystem.component.KptBottomAppBar
import template.core.base.designsystem.component.KptBottomSheet
import template.core.base.designsystem.component.KptButton
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptFloatingActionButton
import template.core.base.designsystem.component.KptProgressIndicator
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTextField
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.variant.AlertDialogVariant
import template.core.base.designsystem.component.variant.BottomAppBarVariant
import template.core.base.designsystem.component.variant.ButtonVariant
import template.core.base.designsystem.component.variant.CardVariant
import template.core.base.designsystem.component.variant.FloatingActionButtonVariant
import template.core.base.designsystem.component.variant.ProgressIndicatorVariant
import template.core.base.designsystem.component.variant.TextFieldVariant
import template.core.base.designsystem.component.variant.TopAppBarVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DesignSystemCatalogScreen(
    navigateBack: () -> Unit,
) {
    var showDialog by remember { mutableIntStateOf(0) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf("") }

    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = { Text("Design System Catalog") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(AppIcons.Back, contentDescription = null)
                    }
                },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .padding(it),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item("KptButton") {
                ComponentTitle("KptButton")

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    items(ButtonVariant.entries.size) { index ->
                        val variant = ButtonVariant.entries[index]
                        KptButton(
                            onClick = {},
                            variant = variant,
                            modifier = Modifier.padding(4.dp),
                        ) {
                            Text(variant.name)
                        }
                    }
                }
            }

            item("KptCard") {
                ComponentTitle("KptCard")

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    items(CardVariant.entries.size) { index ->
                        val variant = CardVariant.entries[index]
                        KptCard(
                            variant = variant,
                            modifier = Modifier.padding(4.dp),
                        ) {
                            Text(variant.name, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }

            item("KptTextField") {
                ComponentTitle("KptTextField")

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    items(TextFieldVariant.entries.size) { index ->
                        val variant = TextFieldVariant.entries[index]
                        KptTextField(
                            value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            variant = variant,
                            modifier = Modifier.width(180.dp),
                        )
                    }
                }
            }

            item("KptTopAppBar") {
                ComponentTitle("KptTopAppBar")
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    TopAppBarVariant.entries.forEach { variant ->
                        KptTopAppBar(
                            title = { Text("TopAppBar: ${variant.name}") },
                            variant = variant,
                            navigationIcon = {
                                IconButton(onClick = {}) {
                                    Icon(Icons.Default.Home, contentDescription = null)
                                }
                            },
                            actions = {
                                IconButton(onClick = {}) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                    )
                                }
                                IconButton(onClick = {}) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                    )
                                }
                            },
                        )
                    }
                }
            }

            item("KptAlertDialog") {
                ComponentTitle("KptAlertDialog")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(AlertDialogVariant.entries.size) { index ->
                        val variant = AlertDialogVariant.entries[index]
                        KptButton(
                            onClick = {
                                showDialog = variant.ordinal + 1
                            },
                            modifier = Modifier.padding(4.dp),
                        ) {
                            Text("Show ${variant.name}")
                        }
                    }
                }

                AlertDialogVariant.entries.forEach { variant ->
                    if (showDialog == variant.ordinal + 1) {
                        if (variant == AlertDialogVariant.CUSTOM) {
                            KptAlertDialog(
                                onDismissRequest = { showDialog = 0 },
                                confirmButton = {
                                    KptButton(
                                        onClick = { showDialog = 0 },
                                        variant = ButtonVariant.TEXT,
                                    ) { Text("OK") }
                                },
                                dismissButton = {
                                    KptButton(
                                        onClick = { showDialog = 0 },
                                        variant = ButtonVariant.TEXT,
                                    ) { Text("Cancel") }
                                },
                                title = { Text("KptAlertDialog Title") },
                                text = { Text("This is a KptAlertDialog (CUSTOM) sample.") },
                                variant = variant,
                            )
                        } else {
                            KptAlertDialog(
                                onDismissRequest = { showDialog = 0 },
                                confirmButton = {

                                },
                                variant = variant,
                                basicContent = {
                                    Surface {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                        ) {
                                            Text("This is a KptAlertDialog (BASIC) sample.")
                                            Text("You can customize the content as needed.")
                                            Text("Click OK to dismiss.")

                                            Spacer(modifier = Modifier.height(12.dp))

                                            KptButton(
                                                onClick = { showDialog = 0 },
                                                variant = ButtonVariant.TEXT,
                                            ) { Text("OK") }
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }

            item("KptBasicAlertDialog") {
                var showBasicDialog by remember {
                    mutableStateOf<BasicDialogState>(BasicDialogState.Hidden)
                }

                ComponentTitle("KptBasicAlertDialog")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        KptButton(
                            onClick = {
                                showBasicDialog = BasicDialogState.Shown(
                                    title = "Basic AlertDialog Title",
                                    message = "This is a KptBasicAlertDialog sample",
                                )
                            },
                            modifier = Modifier.padding(4.dp),
                        ) {
                            Text("Show Basic Dialog")
                        }
                    }
                }

                KptBasicDialog(
                    visibilityState = showBasicDialog,
                    onDismissRequest = {
                        showBasicDialog = BasicDialogState.Hidden
                    },
                )
            }

            item("KptFloatingActionButton") {
                ComponentTitle("KptFloatingActionButton")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    item {
                        KptFloatingActionButton(
                            variant = FloatingActionButtonVariant.Default(
                                icon = Icons.Default.Add,
                            ),
                            onClick = {},
                        )
                    }
                    item {
                        KptFloatingActionButton(
                            variant = FloatingActionButtonVariant.Expanded(
                                icon = Icons.Default.Add,
                                text = "Expanded",
                            ),
                            onClick = {},
                        )
                    }

                    item {
                        KptFloatingActionButton(
                            variant = FloatingActionButtonVariant.Small(
                                icon = Icons.Default.Add,
                            ),
                            onClick = {},
                        )
                    }
                }
            }

            item("KptBottomAppBar") {
                ComponentTitle("KptBottomAppBar")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BottomAppBarVariant.entries.forEach { variant ->
                        if (variant == BottomAppBarVariant.BOTTOM_WITH_ACTIONS) {
                            KptBottomAppBar(
                                actions = {
                                    IconButton(onClick = {}) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                        )
                                    }
                                    IconButton(onClick = {}) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                        )
                                    }
                                },
                                floatingActionButton = {
                                    KptFloatingActionButton(
                                        variant = FloatingActionButtonVariant.Default(
                                            icon = Icons.Default.Add,
                                        ),
                                        onClick = {},
                                    )
                                },
                                variant = variant,
                            ) {
                                Text("BottomAppBar: ${variant.name}")
                            }
                        } else {
                            KptBottomAppBar(
                                actions = {},
                                variant = variant,
                                customContent = {
                                    Text(
                                        "Custom Content in BottomAppBar",
                                        modifier = Modifier.padding(16.dp),
                                    )
                                },
                            )
                        }
                    }
                }
            }

            item("KptProgressIndicator") {
                ComponentTitle("KptProgressIndicator")

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProgressIndicatorVariant.entries.forEach { variant ->
                        Column {
                            Text(variant.name, modifier = Modifier.padding(bottom = 4.dp))
                            if (variant.name.startsWith("DETERMINATE")) {
                                KptProgressIndicator(variant = variant, progress = 0.6f)
                            } else {
                                KptProgressIndicator(variant = variant)
                            }
                        }
                    }
                }
            }

            item("KptBottomSheet") {
                ComponentTitle("KptBottomSheet")

                KptButton(
                    onClick = {
                        showBottomSheet = true
                    },
                    content = {
                        Text("Show KptBottomSheet")
                    },
                )

                if (showBottomSheet) {
                    KptBottomSheet(
                        onDismiss = { showBottomSheet = false },
                    ) { hideSheet ->
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("KptBottomSheet content")
                            KptButton(onClick = hideSheet) { Text("Close") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview
@Composable
private fun PreviewDesignSystemShowcaseScreen() {
    DesignSystemCatalogScreen(
        navigateBack = {},
    )
}