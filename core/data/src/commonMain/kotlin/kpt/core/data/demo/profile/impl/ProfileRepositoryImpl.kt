/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.profile.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.demo.profile.ProfileRepository
import kpt.core.model.demo.profile.ProfileInfo
import org.mobilenativefoundation.store.store5.Store

/**
 * Store5-backed impl of [ProfileRepository].
 *
 * `CACHE_ONLY` — the template's profile source is local. A fork that loads a remote user
 * changes this policy (and the store's fetcher); nothing above this line moves.
 */
internal class ProfileRepositoryImpl(
    private val profileStore: Store<Unit, ProfileInfo>,
) : ProfileRepository {

    override fun profileStream(scope: CoroutineScope): ScreenDataStream<ProfileInfo> =
        profileStore.asScreenStream(
            key = Unit,
            cacheKey = "profile",
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
        )
}
