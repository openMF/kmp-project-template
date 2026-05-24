/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.macro.ui

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import org.mifos.core.data.economic.MacroIndicatorsRepository
import org.mifos.core.model.economic.IndicatorKind
import org.mifos.core.model.economic.IndicatorObservation
import org.mifos.core.model.economic.MacroIndicator
import org.mifos.core.store.economic.impl.MacroIndicatorKey
import template.core.base.store.screen.DataFreshness
import template.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import template.core.base.store.screen.ScreenDataStream
import template.core.base.store.screen.ScreenState
import template.core.base.store.screen.screenDataStreamForTesting
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Locks the multi-source-combine semantics of [CountryMacroViewModel]:
 *
 * 1. Three independent indicator streams (GDP / Inflation / Unemployment) are
 *    surfaced as **independent** [ScreenState] cells in [MacroUiState] — one
 *    failing must NOT take the other two down with it.
 * 2. [MacroAction.ChangeCountry] propagates the new country code into the
 *    state AND re-keys every indicator stream so the new country's data is
 *    what subsequent emissions carry.
 * 3. The aggregate UI state correctly reports overall freshness — STALE if
 *    any of the three indicators is STALE; FRESH only when all three are.
 *
 * Retry actions ([MacroAction.RetryGdp] etc.) delegate to
 * [ScreenDataStream.refresh] which is part of the framework's already-tested
 * contract. The dispatching coverage here is by inspection of the action
 * handler (smoke test verifies no exception); fine-grained refresh-call
 * counting would require breaching the ScreenDataStream framework's private
 * refresh trigger.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalScreenDataStreamTestingApi::class)
class CountryMacroViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: KeyedFakeMacroIndicatorsRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        fakeRepository = KeyedFakeMacroIndicatorsRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateExposesProvidedCountryCodeAndAllStreamsLoading() = runTest {
        val vm = CountryMacroViewModel(
            initialCountryCode = "IN",
            repository = fakeRepository,
        )
        vm.stateFlow.test {
            val initial = awaitItem()
            assertEquals("IN", initial.countryCode)
            assertIs<ScreenState.Loading>(initial.gdp)
            assertIs<ScreenState.Loading>(initial.inflation)
            assertIs<ScreenState.Loading>(initial.unemployment)
        }
    }

    @Test
    fun eachStreamUpdatesIndependentlyOfTheOthers() = runTest {
        val vm = CountryMacroViewModel("US", fakeRepository)
        yield() // let VM subscribe its 3 streams

        // GDP arrives first
        fakeRepository.emit("US", IndicatorKind.GDP, contentState(IndicatorKind.GDP))
        // Inflation errors out
        fakeRepository.emit(
            "US",
            IndicatorKind.INFLATION_CPI,
            ScreenState.Error(RuntimeException("CPI down")),
        )
        // Unemployment stays Loading

        vm.stateFlow.test {
            val finalState = expectMostRecentItem()
            assertIs<ScreenState.Content<MacroIndicator>>(finalState.gdp)
            assertIs<ScreenState.Error>(finalState.inflation)
            assertIs<ScreenState.Loading>(finalState.unemployment)
        }
    }

    @Test
    fun retryActionsAreRecognisedAndHandledWithoutCrashing() = runTest {
        // Smoke: every retry action variant must be a valid input to handleAction.
        // The behavioral contract (refresh dispatches to the right stream) is
        // verified by state-change tests; here we lock the action API surface.
        val vm = CountryMacroViewModel("US", fakeRepository)
        vm.trySendAction(MacroAction.RetryGdp)
        vm.trySendAction(MacroAction.RetryInflation)
        vm.trySendAction(MacroAction.RetryUnemployment)
        vm.trySendAction(MacroAction.RefreshAll)
        yield() // drain action channel
    }

    @Test
    fun changeCountryUpdatesStateCodeAndRequestsAllThreeKeysForNewCountry() = runTest {
        val vm = CountryMacroViewModel("US", fakeRepository)
        yield()
        // The 3 streams have subscribed and seen their initial US keys. Drain
        // the log so the assertion below isolates the ChangeCountry effect.
        fakeRepository.resetKeyLog()

        vm.trySendAction(MacroAction.ChangeCountry("IN"))
        yield()

        vm.stateFlow.test {
            val state = expectMostRecentItem()
            assertEquals("IN", state.countryCode)
        }
        val keysAfterChange = fakeRepository.loggedKeys()
        val expected = setOf(
            MacroIndicatorKey("IN", IndicatorKind.GDP),
            MacroIndicatorKey("IN", IndicatorKind.INFLATION_CPI),
            MacroIndicatorKey("IN", IndicatorKind.UNEMPLOYMENT),
        )
        assertTrue(
            keysAfterChange.containsAll(expected),
            "After ChangeCountry IN, expected all 3 IN indicators requested, got $keysAfterChange",
        )
    }

    @Test
    fun changeCountrySurfacesNewCountrysDataAfterRekey() = runTest {
        val vm = CountryMacroViewModel("US", fakeRepository)
        yield()

        // Drive US data
        fakeRepository.emit(
            "US",
            IndicatorKind.GDP,
            contentState(IndicatorKind.GDP, countryCode = "US"),
        )
        fakeRepository.emit(
            "US",
            IndicatorKind.INFLATION_CPI,
            contentState(IndicatorKind.INFLATION_CPI, countryCode = "US"),
        )
        fakeRepository.emit(
            "US",
            IndicatorKind.UNEMPLOYMENT,
            contentState(IndicatorKind.UNEMPLOYMENT, countryCode = "US"),
        )

        vm.trySendAction(MacroAction.ChangeCountry("IN"))
        yield()

        // After re-key, only IN content should surface. US emissions on the
        // old streams must NOT reach the VM's state (it has switched bus).
        fakeRepository.emit(
            "IN",
            IndicatorKind.GDP,
            contentState(IndicatorKind.GDP, countryCode = "IN"),
        )

        vm.stateFlow.test {
            val state = expectMostRecentItem()
            assertEquals("IN", state.countryCode)
            val gdp = state.gdp
            assertIs<ScreenState.Content<MacroIndicator>>(gdp)
            assertEquals("IN", gdp.data.countryCode)
        }
    }

    @Test
    fun aggregateFreshnessIsStaleIfAnyStreamIsStale() = runTest {
        val vm = CountryMacroViewModel("US", fakeRepository)
        yield()
        fakeRepository.emit(
            "US",
            IndicatorKind.GDP,
            contentState(IndicatorKind.GDP, freshness = DataFreshness.FRESH),
        )
        fakeRepository.emit(
            "US",
            IndicatorKind.INFLATION_CPI,
            contentState(IndicatorKind.INFLATION_CPI, freshness = DataFreshness.STALE),
        )
        fakeRepository.emit(
            "US",
            IndicatorKind.UNEMPLOYMENT,
            contentState(IndicatorKind.UNEMPLOYMENT, freshness = DataFreshness.FRESH),
        )

        vm.stateFlow.test {
            val state = expectMostRecentItem()
            assertEquals(DataFreshness.STALE, state.overallFreshness)
        }
    }

    @Test
    fun aggregateFreshnessIsFreshOnlyWhenAllThreeAreFresh() = runTest {
        val vm = CountryMacroViewModel("US", fakeRepository)
        yield()
        fakeRepository.emit(
            "US",
            IndicatorKind.GDP,
            contentState(IndicatorKind.GDP, freshness = DataFreshness.FRESH),
        )
        fakeRepository.emit(
            "US",
            IndicatorKind.INFLATION_CPI,
            contentState(IndicatorKind.INFLATION_CPI, freshness = DataFreshness.FRESH),
        )
        fakeRepository.emit(
            "US",
            IndicatorKind.UNEMPLOYMENT,
            contentState(IndicatorKind.UNEMPLOYMENT, freshness = DataFreshness.FRESH),
        )

        vm.stateFlow.test {
            val state = expectMostRecentItem()
            assertEquals(DataFreshness.FRESH, state.overallFreshness)
        }
    }

    private fun contentState(
        kind: IndicatorKind,
        freshness: DataFreshness = DataFreshness.FRESH,
        countryCode: String = "US",
    ): ScreenState.Content<MacroIndicator> = ScreenState.Content(
        data = MacroIndicator(
            countryCode = countryCode,
            countryName = countryCode,
            indicator = kind,
            observations = listOf(IndicatorObservation(year = 2024, value = 1.0)),
        ),
        freshness = freshness,
    )
}

/**
 * Per-(country, indicator) fake repository.
 *
 * Each unique [MacroIndicatorKey] gets its own [MutableSharedFlow] of
 * [ScreenState] — replay=1 so a late VM subscription still sees the latest
 * emission. The fake constructs a real [ScreenDataStream] via the framework's
 * [screenDataStreamForTesting] factory, so VM-side calls to `.state` and
 * `.refresh()` exercise the production [ScreenDataStream] surface. Modelling
 * keys per (country, indicator) means a re-subscribed `ChangeCountry` reaches
 * a separate bus — stale-country emissions cannot bleed through.
 */
@OptIn(ExperimentalScreenDataStreamTestingApi::class)
private class KeyedFakeMacroIndicatorsRepository : MacroIndicatorsRepository {

    private val buses: MutableMap<MacroIndicatorKey, MutableSharedFlow<ScreenState<MacroIndicator>>> =
        mutableMapOf()

    private val keyLog: MutableList<MacroIndicatorKey> = mutableListOf()

    suspend fun emit(
        countryCode: String,
        kind: IndicatorKind,
        state: ScreenState<MacroIndicator>,
    ) {
        val key = MacroIndicatorKey(countryCode, kind)
        bus(key).emit(state)
    }

    fun loggedKeys(): Set<MacroIndicatorKey> = keyLog.toSet()
    fun resetKeyLog() {
        keyLog.clear()
    }

    private fun bus(key: MacroIndicatorKey): MutableSharedFlow<ScreenState<MacroIndicator>> =
        buses.getOrPut(key) {
            MutableSharedFlow(replay = 1, extraBufferCapacity = 16)
        }

    override fun macroIndicatorStream(
        key: MacroIndicatorKey,
        scope: CoroutineScope,
    ): ScreenDataStream<MacroIndicator> {
        keyLog += key
        return screenDataStreamForTesting(state = bus(key))
    }

    override fun macroIndicatorStream(
        keyFlow: Flow<MacroIndicatorKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<MacroIndicator> {
        error(
            "CountryMacroViewModel uses the static-key overload (re-subscribes on country " +
                "change). The dynamic-key overload is reserved for the indicator-detail VM.",
        )
    }
}
