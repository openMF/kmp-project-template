/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.core.KptComponent
import template.core.base.designsystem.theme.KptTheme

@Composable
fun KptValidatedTextField(configuration: ValidatedTextFieldConfiguration) {
    Column {
        OutlinedTextField(
            value = configuration.fieldState.value,
            onValueChange = configuration.fieldState::updateValue,
            label = { Text(configuration.label) },
            placeholder = if (configuration.placeholder.isNotEmpty()) {
                { Text(configuration.placeholder) }
            } else {
                null
            },
            leadingIcon = configuration.leadingIcon,
            trailingIcon = if (configuration.fieldState.showError) {
                {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = KptTheme.colorScheme.error,
                    )
                }
            } else {
                null
            },
            isError = configuration.fieldState.showError,
            keyboardOptions = configuration.keyboardOptions,
            keyboardActions = configuration.keyboardActions,
            visualTransformation = configuration.visualTransformation,
            singleLine = configuration.singleLine,
            enabled = configuration.enabled,
            readOnly = configuration.readOnly,
            maxLines = configuration.maxLines,
            minLines = configuration.minLines,
            modifier = configuration.modifier
                .fillMaxWidth()
                .testTag(configuration.testTag ?: "KptValidatedTextField"),
        )

        if (configuration.fieldState.showError && configuration.fieldState.error != null) {
            Text(
                text = configuration.fieldState.error!!,
                color = KptTheme.colorScheme.error,
                style = KptTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }
}

@Composable
fun KptEmailField(
    fieldState: FormFieldState = rememberFormFieldState(
        validator = Validator<String>()
            .addRule(RequiredRule())
            .addRule(EmailRule()),
    ),
    modifier: Modifier = Modifier,
    label: String = "Email",
    placeholder: String = "Enter your email address",
) {
    val focusManager = LocalFocusManager.current

    KptValidatedTextField(
        ValidatedTextFieldConfiguration(
            fieldState = fieldState,
            label = label,
            placeholder = placeholder,
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            modifier = modifier,
        ),
    )
}

@Composable
fun KptPasswordField(
    fieldState: FormFieldState = rememberFormFieldState(
        validator = Validator<String>()
            .addRule(RequiredRule())
            .addRule(PasswordRule()),
    ),
    modifier: Modifier = Modifier,
    label: String = "Password",
    placeholder: String = "Enter your password",
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    KptValidatedTextField(
        ValidatedTextFieldConfiguration(
            fieldState = fieldState,
            label = label,
            placeholder = placeholder,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
            modifier = modifier,
        ),
    )
}

@Composable
fun KptConfirmPasswordField(
    fieldState: FormFieldState,
    originalPasswordState: FormFieldState,
    modifier: Modifier = Modifier,
    label: String = "Confirm Password",
    placeholder: String = "Confirm your password",
) {
    val confirmPasswordValidator = remember(originalPasswordState) {
        Validator<String>()
            .addRule(RequiredRule())
            .addRule(
                MatchRule(
                    otherValue = { originalPasswordState.value },
                    errorMessage = "Passwords do not match",
                ),
            )
    }

    // Update validator when original password changes
    LaunchedEffect(originalPasswordState.value) {
        fieldState.updateValue(fieldState.value) // Re-trigger validation
    }

    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    KptValidatedTextField(
        ValidatedTextFieldConfiguration(
            fieldState = fieldState,
            label = label,
            placeholder = placeholder,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
            modifier = modifier,
        ),
    )
}

@Composable
fun KptPhoneField(
    fieldState: FormFieldState = rememberFormFieldState(
        validator = Validator<String>()
            .addRule(RequiredRule())
            .addRule(PhoneRule()),
    ),
    modifier: Modifier = Modifier,
    label: String = "Phone Number",
    placeholder: String = "Enter your phone number",
) {
    val focusManager = LocalFocusManager.current

    KptValidatedTextField(
        ValidatedTextFieldConfiguration(
            fieldState = fieldState,
            label = label,
            placeholder = placeholder,
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            modifier = modifier,
        ),
    )
}

@Immutable
data class ValidatedTextFieldConfiguration(
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    override val modifier: Modifier = Modifier,
    val fieldState: FormFieldState,
    val label: String,
    val placeholder: String = "",
    val leadingIcon: @Composable (() -> Unit)? = null,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val keyboardActions: KeyboardActions = KeyboardActions.Default,
    val visualTransformation: VisualTransformation = VisualTransformation.None,
    val singleLine: Boolean = true,
    val enabled: Boolean = true,
    val readOnly: Boolean = false,
    val maxLines: Int = Int.MAX_VALUE,
    val minLines: Int = 1,
) : KptComponent
