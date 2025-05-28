/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun KptResponsiveLayout(
    compact: @Composable () -> Unit,
    medium: (@Composable () -> Unit)? = null,
    expanded: (@Composable () -> Unit)? = null,
) {
    val layoutInfo = rememberResponsiveLayoutInfo()

    when {
        layoutInfo.isExpanded && expanded != null -> expanded()
        layoutInfo.isMedium && medium != null -> medium()
        else -> compact()
    }
}

@Stable
class ResponsiveLayoutInfo(
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
    val isCompact: Boolean,
    val isMedium: Boolean,
    val isExpanded: Boolean,
)

@Composable
fun rememberResponsiveLayoutInfo(): ResponsiveLayoutInfo {
    val configuration = LocalWindowInfo.current
    val width = configuration.containerSize.width
    val height = configuration.containerSize.height.dp

    return remember(configuration) {
        ResponsiveLayoutInfo(
            screenWidthDp = width.dp,
            screenHeightDp = height,
            isCompact = width < 600,
            isMedium = width >= 600 && width < 840,
            isExpanded = width >= 840,
        )
    }
}
