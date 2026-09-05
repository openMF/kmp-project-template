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
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.data.demo.profile.ProfileRepository
import kpt.core.model.demo.profile.ProfileInfo

/**
 * ViewModel for the profile DEMO body (`feature_profile.combo_id: static_content`).
 *
 * Straight pass-through of a FIXED-key stream: the screen renders it directly via
 * `ScreenContent(stream = viewModel.profile)`, which collects with lifecycle awareness and wires
 * `onRetry = stream::retry` itself. No `stateIn` and no re-exposed `StateFlow<ScreenState<…>>` —
 * that projection layer is only for a VM that must SHAPE the stream (`.mapContent { }` /
 * `.emptyIfContent { }`, as `feature/amortization` does) or that re-streams on a CHANGING key
 * (`flatMapLatest`, as the calculators do). Adding it here would duplicate state the stream
 * already holds. Same shape as `feature/watchlist` and `feature/macro`'s detail VM.
 *
 * Lives under `demo/` on purpose: `ProfileScreen` stays the opaque shell that renders whatever
 * `BackboneRegistry.profileBody` supplies (the WS01 shell/seam split), and only the default demo
 * body is store-backed. The two seams are orthogonal — `BackboneRegistry` decides WHICH composable
 * renders, the store decides WHERE its data comes from — so a fork can replace the body wholesale,
 * or keep it and swap the store's fetcher for a real signed-in user. This mirrors `feature/home`,
 * whose shell is likewise opaque while `demo/HomeDashboard` carries a ViewModel.
 */
class ProfileViewModel(
    repository: ProfileRepository,
) : ViewModel() {

    /** The repository-built stream — the screen renders it directly. */
    val profile: ScreenDataStream<ProfileInfo> = repository.profileStream(viewModelScope)
}
