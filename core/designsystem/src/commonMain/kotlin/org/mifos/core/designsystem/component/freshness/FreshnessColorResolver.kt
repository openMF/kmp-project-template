/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.designsystem.component.freshness

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import org.mifos.core.designsystem.component.FreshnessState
import org.mifos.core.designsystem.theme.finance

/**
 * Maps a [FreshnessState] to its semantic [Color] from the active [MaterialTheme.finance]
 * token set. Single canonical resolver used by every freshness-aware widget
 * (FreshnessIndicator, CardFreshness, ButtonFreshness, BannerFreshness, …) so that
 * a theme tweak in [org.mifos.core.designsystem.theme.FinanceColors] propagates everywhere.
 *
 * Marked [ReadOnlyComposable] so call sites can use it from any composable scope without
 * cost (no implicit recomposition scope beyond the MaterialTheme read).
 */
@Composable
@ReadOnlyComposable
fun resolveFreshnessColor(state: FreshnessState): Color {
    val f = MaterialTheme.finance
    return when (state) {
        FreshnessState.Fresh -> f.freshnessFresh
        FreshnessState.Stale -> f.freshnessStale
        FreshnessState.Updating -> f.freshnessUpdating
        FreshnessState.Offline -> f.freshnessOffline
    }
}
