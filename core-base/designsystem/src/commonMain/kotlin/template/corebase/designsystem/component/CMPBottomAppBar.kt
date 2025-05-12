/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.corebase.designsystem.component

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
import template.corebase.designsystem.component.variant.BottomAppBarVariant

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
