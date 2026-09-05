/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.demo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.demo.profile.ProfileRepository
import kpt.core.model.demo.profile.ProfileInfo

/**
 * ViewModel for the profile DEMO body (`feature_profile.combo_id: static_content`).
 *
 * Lives under `demo/` on purpose: `ProfileScreen` stays the opaque shell that renders whatever
 * `BackboneRegistry.profileBody` supplies (the WS01 shell/seam split), and only the default
 * demo body is store-backed. The two seams are orthogonal — `BackboneRegistry` decides WHICH
 * composable renders, the store decides WHERE its data comes from — so a fork can replace the
 * body wholesale, or keep it and swap the store's fetcher for a real signed-in user. This
 * mirrors `feature/home`, whose shell is likewise opaque while `demo/HomeDashboard` carries a
 * ViewModel.
 */
class ProfileViewModel(
    repository: ProfileRepository,
) : ViewModel() {

    private val stream = repository.profileStream(viewModelScope)

    val screenState: StateFlow<ScreenState<ProfileInfo>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    fun onRetry() = stream.retry()
}
