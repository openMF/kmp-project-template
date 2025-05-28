package template.core.base.designsystem.form

// ===== VALIDATION FRAMEWORK =====
interface ValidationRule<T> {
    fun validate(value: T): ValidationResult
    val errorMessage: String
}

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

class Validator<T> {
    private val rules = mutableListOf<ValidationRule<T>>()

    fun addRule(rule: ValidationRule<T>): Validator<T> {
        rules.add(rule)
        return this
    }

    fun validate(value: T): ValidationResult {
        rules.forEach { rule ->
            val result = rule.validate(value)
            if (result is ValidationResult.Invalid) {
                return result
            }
        }
        return ValidationResult.Valid
    }
}