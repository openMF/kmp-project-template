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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import template.core.base.designsystem.component.variant.TextFieldVariant
import template.core.base.designsystem.config.KptTestTags

/**
 * A customizable and theme-aware text input field for the KPT design
 * system.
 *
 * This composable wraps Material3's [TextField] and [OutlinedTextField]
 * and selects the appropriate style based on the [variant] parameter.
 * It supports full customization for icons, labels, placeholders, and
 * advanced keyboard behaviors.
 *
 * @param value The current text value inside the text field.
 * @param onValueChange Callback invoked when the input text changes.
 * @param modifier Modifier applied to the text field. *(Default:
 *    [Modifier])*
 * @param enabled Whether the text field is enabled for input. *(Default:
 *    `true`)*
 * @param readOnly Whether the text field is read-only. *(Default:
 *    `false`)*
 * @param textStyle The style to apply to the input text. *(Default:
 *    [LocalTextStyle.current])*
 * @param label Optional label displayed inside the text field when it's
 *    empty and unfocused. *(Optional)*
 * @param placeholder Optional hint text displayed when the field is empty
 *    and not focused. *(Optional)*
 * @param leadingIcon Optional icon displayed at the start of the text
 *    field. *(Optional)*
 * @param trailingIcon Optional icon displayed at the end of the text
 *    field. *(Optional)*
 * @param prefix Optional composable displayed before the input text.
 *    *(Optional)*
 * @param suffix Optional composable displayed after the input text.
 *    *(Optional)*
 * @param supportingText Optional composable shown below the text field
 *    (e.g., for helper or error messages). *(Optional)*
 * @param isError Whether the field is currently in an error state.
 *    *(Default: `false`)*
 * @param visualTransformation Optional transformation applied to the input
 *    (e.g., for passwords). (Default: [VisualTransformation.None])*
 * @param keyboardOptions Keyboard configuration for the input field.
 *    *(Default: [KeyboardOptions.Default])*
 * @param keyboardActions Actions to handle keyboard events (e.g., "Done"
 *    or "Next"). (Default: [KeyboardActions.Default])*
 * @param singleLine Whether the input should be restricted to a single
 *    line. *(Default: `false`)*
 * @param maxLines Maximum number of visible lines. (Default: `1` if
 *    [singleLine] is `true`, otherwise [Int.MAX_VALUE])*
 * @param minLines Minimum number of visible lines. *(Default: `1`)*
 * @param interactionSource Optional [MutableInteractionSource] to observe
 *    focus and pressed states. *(Optional)*
 * @param shape The shape of the text field container. (Optional; defaults
 *    to design token or variant's shape via [TextFieldDefaults] or
 *    [OutlinedTextFieldDefaults])*
 * @param colors Color configuration for the text field. (Optional;
 *    defaults to variant's colors via [TextFieldDefaults] or
 *    [OutlinedTextFieldDefaults])*
 * @param variant The visual style of the text field. *(Default:
 *    [TextFieldVariant.FILLED])*
 * @param testTag Tag for UI testing. *(Optional)*
 * @param contentDescription Content description for accessibility.
 *    *(Optional)*
 */
@Suppress("LongParameterList")
@Composable
fun KptTextField(
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
    testTag: String? = null,
    contentDescription: String? = null,
) {
    val textFieldModifier = modifier
        .then(Modifier.testTag(testTag ?: KptTestTags.TextField))
        .then(
            if (contentDescription != null) Modifier.semantics {
                this.contentDescription = contentDescription
            } else Modifier,
        )

    when (variant) {
        TextFieldVariant.FILLED -> TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = textFieldModifier,
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
            modifier = textFieldModifier,
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
            colors = colors ?: OutlinedTextFieldDefaults.colors(),
        )
    }
}

@Composable
@Suppress("LongParameterList")
fun KptOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showClearIcon: Boolean = true,
    readOnly: Boolean = false,
    clearIcon: ImageVector = Icons.Default.Close,
    isError: Boolean = false,
    errorText: String? = null,
    onClickClearIcon: () -> Unit = { onValueChange("") },
    textStyle: TextStyle = LocalTextStyle.current,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    val showIcon by rememberUpdatedState(value.isNotEmpty())

    OutlinedTextField(
        value = value,
        label = { Text(text = label) },
        onValueChange = onValueChange,
        textStyle = textStyle,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        leadingIcon = leadingIcon,
        isError = isError,
        trailingIcon = @Composable {
            AnimatedContent(
                targetState = showClearIcon && isFocused && showIcon,
            ) {
                if (it) {
                    ClearIconButton(
                        showClearIcon = true,
                        clearIcon = clearIcon,
                        onClickClearIcon = onClickClearIcon,
                    )
                } else {
                    trailingIcon?.invoke()
                }
            }
        },
        supportingText = errorText?.let {
            {
                Text(
                    modifier = Modifier.testTag("errorTag"),
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
}

@Composable
private fun ClearIconButton(
    showClearIcon: Boolean,
    clearIcon: ImageVector,
    onClickClearIcon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = showClearIcon,
        modifier = modifier,
    ) {
        IconButton(
            onClick = onClickClearIcon,
            modifier = Modifier.semantics {
                contentDescription = "clearIcon"
            },
        ) {
            Icon(
                imageVector = clearIcon,
                contentDescription = "trailingIcon",
            )
        }
    }
}
