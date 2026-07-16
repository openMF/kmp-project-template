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

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkChangeEvent
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.currency.ExchangeRates
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Compose Multiplatform UI test for [CurrencyRatesScreen] — verifies the root
 * [Scaffold] node renders in the in-process `runComposeUiTest` harness
 * (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 *
 * [CurrencyRatesViewModel] requires four collaborators. The test constructs
 * minimal in-process doubles for each so no Koin, Room, or network is needed:
 *  - [FakeCurrencyRepository] — existing test double from `commonTest`.
 *  - [UiTestNetworkMonitor] — file-private online stub.
 *  - [UiTestFetchedAtRepository] — file-private in-memory stub.
 *  - An in-memory [StoreBuilder] — returns a static [ExchangeRates] value.
 *
 * The screen starts in [kpt.core.base.store.screen.ScreenState.Loading] which
 * still renders the Scaffold root — so the tag assertion is stable.
 */
@OptIn(ExperimentalTestApi::class)
class CurrencyRatesScreenUiTest {

    @Test
    fun rootScaffoldIsDisplayed() = runComposeUiTest {
        val viewModel = CurrencyRatesViewModel(
            currencyRepository = FakeCurrencyRepository(),
            networkMonitor = UiTestNetworkMonitor,
            fetchedAtRepository = UiTestFetchedAtRepository(),
            spotRateStore = uiTestSpotRateStore(),
        )
        setContent {
            KptTheme {
                CurrencyRatesScreen(
                    onBackClick = {},
                    viewModel = viewModel,
                )
            }
        }
        onNodeWithTag(TestTags.CurrencyRates.ROOT).assertIsDisplayed()
    }
}

// region Minimal file-private test doubles

/** Online [NetworkMonitor] stub — always reports WiFi available. */
private object UiTestNetworkMonitor : NetworkMonitor {
    override val networkStatus: StateFlow<NetworkStatus> = MutableStateFlow(
        NetworkStatus.Available(NetworkInfo(NetworkType.WiFi, isMetered = false)),
    ).asStateFlow()
    override val isOnline: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
    override val networkChanges: SharedFlow<NetworkChangeEvent> =
        MutableSharedFlow<NetworkChangeEvent>().asSharedFlow()
    override fun close() = Unit
}

/** In-memory [FetchedAtRepository] stub — never throws, returns null on cold start. */
@OptIn(ExperimentalTime::class)
private class UiTestFetchedAtRepository : FetchedAtRepository {
    private val map = mutableMapOf<String, Instant>()
    override suspend fun read(storeKey: String): Instant? = map[storeKey]
    override suspend fun write(storeKey: String, instant: Instant) {
        map[storeKey] = instant
    }
}

/** Minimal in-memory [Store] that returns a static [ExchangeRates] without any network call. */
private fun uiTestSpotRateStore(): Store<String, ExchangeRates> =
    StoreBuilder.from<String, ExchangeRates>(
        fetcher = Fetcher.of { _ ->
            ExchangeRates(base = "USD", date = "2026-07-16", rates = emptyMap())
        },
    ).build()

// endregion
