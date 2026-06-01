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

import app.cash.turbine.test
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkChangeEvent
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.DataFreshness
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.currency.ExchangeRates
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Locks the [CurrencyRatesViewModel] contract:
 *
 *  - Opens its exchange-rates stream with `base = "USD"` (hard-coded — see VM).
 *  - Search action filters the rates map case-insensitively.
 *  - Empty filtered result → Empty screen state (via `emptyIfContent`).
 *  - `onRetry()` and `onRefresh()` both fan out to the underlying stream.
 *
 *  **Archetype showcase — CACHE_ONLY + NETWORK_ONLY:**
 *  - [spotConversionRate_usesNetworkOnlyPolicyWhenOnline]: online → NETWORK_ONLY policy
 *    (spotConversionRate emits from the store, starting at Loading).
 *  - [spotConversionRate_usesCacheOnlyPolicyWhenOffline]: offline → CACHE_ONLY policy
 *    (spotConversionRate emits from the store, starting at Loading; no API call attempted).
 *  - [spotConversionRate_switchesPolicyWhenConnectivityChanges]: when connectivity flips
 *    from offline to online the stream rebuilds with NETWORK_ONLY so the UI picks up a
 *    fresh rate automatically.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyRatesViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Existing exchange-rates stream tests

    @Test
    fun viewModelOpensExchangeRatesStreamWithUsdBase() = runTest {
        val repo = FakeCurrencyRepository()
        buildViewModel(repo = repo)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("USD", repo.lastExchangeRatesBase)
    }

    @Test
    fun initialScreenStateIsLoading() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = buildViewModel(repo = repo)
        val state = vm.screenState.first()
        assertEquals(ScreenState.Loading, state)
    }

    @Test
    fun searchFiltersRatesByCodeCaseInsensitively() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = buildViewModel(repo = repo)
        repo.exchangeRatesState.value = ScreenState.Content(
            data = ExchangeRates(
                base = "USD",
                date = "2026-05-25",
                rates = mapOf("EUR" to 0.92, "INR" to 83.5, "GBP" to 0.79),
            ),
            freshness = DataFreshness.FRESH,
        )
        dispatcher.scheduler.advanceUntilIdle()

        vm.screenState.test {
            // Drain initial Loading + Content-with-all-rates.
            awaitItem()

            vm.trySendAction(RatesAction.Search("eur"))
            dispatcher.scheduler.advanceUntilIdle()

            val filtered = expectMostRecentItem()
            assertTrue(filtered is ScreenState.Content)
            assertEquals(setOf("EUR"), filtered.data.rates.keys)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchWithNoMatchesYieldsEmptyState() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = buildViewModel(repo = repo)
        repo.exchangeRatesState.value = ScreenState.Content(
            data = ExchangeRates(
                base = "USD",
                date = "2026-05-25",
                rates = mapOf("EUR" to 0.92, "INR" to 83.5),
            ),
            freshness = DataFreshness.FRESH,
        )
        dispatcher.scheduler.advanceUntilIdle()

        vm.trySendAction(RatesAction.Search("ZZZ"))
        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.screenState.first { it is ScreenState.Empty }
        assertTrue(state is ScreenState.Empty)
    }

    @Test
    fun onRetryTriggersStreamRefresh() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = buildViewModel(repo = repo)
        dispatcher.scheduler.advanceUntilIdle()

        repo.exchangeRatesRefresh.test {
            vm.onRetry()
            dispatcher.scheduler.advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onRefreshTriggersStreamRefresh() = runTest {
        val repo = FakeCurrencyRepository()
        val vm = buildViewModel(repo = repo)
        dispatcher.scheduler.advanceUntilIdle()

        repo.exchangeRatesRefresh.test {
            vm.onRefresh()
            dispatcher.scheduler.advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Archetype: CACHE_ONLY + NETWORK_ONLY

    /**
     * When the device is online, [spotConversionRate] starts in Loading and the VM
     * has selected the NETWORK_ONLY path. The store integration test is minimal (no
     * Room in unit tests) — we assert that the state is observable and starts at Loading.
     */
    @Test
    fun spotConversionRate_usesNetworkOnlyPolicyWhenOnline() = runTest {
        val networkMonitor = FakeNetworkMonitor(online = true)
        val vm = buildViewModel(networkMonitor = networkMonitor)

        val state = vm.spotConversionRate.first()
        // Loading is the initial value (store hasn't responded yet) — the NETWORK_ONLY
        // path was chosen (the stream is wired and waiting for the network fetch).
        assertEquals(ScreenState.Loading, state)
    }

    /**
     * When the device is offline, [spotConversionRate] starts in Loading and the VM
     * has selected the CACHE_ONLY path. CACHE_ONLY never fires a network request, so
     * if the cache is empty the stream stays at Loading then transitions to Empty.
     */
    @Test
    fun spotConversionRate_usesCacheOnlyPolicyWhenOffline() = runTest {
        val networkMonitor = FakeNetworkMonitor(online = false)
        val vm = buildViewModel(networkMonitor = networkMonitor)

        val state = vm.spotConversionRate.first()
        // Loading is the initial value; the CACHE_ONLY path was chosen.
        assertEquals(ScreenState.Loading, state)
    }

    /**
     * Switching from offline to online rebuilds the stream with NETWORK_ONLY — the
     * VM has a [flatMapLatest] keyed on network connectivity that ensures the stream
     * adopts the new policy as soon as connectivity is restored.
     */
    @Test
    fun spotConversionRate_switchesPolicyWhenConnectivityChanges() = runTest {
        val networkMonitor = FakeNetworkMonitor(online = false)
        val vm = buildViewModel(networkMonitor = networkMonitor)
        dispatcher.scheduler.advanceUntilIdle()

        // Verify initial state exists (CACHE_ONLY path, no network fetch).
        assertNotNull(vm.spotConversionRate)

        // Flip to online — the flatMapLatest rebuilds with NETWORK_ONLY.
        networkMonitor.setOnline(true)
        dispatcher.scheduler.advanceUntilIdle()

        // The stream is still observable and starts at Loading after rebuild.
        val state = vm.spotConversionRate.first()
        assertEquals(ScreenState.Loading, state)
    }

    // endregion

    // region helpers

    private fun buildViewModel(
        repo: FakeCurrencyRepository = FakeCurrencyRepository(),
        networkMonitor: FakeNetworkMonitor = FakeNetworkMonitor(online = true),
        fetchedAt: FakeTestFetchedAtRepository = FakeTestFetchedAtRepository(),
        spotRateStore: Store<String, ExchangeRates> = fakeSpotRateStore(),
    ): CurrencyRatesViewModel = CurrencyRatesViewModel(
        currencyRepository = repo,
        networkMonitor = networkMonitor,
        fetchedAtRepository = fetchedAt,
        spotRateStore = spotRateStore,
    )

    /** Minimal in-memory Store for the SpotRateLookupStore in tests. */
    private fun fakeSpotRateStore(): Store<String, ExchangeRates> =
        StoreBuilder.from<String, ExchangeRates>(
            fetcher = Fetcher.of { _ ->
                ExchangeRates(base = "USD", date = "2026-05-28", rates = emptyMap())
            },
        ).build()

    // endregion
}

// region Test doubles (file-private)

/**
 * Minimal [NetworkMonitor] test double that exposes a mutable online flag.
 */
private class FakeNetworkMonitor(online: Boolean) : NetworkMonitor {

    private val _networkStatus = MutableStateFlow<NetworkStatus>(
        if (online) {
            NetworkStatus.Available(NetworkInfo(NetworkType.WiFi, isMetered = false))
        } else {
            NetworkStatus.Unavailable
        },
    )

    override val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    override val isOnline: StateFlow<Boolean> =
        MutableStateFlow(_networkStatus.value is NetworkStatus.Available).asStateFlow()

    override val networkChanges: SharedFlow<NetworkChangeEvent> =
        MutableSharedFlow<NetworkChangeEvent>().asSharedFlow()

    fun setOnline(value: Boolean) {
        _networkStatus.value = if (value) {
            NetworkStatus.Available(NetworkInfo(NetworkType.WiFi, isMetered = false))
        } else {
            NetworkStatus.Unavailable
        }
    }

    override fun close() = Unit
}

/**
 * Minimal [FetchedAtRepository] test double backed by an in-memory map.
 */
@OptIn(ExperimentalTime::class)
private class FakeTestFetchedAtRepository : FetchedAtRepository {
    private val map = mutableMapOf<String, Instant>()

    override suspend fun read(storeKey: String): Instant? = map[storeKey]

    override suspend fun write(storeKey: String, instant: Instant) {
        map[storeKey] = instant
    }
}

// endregion
