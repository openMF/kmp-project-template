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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

sealed interface CardVariant : ComponentVariant {
    override val name: String

    data object Filled : CardVariant {
        override val name: String = "filled"
    }

    data object Elevated : CardVariant {
        override val name: String = "elevated"
    }

    data object Outlined : CardVariant {
        override val name: String = "outlined"
    }

    data class Custom(
        override val name: String,
        val renderer: @Composable (KptCardConfiguration) -> Unit,
    ) : CardVariant
}

// Card Content Types
sealed interface CardContentType {
    data object Simple : CardContentType
    data object WithHeader : CardContentType
    data object WithFooter : CardContentType
    data object WithHeaderAndFooter : CardContentType
    data object Media : CardContentType
    data object Custom : CardContentType
}

// Configuration class
@Immutable
data class KptCardConfiguration(
    override val modifier: Modifier = Modifier,
    val onClick: (() -> Unit)? = null,
    val enabled: Boolean = true,
    val variant: CardVariant = CardVariant.Filled,
    val colors: CardColors? = null,
    val elevation: CardElevation? = null,
    val border: BorderStroke? = null,
    val shape: Shape? = null,
    val interactionSource: MutableInteractionSource? = null,
    val contentPadding: PaddingValues = PaddingValues(16.dp),
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    val header: (@Composable () -> Unit)? = null,
    val footer: (@Composable () -> Unit)? = null,
    val content: @Composable ColumnScope.() -> Unit,
) : KptComponent

@ComponentDsl
class KptCardBuilder : ComponentConfigurationScope {
    override var modifier: Modifier = Modifier
    var onClick: (() -> Unit)? = null
    override var enabled: Boolean = true
    var variant: CardVariant = CardVariant.Filled
    var colors: CardColors? = null
    var elevation: CardElevation? = null
    var border: BorderStroke? = null
    var shape: Shape? = null
    var interactionSource: MutableInteractionSource? = null
    var contentPadding: PaddingValues = PaddingValues(16.dp)
    override var testTag: String? = null
    override var contentDescription: String? = null
    var header: (@Composable () -> Unit)? = null
    var footer: (@Composable () -> Unit)? = null
    var content: @Composable ColumnScope.() -> Unit = {}

    fun build(): KptCardConfiguration = KptCardConfiguration(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        variant = variant,
        colors = colors,
        elevation = elevation,
        border = border,
        shape = shape,
        interactionSource = interactionSource,
        contentPadding = contentPadding,
        testTag = testTag,
        contentDescription = contentDescription,
        header = header,
        footer = footer,
        content = content,
    )
}

// DSL Function
fun kptCard(block: KptCardBuilder.() -> Unit): KptCardConfiguration {
    return KptCardBuilder().apply(block).build()
}
