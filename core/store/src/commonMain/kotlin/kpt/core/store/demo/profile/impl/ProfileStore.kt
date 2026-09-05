/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.profile.impl

import kpt.core.base.store.infra.StoreFactory
import kpt.core.model.demo.profile.ProfileInfo
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.Store

/**
 * The read PORT for the profile store.
 *
 * The template's profile data is the app display name, which comes from `AppInfo` in
 * `core-base/ui` — a Compose-side module `core/store` does not depend on. The feature binds
 * the source, exactly as the calculator features bind their compute ports.
 */
fun interface ProfileInfoSource {
    suspend fun load(): ProfileInfo
}

/**
 * MEMORY_ONLY Store5 store for the profile screen
 * (`feature_profile.combo_id: static_content`).
 *
 * The template's profile is a static local placeholder, so this store is deliberately thin —
 * its value is the SEAM, not the caching. A real fork's profile screen loads a signed-in user
 * over the network, and because the read path is already
 * `store → repository.profileStream(scope) → ScreenState → ScreenContent`, that fork changes
 * ONE thing — this fetcher — instead of restructuring a Composable that read a constant
 * directly. Shipping the static screen without the seam is what forces that later rewrite.
 */
fun provideProfileStore(source: ProfileInfoSource): Store<Unit, ProfileInfo> =
    StoreFactory.createMemoryStore(
        fetcher = Fetcher.of { _: Unit -> source.load() },
    )
