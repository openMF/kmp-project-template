/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.core

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp

sealed interface BottomAppBarVariant : ComponentVariant {
    override val name: String

    data object WithActions : BottomAppBarVariant {
        override val name: String = "with_actions"
    }

    data object Custom : BottomAppBarVariant {
        override val name: String = "custom"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Immutable
data class KptBottomAppBarConfiguration(
    val modifier: Modifier = Modifier,
    val variant: BottomAppBarVariant = BottomAppBarVariant.WithActions,
    val actions: List<BottomAppBarAction> = emptyList(),
    val floatingActionButton: (@Composable () -> Unit)? = null,
    val customContent: (@Composable RowScope.() -> Unit)? = null,
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val tonalElevation: Dp = BottomAppBarDefaults.ContainerElevation,
    val contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding,
    val windowInsets: WindowInsets? = null,
    val scrollBehavior: BottomAppBarScrollBehavior? = null,
    val testTag: String? = null,
    val contentDescription: String? = null,
)

data class BottomAppBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val badge: String? = null,
)

@DslMarker
annotation class BottomAppBarDsl

@BottomAppBarDsl
class KptBottomAppBarBuilder {
    var modifier: Modifier = Modifier
    var variant: BottomAppBarVariant = BottomAppBarVariant.WithActions
    var floatingActionButton: (@Composable () -> Unit)? = null
    var customContent: (@Composable RowScope.() -> Unit)? = null
    var containerColor: Color? = null
    var contentColor: Color? = null
    var tonalElevation: Dp = BottomAppBarDefaults.ContainerElevation
    var contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding
    var windowInsets: WindowInsets? = null

    @OptIn(ExperimentalMaterial3Api::class)
    var scrollBehavior: BottomAppBarScrollBehavior? = null
    var testTag: String? = null
    var contentDescription: String? = null

    private val actionsList = mutableListOf<BottomAppBarAction>()

    fun action(
        icon: ImageVector,
        contentDescription: String,
        enabled: Boolean = true,
        badge: String? = null,
        onClick: () -> Unit,
    ) {
        actionsList.add(BottomAppBarAction(icon, contentDescription, onClick, enabled, badge))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun build(): KptBottomAppBarConfiguration = KptBottomAppBarConfiguration(
        modifier = modifier,
        variant = variant,
        actions = actionsList.toList(),
        floatingActionButton = floatingActionButton,
        customContent = customContent,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        contentPadding = contentPadding,
        windowInsets = windowInsets,
        scrollBehavior = scrollBehavior,
        testTag = testTag,
        contentDescription = contentDescription,
    )
}

fun kptBottomAppBar(block: KptBottomAppBarBuilder.() -> Unit): KptBottomAppBarConfiguration {
    return KptBottomAppBarBuilder().apply(block).build()
}
