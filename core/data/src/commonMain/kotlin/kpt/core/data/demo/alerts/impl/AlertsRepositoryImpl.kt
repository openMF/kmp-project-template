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
import kpt.core.base.database.invalidation.notifyingWrite
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.demo.alerts.AlertsRepository
import kpt.core.database.demo.alerts.AlertDao
import kpt.core.model.demo.alerts.PriceAlert
import kpt.core.store.demo.alerts.impl.toAlertEntity
import org.mobilenativefoundation.store.store5.Store

internal class AlertsRepositoryImpl(
    private val alertsStore: Store<Unit, List<PriceAlert>>,
    private val alertDao: AlertDao,
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
        notifyingWrite(ALERTS_TABLE) {
            alertDao.upsert(alert.toAlertEntity())
        }
        return alert
    }

    override suspend fun deleteAlert(id: String) {
        notifyingWrite(ALERTS_TABLE) {
            alertDao.deleteById(id)
        }
    }

    private companion object {
        /** Room `@Entity(tableName = …)` for the alerts table. Shared with [provideAlertsStore]'s reader. */
        const val ALERTS_TABLE = "alerts"
    }
}
