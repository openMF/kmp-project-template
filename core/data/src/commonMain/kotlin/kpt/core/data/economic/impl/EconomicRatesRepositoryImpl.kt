/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.economic.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.economic.EconomicRatesRepository
import kpt.core.model.economic.InterestRateSeries
import kpt.core.store.economic.impl.InterestRateSeriesKey
import org.mobilenativefoundation.store.store5.Store

class EconomicRatesRepositoryImpl(
    private val interestRateSeriesStore: Store<InterestRateSeriesKey, InterestRateSeries>,
    private val networkMonitor: NetworkMonitor,
    private val fetchedAtRepository: FetchedAtRepository,
) : EconomicRatesRepository {

    override fun interestRateSeriesStream(
        key: InterestRateSeriesKey,
        scope: CoroutineScope,
    ): ScreenDataStream<InterestRateSeries> = interestRateSeriesStore.asScreenStream(
        key = key,
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAtRepository,
        cacheKey = "economic:rates:${key.seriesId}:${key.days}d",
        scope = scope,
    )

    override fun interestRateSeriesStream(
        keyFlow: Flow<InterestRateSeriesKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<InterestRateSeries> = interestRateSeriesStore.asScreenStream(
        keyFlow = keyFlow,
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAtRepository,
        cacheKeyFor = { key -> "economic:rates:${key.seriesId}:${key.days}d" },
        scope = scope,
    )
}
