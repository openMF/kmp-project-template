/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.currency.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.RetryPolicy
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.executeWithRetry
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kpt.core.database.currency.dao.RateHistoryDao
import kpt.core.database.currency.mapper.toDomain
import kpt.core.database.currency.mapper.toEntity
import kpt.core.model.currency.RateHistory
import kpt.core.model.currency.RateHistoryKey
import kpt.core.network.currency.api.FrankfurterApi
import kpt.core.store.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.store.infra.DefaultValidator
import kpt.core.base.store.infra.StoreFactory
import kotlin.time.Clock

fun provideRateHistoryStore(
    api: FrankfurterApi,
    networkMonitor: NetworkMonitor,
    dao: RateHistoryDao,
): Store<RateHistoryKey, RateHistory> {
    val validator = DefaultValidator.withTtl<RateHistory>(AppStoreRegistry.Ttl.RATE_HISTORY)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { key: RateHistoryKey ->
            val today = Clock.System.todayIn(TimeZone.UTC)
            val start = today.minus(key.days, DateTimeUnit.DAY)
            networkMonitor.executeWithRetry(
                RetryPolicy { maxAttempts = 1 },
            ) {
                api.getHistoricalRates(
                    from = key.from,
                    to = key.to,
                    startDate = start.toString(),
                    endDate = today.toString(),
                ).toDomain(key.to)
            }
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key ->
                val today = Clock.System.todayIn(TimeZone.UTC)
                val start = today.minus(key.days, DateTimeUnit.DAY)
                dao.get(key.from, key.to, start.toString(), today.toString())
                    .map { it?.toDomain() }
            },
            writer = { key, history ->
                // Use requested dates from key, not API response dates.
                // Frankfurter API returns end_date as last business day with data
                // (e.g., 2026-04-30), not the requested date (e.g., 2026-05-02).
                // SOT reader queries with key-derived dates, so entity must match.
                val today = Clock.System.todayIn(TimeZone.UTC)
                val start = today.minus(key.days, DateTimeUnit.DAY)
                dao.upsert(
                    history.copy(
                        startDate = start.toString(),
                        endDate = today.toString(),
                    ).toEntity(),
                )
                validator.markFresh()
            },
            delete = { key -> dao.delete(key.from, key.to) },
            deleteAll = { dao.deleteAll() },
        ),
        validator = validator,
    )
}
