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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation

// Enhanced TextField Variant System
sealed interface TextFieldVariant : ComponentVariant {
    override val name: String

    data object Filled : TextFieldVariant {
        override val name: String = "filled"
    }

    data object Outlined : TextFieldVariant {
        override val name: String = "outlined"
    }
}

// Input Types
enum class InputType {
    Text,
    Email,
    Password,
    Phone,
    Number,
    Url,
    Search,
}

// Validation State
sealed interface ValidationState {
    data object None : ValidationState
    data object Valid : ValidationState
    data class Invalid(val message: String) : ValidationState
}

// Configuration class
@Immutable
data class KptTextFieldConfiguration(
    val value: String,
    val onValueChange: (String) -> Unit,
    override val modifier: Modifier = Modifier,
    val enabled: Boolean = true,
    val readOnly: Boolean = false,
    val variant: TextFieldVariant = TextFieldVariant.Outlined,
    val inputType: InputType = InputType.Text,
    val label: String? = null,
    val placeholder: String? = null,
    val helperText: String? = null,
    val leadingIcon: ImageVector? = null,
    val trailingIcon: ImageVector? = null,
    val onTrailingIconClick: (() -> Unit)? = null,
    val prefix: String? = null,
    val suffix: String? = null,
    val supportingText: (@Composable () -> Unit)? = null,
    val validationState: ValidationState = ValidationState.None,
    val keyboardOptions: KeyboardOptions? = null,
    val keyboardActions: KeyboardActions? = null,
    val visualTransformation: VisualTransformation = VisualTransformation.None,
    val singleLine: Boolean = true,
    val maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    val minLines: Int = 1,
    val maxLength: Int? = null,
    val interactionSource: MutableInteractionSource? = null,
    val shape: Shape? = null,
    val colors: TextFieldColors? = null,
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    val showCharacterCount: Boolean = false,
    val clearable: Boolean = false,
) : KptComponent

@ComponentDsl
class KptTextFieldBuilder : ComponentConfigurationScope {
    var value: String = ""
    var onValueChange: (String) -> Unit = {}
    override var modifier: Modifier = Modifier
    override var enabled: Boolean = true
    var readOnly: Boolean = false
    var variant: TextFieldVariant = TextFieldVariant.Outlined
    var inputType: InputType = InputType.Text
    var label: String? = null
    var placeholder: String? = null
    var helperText: String? = null
    var leadingIcon: ImageVector? = null
    var trailingIcon: ImageVector? = null
    var onTrailingIconClick: (() -> Unit)? = null
    var prefix: String? = null
    var suffix: String? = null
    var supportingText: (@Composable () -> Unit)? = null
    var validationState: ValidationState = ValidationState.None
    var keyboardOptions: KeyboardOptions? = null
    var keyboardActions: KeyboardActions? = null
    var visualTransformation: VisualTransformation = VisualTransformation.None
    var singleLine: Boolean = true
    var maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
    var minLines: Int = 1
    var maxLength: Int? = null
    var interactionSource: MutableInteractionSource? = null
    var shape: Shape? = null
    var colors: TextFieldColors? = null
    override var testTag: String? = null
    override var contentDescription: String? = null
    var showCharacterCount: Boolean = false
    var clearable: Boolean = false

    fun build(): KptTextFieldConfiguration = KptTextFieldConfiguration(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        variant = variant,
        inputType = inputType,
        label = label,
        placeholder = placeholder,
        helperText = helperText,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        validationState = validationState,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        maxLength = maxLength,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        testTag = testTag,
        contentDescription = contentDescription,
        showCharacterCount = showCharacterCount,
        clearable = clearable,
    )
}
