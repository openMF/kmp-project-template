/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.crypto.ui

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkChangeEvent
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.paging.PageKey
import kpt.core.base.store.paging.PagingScreenStream
import kpt.core.base.store.paging.asPagingScreenStream
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.crypto.CryptoRepository
import kpt.core.model.demo.crypto.CoinDetail
import kpt.core.model.demo.crypto.CoinMarket
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Minimal [CryptoRepository] for Compose UI tests.
 *
 * Returns an always-empty paging stream via an in-memory Store so
 * [CoinMarketsViewModel] can be constructed without Koin, without a
 * real network, and without Room.
 */
@OptIn(ExperimentalScreenDataStreamTestingApi::class)
internal class FakeCryptoRepository : CryptoRepository {

    override fun coinMarketsStream(
        scope: CoroutineScope,
        pageSize: Int,
    ): PagingScreenStream<CoinMarket> {
        val store = StoreBuilder
            .from<PageKey, List<CoinMarket>>(
                fetcher = Fetcher.of { emptyList() },
            )
            .build()
        return store.asPagingScreenStream(
            networkMonitor = AlwaysOnlineNetworkMonitor,
            fetchedAtRepository = NoOpFetchedAtRepository,
            cacheKey = "crypto:coinMarkets:test",
            scope = scope,
            pageSize = pageSize,
        )
    }

    /**
     * Not exercised by [CoinMarketsScreen] — only used by the coin-detail
     * route. Stays permanently in [ScreenState.Loading] so no assertion targets
     * appear; sufficient for UI-test construction.
     */
    override fun coinDetailStream(
        coinId: String,
        scope: CoroutineScope,
    ): ScreenDataStream<CoinDetail> = screenDataStreamForTesting(
        state = MutableStateFlow(ScreenState.Loading),
        refreshTrigger = MutableSharedFlow(),
    )
}

/**
 * [NetworkMonitor] stub that permanently reports an online
 * [NetworkStatus.Available] connection. No network I/O is performed.
 */
private object AlwaysOnlineNetworkMonitor : NetworkMonitor {

    private val _status = MutableStateFlow<NetworkStatus>(
        NetworkStatus.Available(NetworkInfo()),
    )
    private val _changes = MutableSharedFlow<NetworkChangeEvent>()

    override val isOnline: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
    override val networkStatus: StateFlow<NetworkStatus> = _status.asStateFlow()
    override val networkChanges: SharedFlow<NetworkChangeEvent> = _changes.asSharedFlow()
    override fun close() = Unit
}

/**
 * [FetchedAtRepository] that always returns `null` and silently
 * discards writes. Sufficient for tests that do not assert on
 * data-freshness timestamps.
 */
@OptIn(ExperimentalTime::class)
private object NoOpFetchedAtRepository : FetchedAtRepository {
    override suspend fun read(storeKey: String): Instant? = null
    override suspend fun write(storeKey: String, instant: Instant) = Unit
}
