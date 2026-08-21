/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.alerts.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.demo.alerts.AlertsRepository
import kpt.core.model.demo.alerts.PriceAlert
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreWriteRequest

/**
 * Store5 single-source-of-truth: every mutation flows through the [alertsWriteStore] — `store.write`
 * (upsert) / `store.clear` (delete). The repository never touches the DAO; the write store's
 * `SourceOfTruth` writer/delete are the only DAO callers, so Room stays the durable SoT and the
 * paired read store ([alertsStore]) re-projects reactively (same `alerts` table). Alerts are
 * device-local (no backend), so there is no gateway online/command path here.
 */
internal class AlertsRepositoryImpl(
    private val alertsStore: Store<Unit, List<PriceAlert>>,
    private val alertsWriteStore: MutableStore<String, PriceAlert>,
    private val networkMonitor: NetworkMonitor,
    private val fetchedAtRepository: FetchedAtRepository,
) : AlertsRepository {

    // Read-path contract: the repository builds the ScreenDataStream (offline-local → CACHE_ONLY);
    // the ViewModel injects this repository and consumes `.state`. It never touches the Store.
    override fun alertsStream(scope: CoroutineScope): ScreenDataStream<List<PriceAlert>> =
        alertsStore.asScreenStream(
            key = Unit,
            networkMonitor = networkMonitor,
            fetchedAtRepository = fetchedAtRepository,
            cacheKey = "alerts",
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
            isEmpty = { it.isEmpty() },
        )

    override suspend fun submitAlert(alert: PriceAlert): PriceAlert {
        // Write through the store — persists to the Room SoT (via the SoT writer); the read store re-emits.
        alertsWriteStore.write(
            StoreWriteRequest.of<String, PriceAlert, Any>(key = alert.id, value = alert),
        )
        return alert
    }

    override suspend fun deleteAlert(id: String) {
        // Clear through the store — removes the row from the Room SoT (via the SoT delete).
        alertsWriteStore.clear(id)
    }
}
