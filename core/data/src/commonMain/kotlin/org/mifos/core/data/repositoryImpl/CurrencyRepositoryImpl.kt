/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.repositoryImpl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.mifos.core.data.repository.CurrencyRepository
import org.mifos.core.model.fintech.ExchangeRates
import org.mifos.core.model.fintech.RateHistory
import org.mifos.core.model.fintech.RateHistoryKey
import org.mobilenativefoundation.store.store5.Store
import template.core.base.store.infra.FetchedAtRepository
import template.core.base.store.screen.ScreenDataStream
import template.core.base.store.screen.asScreenStream

class CurrencyRepositoryImpl(
    private val exchangeRatesStore: Store<String, ExchangeRates>,
    private val rateHistoryStore: Store<RateHistoryKey, RateHistory>,
    private val networkMonitor: NetworkMonitor,
    private val fetchedAtRepository: FetchedAtRepository,
) : CurrencyRepository {

    override fun exchangeRatesStream(
        baseCurrency: String,
        scope: CoroutineScope,
    ): ScreenDataStream<ExchangeRates> = exchangeRatesStore.asScreenStream(
        key = baseCurrency,
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAtRepository,
        cacheKey = "currency:exchangeRates:$baseCurrency",
        scope = scope,
    )

    override fun rateHistoryStream(
        keyFlow: Flow<RateHistoryKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<RateHistory> = rateHistoryStore.asScreenStream(
        keyFlow = keyFlow,
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAtRepository,
        cacheKeyFor = { key -> "currency:rateHistory:${key.from}-${key.to}-${key.days}d" },
        scope = scope,
    )
}
