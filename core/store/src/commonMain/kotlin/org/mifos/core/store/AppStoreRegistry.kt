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

import template.core.base.store.infra.StoreRegistry

/**
 * Application-level [StoreRegistry] — the single named-qualifier registry for every
 * `org.mobilenativefoundation.store.store5.Store` the app exposes.
 *
 * Forks of `kmp-project-template` should add their domain stores here as `val`s, e.g.:
 *
 * ```kotlin
 * object AppStoreRegistry : StoreRegistry() {
 *     val UserProfile = store("userProfile")
 *     val Transactions = store("transactions")
 * }
 * ```
 *
 * Then reference the qualifier from Koin DI:
 *
 * ```kotlin
 * single<Store<UserId, UserProfile>>(qualifier = AppStoreRegistry.UserProfile) { ... }
 * ```
 *
 * Centralizing here gives a one-place audit of every Store the app owns and prevents
 * qualifier-name collisions across feature modules.
 */
object AppStoreRegistry : StoreRegistry() {
    // TODO(fork): add app-specific stores here as `val Foo = store("foo")`.
}
