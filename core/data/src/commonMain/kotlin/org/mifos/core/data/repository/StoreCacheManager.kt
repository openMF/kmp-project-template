/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.repository

/**
 * Manages Store cache lifecycle. Call [clearAll] on logout to prevent
 * stale data from leaking across user sessions.
 */
interface StoreCacheManager {
    /** Clears all store caches (in-memory + database). Call on logout. */
    suspend fun clearAll()
}
