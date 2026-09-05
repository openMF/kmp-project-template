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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.error.OfflineException
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.base.store.submit.SubmitOutboxStatus
import kpt.core.data.demo.alerts.AlertsRepository
import kpt.core.model.demo.alerts.AlertDirection
import kpt.core.model.demo.alerts.PriceAlert
import kpt.feature.alerts.testing.InMemorySubmitOutbox
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Locks the [AlertCreateViewModel] contract — the toolkit's canonical `submit_offline_write` demo.
 *
 * The behaviour under test is the OFFLINE-WRITE guarantee: a submit must reach the outbox even
 * when the repository throws, because that persisted row is the only thing `OfflineSubmitSyncer`
 * can replay on reconnect. A submit that goes straight to the repository looks identical while
 * online and silently loses the user's alert while offline — [submitPersistsToTheOutboxEvenWhenTheRepositoryFails]
 * is the guard, and it is the reason this VM extends `BaseMutationViewModel` at all.
 *
 * The form→payload materialization is asserted separately because [AlertFormState] is deliberately
 * a string-typed edit buffer: `targetValueText` is parsed at payload time, so a mid-type "12." must
 * not reach the domain model as anything but a defined value.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalScreenDataStreamTestingApi::class)
class AlertCreateViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1_700_000_000_000L)
    }

    private class FakeAlertsRepository(private val failWith: Throwable? = null) : AlertsRepository {
        val submitted = mutableListOf<PriceAlert>()
        val rows = MutableStateFlow<List<PriceAlert>>(emptyList())

        override fun alertsStream(scope: CoroutineScope): ScreenDataStream<List<PriceAlert>> =
            screenDataStreamForTesting(
                rows.map { if (it.isEmpty()) ScreenState.Empty else ScreenState.Content(it) },
            )

        override suspend fun submitAlert(alert: PriceAlert): PriceAlert {
            failWith?.let { throw it }
            submitted += alert
            rows.value = rows.value + alert
            return alert
        }

        override suspend fun deleteAlert(id: String) {
            rows.value = rows.value.filterNot { it.id == id }
        }
    }

    private fun vmWith(
        repo: FakeAlertsRepository = FakeAlertsRepository(),
        outbox: InMemorySubmitOutbox<PriceAlert> = InMemorySubmitOutbox(),
    ) = AlertCreateViewModel(repo, outbox, fixedClock)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun editsAccumulateOntoTheFormBuffer() = runTest(dispatcher) {
        val vm = vmWith()

        vm.onCoinIdChange("bitcoin")
        vm.onDirectionChange(AlertDirection.BELOW)
        vm.onTargetValueChange("42000")

        val form = vm.formState.value
        assertEquals("bitcoin", form.coinId)
        assertEquals(AlertDirection.BELOW, form.direction)
        assertEquals("42000", form.targetValueText)
        assertTrue(form.canSubmit)
    }

    @Test
    fun submitMaterializesTheFormIntoADomainPayload() = runTest(dispatcher) {
        val repo = FakeAlertsRepository()
        val vm = vmWith(repo)

        vm.onCoinIdChange("  bitcoin  ")
        vm.onDirectionChange(AlertDirection.BELOW)
        vm.onTargetValueChange("42000.5")
        vm.submitForm()
        drain()

        assertEquals(1, repo.submitted.size)
        val alert = repo.submitted.single()
        // The coin id is trimmed — a trailing space would otherwise become part of the API key.
        assertEquals("bitcoin", alert.coinId)
        assertEquals(AlertDirection.BELOW, alert.direction)
        assertEquals(42_000.5, alert.targetValue)
        assertEquals(true, alert.enabled)
        assertEquals(1_700_000_000_000L, alert.createdAtMs)
        assertTrue(alert.id.startsWith("alert-"), "id was '${alert.id}'")
    }

    @Test
    fun unparseableTargetBecomesZeroRatherThanCrashing() = runTest(dispatcher) {
        // "-" is a legal intermediate keystroke that parses to nothing. It must reach the payload
        // as a defined value; `canSubmit` is what keeps it out of the UI, not an exception at
        // materialization time. (Note "12." is NOT this case — Kotlin parses it as 12.0.)
        val repo = FakeAlertsRepository()
        val vm = vmWith(repo)

        vm.onCoinIdChange("bitcoin")
        vm.onTargetValueChange("-")
        vm.submitForm()
        drain()

        assertEquals(0.0, repo.submitted.single().targetValue)
    }

    @Test
    fun networkFailureAutoSavesAReplayableDraft() = runTest(dispatcher) {
        // The whole point of MutationMode.Draft(autoSaveDraft = true): on a NETWORK failure the
        // payload lands in the outbox as PENDING so OfflineSubmitSyncer can replay it on
        // reconnect. No row = the user's alert is simply gone the moment they were offline.
        val outbox = InMemorySubmitOutbox<PriceAlert>()
        val vm = vmWith(FakeAlertsRepository(failWith = OfflineException()), outbox)
        backgroundScope.launch { vm.uiState.collect { } }
        drain()

        vm.onCoinIdChange("bitcoin")
        vm.onTargetValueChange("42000")
        vm.submitForm()
        drain()

        val entry = assertNotNull(
            outbox.entries.singleOrNull(),
            "a network failure must leave a replayable outbox row; entries=${outbox.entries}",
        )
        assertEquals("bitcoin", entry.payload.coinId)
        assertEquals(SubmitOutboxStatus.PENDING, entry.status)
    }

    @Test
    fun nonNetworkFailureDoesNotAutoSaveADraft() = runTest(dispatcher) {
        // The deliberate boundary in DraftSubmitHandler: auto-save is Network-ONLY. A validation or
        // server rejection is not fixed by replaying the identical payload later, so silently
        // queueing it would resubmit a request already known to fail — and show the user a
        // "saved" draft that can never succeed.
        val outbox = InMemorySubmitOutbox<PriceAlert>()
        val vm = vmWith(FakeAlertsRepository(failWith = IllegalArgumentException("rejected")), outbox)
        backgroundScope.launch { vm.uiState.collect { } }
        drain()

        vm.onCoinIdChange("bitcoin")
        vm.onTargetValueChange("42000")
        vm.submitForm()
        drain()

        assertEquals(emptyList(), outbox.entries)
    }

    @Test
    fun resumePreFillsTheFormFromTheSavedDraft() = runTest(dispatcher) {
        // Case 3 of the three-case resume. A resume that does NOT pre-fill hands the user a blank
        // form after a crash while telling them their draft was restored.
        val outbox = InMemorySubmitOutbox<PriceAlert>()
        // MutationMode.Draft observes by (formKey, uniqueKey) — both "alert-create" for this VM.
        // Saving with a null uniqueKey would store a row the ViewModel never sees.
        outbox.saveByUniqueKey(
            formKey = "alert-create",
            uniqueKey = "alert-create",
            payload = PriceAlert(
                id = "alert-existing",
                coinId = "ethereum",
                direction = AlertDirection.BELOW,
                targetValue = 1_500.0,
                createdAtMs = 1_699_000_000_000L,
            ),
        )
        val vm = vmWith(outbox = outbox)
        backgroundScope.launch { vm.uiState.collect { } }
        drain()

        vm.onResume()
        drain()

        val form = vm.formState.value
        assertEquals("ethereum", form.coinId)
        assertEquals(AlertDirection.BELOW, form.direction)
        assertEquals("1500.0", form.targetValueText)
    }
}

/**
 * Drains BOTH the `runTest` job tree and the scopes that live outside it — `viewModelScope` and
 * `backgroundScope`. `advanceUntilIdle()` alone does not start those, so a collector stays
 * unsubscribed and a replay-0 `tryEmit` is dropped with nothing to show for it; `runCurrent()`
 * does. Interleaving both, a few rounds, covers work that re-schedules itself.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun TestScope.drain() {
    repeat(3) {
        runCurrent()
        advanceUntilIdle()
    }
}
