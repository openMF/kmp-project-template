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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import template.core.base.designsystem.config.KptTestTags
import template.core.base.designsystem.core.InputType
import template.core.base.designsystem.core.KptTextFieldBuilder
import template.core.base.designsystem.core.KptTextFieldConfiguration
import template.core.base.designsystem.core.TextFieldVariant
import template.core.base.designsystem.core.ValidationState
import template.core.base.designsystem.theme.KptTheme

// Main Enhanced Component
@Composable
fun KptTextField(configuration: KptTextFieldConfiguration) {
    val finalModifier = configuration.modifier
        .fillMaxWidth()
        .testTag(configuration.testTag ?: KptTestTags.TextField)
        .let { mod ->
            if (configuration.contentDescription != null) {
                mod.semantics { contentDescription = configuration.contentDescription }
            } else mod
        }

    val finalKeyboardOptions = configuration.keyboardOptions
        ?: getKeyboardOptionsForType(configuration.inputType)

    val finalVisualTransformation =
        if (configuration.visualTransformation != VisualTransformation.None) {
            configuration.visualTransformation
        } else {
            getVisualTransformationForType(configuration.inputType)
        }

    val defaultLeadingIcon =
        configuration.leadingIcon ?: getLeadingIconForType(configuration.inputType)
    val isError = configuration.validationState is ValidationState.Invalid

    Column {
        when (configuration.variant) {
            TextFieldVariant.Filled -> TextField(
                value = configuration.value,
                onValueChange = { newValue ->
                    val finalValue = if (configuration.maxLength != null) {
                        newValue.take(configuration.maxLength)
                    } else newValue
                    configuration.onValueChange(finalValue)
                },
                modifier = finalModifier,
                enabled = configuration.enabled,
                readOnly = configuration.readOnly,
                label = configuration.label?.let { { Text(it) } },
                placeholder = configuration.placeholder?.let { { Text(it) } },
                leadingIcon = defaultLeadingIcon?.let { { Icon(it, contentDescription = null) } },
                trailingIcon = {
                    TrailingIconContent(configuration)
                },
                prefix = configuration.prefix?.let { { Text(it) } },
                suffix = configuration.suffix?.let { { Text(it) } },
                supportingText = {
                    SupportingTextContent(configuration)
                },
                isError = isError,
                visualTransformation = finalVisualTransformation,
                keyboardOptions = finalKeyboardOptions,
                keyboardActions = configuration.keyboardActions ?: KeyboardActions.Default,
                singleLine = configuration.singleLine,
                maxLines = configuration.maxLines,
                minLines = configuration.minLines,
                interactionSource = configuration.interactionSource,
                shape = configuration.shape ?: KptTheme.shapes.small,
                colors = configuration.colors ?: TextFieldDefaults.colors(),
            )

            TextFieldVariant.Outlined -> OutlinedTextField(
                value = configuration.value,
                onValueChange = { newValue ->
                    val finalValue = if (configuration.maxLength != null) {
                        newValue.take(configuration.maxLength)
                    } else newValue
                    configuration.onValueChange(finalValue)
                },
                modifier = finalModifier,
                enabled = configuration.enabled,
                readOnly = configuration.readOnly,
                label = configuration.label?.let { { Text(it) } },
                placeholder = configuration.placeholder?.let { { Text(it) } },
                leadingIcon = defaultLeadingIcon?.let { { Icon(it, contentDescription = null) } },
                trailingIcon = {
                    TrailingIconContent(configuration)
                },
                prefix = configuration.prefix?.let { { Text(it) } },
                suffix = configuration.suffix?.let { { Text(it) } },
                supportingText = {
                    SupportingTextContent(configuration)
                },
                isError = isError,
                visualTransformation = finalVisualTransformation,
                keyboardOptions = finalKeyboardOptions,
                keyboardActions = configuration.keyboardActions ?: KeyboardActions.Default,
                singleLine = configuration.singleLine,
                maxLines = configuration.maxLines,
                minLines = configuration.minLines,
                interactionSource = configuration.interactionSource,
                shape = configuration.shape ?: KptTheme.shapes.small,
                colors = configuration.colors ?: OutlinedTextFieldDefaults.colors(),
            )
        }
    }
}

@Composable
private fun TrailingIconContent(configuration: KptTextFieldConfiguration) {
    Row {
        if (configuration.clearable && configuration.value.isNotEmpty()) {
            IconButton(onClick = { configuration.onValueChange("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
        }

        configuration.trailingIcon?.let { icon ->
            if (configuration.onTrailingIconClick != null) {
                IconButton(onClick = configuration.onTrailingIconClick) {
                    Icon(icon, contentDescription = null)
                }
            } else {
                Icon(icon, contentDescription = null)
            }
        }

        when (configuration.validationState) {
            is ValidationState.Valid -> Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Valid",
                tint = Color.Green,
            )

            is ValidationState.Invalid -> Icon(
                Icons.Default.Error,
                contentDescription = "Error",
                tint = KptTheme.colorScheme.error,
            )

            ValidationState.None -> {}
        }
    }
}

@Composable
private fun SupportingTextContent(configuration: KptTextFieldConfiguration) {
    Column {
        when (configuration.validationState) {
            is ValidationState.Invalid -> {
                Text(
                    text = configuration.validationState.message,
                    color = KptTheme.colorScheme.error,
                    style = KptTheme.typography.bodySmall,
                )
            }

            else -> {
                configuration.helperText?.let { text ->
                    Text(
                        text = text,
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (configuration.showCharacterCount && configuration.maxLength != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "${configuration.value.length}/${configuration.maxLength}",
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        configuration.supportingText?.invoke()
    }
}

// DSL Function
fun kptTextField(block: KptTextFieldBuilder.() -> Unit): KptTextFieldConfiguration {
    return KptTextFieldBuilder().apply(block).build()
}

// Helper functions
private fun getKeyboardOptionsForType(type: InputType): KeyboardOptions {
    return when (type) {
        InputType.Text -> KeyboardOptions(imeAction = ImeAction.Next)
        InputType.Email -> KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        )

        InputType.Password -> KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        )

        InputType.Phone -> KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
        )

        InputType.Number -> KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
        )

        InputType.Url -> KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
        )

        InputType.Search -> KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        )
    }
}

private fun getVisualTransformationForType(type: InputType): VisualTransformation {
    return when (type) {
        InputType.Password -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }
}

private fun getLeadingIconForType(type: InputType): ImageVector? {
    return when (type) {
        InputType.Email -> Icons.Default.Email
        InputType.Password -> Icons.Default.Lock
        InputType.Phone -> Icons.Default.Phone
        InputType.Search -> Icons.Default.Search
        InputType.Url -> Icons.Default.Link
        else -> null
    }
}

// 1. Simple text field
@Composable
fun KptTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    variant: TextFieldVariant = TextFieldVariant.Outlined,
    enabled: Boolean = true,
) {
    KptTextField(
        KptTextFieldConfiguration(
            value = value,
            onValueChange = onValueChange,
            label = label,
            modifier = modifier,
            variant = variant,
            enabled = enabled,
        ),
    )
}

// 2. With placeholder
@Composable
fun KptTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    variant: TextFieldVariant = TextFieldVariant.Outlined,
) {
    KptTextField(
        KptTextFieldConfiguration(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            modifier = modifier,
            variant = variant,
        ),
    )
}

// 3. Email field
@Composable
fun KptEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Email",
    placeholder: String = "Enter your email",
    validationState: ValidationState = ValidationState.None,
    variant: TextFieldVariant = TextFieldVariant.Outlined,
) {
    val focusManager = LocalFocusManager.current

    KptTextField(
        KptTextFieldConfiguration(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            label = label,
            placeholder = placeholder,
            inputType = InputType.Email,
            validationState = validationState,
            variant = variant,
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        ),
    )
}

// 4. Password field
@Composable
fun KptPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    placeholder: String = "Enter your password",
    validationState: ValidationState = ValidationState.None,
    variant: TextFieldVariant = TextFieldVariant.Outlined,
    showVisibilityToggle: Boolean = true,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    KptTextField(
        KptTextFieldConfiguration(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            label = label,
            placeholder = placeholder,
            inputType = InputType.Password,
            validationState = validationState,
            variant = variant,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = if (showVisibilityToggle) {
                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
            } else null,
            onTrailingIconClick = if (showVisibilityToggle) {
                { passwordVisible = !passwordVisible }
            } else null,
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
        ),
    )
}

// 5. Phone field
@Composable
fun KptPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Phone Number",
    placeholder: String = "Enter your phone number",
    validationState: ValidationState = ValidationState.None,
    variant: TextFieldVariant = TextFieldVariant.Outlined,
) {
    val focusManager = LocalFocusManager.current

    KptTextField(
        KptTextFieldConfiguration(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            label = label,
            placeholder = placeholder,
            inputType = InputType.Phone,
            validationState = validationState,
            variant = variant,
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        ),
    )
}

// 6. Search field
@Composable
fun KptSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    variant: TextFieldVariant = TextFieldVariant.Outlined,
) {
    KptTextField(
        KptTextFieldConfiguration(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            placeholder = placeholder,
            inputType = InputType.Search,
            variant = variant,
            clearable = true,
            keyboardActions = KeyboardActions(
                onSearch = { onSearch(value) },
            ),
        ),
    )
}

// 7. Number field
@Composable
fun KptNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Number",
    placeholder: String = "Enter a number",
    validationState: ValidationState = ValidationState.None,
    variant: TextFieldVariant = TextFieldVariant.Outlined,
) {
    KptTextField(
        KptTextFieldConfiguration(
            value = value,
            onValueChange = { newValue ->
                // Only allow numbers
                if (newValue.all { it.isDigit() || it == '.' || it == '-' }) {
                    onValueChange(newValue)
                }
            },
            modifier = modifier,
            label = label,
            placeholder = placeholder,
            inputType = InputType.Number,
            validationState = validationState,
            variant = variant,
        ),
    )
}

// 8. Multi-line text field
@Composable
fun KptMultiLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Message",
    placeholder: String = "Enter your message",
    minLines: Int = 3,
    maxLines: Int = 6,
    maxLength: Int? = null,
    variant: TextFieldVariant = TextFieldVariant.Outlined,
) {
    KptTextField(
        KptTextFieldConfiguration(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            label = label,
            placeholder = placeholder,
            singleLine = false,
            minLines = minLines,
            maxLines = maxLines,
            maxLength = maxLength,
            showCharacterCount = maxLength != null,
            variant = variant,
        ),
    )
}

// 9. URL field
@Composable
fun KptUrlField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "URL",
    placeholder: String = "https://example.com",
    validationState: ValidationState = ValidationState.None,
    variant: TextFieldVariant = TextFieldVariant.Outlined,
) {
    KptTextField(
        KptTextFieldConfiguration(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            label = label,
            placeholder = placeholder,
            inputType = InputType.Url,
            validationState = validationState,
            variant = variant,
            prefix = "https://",
        ),
    )
}