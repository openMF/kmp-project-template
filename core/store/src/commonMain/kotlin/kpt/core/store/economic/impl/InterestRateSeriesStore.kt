/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.economic.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.RetryPolicy
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.executeWithRetry
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.economic.InterestRateSeriesDao
import kpt.core.database.economic.InterestRateSeriesEntity
import kpt.core.model.economic.InterestRateSeries
import kpt.core.model.economic.RateObservation
import kpt.core.network.economic.api.FredApi
import kpt.core.network.economic.config.FredApiConfig
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

/**
 * Build a [Store] for FRED interest-rate series with [InterestRateSeriesDao]-backed
 * offline cache (NETWORK_WITH_CACHE archetype).
 *
 * Previously in-memory only. Now persists fetched observations to the
 * `interest_rate_series` Room table so data survives process death and users
 * see meaningful content on first launch after a cold start with no network.
 *
 * The [SourceOfTruth] uses [InterestRateSeriesEntity] rows as the persisted
 * representation, mapping to/from the [InterestRateSeries] domain model inline
 * (no separate mapper file — the mapping is trivial and local to this store).
 */
fun provideInterestRateSeriesStore(
    api: FredApi,
    config: FredApiConfig,
    networkMonitor: NetworkMonitor,
    dao: InterestRateSeriesDao,
): Store<InterestRateSeriesKey, InterestRateSeries> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: InterestRateSeriesKey ->
        val apiKey = config.apiKey
            ?: error(
                "FRED_API_KEY is not configured — supply a key via FredApiConfig " +
                    "before requesting interest-rate data. See CLAUDE.md " +
                    "→ \"First-time fork setup\".",
            )
        val today = Clock.System.todayIn(TimeZone.UTC)
        val start = today.minus(key.days, DateTimeUnit.DAY)
        networkMonitor.executeWithRetry(
            RetryPolicy { maxAttempts = 1 },
        ) {
            api.seriesObservations(
                seriesId = key.seriesId,
                apiKey = apiKey,
                observationStart = start.toString(),
                observationEnd = today.toString(),
            ).toDomain(
                seriesId = key.seriesId,
                name = key.name,
                unit = key.unit,
            )
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: InterestRateSeriesKey ->
            dao.observeBySeriesId(key.seriesId).map { entities ->
                entities.toInterestRateSeries(key)
            }
        },
        writer = { _: InterestRateSeriesKey, series: InterestRateSeries ->
            val now = Clock.System.now().toEpochMilliseconds()
            dao.upsertAll(series.toEntities(updatedAt = now))
        },
        delete = { key: InterestRateSeriesKey -> dao.deleteBySeriesId(key.seriesId) },
        deleteAll = { dao.deleteAll() },
    ),
)

// ---------------------------------------------------------------------------
// Inline mapping helpers — private to this file
// ---------------------------------------------------------------------------

private fun List<InterestRateSeriesEntity>.toInterestRateSeries(key: InterestRateSeriesKey): InterestRateSeries? {
    if (isEmpty()) return null
    val observations = mapNotNull { entity ->
        runCatching { LocalDate.parse(entity.date) }.getOrNull()
            ?.let { date -> RateObservation(date = date, value = entity.value) }
    }.sortedByDescending { it.date }
    return InterestRateSeries(
        seriesId = key.seriesId,
        name = key.name,
        unit = key.unit,
        current = observations.firstOrNull()?.value ?: 0.0,
        observations = observations,
    )
}

private fun InterestRateSeries.toEntities(updatedAt: Long): List<InterestRateSeriesEntity> = observations.map { obs ->
    InterestRateSeriesEntity(
        seriesId = seriesId,
        date = obs.date.toString(),
        value = obs.value,
        updatedAt = updatedAt,
    )
}
