/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import template.core.base.designsystem.component.variant.TopAppBarVariant
import template.core.base.designsystem.config.KptTestTags

/**
 * A top app bar component for the KPT design system, supporting small,
 * center-aligned, medium, and large variants.
 *
 * @param title Title composable.
 * @param modifier Modifier applied to the app bar. *(Default: [Modifier])*
 * @param navigationIcon Leading icon composable. *(Default: empty lambda)*
 * @param actions Trailing actions composable. *(Default: empty lambda)*
 * @param expandedHeight Expanded height for scrollable variants.
 *    *(Optional)*
 * @param collapsedHeight Collapsed height for scrollable variants.
 *    *(Optional)*
 * @param windowInsets Insets for layout. *(Default:
 *    [TopAppBarDefaults.windowInsets])*
 * @param colors Color configuration. *(Optional)*
 * @param scrollBehavior Scroll behavior for dynamic appearance.
 *    *(Optional)*
 * @param variant Determines the style of the app bar. *(Default:
 *    [TopAppBarVariant.SMALL])*
 * @param testTag Tag for UI testing. *(Optional)*
 * @param contentDescription Content description for accessibility.
 *    *(Optional)*
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp? = null,
    collapsedHeight: Dp? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    variant: TopAppBarVariant = TopAppBarVariant.SMALL,
    testTag: String? = null,
) {
    when (variant) {
        TopAppBarVariant.SMALL -> TopAppBar(
            title = title,
            modifier = modifier.testTag(testTag ?: KptTestTags.TopAppBar),
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.TopAppBarExpandedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.topAppBarColors(),
            scrollBehavior = scrollBehavior,
        )

        TopAppBarVariant.CENTER_ALIGNED -> CenterAlignedTopAppBar(
            title = title,
            modifier = modifier.testTag(testTag ?: KptTestTags.TopAppBar),
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.TopAppBarExpandedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.centerAlignedTopAppBarColors(),
            scrollBehavior = scrollBehavior ?: TopAppBarDefaults.enterAlwaysScrollBehavior(),
        )

        TopAppBarVariant.MEDIUM -> MediumTopAppBar(
            title = title,
            modifier = modifier.testTag(testTag ?: KptTestTags.TopAppBar),
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.MediumAppBarExpandedHeight,
            collapsedHeight = collapsedHeight ?: TopAppBarDefaults.MediumAppBarCollapsedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.mediumTopAppBarColors(),
            scrollBehavior = scrollBehavior ?: TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        )

        TopAppBarVariant.LARGE -> LargeTopAppBar(
            title = title,
            modifier = modifier.testTag(testTag ?: KptTestTags.TopAppBar),
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.LargeAppBarExpandedHeight,
            collapsedHeight = collapsedHeight ?: TopAppBarDefaults.LargeAppBarCollapsedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.largeTopAppBarColors(),
            scrollBehavior = scrollBehavior ?: TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        )
    }
}

/**
 * A top app bar component for the KPT design system, supporting small,
 * center-aligned, medium, and large variants.
 *
 * @param title Title composable.
 * @param navigationIcon Leading icon composable. *(Default: empty lambda)*
 * @param modifier Modifier applied to the app bar. *(Default: [Modifier])*
 * @param actions Trailing actions composable. *(Default: empty lambda)*
 * @param expandedHeight Expanded height for scrollable variants.
 *    *(Optional)*
 * @param collapsedHeight Collapsed height for scrollable variants.
 *    *(Optional)*
 * @param windowInsets Insets for layout. *(Default:
 *    [TopAppBarDefaults.windowInsets])*
 * @param colors Color configuration. *(Optional)*
 * @param scrollBehavior Scroll behavior for dynamic appearance.
 *    *(Optional)*
 * @param variant Determines the style of the app bar. *(Default:
 *    [TopAppBarVariant.SMALL])*
 * @param testTag Tag for UI testing. *(Optional)*
 * @param contentDescription Content description for accessibility.
 *    *(Optional)*
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptTopAppBar(
    title: String,
    onNavigationIconClick: () -> Unit,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationIconContentDescription: String = "Back",
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp? = null,
    collapsedHeight: Dp? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    variant: TopAppBarVariant = TopAppBarVariant.SMALL,
    testTag: String? = null,
    contentDescription: String? = null,
) {
    val barModifier = modifier
        .then(Modifier.testTag(testTag ?: "KptTopAppBar"))
        .then(
            if (contentDescription != null) Modifier.semantics {
                this.contentDescription = contentDescription
            } else Modifier,
        )

    val navigationIconContent: @Composable () -> Unit = remember(navigationIcon) {
        {
            IconButton(
                onClick = onNavigationIconClick,
                modifier = Modifier.testTag("CloseButton"),
            ) {
                Icon(
                    modifier = Modifier,
                    imageVector = navigationIcon,
                    contentDescription = navigationIconContentDescription,
                )
            }
        }
    }

    when (variant) {
        TopAppBarVariant.SMALL -> TopAppBar(
            title = {
                Text(text = title)
            },
            navigationIcon = navigationIconContent,
            actions = actions,
            modifier = barModifier,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.TopAppBarExpandedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.topAppBarColors(),
            scrollBehavior = scrollBehavior,
        )

        TopAppBarVariant.CENTER_ALIGNED -> CenterAlignedTopAppBar(
            title = {
                Text(text = title)
            },
            navigationIcon = navigationIconContent,
            modifier = barModifier,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.TopAppBarExpandedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.centerAlignedTopAppBarColors(),
            scrollBehavior = scrollBehavior ?: TopAppBarDefaults.enterAlwaysScrollBehavior(),
        )

        TopAppBarVariant.MEDIUM -> MediumTopAppBar(
            title = {
                Text(text = title)
            },
            navigationIcon = navigationIconContent,
            modifier = barModifier,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.MediumAppBarExpandedHeight,
            collapsedHeight = collapsedHeight ?: TopAppBarDefaults.MediumAppBarCollapsedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.mediumTopAppBarColors(),
            scrollBehavior = scrollBehavior ?: TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        )

        TopAppBarVariant.LARGE -> LargeTopAppBar(
            title = {
                Text(text = title)
            },
            navigationIcon = navigationIconContent,
            modifier = barModifier,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.LargeAppBarExpandedHeight,
            collapsedHeight = collapsedHeight ?: TopAppBarDefaults.LargeAppBarCollapsedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.largeTopAppBarColors(),
            scrollBehavior = scrollBehavior ?: TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        )
    }
}

/**
 * A top app bar component for the KPT design system, supporting small,
 * center-aligned, medium, and large variants.
 *
 * @param title Title composable.
 * @param navigationIcon Leading icon composable. *(Default: empty lambda)*
 * @param modifier Modifier applied to the app bar. *(Default: [Modifier])*
 * @param actions Trailing actions composable. *(Default: empty lambda)*
 * @param expandedHeight Expanded height for scrollable variants.
 *    *(Optional)*
 * @param collapsedHeight Collapsed height for scrollable variants.
 *    *(Optional)*
 * @param windowInsets Insets for layout. *(Default:
 *    [TopAppBarDefaults.windowInsets])*
 * @param colors Color configuration. *(Optional)*
 * @param scrollBehavior Scroll behavior for dynamic appearance.
 *    *(Optional)*
 * @param variant Determines the style of the app bar. *(Default:
 *    [TopAppBarVariant.SMALL])*
 * @param testTag Tag for UI testing. *(Optional)*
 * @param contentDescription Content description for accessibility.
 *    *(Optional)*
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptTopAppBar(
    title: String,
    navigationIcon: NavigationIcon?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp? = null,
    collapsedHeight: Dp? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    variant: TopAppBarVariant = TopAppBarVariant.SMALL,
    testTag: String? = null,
    contentDescription: String? = null,
) {
    val barModifier = modifier
        .then(Modifier.testTag(testTag ?: "KptTopAppBar"))
        .then(
            if (contentDescription != null) Modifier.semantics {
                this.contentDescription = contentDescription
            } else Modifier,
        )

    val navigationIconContent: @Composable () -> Unit = remember(navigationIcon) {
        {
            navigationIcon?.let {
                IconButton(
                    onClick = it.onNavigationIconClick,
                    modifier = Modifier.testTag("CloseButton"),
                ) {
                    Icon(
                        modifier = Modifier,
                        imageVector = it.navigationIcon,
                        contentDescription = it.navigationIconContentDescription,
                    )
                }
            }
        }
    }

    when (variant) {
        TopAppBarVariant.SMALL -> TopAppBar(
            title = {
                Text(text = title)
            },
            navigationIcon = navigationIconContent,
            actions = actions,
            modifier = barModifier,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.TopAppBarExpandedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.topAppBarColors(),
            scrollBehavior = scrollBehavior,
        )

        TopAppBarVariant.CENTER_ALIGNED -> CenterAlignedTopAppBar(
            title = {
                Text(text = title)
            },
            navigationIcon = navigationIconContent,
            modifier = barModifier,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.TopAppBarExpandedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.centerAlignedTopAppBarColors(),
            scrollBehavior = scrollBehavior ?: TopAppBarDefaults.enterAlwaysScrollBehavior(),
        )

        TopAppBarVariant.MEDIUM -> MediumTopAppBar(
            title = {
                Text(text = title)
            },
            navigationIcon = navigationIconContent,
            modifier = barModifier,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.MediumAppBarExpandedHeight,
            collapsedHeight = collapsedHeight ?: TopAppBarDefaults.MediumAppBarCollapsedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.mediumTopAppBarColors(),
            scrollBehavior = scrollBehavior ?: TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        )

        TopAppBarVariant.LARGE -> LargeTopAppBar(
            title = {
                Text(text = title)
            },
            navigationIcon = navigationIconContent,
            modifier = barModifier,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.LargeAppBarExpandedHeight,
            collapsedHeight = collapsedHeight ?: TopAppBarDefaults.LargeAppBarCollapsedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.largeTopAppBarColors(),
            scrollBehavior = scrollBehavior ?: TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        )
    }
}

data class NavigationIcon(
    val navigationIcon: ImageVector,
    val navigationIconContentDescription: String,
    val onNavigationIconClick: () -> Unit,
)
