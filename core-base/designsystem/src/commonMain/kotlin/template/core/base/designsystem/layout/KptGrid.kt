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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import template.core.base.designsystem.component.KptExpandableCard
import template.core.base.designsystem.component.KptTabLayout
import template.core.base.designsystem.component.TabItem
import template.core.base.designsystem.theme.KptTheme
import kotlin.math.min

@Composable
fun KptGrid(
    modifier: Modifier = Modifier,
    configuration: GridConfiguration = GridConfiguration(
        spacing = KptTheme.spacing.md,
        horizontalPadding = KptTheme.spacing.md,
    ),
    content: @Composable GridScope.() -> Unit,
) {
    val density = LocalDensity.current
    val columns = configuration.getColumnsForCurrentScreen()

    Layout(
        modifier = modifier
            .padding(horizontal = configuration.horizontalPadding)
            .testTag("KptGrid"),
        content = {
            GridScopeImpl(columns, configuration.spacing).content()
        },
    ) { measurables, constraints ->
        val spacing = with(density) { configuration.spacing.roundToPx() }
        val horizontalPadding = with(density) { configuration.horizontalPadding.roundToPx() }

        val availableWidth = constraints.maxWidth - horizontalPadding * 2
        val columnWidth = (availableWidth - spacing * (columns - 1)) / columns

        val placeables = measurables.fastMap { measurable ->
            val gridItem = measurable.parentData as? GridItemData ?: GridItemData()
            val itemColumns = min(gridItem.span, columns)
            val itemWidth = columnWidth * itemColumns + spacing * (itemColumns - 1)

            measurable.measure(
                constraints.copy(
                    minWidth = itemWidth,
                    maxWidth = itemWidth,
                ),
            )
        }

        val rows = mutableListOf<MutableList<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentRowColumns = 0

        placeables.fastForEach { placeable ->
            val gridItem = placeable.parentData as? GridItemData ?: GridItemData()
            val itemColumns = min(gridItem.span, columns)

            if (currentRowColumns + itemColumns > columns) {
                if (currentRow.isNotEmpty()) {
                    rows.add(currentRow)
                    currentRow = mutableListOf()
                    currentRowColumns = 0
                }
            }

            currentRow.add(placeable)
            currentRowColumns += itemColumns
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val rowHeights = rows.fastMap { row ->
            row.fastMaxBy { it.height }?.height ?: 0
        }

        val totalHeight = rowHeights.sum() + spacing * (rows.size - 1).coerceAtLeast(0)

        layout(constraints.maxWidth, totalHeight) {
            var yPosition = 0

            rows.forEachIndexed { rowIndex, row ->
                var xPosition = 0

                row.fastForEach { placeable ->
                    placeable.place(xPosition, yPosition)
                    val gridItem = placeable.parentData as? GridItemData ?: GridItemData()
                    val itemColumns = min(gridItem.span, columns)
                    xPosition += columnWidth * itemColumns + spacing * itemColumns
                }

                yPosition += rowHeights[rowIndex] + spacing
            }
        }
    }
}

interface GridScope {
    fun Modifier.gridItem(span: Int = 1): Modifier
}

private class GridScopeImpl(
    private val columns: Int,
    private val spacing: Dp,
) : GridScope {
    override fun Modifier.gridItem(span: Int): Modifier {
        return this.then(
            GridItemModifier(
                span = span.coerceIn(1, columns),
            ),
        )
    }
}

private data class GridItemData(
    val span: Int = 1,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any = this@GridItemData
}

private data class GridItemModifier(
    val span: Int,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any {
        return GridItemData(span)
    }
}

@Immutable
data class GridConfiguration(
    val spacing: Dp,
    val horizontalPadding: Dp,
    val columns: Int = 12,
    val breakpoints: BreakpointConfiguration = BreakpointConfiguration(),
) {
    @Composable
    fun getColumnsForCurrentScreen(): Int {
        val window = LocalWindowInfo.current
        val screenWidth = window.containerSize.width.dp

        return when {
            screenWidth >= breakpoints.xl -> breakpoints.xlColumns
            screenWidth >= breakpoints.lg -> breakpoints.lgColumns
            screenWidth >= breakpoints.md -> breakpoints.mdColumns
            screenWidth >= breakpoints.sm -> breakpoints.smColumns
            else -> breakpoints.xsColumns
        }
    }
}

@Immutable
data class BreakpointConfiguration(
    val xs: Dp = 0.dp,
    val sm: Dp = 600.dp,
    val md: Dp = 840.dp,
    val lg: Dp = 1200.dp,
    val xl: Dp = 1600.dp,
    val xsColumns: Int = 4,
    val smColumns: Int = 8,
    val mdColumns: Int = 12,
    val lgColumns: Int = 12,
    val xlColumns: Int = 12,
)

// ===== USAGE EXAMPLES =====
@Composable
fun LayoutExamples() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Layout Examples", style = KptTheme.typography.headlineMedium)

        // Grid example
        Text("Responsive Grid", style = KptTheme.typography.titleLarge)
        KptGrid(
            modifier = Modifier.height(200.dp),
        ) {
            repeat(6) { index ->
                Card(
                    modifier = Modifier
                        .gridItem(span = if (index == 0) 2 else 1)
                        .height(80.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Item ${index + 1}")
                    }
                }
            }
        }

        // Flow layout example
        Text("Flow Layout", style = KptTheme.typography.titleLarge)
        KptFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val chipLabels =
                listOf("Kotlin", "Compose", "Android", "iOS", "Design System", "Material 3")
            chipLabels.forEach { label ->
                FilterChip(
                    onClick = { },
                    label = { Text(label) },
                    selected = false,
                )
            }
        }

        // Expandable card example
        Text("Expandable Card", style = KptTheme.typography.titleLarge)
        var expanded by remember { mutableStateOf(false) }
        KptExpandableCard(
            title = "Settings",
            subtitle = "Application preferences",
            icon = Icons.Default.Settings,
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dark Mode")
                Switch(checked = false, onCheckedChange = { })
                Text("Notifications")
                Switch(checked = true, onCheckedChange = { })
            }
        }

        // Tab layout example
        Text("Tab Layout", style = KptTheme.typography.titleLarge)
        var selectedTab by remember { mutableIntStateOf(0) }
        KptTabLayout(
            tabs = listOf(
                TabItem("Home", Icons.Default.Home) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Home Content")
                    }
                },
                TabItem("Profile", Icons.Default.Person) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Profile Content")
                    }
                },
            ),
            selectedTabIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.height(200.dp),
        )
    }
}
