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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.ui.tooling.preview.Preview
import template.core.base.designsystem.core.ButtonSize
import template.core.base.designsystem.core.ButtonVariant
import template.core.base.designsystem.core.IconPosition
import template.core.base.designsystem.core.KptButton
import template.core.base.designsystem.core.KptButtonConfiguration

/**
 * KPT Design System Button composable. Supports multiple variants, icons, loading state, and full-width options.
 *
 * Basic usage:
 * ```kotlin
 * KptButton(text = "Submit", onClick = { /* handle click */ })
 * ```
 *
 * With icon:
 * ```kotlin
 * KptButton(text = "Add", icon = Icons.Default.Add, onClick = { /* handle click */ })
 * ```
 *
 * @param text The text label of the button.
 * @param onClick Callback invoked when the button is clicked.
 * @param modifier Modifier for styling.
 * @param enabled Whether the button is enabled.
 * @param variant The button style variant.
 */
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

/**
 * KPT Button with icon support.
 *
 * Example:
 * ```kotlin
 * KptButton(
 *   text = "Add",
 *   icon = Icons.Default.Add,
 *   onClick = { /* handle click */ },
 *   iconPosition = IconPosition.End
 * )
 * ```
 *
 * @param text The text label.
 * @param icon The icon to display.
 * @param onClick Click callback.
 * @param modifier Modifier for styling.
 * @param enabled Whether enabled.
 * @param variant Button style.
 * @param iconPosition Icon placement.
 */
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

/**
 * Button with loading indicator.
 *
 * Example:
 * ```kotlin
 * KptLoadingButton(
 *   text = "Save",
 *   loading = true,
 *   onClick = { /* handle click */ }
 * )
 * ```
 *
 * @param text The text label.
 * @param loading Whether to show loading spinner.
 * @param onClick Click callback.
 * @param modifier Modifier for styling.
 * @param enabled Whether enabled.
 * @param loadingText Optional text to show while loading.
 * @param variant Button style.
 */
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

/**
 * Full-width button.
 *
 * Example:
 * ```kotlin
 * KptFullWidthButton(text = "Continue", onClick = { /* handle click */ })
 * ```
 *
 * @param text The text label.
 * @param onClick Click callback.
 * @param modifier Modifier for styling.
 * @param enabled Whether enabled.
 * @param variant Button style.
 * @param size Button size.
 */
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

/**
 * Filled button variant shortcut.
 *
 * Example:
 * ```kotlin
 * KptFilledButton(text = "Primary", onClick = { /* handle click */ })
 * ```
 */
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

/**
 * Outlined button variant shortcut.
 *
 * Example:
 * ```kotlin
 * KptOutlinedButton(text = "Secondary", onClick = { /* handle click */ })
 * ```
 */
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

/**
 * Text button variant shortcut.
 *
 * Example:
 * ```kotlin
 * KptTextButton(text = "Text", onClick = { /* handle click */ })
 * ```
 */
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

/**
 * Tonal button variant shortcut.
 *
 * Example:
 * ```kotlin
 * KptTonalButton(text = "Tonal", onClick = { /* handle click */ })
 * ```
 */
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

/**
 * Elevated button variant shortcut.
 *
 * Example:
 * ```kotlin
 * KptElevatedButton(text = "Elevated", onClick = { /* handle click */ })
 * ```
 */
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

/**
 * Small button shortcut.
 *
 * Example:
 * ```kotlin
 * KptSmallButton(text = "Small", onClick = { /* handle click */ })
 * ```
 */
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

/**
 * Large button shortcut.
 *
 * Example:
 * ```kotlin
 * KptLargeButton(text = "Large", onClick = { /* handle click */ })
 * ```
 */
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

/**
 * Advanced KPT Button with full configuration.
 *
 * @param variant Button variant.
 * @param onClick Click callback.
 * @param modifier Modifier for styling.
 * @param enabled Whether enabled.
 * @param colors Button colors.
 * @param elevation Elevation.
 * @param border Border stroke.
 * @param shape Shape.
 * @param interactionSource Interaction source.
 * @param contentPadding Padding.
 * @param testTag Test tag.
 * @param contentDescription Accessibility description.
 * @param content Composable content.
 */
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

/**
 * Submit button with loading state.
 *
 * Example:
 * ```kotlin
 * KptSubmitButton(loading = true, onClick = { /* handle submit */ })
 * ```
 */
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

/**
 * Cancel button shortcut.
 *
 * Example:
 * ```kotlin
 * KptCancelButton(onClick = { /* handle cancel */ })
 * ```
 */
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

/**
 * Delete button with optional confirmation dialog.
 *
 * Example:
 * ```kotlin
 * KptDeleteButton(onClick = { /* handle delete */ }, confirmationRequired = true)
 * ```
 */
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
            message = "Are you sure you want to delete this item?",
            onDismiss = { showConfirmation = false },
            onConfirm = {
                onClick()
                showConfirmation = false
            },
        )
    }
}

/**
 * Add button with icon or text+icon.
 *
 * Example:
 * ```kotlin
 * KptAddButton(onClick = { /* handle add */ }, iconOnly = true)
 * ```
 */
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

// region: Previews
@Preview
@Composable
fun PreviewKptButton() {
    Surface { KptButton(text = "Submit", onClick = {}) }
}

@Preview
@Composable
fun PreviewKptButtonWithIcon() {
    Surface { KptButton(text = "Add", icon = Icons.Default.Add, onClick = {}) }
}

@Preview
@Composable
fun PreviewKptLoadingButton() {
    Surface { KptLoadingButton(text = "Save", loading = true, onClick = {}) }
}

@Preview
@Composable
fun PreviewKptFullWidthButton() {
    Surface { KptFullWidthButton(text = "Continue", onClick = {}) }
}

@Preview
@Composable
fun PreviewKptFilledButton() {
    Surface { KptFilledButton(text = "Primary", onClick = {}) }
}

@Preview
@Composable
fun PreviewKptOutlinedButton() {
    Surface { KptOutlinedButton(text = "Secondary", onClick = {}) }
}

@Preview
@Composable
fun PreviewKptTextButton() {
    Surface { KptTextButton(text = "Text", onClick = {}) }
}

@Preview
@Composable
fun PreviewKptTonalButton() {
    Surface { KptTonalButton(text = "Tonal", onClick = {}) }
}

@Preview
@Composable
fun PreviewKptElevatedButton() {
    Surface { KptElevatedButton(text = "Elevated", onClick = {}) }
}

@Preview
@Composable
fun PreviewKptSmallButton() {
    Surface { KptSmallButton(text = "Small", onClick = {}) }
}

@Preview
@Composable
fun PreviewKptLargeButton() {
    Surface { KptLargeButton(text = "Large", onClick = {}) }
}

@Preview
@Composable
fun PreviewKptButtonAdvanced() {
    Surface {
        KptButton(
            variant = ButtonVariant.Filled,
            onClick = {},
            content = { Text("Advanced") },
        )
    }
}

@Preview
@Composable
fun PreviewKptSubmitButton() {
    Surface { KptSubmitButton(loading = true, onClick = {}) }
}

@Preview
@Composable
fun PreviewKptCancelButton() {
    Surface { KptCancelButton(onClick = {}) }
}

@Preview
@Composable
fun PreviewKptDeleteButton() {
    Surface { KptDeleteButton(onClick = {}, confirmationRequired = false) }
}

@Preview
@Composable
fun PreviewKptDeleteButtonWithConfirmation() {
    Surface { KptDeleteButton(onClick = {}, confirmationRequired = true) }
}

@Preview
@Composable
fun PreviewKptAddButtonIconOnly() {
    Surface { KptAddButton(onClick = {}, iconOnly = true) }
}

@Preview
@Composable
fun PreviewKptAddButtonTextAndIcon() {
    Surface { KptAddButton(onClick = {}, iconOnly = false) }
}
// endregion
