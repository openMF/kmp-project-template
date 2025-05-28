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

package template.core.base.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.config.KptTestTags
import template.core.base.designsystem.core.BottomAppBarAction
import template.core.base.designsystem.core.BottomAppBarVariant
import template.core.base.designsystem.core.KptBottomAppBarConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptBottomAppBar(configuration: KptBottomAppBarConfiguration) {
    val finalModifier = configuration.modifier
        .testTag(configuration.testTag ?: KptTestTags.BottomAppBar)
        .let { mod ->
            if (configuration.contentDescription != null) {
                mod.semantics { contentDescription = configuration.contentDescription }
            } else mod
        }

    when (configuration.variant) {
        BottomAppBarVariant.WithActions -> BottomAppBar(
            actions = {
                configuration.actions.forEach { action ->
                    Box {
                        IconButton(
                            onClick = action.onClick,
                            enabled = action.enabled,
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.contentDescription,
                            )
                        }

                        action.badge?.let { badgeText ->
                            Badge(
                                modifier = Modifier.offset(x = 12.dp, y = (-12).dp),
                            ) {
                                Text(badgeText)
                            }
                        }
                    }
                }
            },
            modifier = finalModifier,
            floatingActionButton = configuration.floatingActionButton,
            containerColor = configuration.containerColor ?: BottomAppBarDefaults.containerColor,
            contentColor = contentColorFor(
                configuration.containerColor ?: BottomAppBarDefaults.containerColor,
            ),
            tonalElevation = configuration.tonalElevation,
            contentPadding = configuration.contentPadding,
            windowInsets = configuration.windowInsets ?: BottomAppBarDefaults.windowInsets,
            scrollBehavior = configuration.scrollBehavior,
        )

        BottomAppBarVariant.Custom -> BottomAppBar(
            modifier = finalModifier,
            containerColor = configuration.containerColor ?: BottomAppBarDefaults.containerColor,
            contentColor = contentColorFor(
                configuration.containerColor ?: BottomAppBarDefaults.containerColor,
            ),
            tonalElevation = configuration.tonalElevation,
            contentPadding = configuration.contentPadding,
            windowInsets = configuration.windowInsets ?: BottomAppBarDefaults.windowInsets,
            scrollBehavior = configuration.scrollBehavior,
        ) {
            configuration.customContent?.invoke(this)
        }
    }
}

// 1. Simple bottom app bar with FAB
@Composable
fun KptBottomAppBar(
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
    fabIcon: ImageVector = Icons.Default.Add,
) {
    KptBottomAppBar(
        KptBottomAppBarConfiguration(
            modifier = modifier,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onFabClick,
                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                ) {
                    Icon(fabIcon, contentDescription = "Add")
                }
            },
        ),
    )
}

// 2. With single action
@Composable
fun KptBottomAppBar(
    actionIcon: ImageVector,
    onActionClick: () -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionContentDescription: String = "Action",
    fabIcon: ImageVector = Icons.Default.Add,
) {
    KptBottomAppBar(
        KptBottomAppBarConfiguration(
            modifier = modifier,
            actions = listOf(
                BottomAppBarAction(actionIcon, actionContentDescription, onActionClick),
            ),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onFabClick,
                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                ) {
                    Icon(fabIcon, contentDescription = "Add")
                }
            },
        ),
    )
}

// 3. Navigation bottom app bar
@Composable
fun KptNavigationBottomAppBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavigationItem> = defaultNavigationItems(),
) {
    KptBottomAppBar(
        KptBottomAppBarConfiguration(
            modifier = modifier,
            variant = BottomAppBarVariant.Custom,
            customContent = {
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    IconButton(
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            },
        ),
    )
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private fun defaultNavigationItems() = listOf(
    NavigationItem("home", "Home", Icons.Default.Home),
    NavigationItem("search", "Search", Icons.Default.Search),
    NavigationItem("favorites", "Favorites", Icons.Default.Favorite),
    NavigationItem("profile", "Profile", Icons.Default.Person),
)

// 4. Social media bottom app bar
@Composable
fun KptSocialBottomAppBar(
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onBookmark: () -> Unit,
    modifier: Modifier = Modifier,
    likeCount: String? = null,
    commentCount: String? = null,
) {
    KptBottomAppBar(
        KptBottomAppBarConfiguration(
            modifier = modifier,
            actions = listOf(
                BottomAppBarAction(Icons.Default.FavoriteBorder, "Like", onLike, badge = likeCount),
                BottomAppBarAction(
                    Icons.Default.ChatBubbleOutline,
                    "Comment",
                    onComment,
                    badge = commentCount,
                ),
                BottomAppBarAction(Icons.Default.Share, "Share", onShare),
                BottomAppBarAction(Icons.Default.BookmarkBorder, "Bookmark", onBookmark),
            ),
        ),
    )
}

// 5. Media player bottom app bar
@Composable
fun KptMediaPlayerBottomAppBar(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KptBottomAppBar(
        KptBottomAppBarConfiguration(
            modifier = modifier,
            actions = listOf(
                BottomAppBarAction(Icons.Default.Shuffle, "Shuffle", onShuffle),
                BottomAppBarAction(Icons.Default.SkipPrevious, "Previous", onPrevious),
                BottomAppBarAction(Icons.Default.SkipNext, "Next", onNext),
                BottomAppBarAction(Icons.Default.Repeat, "Repeat", onRepeat),
            ),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onPlayPause,
                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                    )
                }
            },
        ),
    )
}

// 6. E-commerce bottom app bar
@Composable
fun KptEcommerceBottomAppBar(
    onAddToCart: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    cartItemCount: Int = 0,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
) {
    KptBottomAppBar(
        KptBottomAppBarConfiguration(
            modifier = modifier,
            actions = listOf(
                BottomAppBarAction(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    "Favorite",
                    onFavorite,
                ),
                BottomAppBarAction(Icons.Default.Share, "Share", onShare),
                BottomAppBarAction(
                    Icons.Default.ShoppingCart,
                    "Cart",
                    {},
                    badge = if (cartItemCount > 0) cartItemCount.toString() else null,
                ),
            ),
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onAddToCart,
                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Cart")
                }
            },
        ),
    )
}

// 7. Chat bottom app bar
@Composable
fun KptChatBottomAppBar(
    onAttach: () -> Unit,
    onCamera: () -> Unit,
    onMicrophone: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KptBottomAppBar(
        KptBottomAppBarConfiguration(
            modifier = modifier,
            actions = listOf(
                BottomAppBarAction(Icons.Default.AttachFile, "Attach", onAttach),
                BottomAppBarAction(Icons.Default.CameraAlt, "Camera", onCamera),
                BottomAppBarAction(Icons.Default.Mic, "Microphone", onMicrophone),
            ),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onSend,
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            },
        ),
    )
}

// 8. Original component compatibility
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptBottomAppBar(
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable (() -> Unit)? = null,
    containerColor: Color = BottomAppBarDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = BottomAppBarDefaults.ContainerElevation,
    contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding,
    windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets,
    scrollBehavior: BottomAppBarScrollBehavior? = null,
    variant: BottomAppBarVariant = BottomAppBarVariant.WithActions,
    testTag: String? = null,
    customContent: @Composable RowScope.() -> Unit = {},
) {
    when (variant) {
        BottomAppBarVariant.WithActions -> BottomAppBar(
            actions = actions,
            modifier = modifier.testTag(testTag ?: KptTestTags.BottomAppBar),
            floatingActionButton = floatingActionButton,
            containerColor = containerColor,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            contentPadding = contentPadding,
            windowInsets = windowInsets,
            scrollBehavior = scrollBehavior,
        )

        BottomAppBarVariant.Custom -> BottomAppBar(
            modifier = modifier.testTag(testTag ?: KptTestTags.BottomAppBar),
            containerColor = containerColor,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            contentPadding = contentPadding,
            windowInsets = windowInsets,
            scrollBehavior = scrollBehavior,
        ) {
            customContent()
        }
    }
}
