/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.store

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus

/**
 * Pure function combining StoreData metadata + NetworkStatus into ScreenState.
 * No side effects, no coroutines — exhaustively unit testable.
 *
 * Uses full [NetworkStatus] (not just Boolean) to detect captive portals.
 */
object DecisionEngine {

    fun <T> decide(
        storeData: StoreData<T>,
        networkStatus: NetworkStatus,
    ): ScreenState<T> {
        val noData = storeData.isEmpty
        val error = storeData.error
        val isOnline = networkStatus is NetworkStatus.Available
        val isCaptivePortal = networkStatus is NetworkStatus.CaptivePortal

        // === No data branch ===
        if (noData) {
            return when {
                isCaptivePortal -> ScreenState.NoNetwork(isCaptivePortal = true)
                !isOnline -> ScreenState.NoNetwork()
                error != null && error.isNetworkError() -> ScreenState.NoNetwork()
                error != null -> ScreenState.Error(error, isNetworkError = false)
                else -> ScreenState.Loading
            }
        }

        // === Has data branch ===
        val fetchedAt = storeData.fetchedAtInstant
        return when {
            storeData.isRefreshing -> ScreenState.Content(storeData.data, DataFreshness.UPDATING, fetchedAt)
            !isOnline || isCaptivePortal -> ScreenState.Content(storeData.data, DataFreshness.STALE, fetchedAt)
            error != null -> ScreenState.Content(storeData.data, DataFreshness.STALE, fetchedAt)
            else -> ScreenState.Content(storeData.data, DataFreshness.FRESH, fetchedAt)
        }
    }

    private fun Throwable.isNetworkError(): Boolean {
        val name = this::class.simpleName.orEmpty()
        val causeName = cause?.let { it::class.simpleName.orEmpty() }.orEmpty()
        return name.contains("IOException") ||
            causeName.contains("IOException") ||
            name.contains("Connect") ||
            name.contains("Timeout") ||
            causeName.contains("Connect") ||
            causeName.contains("Timeout")
    }
}
