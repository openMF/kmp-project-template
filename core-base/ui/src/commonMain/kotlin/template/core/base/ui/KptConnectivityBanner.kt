/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.compose.ConnectivityBanner

/**
 * Thin wrapper around [ConnectivityBanner] from `cmp-network-monitor-compose`.
 *
 * Isolates the library import inside `core-base/ui` (which already depends on
 * `cmp-network-monitor-compose`) so that modules higher in the graph (e.g.,
 * `cmp-navigation`) can show a connectivity banner without needing a direct
 * dependency on the library artifact.
 *
 * Placed via [KptRootScaffold]'s `utilityBar` slot at the authenticated root.
 * Slides in from the top when the device goes offline; slides out when back online.
 * The [debounceMs] default (300 ms) suppresses WiFi↔Cell handoff flicker.
 *
 * @param showBanner Set to `false` to suppress the banner entirely — e.g. for apps
 *   without `INTERNET` permission or forks that surface connectivity via a different UI.
 * @param debounceMs Debounce applied to the underlying online/offline state transition.
 *   Defaults to 300 ms to avoid flash on WiFi↔Cell handoff.
 * @param modifier Modifier forwarded to [ConnectivityBanner].
 */
@Composable
fun KptConnectivityBanner(
    showBanner: Boolean = true,
    debounceMs: Long = 300L,
    modifier: Modifier = Modifier,
) {
    if (showBanner) {
        ConnectivityBanner(modifier = modifier, debounceMs = debounceMs)
    }
}
