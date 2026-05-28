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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mifos.core.designsystem.component.FreshnessState
import template.core.base.store.screen.DataFreshness
import template.core.base.ui.screen.DataFreshnessIndicator
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Project-themed wrapper around the framework's [DataFreshnessIndicator]. Adapts the
 * project's [FreshnessState] enum (which carries [FreshnessState.Offline] in addition
 * to FRESH/STALE/UPDATING) onto the framework's [DataFreshness] vocabulary
 * ([FreshnessState.Offline] collapses to [DataFreshness.STALE] — the banner copy
 * already reads "Offline · …" in the stale path so the user-visible string is correct).
 *
 * Use this on app-shell-level screens that want the full collapsible "Offline / Refreshing
 * / Just reconnected" banner instead of the inline dot+label [FreshnessIndicator].
 *
 * @param state The project's freshness state. FRESH renders nothing; STALE/Offline shows
 *   the offline banner; UPDATING shows the refresh banner.
 * @param fetchedAt Optional wall-clock instant of last successful fetch for human-readable
 *   "Updated 5m ago" timestamp.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun BannerFreshness(
    state: FreshnessState,
    modifier: Modifier = Modifier,
    fetchedAt: Instant? = null,
) {
    DataFreshnessIndicator(
        freshness = state.toDataFreshness(),
        modifier = modifier,
        fetchedAt = fetchedAt,
    )
}

internal fun FreshnessState.toDataFreshness(): DataFreshness = when (this) {
    FreshnessState.Fresh -> DataFreshness.FRESH
    FreshnessState.Stale -> DataFreshness.STALE
    FreshnessState.Updating -> DataFreshness.UPDATING
    // Offline collapses to STALE — the banner already renders the "Offline · …" copy
    // for STALE state, so the user-visible message remains accurate.
    FreshnessState.Offline -> DataFreshness.STALE
}
