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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kpt.core.base.store.freshness.FreshnessSignal
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
 * Minimal [CryptoRepository] for Compose UI tests AND ViewModel tests.
 *
 * Returns an always-empty paging stream via an in-memory Store so
 * [CoinMarketsViewModel] can be constructed without Koin, without a
 * real network, and without Room.
 *
 * It also RECORDS what each ViewModel asked for ([lastPageSize], [lastCoinId], [fetchCount]).
 * Those are what let a ViewModel test assert the request contract rather than only the render:
 * a detail screen that streams the wrong `coinId`, or a list that silently drops to a 1-row page
 * size, produces a perfectly valid-looking screen and is invisible to a render-only assertion.
 */
@OptIn(ExperimentalScreenDataStreamTestingApi::class)
internal class FakeCryptoRepository(
    /** Detail state served by [coinDetailStream]; Loading by default (UI tests want no targets). */
    private val detailState: MutableStateFlow<ScreenState<CoinDetail>> =
        MutableStateFlow(ScreenState.Loading),
    /** Refresh trigger handed to the detail stream, so a test can observe `refresh()` dispatch. */
    val detailRefreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1),
    /** Freshness fed to the detail stream, so a test can assert the ViewModel's projection. */
    private val detailFreshness: Flow<FreshnessSignal> = emptyFlow(),
) : CryptoRepository {

    /** Page size the last [coinMarketsStream] caller requested. */
    var lastPageSize: Int? = null
        private set

    /** The stream instance last handed out — lets a test assert the ViewModel passes it through. */
    var lastMarketsStream: PagingScreenStream<CoinMarket>? = null
        private set

    /** Coin id the last [coinDetailStream] caller requested. */
    var lastCoinId: String? = null
        private set

    private val fetches = MutableStateFlow(0)

    /** How many times the paging store's fetcher ran — a refresh must increment this. */
    val fetchCount: Int get() = fetches.value

    override fun coinMarketsStream(
        scope: CoroutineScope,
        pageSize: Int,
    ): PagingScreenStream<CoinMarket> {
        lastPageSize = pageSize
        val store = StoreBuilder
            .from<PageKey, List<CoinMarket>>(
                fetcher = Fetcher.of {
                    fetches.value += 1
                    emptyList()
                },
            )
            .build()
        return store.asPagingScreenStream(
            networkMonitor = AlwaysOnlineNetworkMonitor,
            fetchedAtRepository = NoOpFetchedAtRepository,
            cacheKey = "crypto:coinMarkets:test",
            scope = scope,
            pageSize = pageSize,
        ).also { lastMarketsStream = it }
    }

    /**
     * Serves [detailState]. Defaults to permanently [ScreenState.Loading] so the Compose UI tests
     * that construct this fake with no arguments see no assertion targets, exactly as before.
     */
    override fun coinDetailStream(
        coinId: String,
        scope: CoroutineScope,
    ): ScreenDataStream<CoinDetail> {
        lastCoinId = coinId
        return screenDataStreamForTesting(
            state = detailState,
            refreshTrigger = detailRefreshTrigger,
            freshness = detailFreshness,
        )
    }
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
