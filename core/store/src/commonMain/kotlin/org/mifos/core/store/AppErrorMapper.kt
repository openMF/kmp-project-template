/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.store

import template.core.base.store.error.ErrorCategory
import template.core.base.store.error.categorize

/**
 * Application-level error → user-facing message mapper.
 *
 * The library default ([template.core.base.ui.defaultErrorMessage]) routes through
 * [categorize] to return generic per-category copy. This mapper layers app-specific
 * decisions on top:
 * - branded copy ("Sign in to Mifos again." instead of "Sign in again.")
 * - domain-specific error types (e.g., `InsufficientFundsException`)
 * - localized strings (wire `composeResources` here once string indirection lands)
 *
 * Forks should extend the `when` with their own domain-error branches before falling
 * back to [categorize].
 */
fun mapErrorToUserMessage(error: Throwable): String = when (categorize(error)) {
    ErrorCategory.Network -> "Can't reach the server. Check your connection and try again."
    ErrorCategory.Auth -> "Your session expired. Please sign in again."
    ErrorCategory.Server -> "Our servers are having a moment. Please try again shortly."
    ErrorCategory.Generic -> error.message ?: "Something went wrong."
    // TODO(fork): add branches above for app-specific exception types — e.g.
    //   error is InsufficientFundsException -> "Not enough balance for this transfer."
}
