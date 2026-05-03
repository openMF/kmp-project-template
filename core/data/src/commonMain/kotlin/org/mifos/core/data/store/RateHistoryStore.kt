/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.store

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.RetryPolicy
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.executeWithRetry
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.mifos.core.data.di.ApplicationStoreRegistry
import org.mifos.core.database.dao.RateHistoryDao
import org.mifos.core.database.mapper.toDomain
import org.mifos.core.database.mapper.toEntity
import org.mifos.core.model.fintech.RateHistory
import org.mifos.core.model.fintech.RateHistoryKey
import org.mifos.core.network.fintech.FrankfurterApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import template.core.base.store.DefaultValidator
import template.core.base.store.StoreFactory
import kotlin.time.Clock

fun provideRateHistoryStore(
    api: FrankfurterApi,
    networkMonitor: NetworkMonitor,
    dao: RateHistoryDao,
): Store<RateHistoryKey, RateHistory> {
    val validator = DefaultValidator.withTtl<RateHistory>(ApplicationStoreRegistry.Ttl.RATE_HISTORY)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { key: RateHistoryKey ->
            val today = Clock.System.todayIn(TimeZone.UTC)
            val start = today.minus(key.days, DateTimeUnit.DAY)
            networkMonitor.executeWithRetry(
                RetryPolicy { maxAttempts = 3 },
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
