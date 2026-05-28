/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.auth.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.mifos.core.auth.AuthProvider
import org.mifos.core.auth.NoOpAuthProvider

/**
 * Koin module exposing the default [AuthProvider] binding (no-op).
 *
 * Forks override this binding in their app-level Koin module:
 *
 * ```kotlin
 * // In fork's KoinAppDeclaration {} or app-module module {}
 * single<AuthProvider> { MyFirebaseAuthProvider(get(), get()) }
 * ```
 *
 * Koin's last-binding-wins resolution means the fork's `single<AuthProvider>` overrides
 * the framework default — no framework files need editing.
 *
 * If the fork wants the override to be explicit at module-registration time, register
 * it with `override = true` (Koin 4 — `org.koin.dsl.module.overrideExisting` true on
 * the new module).
 */
val authModule = module {
    singleOf(::NoOpAuthProvider) bind AuthProvider::class
}
