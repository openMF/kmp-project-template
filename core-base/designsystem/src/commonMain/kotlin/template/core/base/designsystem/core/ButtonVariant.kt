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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.config.KptTestTags
import template.core.base.designsystem.theme.KptTheme

// Enhanced Button Variant System
sealed interface ButtonVariant {
    val name: String

    data object Filled : ButtonVariant {
        override val name: String = "filled"
    }

    data object Tonal : ButtonVariant {
        override val name: String = "tonal"
    }

    data object Elevated : ButtonVariant {
        override val name: String = "elevated"
    }

    data object Outlined : ButtonVariant {
        override val name: String = "outlined"
    }

    data object Text : ButtonVariant {
        override val name: String = "text"
    }

    data class Custom(
        override val name: String,
        val renderer: @Composable (KptButtonConfiguration) -> Unit,
    ) : ButtonVariant
}

// Button Size System
sealed interface ButtonSize {
    val contentPadding: PaddingValues
    val minHeight: Dp

    data object Small : ButtonSize {
        override val contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        override val minHeight = 32.dp
    }

    data object Medium : ButtonSize {
        override val contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        override val minHeight = 40.dp
    }

    data object Large : ButtonSize {
        override val contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        override val minHeight = 48.dp
    }

    data class Custom(
        override val contentPadding: PaddingValues,
        override val minHeight: Dp,
    ) : ButtonSize
}

// Icon Position
enum class IconPosition { Start, End, Top, Bottom }

/**
 * Configuration for KPT Button. Use this for advanced customization.
 *
 * @param onClick Click callback.
 * @param modifier Modifier for styling.
 * @param enabled Whether enabled.
 * @param variant Button variant.
 * @param size Button size.
 * @param colors Button colors.
 * @param elevation Elevation.
 * @param border Border stroke.
 * @param shape Shape.
 * @param interactionSource Interaction source.
 * @param contentPadding Padding.
 * @param testTag Test tag.
 * @param contentDescription Accessibility description.
 * @param loading Show loading spinner.
 * @param loadingText Loading text.
 * @param icon Icon.
 * @param iconPosition Icon position.
 * @param fullWidth Full width.
 * @param content Composable content.
 */
@Immutable
data class KptButtonConfiguration(
    val onClick: () -> Unit,
    val modifier: Modifier = Modifier,
    val enabled: Boolean = true,
    val variant: ButtonVariant = ButtonVariant.Filled,
    val size: ButtonSize = ButtonSize.Medium,
    val colors: ButtonColors? = null,
    val elevation: ButtonElevation? = null,
    val border: BorderStroke? = null,
    val shape: Shape? = null,
    val interactionSource: MutableInteractionSource? = null,
    val contentPadding: PaddingValues? = null,
    val testTag: String? = null,
    val contentDescription: String? = null,
    val loading: Boolean = false,
    val loadingText: String? = null,
    val icon: ImageVector? = null,
    val iconPosition: IconPosition = IconPosition.Start,
    val fullWidth: Boolean = false,
    val content: @Composable RowScope.() -> Unit,
)

// DSL Builder
@DslMarker
annotation class ButtonDsl

@ButtonDsl
class KptButtonBuilder {
    var onClick: () -> Unit = {}
    var modifier: Modifier = Modifier
    var enabled: Boolean = true
    var variant: ButtonVariant = ButtonVariant.Filled
    var size: ButtonSize = ButtonSize.Medium
    var colors: ButtonColors? = null
    var elevation: ButtonElevation? = null
    var border: BorderStroke? = null
    var shape: Shape? = null
    var interactionSource: MutableInteractionSource? = null
    var contentPadding: PaddingValues? = null
    var testTag: String? = null
    var contentDescription: String? = null
    var loading: Boolean = false
    var loadingText: String? = null
    var icon: ImageVector? = null
    var iconPosition: IconPosition = IconPosition.Start
    var fullWidth: Boolean = false
    var content: @Composable RowScope.() -> Unit = {}

    fun build(): KptButtonConfiguration = KptButtonConfiguration(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        size = size,
        colors = colors,
        elevation = elevation,
        border = border,
        shape = shape,
        interactionSource = interactionSource,
        contentPadding = contentPadding,
        testTag = testTag,
        contentDescription = contentDescription,
        loading = loading,
        loadingText = loadingText,
        icon = icon,
        iconPosition = iconPosition,
        fullWidth = fullWidth,
        content = content,
    )
}

@Composable
private fun RowScope.ButtonContent(configuration: KptButtonConfiguration) {
    if (configuration.loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = LocalContentColor.current,
        )
        if (configuration.loadingText != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(configuration.loadingText)
        }
    } else {
        when (configuration.iconPosition) {
            IconPosition.Start -> {
                configuration.icon?.let { icon ->
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                configuration.content(this)
            }

            IconPosition.End -> {
                configuration.content(this)
                configuration.icon?.let { icon ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            IconPosition.Top, IconPosition.Bottom -> {
                // Handle in Column layout for vertical icons
                configuration.content(this)
            }
        }
    }
}

/**
 * DSL builder for KPT Button configuration.
 *
 * Example:
 * ```kotlin
 * val config = kptButton {
 *   text = "Custom"
 *   onClick = { /* handle click */ }
 *   variant = ButtonVariant.Outlined
 * }
 * ```
 */
fun kptButton(block: KptButtonBuilder.() -> Unit): KptButtonConfiguration {
    return KptButtonBuilder().apply(block).build()
}

/**
 * Main KPT Button composable. Use [KptButtonConfiguration] for advanced usage.
 *
 * Example:
 * ```kotlin
 * KptButton(KptButtonConfiguration(text = "Advanced", onClick = { /* ... */ }))
 * ```
 *
 * @param configuration Button configuration.
 */
@Composable
fun KptButton(configuration: KptButtonConfiguration) {
    val finalModifier = if (configuration.fullWidth) {
        configuration.modifier.fillMaxWidth()
    } else {
        configuration.modifier
    }.then(
        Modifier
            .heightIn(min = configuration.size.minHeight)
            .testTag(configuration.testTag ?: KptTestTags.BUTTON)
            .let { mod ->
                if (configuration.contentDescription != null) {
                    mod.semantics { contentDescription = configuration.contentDescription }
                } else {
                    mod
                }
            },
    )

    val finalContentPadding = configuration.contentPadding ?: configuration.size.contentPadding
    val finalShape = configuration.shape ?: KptTheme.shapes.small

    when (configuration.variant) {
        ButtonVariant.Filled -> Button(
            onClick = configuration.onClick,
            modifier = finalModifier,
            enabled = configuration.enabled && !configuration.loading,
            colors = configuration.colors ?: ButtonDefaults.buttonColors(),
            elevation = configuration.elevation ?: ButtonDefaults.buttonElevation(),
            shape = finalShape,
            border = configuration.border,
            interactionSource = configuration.interactionSource,
            contentPadding = finalContentPadding,
        ) {
            ButtonContent(configuration)
        }

        ButtonVariant.Tonal -> FilledTonalButton(
            onClick = configuration.onClick,
            modifier = finalModifier,
            enabled = configuration.enabled && !configuration.loading,
            colors = configuration.colors ?: ButtonDefaults.filledTonalButtonColors(),
            elevation = configuration.elevation ?: ButtonDefaults.filledTonalButtonElevation(),
            shape = finalShape,
            border = configuration.border,
            interactionSource = configuration.interactionSource,
            contentPadding = finalContentPadding,
        ) {
            ButtonContent(configuration)
        }

        ButtonVariant.Elevated -> ElevatedButton(
            onClick = configuration.onClick,
            modifier = finalModifier,
            enabled = configuration.enabled && !configuration.loading,
            colors = configuration.colors ?: ButtonDefaults.elevatedButtonColors(),
            elevation = configuration.elevation ?: ButtonDefaults.elevatedButtonElevation(),
            shape = finalShape,
            border = configuration.border,
            interactionSource = configuration.interactionSource,
            contentPadding = finalContentPadding,
        ) {
            ButtonContent(configuration)
        }

        ButtonVariant.Outlined -> OutlinedButton(
            onClick = configuration.onClick,
            modifier = finalModifier,
            enabled = configuration.enabled && !configuration.loading,
            colors = configuration.colors ?: ButtonDefaults.outlinedButtonColors(),
            elevation = configuration.elevation,
            shape = finalShape,
            border = configuration.border
                ?: ButtonDefaults.outlinedButtonBorder(configuration.enabled),
            interactionSource = configuration.interactionSource,
            contentPadding = finalContentPadding,
        ) {
            ButtonContent(configuration)
        }

        ButtonVariant.Text -> TextButton(
            onClick = configuration.onClick,
            modifier = finalModifier,
            enabled = configuration.enabled && !configuration.loading,
            colors = configuration.colors ?: ButtonDefaults.textButtonColors(),
            elevation = configuration.elevation,
            shape = finalShape,
            border = configuration.border,
            interactionSource = configuration.interactionSource,
            contentPadding = finalContentPadding,
        ) {
            ButtonContent(configuration)
        }

        is ButtonVariant.Custom -> configuration.variant.renderer(configuration)
    }
}
