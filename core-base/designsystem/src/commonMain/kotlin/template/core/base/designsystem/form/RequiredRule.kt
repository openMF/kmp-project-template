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

class RequiredRule(
    override val errorMessage: String = "This field is required",
) : ValidationRule<String> {
    override fun validate(value: String): ValidationResult {
        return if (value.isBlank()) {
            ValidationResult.Invalid(errorMessage)
        } else {
            ValidationResult.Valid
        }
    }
}

class MinLengthRule(
    private val minLength: Int,
    override val errorMessage: String = "Minimum $minLength characters required",
) : ValidationRule<String> {
    override fun validate(value: String): ValidationResult {
        return if (value.length < minLength) {
            ValidationResult.Invalid(errorMessage)
        } else {
            ValidationResult.Valid
        }
    }
}

class MaxLengthRule(
    private val maxLength: Int,
    override val errorMessage: String = "Maximum $maxLength characters allowed",
) : ValidationRule<String> {
    override fun validate(value: String): ValidationResult {
        return if (value.length > maxLength) {
            ValidationResult.Invalid(errorMessage)
        } else {
            ValidationResult.Valid
        }
    }
}

class EmailRule(
    override val errorMessage: String = "Please enter a valid email address",
) : ValidationRule<String> {
    private val emailPattern = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
    )

    override fun validate(value: String): ValidationResult {
        return if (value.isBlank() || emailPattern.matches(value)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errorMessage)
        }
    }
}

class PhoneRule(
    override val errorMessage: String = "Please enter a valid phone number",
) : ValidationRule<String> {
    private val phonePattern = Regex(
        "^[+]?[1-9]?[0-9]{7,15}$",
    )

    override fun validate(value: String): ValidationResult {
        return if (value.isBlank() || phonePattern.matches(value.replace("\\s".toRegex(), ""))) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errorMessage)
        }
    }
}

class PasswordRule(
    private val minLength: Int = 8,
    private val requireUppercase: Boolean = true,
    private val requireLowercase: Boolean = true,
    private val requireNumbers: Boolean = true,
    private val requireSpecialChars: Boolean = true,
    override val errorMessage: String = buildPasswordErrorMessage(
        minLength,
        requireUppercase,
        requireLowercase,
        requireNumbers,
        requireSpecialChars,
    ),
) : ValidationRule<String> {
    override fun validate(value: String): ValidationResult {
        if (value.length < minLength) {
            return ValidationResult.Invalid("Password must be at least $minLength characters")
        }

        if (requireUppercase && !value.any { it.isUpperCase() }) {
            return ValidationResult.Invalid("Password must contain at least one uppercase letter")
        }

        if (requireLowercase && !value.any { it.isLowerCase() }) {
            return ValidationResult.Invalid("Password must contain at least one lowercase letter")
        }

        if (requireNumbers && !value.any { it.isDigit() }) {
            return ValidationResult.Invalid("Password must contain at least one number")
        }

        if (requireSpecialChars && !value.any { !it.isLetterOrDigit() }) {
            return ValidationResult.Invalid("Password must contain at least one special character")
        }

        return ValidationResult.Valid
    }

    companion object {
        private fun buildPasswordErrorMessage(
            minLength: Int,
            requireUppercase: Boolean,
            requireLowercase: Boolean,
            requireNumbers: Boolean,
            requireSpecialChars: Boolean,
        ): String {
            val requirements = mutableListOf<String>()
            requirements.add("at least $minLength characters")
            if (requireUppercase) requirements.add("one uppercase letter")
            if (requireLowercase) requirements.add("one lowercase letter")
            if (requireNumbers) requirements.add("one number")
            if (requireSpecialChars) requirements.add("one special character")

            return "Password must contain ${requirements.joinToString(", ")}"
        }
    }
}

class MatchRule(
    private val otherValue: () -> String,
    override val errorMessage: String = "Values do not match",
) : ValidationRule<String> {
    override fun validate(value: String): ValidationResult {
        return if (value == otherValue()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errorMessage)
        }
    }
}
