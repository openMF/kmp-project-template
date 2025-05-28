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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.component.KptBottomAppBar
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptChatBottomAppBar
import template.core.base.designsystem.component.KptEcommerceBottomAppBar
import template.core.base.designsystem.component.KptMediaPlayerBottomAppBar
import template.core.base.designsystem.component.KptNavigationBottomAppBar
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptSocialBottomAppBar
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.core.BottomAppBarVariant
import template.core.base.designsystem.core.KptBottomAppBarConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptBottomAppBarCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "Bottom App Bar Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedBottomBar by remember { mutableStateOf<BottomBarType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "Bottom App Bar Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = BottomBarType.entries.toList()) { bottomBarType ->
                BottomBarCatalogItem(
                    bottomBarType = bottomBarType,
                    onClick = { selectedBottomBar = bottomBarType },
                )
            }
        }

        // Show the selected bottom app bar
        when (selectedBottomBar) {
            BottomBarType.BASIC -> BasicBottomAppBarExample()
            BottomBarType.WITH_FAB -> WithFabBottomAppBarExample()
            BottomBarType.WITH_ACTIONS -> WithActionsBottomAppBarExample()
            BottomBarType.NAVIGATION -> NavigationBottomAppBarExample()
            BottomBarType.SOCIAL -> SocialBottomAppBarExample()
            BottomBarType.MEDIA_PLAYER -> MediaPlayerBottomAppBarExample()
            BottomBarType.ECOMMERCE -> EcommerceBottomAppBarExample()
            BottomBarType.CHAT -> ChatBottomAppBarExample()
            BottomBarType.CUSTOM -> CustomBottomAppBarExample()
            null -> {} // No bottom app bar shown
        }
    }
}

@Composable
private fun BottomBarCatalogItem(
    bottomBarType: BottomBarType,
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
                    text = bottomBarType.title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = bottomBarType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class BottomBarType(
    val title: String,
    val description: String,
) {
    BASIC(
        title = "Basic Bottom App Bar",
        description = "Simple bottom app bar with no actions or FAB",
    ),
    WITH_FAB(
        title = "With Floating Action Button",
        description = "Bottom app bar with a floating action button",
    ),
    WITH_ACTIONS(
        title = "With Actions",
        description = "Bottom app bar with action icons and optional FAB",
    ),
    NAVIGATION(
        title = "Navigation Bar",
        description = "Bottom navigation bar with labeled icons",
    ),
    SOCIAL(
        title = "Social Actions",
        description = "Bottom app bar with social media actions (like, comment, share)",
    ),
    MEDIA_PLAYER(
        title = "Media Player",
        description = "Bottom app bar with media controls (play, pause, skip)",
    ),
    ECOMMERCE(
        title = "E-commerce",
        description = "Bottom app bar with shopping cart and favorite actions",
    ),
    CHAT(
        title = "Chat Interface",
        description = "Bottom app bar with chat input actions",
    ),
    CUSTOM(
        title = "Custom Content",
        description = "Fully customizable bottom app bar content",
    ),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicBottomAppBarExample() {
    Scaffold(
        bottomBar = {
            KptBottomAppBar(
                configuration = KptBottomAppBarConfiguration(),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Basic Bottom App Bar Example")
        }
    }
}

@Composable
private fun WithFabBottomAppBarExample() {
    Scaffold(
        bottomBar = {
            KptBottomAppBar(
                onFabClick = { /* Handle FAB click */ },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Bottom App Bar with FAB Example")
        }
    }
}

@Composable
private fun WithActionsBottomAppBarExample() {
    Scaffold(
        bottomBar = {
            KptBottomAppBar(
                actionIcon = Icons.Default.Search,
                onActionClick = { /* Handle action click */ },
                onFabClick = { /* Handle FAB click */ },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Bottom App Bar with Actions Example")
        }
    }
}

@Composable
private fun NavigationBottomAppBarExample() {
    var currentRoute by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            KptNavigationBottomAppBar(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Navigation Bottom App Bar Example\nCurrent route: $currentRoute")
        }
    }
}

@Composable
private fun SocialBottomAppBarExample() {
    var likeCount by remember { mutableStateOf(0) }
    var commentCount by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            KptSocialBottomAppBar(
                onLike = { likeCount++ },
                onComment = { commentCount++ },
                onShare = { /* Handle share */ },
                onBookmark = { /* Handle bookmark */ },
                likeCount = if (likeCount > 0) likeCount.toString() else null,
                commentCount = if (commentCount > 0) commentCount.toString() else null,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Social Bottom App Bar Example")
            Text("Likes: $likeCount")
            Text("Comments: $commentCount")
        }
    }
}

@Composable
private fun MediaPlayerBottomAppBarExample() {
    var isPlaying by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            KptMediaPlayerBottomAppBar(
                isPlaying = isPlaying,
                onPlayPause = { isPlaying = !isPlaying },
                onPrevious = { /* Handle previous */ },
                onNext = { /* Handle next */ },
                onShuffle = { /* Handle shuffle */ },
                onRepeat = { /* Handle repeat */ },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Media Player Bottom App Bar Example")
                Text("Status: ${if (isPlaying) "Playing" else "Paused"}")
            }
        }
    }
}

@Composable
private fun EcommerceBottomAppBarExample() {
    var cartItemCount by remember { mutableStateOf(0) }
    var isFavorite by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            KptEcommerceBottomAppBar(
                onAddToCart = { cartItemCount++ },
                onFavorite = { isFavorite = !isFavorite },
                onShare = { /* Handle share */ },
                cartItemCount = cartItemCount,
                isFavorite = isFavorite,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("E-commerce Bottom App Bar Example")
            Text("Cart items: $cartItemCount")
            Text("Favorite: $isFavorite")
        }
    }
}

@Composable
private fun ChatBottomAppBarExample() {
    Scaffold(
        bottomBar = {
            KptChatBottomAppBar(
                onAttach = { /* Handle attach */ },
                onCamera = { /* Handle camera */ },
                onMicrophone = { /* Handle microphone */ },
                onSend = { /* Handle send */ },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Chat Bottom App Bar Example")
        }
    }
}

@Composable
private fun CustomBottomAppBarExample() {
    Scaffold(
        bottomBar = {
            KptBottomAppBar(
                configuration = KptBottomAppBarConfiguration(
                    variant = BottomAppBarVariant.Custom,
                    customContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            Text("Custom Left", color = MaterialTheme.colorScheme.primary)
                            Text("Custom Center", color = MaterialTheme.colorScheme.primary)
                            Text("Custom Right", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Custom Bottom App Bar Example")
        }
    }
}
