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

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import template.core.base.designsystem.adaptivelayout.AdaptiveListDetailPaneScaffold
import template.core.base.designsystem.adaptivelayout.AdaptiveNavigableListDetailPaneScaffold
import template.core.base.designsystem.adaptivelayout.AdaptiveNavigableSupportingPaneScaffold
import template.core.base.designsystem.adaptivelayout.AdaptiveNavigationSuiteScaffold
import template.core.base.designsystem.adaptivelayout.PaneScaffoldItem
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
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

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
                    onClick = { currentDestination = it },
                    selected = false,
                    modifier = Modifier,
                )
            }
        },
    ) {
        // Destination content.
        when (currentDestination) {
            AppDestinations.HOME -> Text(
                "Home",
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
            )

            AppDestinations.FAVORITES -> Text(
                "Favorites",
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
            )

            AppDestinations.SHOPPING -> Text(
                "Shopping",
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
            )

            AppDestinations.PROFILE -> Text(
                "Profile",
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
            )
        }
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

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3AdaptiveApi::class)
@PreviewScreenSizes
@Composable
fun AdaptiveNavigableListDetailsScaffoldPreview() {
    // Create some simple sample data
    val loremIpsum = """
        |Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Dui nunc mattis enim ut tellus elementum sagittis. Nunc sed augue lacus viverra vitae. Sit amet dictum sit amet justo donec. Fringilla urna porttitor rhoncus dolor purus non enim praesent elementum. Dictum non consectetur a erat nam at lectus urna. Tellus mauris a diam maecenas sed enim ut sem viverra. Commodo ullamcorper a lacus vestibulum sed arcu non. Lorem mollis aliquam ut porttitor leo a diam sollicitudin tempor. Pellentesque habitant morbi tristique senectus et netus et malesuada. Vitae suscipit tellus mauris a diam maecenas sed. Neque ornare aenean euismod elementum nisi quis. Quam vulputate dignissim suspendisse in est ante in nibh mauris. Tellus in metus vulputate eu scelerisque felis imperdiet proin fermentum. Orci ac auctor augue mauris augue neque gravida.
        |
        |Tempus quam pellentesque nec nam aliquam. Praesent semper feugiat nibh sed. Adipiscing elit duis tristique sollicitudin nibh sit. Netus et malesuada fames ac turpis egestas sed tempus urna. Quis varius quam quisque id diam vel quam. Urna duis convallis convallis tellus id interdum velit laoreet. Id eu nisl nunc mi ipsum. Fermentum dui faucibus in ornare. Nunc lobortis mattis aliquam faucibus. Vulputate mi sit amet mauris commodo quis. Porta nibh venenatis cras sed. Vitae tortor condimentum lacinia quis vel eros donec. Eu non diam phasellus vestibulum.
    """.trimMargin()

    data class DefinedWord(
        override val id: Int,
        val word: String,
        val icon: ImageVector,
        val definition: String = loremIpsum,
    ) : PaneScaffoldItem<Int>

    val sampleWords = listOf(
        DefinedWord(1, "Apple", Icons.Filled.Call),
        DefinedWord(2, "Banana", Icons.Filled.Home),
    )

    AdaptiveNavigableListDetailPaneScaffold(
        items = sampleWords,
        listPaneItem = { word, isListAndDetailVisible, isListVisible, sharedTransitionScope, animatedVisibilityScope ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                val imageModifier = Modifier.padding(horizontal = 8.dp)
                if (!isListAndDetailVisible && isListVisible) {
                    with(sharedTransitionScope) {
                        val state = rememberSharedContentState(key = word.word)
                        imageModifier.then(
                            Modifier.sharedElement(
                                sharedContentState = state,
                                animatedVisibilityScope = animatedVisibilityScope,
                            ),
                        )
                    }
                }

                Image(
                    imageVector = word.icon,
                    contentDescription = word.word,
                    modifier = imageModifier,
                )
                Text(
                    text = word.word,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                )
            }
        },
        detailPaneContent = {
                definedWord, isListAndDetailVisible, isDetailVisible,
                sharedTransitionScope, animatedVisibilityScope,
            ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
            ) {
                val imageModifier = Modifier
                    .padding(horizontal = 8.dp)
                    .then(
                        if (!isListAndDetailVisible && isDetailVisible) {
                            with(sharedTransitionScope) {
                                val state = rememberSharedContentState(key = definedWord.word)
                                Modifier.sharedElement(
                                    state,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                )
                            }
                        } else {
                            Modifier
                        },
                    )

                Image(
                    imageVector = definedWord.icon,
                    contentDescription = definedWord.word,
                    modifier = imageModifier,
                )
                Text(
                    text = definedWord.word,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = definedWord.definition,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@PreviewScreenSizes
@Composable
fun AdaptiveNavigableSupportingPaneScaffoldPreview() {
    // Create some simple sample data
    val data = mapOf(
        "android" to listOf("kotlin", "java", "flutter"),
        "kotlin" to listOf("backend", "android", "desktop"),
        "desktop" to listOf("kotlin", "java", "flutter"),
        "backend" to listOf("kotlin", "java"),
        "java" to listOf("backend", "android", "desktop"),
        "flutter" to listOf("android", "desktop"),
    )

    var selectedTopic: String by rememberSaveable { mutableStateOf(data.keys.first()) }

    AdaptiveNavigableSupportingPaneScaffold(
        supportingPaneContent = { navigateBack ->
            AnimatedPane(
                modifier = Modifier.padding(all = 16.dp),
            ) {
                Column {
                    Text(
                        text = "Related content label",
                        modifier = Modifier.padding(vertical = 16.dp),
                        style = MaterialTheme.typography.titleLarge,
                    )

                    LazyColumn {
                        items(
                            data.getValue(selectedTopic),
                            key = { it },
                        ) { relatedTopic ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(all = 4.dp)
                                    .clickable {
                                        selectedTopic = relatedTopic
                                        navigateBack()
                                    },
                            ) {
                                Text(
                                    text = relatedTopic,
                                    modifier = Modifier,
                                )
                            }
                        }
                    }
                }
            }
        },
        mainPaneContent = { navigateToSupportingPane ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(
                    "Content Label",
                    style = MaterialTheme.typography.titleLarge,
                )

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(all = 8.dp)
                        .clickable {
                            navigateToSupportingPane()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = selectedTopic,
                        modifier = Modifier
                            .padding(16.dp),
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(showBackground = true)
@Composable
private fun AdaptiveListDetailPaneScaffoldPreview() {
    val items = listOf("Apple", "Banana", "Cherry", "Date", "Elderberry")

    AdaptiveListDetailPaneScaffold(
        mainPaneContent = { navigateToDetail ->
            LazyColumn {
                itemsIndexed(items) { index, item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navigateToDetail() }
                            .padding(16.dp),
                    )
                }
            }
        },
        detailPaneContent = { navigateBack ->
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Detail View",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { navigateBack() }) {
                    Text("Go Back")
                }
            }
        },
    )
}
