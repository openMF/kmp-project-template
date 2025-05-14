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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import template.core.base.designsystem.component.variant.TextFieldVariant

/**
 * A customizable and theme-aware text input field for the CMP design system.
 *
 * This composable wraps Material3's [TextField] and [OutlinedTextField] and selects the appropriate
 * style based on the [variant] parameter. It supports full customization for icons, labels,
 * placeholders, and advanced keyboard behaviors.
 *
 * @param value The current text value inside the text field.
 * @param onValueChange Callback invoked when the input text changes.
 * @param modifier Modifier applied to the text field. *(Default: [Modifier])*
 * @param enabled Whether the text field is enabled for input. *(Default: `true`)*
 * @param readOnly Whether the text field is read-only. *(Default: `false`)*
 * @param textStyle The style to apply to the input text. *(Default: [LocalTextStyle.current])*
 * @param label Optional label displayed inside the text field when it's empty and unfocused. *(Optional)*
 * @param placeholder Optional hint text displayed when the field is empty and not focused. *(Optional)*
 * @param leadingIcon Optional icon displayed at the start of the text field. *(Optional)*
 * @param trailingIcon Optional icon displayed at the end of the text field. *(Optional)*
 * @param prefix Optional composable displayed before the input text. *(Optional)*
 * @param suffix Optional composable displayed after the input text. *(Optional)*
 * @param supportingText Optional composable shown below the text field
 * (e.g., for helper or error messages). *(Optional)*
 * @param isError Whether the field is currently in an error state. *(Default: `false`)*
 * @param visualTransformation Optional transformation applied to the input (e.g., for passwords).
 * (Default: [VisualTransformation.None])*
 * @param keyboardOptions Keyboard configuration for the input field. *(Default: [KeyboardOptions.Default])*
 * @param keyboardActions Actions to handle keyboard events (e.g., "Done" or "Next").
 * (Default: [KeyboardActions.Default])*
 * @param singleLine Whether the input should be restricted to a single line. *(Default: `false`)*
 * @param maxLines Maximum number of visible lines.
 * (Default: `1` if [singleLine] is `true`, otherwise [Int.MAX_VALUE])*
 * @param minLines Minimum number of visible lines. *(Default: `1`)*
 * @param interactionSource Optional [MutableInteractionSource] to observe focus and pressed states. *(Optional)*
 * @param shape The shape of the text field container.
 * (Optional; defaults to variant’s shape via [TextFieldDefaults] or [OutlinedTextFieldDefaults])*
 * @param colors Color configuration for the text field.
 * (Optional; defaults to variant’s colors via [TextFieldDefaults] or [OutlinedTextFieldDefaults])*
 * @param variant The visual style of the text field. *(Default: [TextFieldVariant.FILLED])*
 */
@Suppress("LongParameterList")
@Composable
fun CMPTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape? = null,
    colors: TextFieldColors? = null,
    variant: TextFieldVariant = TextFieldVariant.FILLED,
) {
    when (variant) {
        TextFieldVariant.FILLED -> TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            shape = shape ?: TextFieldDefaults.shape,
            colors = colors ?: TextFieldDefaults.colors(),
        )

        TextFieldVariant.OUTLINED -> OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            shape = shape ?: OutlinedTextFieldDefaults.shape,
            colors = colors ?: OutlinedTextFieldDefaults.colors(),
        )
    }
}
