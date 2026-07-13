/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.ui

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
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.economic.MacroIndicatorsRepository
import kpt.core.data.infra.Synchronizer
import kpt.core.model.demo.economic.IndicatorKind
import kpt.core.model.demo.economic.IndicatorObservation
import kpt.core.model.demo.economic.MacroIndicator
import kpt.core.store.demo.economic.impl.MacroIndicatorKey
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Locks the [MacroIndicatorDetailViewModel] single-indicator behavior:
 * - Subscribes to (countryCode, indicatorKind) and surfaces ScreenState as-is.
 * - State emissions map straight through (no extra transformation).
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalScreenDataStreamTestingApi::class)
class MacroIndicatorDetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun subscribesToTheSuppliedCountryAndIndicator() = runTest {
        val repo = SingleKeyFakeRepository()
        MacroIndicatorDetailViewModel(
            countryCode = "DE",
            indicatorKind = IndicatorKind.UNEMPLOYMENT,
            repository = repo,
        )
        yield()
        assertEquals(
            MacroIndicatorKey("DE", IndicatorKind.UNEMPLOYMENT),
            repo.requestedKey,
        )
    }

    @Test
    fun stateMirrorsTheUnderlyingStream() = runTest {
        val repo = SingleKeyFakeRepository()
        val vm = MacroIndicatorDetailViewModel(
            countryCode = "US",
            indicatorKind = IndicatorKind.GDP,
            repository = repo,
        )
        yield()

        val content = ScreenState.Content(
            data = MacroIndicator(
                countryCode = "US",
                countryName = "United States",
                indicator = IndicatorKind.GDP,
                observations = listOf(
                    IndicatorObservation(2023, 1.0),
                    IndicatorObservation(2024, 2.0),
                ),
            ),
        )
        repo.bus.emit(content)

        vm.screenState.test {
            val state = expectMostRecentItem()
            val asContent = assertIs<ScreenState.Content<MacroIndicator>>(state)
            assertEquals(2, asContent.data.observations.size)
        }
    }
}

@OptIn(ExperimentalScreenDataStreamTestingApi::class)
private class SingleKeyFakeRepository : MacroIndicatorsRepository {
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true
    val bus: MutableSharedFlow<ScreenState<MacroIndicator>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 16)
    var requestedKey: MacroIndicatorKey? = null

    override fun macroIndicatorStream(
        key: MacroIndicatorKey,
        scope: CoroutineScope,
        fetchPolicy: FetchPolicy,
    ): ScreenDataStream<MacroIndicator> {
        requestedKey = key
        return screenDataStreamForTesting(state = bus)
    }

    override fun macroIndicatorStream(
        keyFlow: Flow<MacroIndicatorKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<MacroIndicator> = error("not used in this VM")
}
