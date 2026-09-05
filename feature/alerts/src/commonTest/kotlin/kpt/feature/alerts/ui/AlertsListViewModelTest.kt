/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.alerts.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.alerts.AlertsRepository
import kpt.core.model.demo.alerts.AlertDirection
import kpt.core.model.demo.alerts.PriceAlert
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Locks the [AlertsListViewModel] contract: a straight PASS-THROUGH of the repository's
 * store-backed stream, plus delete routed through the action channel.
 *
 * The VM deliberately exposes the `ScreenDataStream` itself rather than re-wrapping it in a
 * `StateFlow` — that is what lets the screen render `ScreenContent(stream = viewModel.alerts)` and
 * get retry wired for free. A change that re-introduces a projection layer would duplicate state
 * the stream already owns, and the pass-through assertions here are what catch it.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalScreenDataStreamTestingApi::class)
class AlertsListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeAlertsRepository(
        initial: List<PriceAlert> = emptyList(),
    ) : AlertsRepository {
        val rows = MutableStateFlow(initial)
        val deleted = mutableListOf<String>()

        override fun alertsStream(scope: CoroutineScope): ScreenDataStream<List<PriceAlert>> =
            screenDataStreamForTesting(
                rows.map { if (it.isEmpty()) ScreenState.Empty else ScreenState.Content(it) },
            )

        override suspend fun submitAlert(alert: PriceAlert): PriceAlert {
            rows.value = rows.value + alert
            return alert
        }

        override suspend fun deleteAlert(id: String) {
            deleted += id
            rows.value = rows.value.filterNot { it.id == id }
        }
    }

    private fun alert(id: String) = PriceAlert(
        id = id,
        coinId = "btc",
        direction = AlertDirection.ABOVE,
        targetValue = 50_000.0,
        createdAtMs = 1_700_000_000_000L,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun exposesRepositoryContentUnchanged() = runTest {
        val a = alert("A1")
        val vm = AlertsListViewModel(FakeAlertsRepository(listOf(a)))

        val state = vm.alerts.state.first { it != ScreenState.Loading }
        assertEquals(listOf(a), assertIs<ScreenState.Content<List<PriceAlert>>>(state).data)
    }

    @Test
    fun surfacesEmptyForAClearedList() = runTest {
        // The repository's own `isEmpty` yields Empty — the VM must not substitute a placeholder
        // or an empty Content list, or the screen renders a blank list instead of its Empty slot.
        val vm = AlertsListViewModel(FakeAlertsRepository(emptyList()))

        val state = vm.alerts.state.first { it != ScreenState.Loading }
        assertEquals(ScreenState.Empty, state)
    }

    @Test
    fun onDeleteRoutesThroughTheActionChannelToTheRepository() = runTest {
        val repo = FakeAlertsRepository(listOf(alert("A1"), alert("A2")))
        val vm = AlertsListViewModel(repo)

        vm.onDelete("A1")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("A1"), repo.deleted)
        val state = vm.alerts.state.first { it is ScreenState.Content<List<PriceAlert>> }
        assertEquals(
            listOf("A2"),
            assertIs<ScreenState.Content<List<PriceAlert>>>(state).data.map { it.id },
        )
    }
}
