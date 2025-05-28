package template.core.base.designsystem.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.theme.KptTheme

@Stable
class FormState {
    private val fields = mutableStateMapOf<String, FormFieldState>()

    val isValid: Boolean
        get() = fields.values.all { it.isValid }

    val hasErrors: Boolean
        get() = fields.values.any { !it.isValid }

    fun addField(key: String, field: FormFieldState) {
        fields[key] = field
    }

    fun getField(key: String): FormFieldState? = fields[key]

    fun validate(): Boolean {
        fields.values.forEach { field ->
            field.onFocusChanged(false) // Trigger validation
        }
        return isValid
    }

    fun reset() {
        fields.values.forEach { it.reset() }
    }

    fun getValues(): Map<String, String> {
        return fields.mapValues { it.value.value }
    }
}

@Composable
fun rememberFormState(): FormState {
    return remember { FormState() }
}

// ===== COMPLETE FORM EXAMPLE =====
@Composable
fun RegistrationFormExample() {
    val formState = rememberFormState()

    val emailField = rememberFormFieldState(
        validator = Validator<String>()
            .addRule(RequiredRule())
            .addRule(EmailRule())
    )

    val passwordField = rememberFormFieldState(
        validator = Validator<String>()
            .addRule(RequiredRule())
            .addRule(PasswordRule())
    )

    val confirmPasswordField = rememberFormFieldState(
        validator = Validator<String>()
            .addRule(RequiredRule())
            .addRule(MatchRule(
                otherValue = { passwordField.value },
                errorMessage = "Passwords do not match"
            ))
    )

    val phoneField = rememberFormFieldState(
        validator = Validator<String>()
            .addRule(RequiredRule())
            .addRule(PhoneRule())
    )

    // Register fields with form state
    LaunchedEffect(Unit) {
        formState.addField("email", emailField)
        formState.addField("password", passwordField)
        formState.addField("confirmPassword", confirmPasswordField)
        formState.addField("phone", phoneField)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Create Account",
            style = KptTheme.typography.headlineMedium
        )

        KptEmailField(fieldState = emailField)

        KptPhoneField(fieldState = phoneField)

        KptPasswordField(fieldState = passwordField)

        KptConfirmPasswordField(
            fieldState = confirmPasswordField,
            originalPasswordState = passwordField
        )

        FormValidationSummary(formState = formState)

        Button(
            onClick = {
                if (formState.validate()) {
                    // Form is valid, proceed with registration
                    val values = formState.getValues()
                    println("Registration data: $values")
                }
            },
            enabled = formState.isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Account")
        }

        TextButton(
            onClick = { formState.reset() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear Form")
        }
    }
}