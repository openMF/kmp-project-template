/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.security.di

import org.koin.core.module.Module
import org.koin.dsl.module
import template.core.base.security.BiometricAuthenticator
import template.core.base.security.FailedAttemptTracker
import template.core.base.security.SecureWiper
import template.core.base.security.SecurityConfig
import template.core.base.security.SecurityPolicy
import template.core.base.security.SessionManager
import template.core.base.security.TamperDetector

/**
 * Creates the security Koin module. Receives [SecurityConfig] as a function
 * parameter to avoid init-order issues — each app module passes its own config.
 */
fun securityModule(config: SecurityConfig) = module {
    includes(platformSecurityModule)

    single { config }
    single { SecurityPolicy.default() }
    single { TamperDetector() }
    single { SecureWiper() }
    single { BiometricAuthenticator() }
    single { FailedAttemptTracker(get(), get()) }
    single { SessionManager(get()) }
}

expect val platformSecurityModule: Module
