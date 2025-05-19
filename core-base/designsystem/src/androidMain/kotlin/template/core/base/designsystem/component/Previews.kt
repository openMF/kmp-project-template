/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("ModifierMissing")

package template.core.base.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import template.core.base.designsystem.adaptivelayout.AdaptiveNavigationSuiteScaffold
import template.core.base.designsystem.component.variant.BottomAppBarVariant
import template.core.base.designsystem.component.variant.ButtonVariant
import template.core.base.designsystem.component.variant.CardVariant
import template.core.base.designsystem.component.variant.ProgressIndicatorVariant
import template.core.base.designsystem.component.variant.TextFieldVariant
import template.core.base.designsystem.component.variant.TopAppBarVariant

@Composable
@Preview(showBackground = true)
fun CMPButtonPreview() {
    Column {
        for (variant in ButtonVariant.entries) {
            CMPButton(
                onClick = {},
                variant = variant,
            ) {
                Text(text = "CMP")
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun CMPCardPreview() {
    Column {
        for (variant in CardVariant.entries) {
            Spacer(modifier = Modifier.height(5.dp))
            CMPCard(
                variant = variant,
                content = {
                    Text(
                        text = "CMP",
                        modifier = Modifier.padding(16.dp),
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CMPTextFieldPreview() {
    Column {
        for (variant in TextFieldVariant.entries) {
            Spacer(modifier = Modifier.height(5.dp))
            CMPTextField(value = "TextField", onValueChange = {}, variant = variant)
        }
    }
}

@Preview
@Composable
fun CMPAlertDialogPreview() {
    CMPAlertDialog(
        onDismissRequest = {},
        confirmButton = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CMPBottomSheetPreview() {
    Column {
        Button(onClick = {}) {
            Text(text = "CMP")
        }
        CMPBottomSheet(
            onDismiss = {},
            modifier = Modifier.fillMaxHeight(),
        ) { hideSheet ->
            CMPButton(onClick = hideSheet) {
                Text(text = "CMP")
            }
        }
    }
}

@Preview
@Composable
fun CMPProgressIndicatorPreview() {
    var input by remember { mutableStateOf("") }
    Column {
        CMPTextField(
            value = input,
            onValueChange = { input = it },
            variant = TextFieldVariant.OUTLINED,
        )
        Spacer(modifier = Modifier.height(15.dp))
        for (variant in ProgressIndicatorVariant.entries) {
            Spacer(modifier = Modifier.height(5.dp))
            CMPProgressIndicator(variant = variant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CMPTopAppBarPreview() {
    Column {
        for (variant in TopAppBarVariant.entries) {
            CMPTopAppBar(
                title = { Text(text = "CMP") },
                variant = variant,
                navigationIcon = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(Icons.Filled.Check, contentDescription = "Localized description")
                    }
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Localized description",
                        )
                    }
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            Icons.Filled.Call,
                            contentDescription = "Localized description",
                        )
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CMPBottomAppBarPreview() {
    Column {
        for (variant in BottomAppBarVariant.entries) {
            CMPBottomAppBar(
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(Icons.Filled.Check, contentDescription = "Localized description")
                    }
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Localized description",
                        )
                    }
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            Icons.Filled.Call,
                            contentDescription = "Localized description",
                        )
                    }
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Localized description",
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { /* do something */ },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    ) {
                        Icon(Icons.Filled.Add, "Localized description")
                    }
                },
                variant = variant,
            ) {
                Text(
                    text = "Example of a custom bottom app bar.",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CMPScaffoldPreview() {
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CMPScaffold(
        topBar = {
            CMPTopAppBar(
                title = { Text(text = "CMP") },
            )
        },
        rememberPullToRefreshStateData = rememberPullToRefreshStateData(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    delay(4000)
                    isRefreshing = false
                }
            },
        ),
    ) {
        LazyColumn {
            items(100) {
                Text(
                    text = "Item $it",
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxSize(),
                )
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun AdaptiveNavigationSuiteScaffoldPreview() {
    AdaptiveNavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = it.contentDescription,
                        )
                    },
                    label = {
                        Text(text = it.label)
                    },
                    onClick = {},
                    selected = false,
                    modifier = Modifier,
                )
            }
        },
    ) {
        Text(
            text = "Home",
            modifier = Modifier.fillMaxSize(),
            textAlign = TextAlign.Center,
        )
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String,
) {
    HOME("Home", Icons.Default.Home, "home"),
    FAVORITES("Favorites", Icons.Default.Favorite, "favorites"),
    SHOPPING("Shopping", Icons.Default.ShoppingCart, "shopping"),
    PROFILE("Profile", Icons.Default.AccountBox, "profile"),
}
