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
