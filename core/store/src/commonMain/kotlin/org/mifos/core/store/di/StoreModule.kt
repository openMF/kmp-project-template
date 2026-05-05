/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.store.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for app-level Store wiring.
 *
 * Forks register their `Store` instances here, qualifier-bound via
 * [org.mifos.core.store.AppStoreRegistry]. Empty by default — the seam exists so apps
 * have one obvious DI module to extend without modifying `core-base/store`.
 *
 * Wire into the Koin start-up:
 * ```kotlin
 * startKoin {
 *     modules(appStoreModule, /* ...other modules */)
 * }
 * ```
 */
val appStoreModule: Module = module {
    // TODO(fork): register Store factories here, e.g.
    //   single(qualifier = AppStoreRegistry.UserProfile) { UserProfileStore(get(), get()) }
}
