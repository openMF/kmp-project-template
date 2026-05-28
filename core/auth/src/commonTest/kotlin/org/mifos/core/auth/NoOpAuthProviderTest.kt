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

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

class NoOpAuthProviderTest {

    @Test
    fun `isConfigured is false — UI can hide login-aware affordances`() {
        val provider = NoOpAuthProvider()
        assertFalse(provider.isConfigured)
    }

    @Test
    fun `signIn always returns Success without side effects`() = runTest {
        val provider = NoOpAuthProvider()
        val result = provider.signIn(AuthCredentials(identifier = "anyone", secret = "anything"))
        assertEquals(AuthResult.Success, result)
    }

    @Test
    fun `authState is permanently Authenticated`() = runTest {
        val provider = NoOpAuthProvider()
        // Initial value
        assertSame(AuthState.Authenticated, provider.authState.value)
        // Stable across signIn
        provider.signIn(AuthCredentials("user", "secret"))
        assertSame(AuthState.Authenticated, provider.authState.value)
        // Stable across signOut
        provider.signOut()
        assertSame(AuthState.Authenticated, provider.authState.value)
    }
}
