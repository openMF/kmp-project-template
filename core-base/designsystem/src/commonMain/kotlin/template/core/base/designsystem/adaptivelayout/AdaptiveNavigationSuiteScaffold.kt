/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.adaptivelayout

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AdaptiveNavigationSuiteScaffold(
    navigationSuiteItems: NavigationSuiteScope.() -> Unit,
    modifier: Modifier = Modifier,
    layoutType: NavigationSuiteType? = null,
    navigationSuiteColors: NavigationSuiteColors = NavigationSuiteDefaults.colors(),
    containerColor: Color = NavigationSuiteScaffoldDefaults.containerColor,
    contentColor: Color = NavigationSuiteScaffoldDefaults.contentColor,
    content: @Composable () -> Unit,
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val windowSizeClass = calculateWindowSizeClass()

    val customNavSuiteType =
        layoutType ?: if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
            NavigationSuiteType.NavigationDrawer
        } else {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
        }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            navigationSuiteItems()
        },
        layoutType = customNavSuiteType,
        navigationSuiteColors = navigationSuiteColors,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
        content = content,
    )
}
