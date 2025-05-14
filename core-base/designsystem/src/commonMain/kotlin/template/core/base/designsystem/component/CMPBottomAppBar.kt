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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import template.core.base.designsystem.component.variant.BottomAppBarVariant

/**
 * A customizable bottom app bar component for the CMP design system, supporting both default action-based
 * and fully custom content layouts.
 *
 * This composable wraps the Material3 [BottomAppBar] and enables flexibility via [BottomAppBarVariant]:
 * - [BottomAppBarVariant.BOTTOM_WITH_ACTIONS] displays standard action icons and an optional FAB.
 * - [BottomAppBarVariant.BOTTOM_CUSTOM] renders fully custom content via [customContent].
 *
 * @param actions Composables representing action icons, typically shown at the end of the bar.
 * Required when [variant] is [BottomAppBarVariant.BOTTOM_WITH_ACTIONS].
 *
 * @param modifier Modifier applied to the BottomAppBar. *(Default: [Modifier])*
 *
 * @param floatingActionButton Optional composable for a floating action button. *(Optional)*
 * Displayed only in [BottomAppBarVariant.BOTTOM_WITH_ACTIONS].
 *
 * @param containerColor Background color of the BottomAppBar. *(Default: [BottomAppBarDefaults.containerColor])*
 *
 * @param contentColor Foreground content color (icons/text) inside the BottomAppBar.
 * (Default: [contentColorFor(containerColor)])*
 * @param tonalElevation Elevation applied to the BottomAppBar. *(Default: [BottomAppBarDefaults.ContainerElevation])*
 *
 * @param contentPadding Padding around the content inside the BottomAppBar.
 * (Default: [BottomAppBarDefaults.ContentPadding])*
 *
 * @param windowInsets Insets to apply for layout (e.g., navigation bar space).
 * (Default: [BottomAppBarDefaults.windowInsets])*
 *
 * @param scrollBehavior Optional scroll behavior for dynamic appearance changes. *(Optional)*
 *
 * @param variant Determines the layout style of the BottomAppBar.
 * (Default: [BottomAppBarVariant.BOTTOM_WITH_ACTIONS])*
 *
 * @param customContent Custom row-scoped composable content to be shown when using [BottomAppBarVariant.BOTTOM_CUSTOM].
 * Required when [variant] is [BottomAppBarVariant.BOTTOM_CUSTOM].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CMPBottomAppBar(
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable (() -> Unit)? = null,
    containerColor: Color = BottomAppBarDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = BottomAppBarDefaults.ContainerElevation,
    contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding,
    windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets,
    scrollBehavior: BottomAppBarScrollBehavior? = null,
    variant: BottomAppBarVariant = BottomAppBarVariant.BOTTOM_WITH_ACTIONS,
    customContent: @Composable RowScope.() -> Unit = {},
) {
    when (variant) {
        BottomAppBarVariant.BOTTOM_WITH_ACTIONS -> BottomAppBar(
            actions = actions,
            modifier = modifier,
            floatingActionButton = floatingActionButton,
            containerColor = containerColor,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            contentPadding = contentPadding,
            windowInsets = windowInsets,
            scrollBehavior = scrollBehavior,
        )

        BottomAppBarVariant.BOTTOM_CUSTOM -> BottomAppBar(
            modifier = modifier,
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
