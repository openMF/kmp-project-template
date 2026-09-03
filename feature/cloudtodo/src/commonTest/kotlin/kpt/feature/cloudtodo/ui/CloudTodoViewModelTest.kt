/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.cloudtodo.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.demo.cloudtodo.CloudTodo
import kpt.feature.cloudtodo.testing.FakeCloudTodoRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Locks the write-path contract of [CloudTodoViewModel] — that EVERY [MutationResult] arm maps to a
 * distinct, rendered [MutationOutcome].
 *
 * This is the coverage that was missing before the feature existed: `MutationResult` is sealed so a
 * ViewModel cannot silently swallow an offline write or a conflict, but with no consumer that
 * exhaustiveness was never exercised. Each arm is asserted here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CloudTodoViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val todo = CloudTodo(id = 1, title = "demo", completed = false)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(repo: FakeCloudTodoRepository) = CloudTodoViewModel(repo)

    @Test
    fun noOutcomeBeforeAnyWrite() = runTest {
        assertNull(vm(FakeCloudTodoRepository()).lastOutcome.value)
    }

    @Test
    fun optimisticToggleReachesTheRepositoryAndReportsAppliedLocally() = runTest {
        val repo = FakeCloudTodoRepository()
        val viewModel = vm(repo)

        viewModel.onToggleOptimistic(todo)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(todo), repo.toggled, "the optimistic write must reach the repository")
        assertEquals(MutationOutcome.OptimisticApplied, viewModel.lastOutcome.value)
        assertEquals(emptyList(), repo.completedOnline, "an optimistic toggle must NOT take the online path")
    }

    @Test
    fun onlineRequiredAppliedSyncedMapsToAppliedSynced() = runTest {
        val repo = FakeCloudTodoRepository()
        repo.nextResult = MutationResult.Applied(todo.copy(completed = true), synced = true)
        val viewModel = vm(repo)

        viewModel.onCompleteOnline(todo)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(MutationOutcome.AppliedSynced, viewModel.lastOutcome.value)
    }

    @Test
    fun appliedButUnsyncedMapsToQueuedNotSynced() = runTest {
        // The synced flag is the whole difference between "saved" and "saved, will sync" — collapsing
        // them would tell a user their write reached the server when it has not.
        val repo = FakeCloudTodoRepository()
        repo.nextResult = MutationResult.Applied(todo, synced = false)
        val viewModel = vm(repo)

        viewModel.onCompleteOnline(todo)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(MutationOutcome.AppliedQueued, viewModel.lastOutcome.value)
    }

    @Test
    fun blockedOfflineIsSurfacedWithItsReason() = runTest {
        // The defining behaviour of MutationPolicy.OnlineRequired — refuse rather than write locally.
        val repo = FakeCloudTodoRepository()
        repo.nextResult = MutationResult.Blocked(BlockReason.OFFLINE)
        val viewModel = vm(repo)

        viewModel.onCompleteOnline(todo)
        dispatcher.scheduler.advanceUntilIdle()

        val outcome = viewModel.lastOutcome.value
        assertIs<MutationOutcome.Blocked>(outcome)
        assertEquals(BlockReason.OFFLINE, outcome.reason)
        assertEquals(emptyList(), repo.toggled, "a Blocked online write must not fall back to a local write")
    }

    @Test
    fun everyBlockReasonRoundTrips() = runTest {
        // Guards the enum: a new BlockReason must not silently render as another one.
        BlockReason.entries.forEach { reason ->
            val repo = FakeCloudTodoRepository()
            repo.nextResult = MutationResult.Blocked(reason)
            val viewModel = vm(repo)

            viewModel.onCompleteOnline(todo)
            dispatcher.scheduler.advanceUntilIdle()

            val outcome = viewModel.lastOutcome.value
            assertIs<MutationOutcome.Blocked>(outcome)
            assertEquals(reason, outcome.reason)
        }
    }

    @Test
    fun conflictedCarriesTheConflictIdForTheHandoff() = runTest {
        // The id is what the Resolve action needs to point the user at the right ConflictEntry.
        val repo = FakeCloudTodoRepository()
        repo.nextResult = MutationResult.Conflicted(conflictId = "conflict-42", server = todo)
        val viewModel = vm(repo)

        viewModel.onCompleteOnline(todo)
        dispatcher.scheduler.advanceUntilIdle()

        val outcome = viewModel.lastOutcome.value
        assertIs<MutationOutcome.Conflicted>(outcome)
        assertEquals("conflict-42", outcome.conflictId)
    }

    @Test
    fun failedReportsWhetherTheLocalWriteWasRolledBack() = runTest {
        val repo = FakeCloudTodoRepository()
        repo.nextResult = MutationResult.Failed(IllegalStateException("boom"), rolledBack = true)
        val viewModel = vm(repo)

        viewModel.onCompleteOnline(todo)
        dispatcher.scheduler.advanceUntilIdle()

        val outcome = viewModel.lastOutcome.value
        assertIs<MutationOutcome.Failed>(outcome)
        assertEquals("boom", outcome.message)
        assertEquals(true, outcome.rolledBack, "rollback state changes what the user must do next")
    }

    @Test
    fun dismissClearsTheOutcome() = runTest {
        val repo = FakeCloudTodoRepository()
        val viewModel = vm(repo)
        viewModel.onToggleOptimistic(todo)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onDismissOutcome()
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.lastOutcome.value)
    }
}
