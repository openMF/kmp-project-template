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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

// 1. Basic overload with just text
@Composable
fun KptButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Filled,
) {
    KptButton(
        KptButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            variant = variant,
            content = { Text(text) },
        ),
    )
}

// 2. With icon
@Composable
fun KptButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Filled,
    iconPosition: IconPosition = IconPosition.Start,
) {
    KptButton(
        KptButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            variant = variant,
            icon = icon,
            iconPosition = iconPosition,
            content = { Text(text) },
        ),
    )
}

// 3. Loading button
@Composable
fun KptLoadingButton(
    text: String,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loadingText: String? = null,
    variant: ButtonVariant = ButtonVariant.Filled,
) {
    KptButton(
        KptButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            variant = variant,
            loading = loading,
            loadingText = loadingText,
            content = { Text(text) },
        ),
    )
}

// 4. Full width button
@Composable
fun KptFullWidthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Filled,
    size: ButtonSize = ButtonSize.Large,
) {
    KptButton(
        KptButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            variant = variant,
            size = size,
            fullWidth = true,
            content = { Text(text) },
        ),
    )
}

// 5. Specific variant convenience functions
@Composable
fun KptFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    KptButton(
        KptButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            variant = ButtonVariant.Filled,
            icon = icon,
            content = { Text(text) },
        ),
    )
}

@Composable
fun KptOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    KptButton(
        KptButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            variant = ButtonVariant.Outlined,
            icon = icon,
            content = { Text(text) },
        ),
    )
}

@Composable
fun KptTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    KptButton(
        KptButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            variant = ButtonVariant.Text,
            icon = icon,
            content = { Text(text) },
        ),
    )
}

@Composable
fun KptTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    KptButton(
        KptButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            variant = ButtonVariant.Tonal,
            icon = icon,
            content = { Text(text) },
        ),
    )
}

@Composable
fun KptElevatedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    KptButton(
        KptButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            variant = ButtonVariant.Elevated,
            icon = icon,
            content = { Text(text) },
        ),
    )
}

// 6. Size-specific functions
@Composable
fun KptSmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Filled,
) {
    KptButton(
        KptButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
            variant = variant,
            size = ButtonSize.Small,
            content = { Text(text) },
        ),
    )
}

@Composable
fun KptLargeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Filled,
) {
    KptButton(
        KptButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
            variant = variant,
            size = ButtonSize.Large,
            content = { Text(text) },
        ),
    )
}

// 7. Original component compatibility (keeping old API working)
@Composable
fun KptButton(
    variant: ButtonVariant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors? = null,
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    shape: Shape? = null,
    interactionSource: MutableInteractionSource? = null,
    contentPadding: PaddingValues? = null,
    testTag: String? = null,
    contentDescription: String? = null,
    content: @Composable RowScope.() -> Unit,
) {
    KptButton(
        KptButtonConfiguration(
            onClick = onClick,
            modifier = modifier,
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
            content = content,
        ),
    )
}

// 8. Action-specific buttons
@Composable
fun KptSubmitButton(
    loading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = "Submit",
) {
    KptLoadingButton(
        text = text,
        loading = loading,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loadingText = "Submitting...",
        variant = ButtonVariant.Filled,
    )
}

@Composable
fun KptCancelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Cancel",
) {
    KptOutlinedButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun KptDeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Delete",
    confirmationRequired: Boolean = false,
) {
    var showConfirmation by remember { mutableStateOf(false) }

    KptButton(
        KptButtonConfiguration(
            onClick = {
                if (confirmationRequired) {
                    showConfirmation = true
                } else {
                    onClick()
                }
            },
            modifier = modifier,
            variant = ButtonVariant.Filled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
            ),
            content = { Text(text) },
        ),
    )

    if (showConfirmation) {
        KptConfirmationDialog(
            title = "Confirm Delete",
            text = "Are you sure you want to delete this item?",
            onDismiss = { showConfirmation = false },
            onConfirm = {
                onClick()
                showConfirmation = false
            },
        )
    }
}

@Composable
fun KptAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Add",
    iconOnly: Boolean = false,
) {
    if (iconOnly) {
        KptButton(
            KptButtonConfiguration(
                onClick = onClick,
                modifier = modifier,
                variant = ButtonVariant.Filled,
                icon = Icons.Default.Add,
                content = {},
            ),
        )
    } else {
        KptButton(
            KptButtonConfiguration(
                onClick = onClick,
                modifier = modifier,
                variant = ButtonVariant.Filled,
                icon = Icons.Default.Add,
                content = { Text(text) },
            ),
        )
    }
}

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

// Configuration class
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

// Main Enhanced Component
@Composable
fun KptButton(configuration: KptButtonConfiguration) {
    val finalModifier = if (configuration.fullWidth) {
        configuration.modifier.fillMaxWidth()
    } else {
        configuration.modifier
    }.then(
        Modifier
            .heightIn(min = configuration.size.minHeight)
            .testTag(configuration.testTag ?: KptTestTags.Button)
            .let { mod ->
                if (configuration.contentDescription != null) {
                    mod.semantics { contentDescription = configuration.contentDescription }
                } else mod
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

// DSL Function
fun kptButton(block: KptButtonBuilder.() -> Unit): KptButtonConfiguration {
    return KptButtonBuilder().apply(block).build()
}
