/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.economic.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.RetryPolicy
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.executeWithRetry
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.coroutines.flow.map
import kpt.core.base.store.infra.DefaultValidator
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.demo.economic.MacroIndicatorDao
import kpt.core.database.demo.economic.MacroIndicatorEntity
import kpt.core.model.demo.economic.IndicatorKind
import kpt.core.model.demo.economic.IndicatorObservation
import kpt.core.model.demo.economic.MacroIndicator
import kpt.core.network.demo.economic.api.WorldBankApi
import kpt.core.store.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

/**
 * Build a Room-backed [Store] for World Bank macro indicators.
 *
 * Room-backed since v13. It was previously `createMemoryStore` — the ONE demo network store with no
 * [SourceOfTruth] — so its cache died with the process and every cold start refetched, the single
 * read path in the app that was not cache-first across a restart. The 7-day TTL plus the World Bank's
 * annual publishing cadence already made real fetches rare (≤ 1 per week per country/indicator pair);
 * persisting them makes a cold start serve instantly from disk instead of blocking on the network.
 *
 * The entity→domain map lives in the [SourceOfTruth] reader (read-path contract) — the store emits
 * the DOMAIN model.
 */
fun provideMacroIndicatorStore(
    api: WorldBankApi,
    networkMonitor: NetworkMonitor,
    dao: MacroIndicatorDao,
): Store<MacroIndicatorKey, MacroIndicator> {
    val validator = DefaultValidator.withTtl<MacroIndicator>(AppStoreRegistry.Ttl.MACRO_INDICATOR)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { key: MacroIndicatorKey ->
            val today = Clock.System.todayIn(TimeZone.UTC)
            val endYear = today.year
            val startYear = endYear - key.years
            networkMonitor.executeWithRetry(
                RetryPolicy { maxAttempts = 1 },
            ) {
                api.indicator(
                    countryCode = key.countryCode,
                    indicator = key.indicator.worldBankCode,
                    dateRange = "$startYear:$endYear",
                ).toDomain(
                    countryCode = key.countryCode,
                    indicator = key.indicator,
                )
            }
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: MacroIndicatorKey ->
                dao.observeSeries(key.countryCode, key.indicator.name).map { rows ->
                    rows.toMacroIndicator(key.indicator)
                }
            },
            writer = { _: MacroIndicatorKey, indicator: MacroIndicator ->
                val now = Clock.System.now().toEpochMilliseconds()
                // Delete-then-insert: REPLACE alone would leave rows for years the World Bank has
                // since retracted, which the new response no longer covers.
                dao.deleteSeries(indicator.countryCode, indicator.indicator.name)
                dao.upsertAll(indicator.toEntities(updatedAt = now))
                validator.markFresh()
            },
            delete = { key: MacroIndicatorKey -> dao.deleteSeries(key.countryCode, key.indicator.name) },
            deleteAll = { dao.clear() },
        ),
        validator = validator,
    )
}

/**
 * Rows → domain. Returns `null` when the table holds nothing for this series, so Store5 treats it as
 * a cache MISS rather than an empty-but-present value (which would suppress the network fetch).
 */
private fun List<MacroIndicatorEntity>.toMacroIndicator(indicator: IndicatorKind): MacroIndicator? {
    val first = firstOrNull() ?: return null
    return MacroIndicator(
        countryCode = first.countryCode,
        countryName = first.countryName,
        indicator = indicator,
        // DAO already orders by year ASC — MacroIndicator.observations is contractually ascending.
        observations = map { IndicatorObservation(year = it.year, value = it.value) },
        source = first.source,
    )
}

/** Domain → rows, one per observation. */
private fun MacroIndicator.toEntities(updatedAt: Long): List<MacroIndicatorEntity> =
    observations.map { obs ->
        MacroIndicatorEntity(
            countryCode = countryCode,
            indicator = indicator.name,
            year = obs.year,
            countryName = countryName,
            value = obs.value,
            source = source,
            updatedAt = updatedAt,
        )
    }
