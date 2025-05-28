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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

// Enhanced TopAppBar Variant System
sealed interface TopAppBarVariant : ComponentVariant {
    override val name: String

    data object Small : TopAppBarVariant {
        override val name: String = "small"
    }

    data object CenterAligned : TopAppBarVariant {
        override val name: String = "center_aligned"
    }

    data object Medium : TopAppBarVariant {
        override val name: String = "medium"
    }

    data object Large : TopAppBarVariant {
        override val name: String = "large"
    }
}

// Configuration class
@OptIn(ExperimentalMaterial3Api::class)
@Immutable
data class KptTopAppBarConfiguration(
    val title: String,
    val modifier: Modifier = Modifier,
    val variant: TopAppBarVariant = TopAppBarVariant.Small,
    val navigationIcon: ImageVector? = null,
    val onNavigationIonClick: (() -> Unit)? = null,
    val actions: List<TopAppBarAction> = emptyList(),
    val subtitle: String? = null,
    val colors: TopAppBarColors? = null,
    val scrollBehavior: TopAppBarScrollBehavior? = null,
    val windowInsets: WindowInsets? = null,
    val testTag: String? = null,
    val contentDescription: String? = null,
)

data class TopAppBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

// DSL Builder
@DslMarker
annotation class TopAppBarDsl

@TopAppBarDsl
class KptTopAppBarBuilder {
    var title: String = ""
    var modifier: Modifier = Modifier
    var variant: TopAppBarVariant = TopAppBarVariant.Small
    var navigationIcon: ImageVector? = null
    var onNavigationClick: (() -> Unit)? = null
    var subtitle: String? = null

    @OptIn(ExperimentalMaterial3Api::class)
    var colors: TopAppBarColors? = null

    @OptIn(ExperimentalMaterial3Api::class)
    var scrollBehavior: TopAppBarScrollBehavior? = null

    @OptIn(ExperimentalMaterial3Api::class)
    var windowInsets: WindowInsets? = null

    var testTag: String? = null

    var contentDescription: String? = null

    private val actionsList = mutableListOf<TopAppBarAction>()

    fun action(
        icon: ImageVector,
        contentDescription: String,
        enabled: Boolean = true,
        onClick: () -> Unit,
    ) {
        actionsList.add(TopAppBarAction(icon, contentDescription, onClick, enabled))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun build(): KptTopAppBarConfiguration = KptTopAppBarConfiguration(
        title = title,
        modifier = modifier,
        variant = variant,
        navigationIcon = navigationIcon,
        onNavigationIonClick = onNavigationClick,
        actions = actionsList.toList(),
        subtitle = subtitle,
        colors = colors,
        scrollBehavior = scrollBehavior,
        windowInsets = windowInsets,
        testTag = testTag,
        contentDescription = contentDescription,
    )
}

// DSL Function
fun kptTopAppBar(block: KptTopAppBarBuilder.() -> Unit): KptTopAppBarConfiguration {
    return KptTopAppBarBuilder().apply(block).build()
}
