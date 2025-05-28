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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun KptTabLayout(
    tabs: List<TabItem>,
    modifier: Modifier = Modifier,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    tabRowModifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("KptTabLayout"),
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = tabRowModifier,
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = { Text(tab.title) },
                    icon = tab.icon?.let { { Icon(it, contentDescription = null) } },
                )
            }
        }

        Box(
            modifier = contentModifier
                .fillMaxSize()
                .weight(1f),
        ) {
            if (selectedTabIndex in tabs.indices) {
                tabs[selectedTabIndex].content()
            }
        }
    }
}

@Immutable
data class TabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val content: @Composable () -> Unit,
)
