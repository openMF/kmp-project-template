/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.currencyrates.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.currency.CurrencyRepository
import kpt.core.data.infra.Synchronizer
import kpt.core.model.demo.currency.ExchangeRates
import kpt.core.model.demo.currency.RateHistory
import kpt.core.model.demo.currency.RateHistoryKey

/**
 * Test double for [CurrencyRepository] that exposes the underlying mutable state
 * flows + refresh trigger so tests can drive the ViewModel through every state
 * transition (Loading → Content → Error → NoNetwork).
 */
@OptIn(ExperimentalScreenDataStreamTestingApi::class)
internal class FakeCurrencyRepository : CurrencyRepository {
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    val exchangeRatesState: MutableStateFlow<ScreenState<ExchangeRates>> =
        MutableStateFlow(ScreenState.Loading)
    val exchangeRatesRefresh: MutableSharedFlow<Unit> =
        MutableSharedFlow(extraBufferCapacity = 8)

    val rateHistoryState: MutableStateFlow<ScreenState<RateHistory>> =
        MutableStateFlow(ScreenState.Loading)
    val rateHistoryRefresh: MutableSharedFlow<Unit> =
        MutableSharedFlow(extraBufferCapacity = 8)

    /** Latest base currency the ViewModel asked for — assert on this in tests. */
    var lastExchangeRatesBase: String? = null
        private set

    /** Snapshot of the latest [RateHistoryKey] the ViewModel emitted into the stream factory. */
    val rateHistoryKeys: MutableList<RateHistoryKey> = mutableListOf()

    override fun exchangeRatesStream(
        baseCurrency: String,
        scope: CoroutineScope,
        fetchPolicy: FetchPolicy,
    ): ScreenDataStream<ExchangeRates> {
        lastExchangeRatesBase = baseCurrency
        return screenDataStreamForTesting(
            state = exchangeRatesState,
            refreshTrigger = exchangeRatesRefresh,
        )
    }

    override fun rateHistoryStream(
        keyFlow: Flow<RateHistoryKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<RateHistory> {
        // Drain key emissions onto a list so tests can assert the keys the VM produces.
        keyFlow.onEach { rateHistoryKeys += it }.launchIn(scope)
        return screenDataStreamForTesting(
            state = rateHistoryState,
            refreshTrigger = rateHistoryRefresh,
        )
    }

    /** Latest connectivity flag the ViewModel supplied to [spotRateStream] — assert on this. */
    var lastSpotOnline: Boolean? = null
        private set
    val spotRateState: MutableStateFlow<ScreenState<ExchangeRates>> = MutableStateFlow(ScreenState.Loading)

    override fun spotRateStream(
        baseCurrency: String,
        online: Boolean,
        scope: CoroutineScope,
    ): ScreenDataStream<ExchangeRates> {
        lastSpotOnline = online
        return screenDataStreamForTesting(state = spotRateState)
    }
}
