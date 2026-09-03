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
import kpt.core.base.store.infra.StoreFactory
import kpt.core.model.demo.economic.MacroIndicator
import kpt.core.network.demo.economic.api.WorldBankApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

/**
 * Build an in-memory [Store] for World Bank macro indicators.
 *
 * In-memory only — no on-device DAO for economic data. The 7-day TTL combined
 * with the World Bank's annual publishing cadence means real fetches are very
 * rare (≤ 1 per week per country/indicator pair).
 */
fun provideMacroIndicatorStore(
    api: WorldBankApi,
    networkMonitor: NetworkMonitor,
): Store<MacroIndicatorKey, MacroIndicator> = StoreFactory.createMemoryStore(
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
)
