/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.cloudtodo.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.cloudtodo.CloudTodoRepository
import kpt.core.model.demo.cloudtodo.CloudTodo

/**
 * In-memory [CloudTodoRepository] for feature tests.
 *
 * The read side is a real [ScreenDataStream] built by the framework's opt-in testing factory, so the
 * screen renders through the SAME `ScreenContent(stream = …)` path as production rather than a
 * bespoke fake — the states under test are genuine [ScreenState] values.
 *
 * The write side records calls and returns a scripted [MutationResult], which is what lets a test
 * drive each arm (Applied / Blocked / Conflicted / Failed) deterministically without a network.
 */
@OptIn(ExperimentalScreenDataStreamTestingApi::class)
class FakeCloudTodoRepository(
    initial: ScreenState<CloudTodo> = ScreenState.Content(CloudTodo(id = 1, title = "demo", completed = false)),
) : CloudTodoRepository {

    private val stateFlow = MutableStateFlow(initial)
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 8)

    /** Calls recorded from the Optimistic path. */
    val toggled = mutableListOf<CloudTodo>()

    /** Calls recorded from the OnlineRequired path. */
    val completedOnline = mutableListOf<CloudTodo>()

    /** Scripted result for [completeOnline] — set per-test to exercise a specific arm. */
    var nextResult: MutationResult<CloudTodo> =
        MutationResult.Applied(CloudTodo(id = 1, title = "demo", completed = true), synced = true)

    override fun todoStream(id: Int, scope: CoroutineScope): ScreenDataStream<CloudTodo> =
        screenDataStreamForTesting(state = stateFlow, refreshTrigger = refreshTrigger)

    override suspend fun toggleCompleted(todo: CloudTodo) {
        toggled += todo
    }

    override suspend fun completeOnline(todo: CloudTodo): MutationResult<CloudTodo> {
        completedOnline += todo
        return nextResult
    }

    /** Push a new read state (e.g. to assert an optimistic write re-emitted). */
    fun emit(state: ScreenState<CloudTodo>) {
        stateFlow.value = state
    }
}
