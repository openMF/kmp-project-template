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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import template.core.base.designsystem.component.variant.TopAppBarVariant

/**
 * A flexible and theme-aware top app bar for the CMP design system.
 *
 * This composable wraps the various Material3 top app bar implementations—[TopAppBar], [CenterAlignedTopAppBar],
 * [MediumTopAppBar], and [LargeTopAppBar]—and selects the appropriate one based on the [variant] parameter.
 *
 * @param title Composable displayed as the title in the app bar.
 * @param modifier Modifier applied to the top app bar. *(Default: [Modifier])*
 * @param navigationIcon Composable shown as the leading icon (e.g., back or menu button).
 * (Optional; Default: empty lambda)*
 * @param actions Composables displayed as trailing icons/actions in the app bar. *(Optional; Default: empty lambda)*
 * @param expandedHeight The expanded height of the top app bar.
 * (Optional; defaults to the variant's expanded height via [TopAppBarDefaults])*
 * @param collapsedHeight The collapsed height for scrollable variants.
 * (Optional; applicable only to [TopAppBarVariant.MEDIUM] and [TopAppBarVariant.LARGE])*
 * @param windowInsets Insets to apply around the app bar (e.g., status bar padding).
 * (Default: [TopAppBarDefaults.windowInsets])*
 * @param colors Color configuration for the top app bar.
 * (Optional; defaults to the variant’s color scheme via [TopAppBarDefaults])*
 * @param scrollBehavior Scroll behavior applied to the app bar (e.g., pinned, collapse on scroll).
 * (Optional; defaults to sensible variant-specific behaviors)*
 * @param variant Determines the style and structure of the top app bar. *(Default: [TopAppBarVariant.SMALL])*
 */
@Suppress("CyclomaticComplexMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CMPTopAppBar(
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
) {
    when (variant) {
        TopAppBarVariant.SMALL -> TopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.TopAppBarExpandedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.topAppBarColors(),
            scrollBehavior = scrollBehavior,
        )

        TopAppBarVariant.CENTER_ALIGNED -> CenterAlignedTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.TopAppBarExpandedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.centerAlignedTopAppBarColors(),
            scrollBehavior = scrollBehavior ?: TopAppBarDefaults.enterAlwaysScrollBehavior(),
        )

        TopAppBarVariant.MEDIUM -> MediumTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.MediumAppBarExpandedHeight,
            collapsedHeight = collapsedHeight ?: TopAppBarDefaults.MediumAppBarCollapsedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.mediumTopAppBarColors(),
            scrollBehavior = scrollBehavior ?: TopAppBarDefaults.enterAlwaysScrollBehavior(),
        )

        TopAppBarVariant.LARGE -> LargeTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = expandedHeight ?: TopAppBarDefaults.LargeAppBarExpandedHeight,
            collapsedHeight = collapsedHeight ?: TopAppBarDefaults.LargeAppBarCollapsedHeight,
            windowInsets = windowInsets,
            colors = colors ?: TopAppBarDefaults.largeTopAppBarColors(),
            scrollBehavior = scrollBehavior
                ?: TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        )
    }
}
