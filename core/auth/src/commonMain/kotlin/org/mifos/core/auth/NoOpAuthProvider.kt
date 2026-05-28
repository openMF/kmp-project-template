/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Default [AuthProvider] binding for forks that do **not** ship authentication.
 *
 * Behaviour:
 * - [authState] is a hot [StateFlow] permanently at [AuthState.Authenticated] — every consumer
 *   of the flow sees a logged-in user and the toolkit can render every screen without a login
 *   gate. This is the "no login wall" semantic of the open-source Money Toolkit.
 * - [isConfigured] is `false` — UI can use this to hide login-aware affordances (Sign In /
 *   Sign Out buttons, profile-picture chrome, multi-account switchers).
 * - [signIn] returns [AuthResult.Success] immediately without side effects.
 * - [signOut] is a no-op.
 *
 * Forks that need authentication override the [org.mifos.core.auth.di.authModule] binding
 * in their app module with a real provider:
 *
 * ```kotlin
 * // In fork's app Koin module
 * single<AuthProvider> { MyFirebaseAuthProvider(get(), get()) }
 * ```
 *
 * The binding is read by `MyFirebaseAuthProvider` consumers (router gate, settings screen,
 * sync worker scheduler) the same way the no-op provider was — no per-consumer changes needed.
 *
 * **TODO**: UserDataRepository integration — TODO — separate plan covers consuming
 * AuthProvider.authState from UserDataRepositoryImpl. Today, UserDataRepository derives
 * "is the user logged in?" from its own field; once that switch flips, the source of truth
 * moves to this provider and UserDataRepository becomes a downstream observer.
 *
 * @see AuthProvider The contract this class fulfils
 */
class NoOpAuthProvider : AuthProvider {

    private val _authState: MutableStateFlow<AuthState> = MutableStateFlow(AuthState.Authenticated)

    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override val isConfigured: Boolean = false

    override suspend fun signIn(credentials: AuthCredentials): AuthResult = AuthResult.Success

    override suspend fun signOut() {
        // No-op — toolkit ships without a login wall, so signOut has nothing to clear.
        // Forks override the AuthProvider binding for real sign-out semantics.
    }
}
