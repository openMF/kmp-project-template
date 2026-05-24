/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.store.economic.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.RetryPolicy
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.executeWithRetry
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.mifos.core.model.economic.InterestRateSeries
import org.mifos.core.network.economic.api.FredApi
import org.mifos.core.network.economic.config.FredApiConfig
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.Store
import template.core.base.store.infra.StoreFactory
import kotlin.time.Clock

/**
 * Build an in-memory [Store] for FRED interest-rate series.
 *
 * In-memory only — there is no on-device DAO for economic data, so observations
 * are cached for the lifetime of the Store5 cache (and re-fetched on TTL
 * expiry). This is a deliberate trade-off: economic data is small, public,
 * and largely insensitive to short app restarts.
 */
fun provideInterestRateSeriesStore(
    api: FredApi,
    config: FredApiConfig,
    networkMonitor: NetworkMonitor,
): Store<InterestRateSeriesKey, InterestRateSeries> =
    StoreFactory.createMemoryStore(
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
    )
