package template.core.base.designsystem.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class FormFieldState(
    initialValue: String = "",
    private val validator: Validator<String> = Validator()
) {
    var value by mutableStateOf(initialValue)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var isValid by mutableStateOf(true)
        private set

    var hasBeenFocused by mutableStateOf(false)
        private set

    val showError: Boolean
        get() = hasBeenFocused && !isValid

    fun updateValue(newValue: String) {
        value = newValue
        validate()
    }

    fun onFocusChanged(focused: Boolean) {
        if (!focused && !hasBeenFocused) {
            hasBeenFocused = true
        }
        if (!focused) {
            validate()
        }
    }

    private fun validate() {
        val result = validator.validate(value)
        when (result) {
            is ValidationResult.Valid -> {
                isValid = true
                error = null
            }
            is ValidationResult.Invalid -> {
                isValid = false
                error = result.message
            }
        }
    }

    fun reset() {
        value = ""
        error = null
        isValid = true
        hasBeenFocused = false
    }
}

@Composable
fun rememberFormFieldState(
    initialValue: String = "",
    validator: Validator<String> = Validator()
): FormFieldState {
    return remember { FormFieldState(initialValue, validator) }
}