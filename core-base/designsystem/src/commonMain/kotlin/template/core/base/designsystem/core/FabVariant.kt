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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector

// Enhanced FAB Variant System
sealed interface EnhancedFabVariant : ComponentVariant {
    override val name: String

    data object Small : EnhancedFabVariant {
        override val name: String = "small"
    }

    data object Regular : EnhancedFabVariant {
        override val name: String = "regular"
    }

    data object Large : EnhancedFabVariant {
        override val name: String = "large"
    }

    data object Extended : EnhancedFabVariant {
        override val name: String = "extended"
    }
}

// FAB State
enum class FabState { Normal, Loading, Success, Error }

// Configuration class
@Immutable
data class KptFloatingActionButtonConfiguration(
    val onClick: () -> Unit,
    override val modifier: Modifier = Modifier,
    val variant: EnhancedFabVariant = EnhancedFabVariant.Regular,
    val icon: ImageVector? = null,
    val text: String? = null,
    val state: FabState = FabState.Normal,
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val elevation: FloatingActionButtonElevation? = null,
    val shape: Shape? = null,
    val interactionSource: MutableInteractionSource? = null,
    val expanded: Boolean = true,
    override val testTag: String? = null,
    override val contentDescription: String? = null,
) : KptComponent

// DSL Builder
@DslMarker
annotation class FabDsl

@FabDsl
class KptFloatingActionButtonBuilder : ComponentConfiguration {
    var onClick: () -> Unit = {}
    var modifier: Modifier = Modifier
    var variant: EnhancedFabVariant = EnhancedFabVariant.Regular
    var icon: ImageVector? = null
    var text: String? = null
    var state: FabState = FabState.Normal
    var containerColor: Color? = null
    var contentColor: Color? = null
    var elevation: FloatingActionButtonElevation? = null
    var shape: Shape? = null
    var interactionSource: MutableInteractionSource? = null
    var expanded: Boolean = true
    var testTag: String? = null
    var contentDescription: String? = null

    override fun build(): KptFloatingActionButtonConfiguration =
        KptFloatingActionButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
            variant = variant,
            icon = icon,
            text = text,
            state = state,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = elevation,
            shape = shape,
            interactionSource = interactionSource,
            expanded = expanded,
            testTag = testTag,
            contentDescription = contentDescription,
        )
}

// DSL Function
fun kptFloatingActionButton(
    block: KptFloatingActionButtonBuilder.() -> Unit,
): KptFloatingActionButtonConfiguration {
    return KptFloatingActionButtonBuilder().apply(block).build()
}
