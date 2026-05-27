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

import kotlinx.coroutines.flow.StateFlow

/**
 * Fork-customization seam for sign-in / sign-out / session-state management.
 *
 * The toolkit ships **no built-in authentication**: every screen runs without a login wall.
 * Forks that need authentication (e.g. self-hosted banking back-office, multi-tenant SaaS)
 * override the default [NoOpAuthProvider] binding in their app-module Koin DI with a real
 * implementation (Firebase Auth, OAuth2, JWT-backed REST, etc.).
 *
 * ## Why a single seam?
 *
 * Without this contract, every fork either has to:
 * 1. Modify framework files (drift across template upgrades), or
 * 2. Bolt session state onto each ViewModel individually (duplicates the auth-gate logic
 *    across the whole app).
 *
 * With this seam, the fork:
 * - Binds **one** `single<AuthProvider> { MyFirebaseAuthProvider() }` in their app module.
 * - All consumers (router gate, profile screen, sync workers) read `authState` from Koin.
 * - Template upgrades never touch `core/auth/` — the contract is stable.
 *
 * ## Default binding
 *
 * The default [NoOpAuthProvider] always reports [AuthState.Authenticated] and
 * `isConfigured = false`. Codepaths that branch on `isConfigured` can hide login-aware
 * UI until the fork wires a real provider.
 *
 * ## Contract notes
 *
 * - [authState] is a **hot** [StateFlow] — collectors get the current value on subscribe.
 * - [signIn] **must** update [authState] before returning [AuthResult.Success], so the UI
 *   observing the flow sees the transition atomically.
 * - [signOut] is suspending so implementations can flush server-side sessions / clear caches.
 * - [isConfigured] is `true` when the fork has wired a real provider. UI can use this to
 *   gate "Sign in" buttons (e.g. hide them entirely on toolkit builds with no auth).
 *
 * @see NoOpAuthProvider Default binding when fork ships without authentication
 * @see AuthState Sealed state vocabulary
 * @see AuthResult Sealed result vocabulary
 */
interface AuthProvider {

    /**
     * Hot [StateFlow] of the current authentication state. Collectors observe transitions
     * between [AuthState.Authenticated] / [AuthState.Unauthenticated] / [AuthState.Loading].
     */
    val authState: StateFlow<AuthState>

    /**
     * Attempt to sign in with the supplied credentials.
     *
     * Implementations should:
     * 1. Emit [AuthState.Loading] (optional, but recommended for UI feedback).
     * 2. Validate credentials against the fork's identity provider.
     * 3. On success: emit [AuthState.Authenticated], return [AuthResult.Success].
     * 4. On failure: revert to [AuthState.Unauthenticated], return [AuthResult.Failure]
     *    with a categorized [AuthFailureReason].
     *
     * @param credentials Identifier + secret pair (interpretation is provider-specific —
     *   identifier may be email / username / phone; secret may be password / TOTP / OTP)
     * @return [AuthResult.Success] on completion, [AuthResult.Failure] on rejection
     */
    suspend fun signIn(credentials: AuthCredentials): AuthResult

    /**
     * End the current session. Should clear local tokens, flush server-side sessions if
     * the provider supports it, and emit [AuthState.Unauthenticated]. No-op when already
     * unauthenticated.
     */
    suspend fun signOut()

    /**
     * `true` when the fork has wired a real authentication provider. `false` for the default
     * [NoOpAuthProvider] binding (signals to UI that login-aware affordances should be hidden).
     */
    val isConfigured: Boolean
}

/**
 * Sealed vocabulary for the three authentication states a session can be in.
 *
 * Modeling as a sealed interface keeps the auth state-machine exhaustive — exhaustive `when`
 * blocks at consumers (router gate, settings screen) catch every transition at compile time.
 */
sealed interface AuthState {

    /** Session is active — user has a valid identity. */
    data object Authenticated : AuthState

    /** No active session — login required (or login-free toolkit mode). */
    data object Unauthenticated : AuthState

    /**
     * A sign-in / refresh / sign-out is in flight.
     *
     * @param reason Optional human-readable label for telemetry / UI hints
     *   (e.g. `"refreshing token"`, `"verifying OTP"`).
     */
    data class Loading(val reason: String? = null) : AuthState
}

/**
 * Generic identifier + secret pair passed to [AuthProvider.signIn].
 *
 * Interpretation is provider-specific:
 * - Firebase email/password: identifier = email, secret = password.
 * - Phone OTP: identifier = phone number, secret = OTP code.
 * - LDAP: identifier = username, secret = password.
 *
 * Provider-specific flows that don't fit the identifier+secret shape (Google Sign-In,
 * passkey, magic-link) should construct a wrapper data class in the fork and pass
 * a marshalled representation here, or expose additional methods on a fork-specific
 * subtype of [AuthProvider].
 *
 * @param identifier User-facing identity (email / username / phone)
 * @param secret Authentication secret (password / OTP / token)
 */
data class AuthCredentials(
    val identifier: String,
    val secret: String,
)

/** Sealed return type for [AuthProvider.signIn]. */
sealed interface AuthResult {

    /** Sign-in completed successfully; [AuthProvider.authState] is now [AuthState.Authenticated]. */
    data object Success : AuthResult

    /**
     * Sign-in was rejected.
     *
     * @param reason Categorized failure for UI / telemetry routing
     * @param message Optional raw message from the provider for log enrichment
     *   (do **not** display to users without translation — provider error strings are
     *   often in English / contain internal codes)
     */
    data class Failure(
        val reason: AuthFailureReason,
        val message: String? = null,
    ) : AuthResult
}

/**
 * Categorized failure taxonomy. Map provider-specific error codes / exceptions into one of
 * these at the [AuthProvider] implementation boundary — UI consumers can render
 * disposition-specific copy without parsing raw error strings.
 *
 * | Reason               | UI hint                                                     |
 * |----------------------|-------------------------------------------------------------|
 * | InvalidCredentials   | Show "wrong username or password" inline error              |
 * | NetworkError         | Show network-aware error + retry; suggest going offline     |
 * | RateLimited          | Disable submit button for N seconds + show countdown        |
 * | ServerError          | Show "service unavailable" + retry CTA                      |
 * | Unknown              | Show generic error; offer "report issue" deep-link          |
 */
enum class AuthFailureReason {
    InvalidCredentials,
    NetworkError,
    RateLimited,
    ServerError,
    Unknown,
}
