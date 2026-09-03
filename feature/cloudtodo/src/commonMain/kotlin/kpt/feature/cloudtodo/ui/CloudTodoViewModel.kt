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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.demo.cloudtodo.CloudTodoRepository
import kpt.core.model.demo.cloudtodo.CloudTodo

/**
 * MUTABLE-archetype write-path ViewModel — the reference implementation for driving
 * [kpt.core.base.store.mutation.MutationGateway] from a screen.
 *
 * The read side is an ordinary offline-first [ScreenDataStream]; the write side demonstrates the two
 * [kpt.core.base.store.mutation.MutationPolicy] arms and surfaces the returned [MutationResult]
 * exhaustively. That exhaustiveness is the point: `MutationResult` is a sealed interface precisely so
 * a ViewModel cannot silently swallow an offline write or a conflict, and mapping every arm to a
 * rendered [MutationOutcome] is what proves it.
 *
 * Spec: `idea-layer/screens/cloudtodo/{docs,ui,flow,api,tests}.yaml`.
 */
class CloudTodoViewModel(
    private val repository: CloudTodoRepository,
) : BaseViewModel<Unit, Nothing, CloudTodoAction>(Unit) {

    /** Read side — CACHE_FIRST_SWR stream over the read Store (shares `cloud_todos` with the write store). */
    val todo: ScreenDataStream<CloudTodo> = repository.todoStream(DEMO_TODO_ID, viewModelScope)

    private val _lastOutcome = MutableStateFlow<MutationOutcome?>(null)

    /** Last write outcome. Rendered verbatim so every [MutationResult] arm is observable on device. */
    val lastOutcome: StateFlow<MutationOutcome?> = _lastOutcome.asStateFlow()

    override fun handleAction(action: CloudTodoAction) {
        when (action) {
            is CloudTodoAction.ToggleOptimistic -> viewModelScope.launch {
                repository.toggleCompleted(action.todo)
                // Optimistic writes land locally first and queue their network leg, so there is no
                // MutationResult to surface — the re-emitted read stream IS the feedback. That
                // asymmetry between the two policies is exactly what the demo exists to show.
                _lastOutcome.value = MutationOutcome.OptimisticApplied
            }

            is CloudTodoAction.CompleteOnline -> viewModelScope.launch {
                _lastOutcome.value = repository.completeOnline(action.todo).toOutcome()
            }

            CloudTodoAction.DismissOutcome -> _lastOutcome.value = null
        }
    }

    fun onToggleOptimistic(todo: CloudTodo) = trySendAction(CloudTodoAction.ToggleOptimistic(todo))

    fun onCompleteOnline(todo: CloudTodo) = trySendAction(CloudTodoAction.CompleteOnline(todo))

    fun onDismissOutcome() = trySendAction(CloudTodoAction.DismissOutcome)

    private companion object {
        /** JSONPlaceholder always serves /todos/1 — a stable id for a demo surface. */
        const val DEMO_TODO_ID = 1
    }
}

/**
 * Screen-facing projection of [MutationResult]. The `when` is exhaustive on purpose — adding a new
 * result arm must not compile until it has somewhere to render.
 */
internal fun MutationResult<CloudTodo>.toOutcome(): MutationOutcome = when (this) {
    is MutationResult.Applied ->
        if (synced) MutationOutcome.AppliedSynced else MutationOutcome.AppliedQueued
    is MutationResult.Blocked -> MutationOutcome.Blocked(reason)
    is MutationResult.Conflicted -> MutationOutcome.Conflicted(conflictId)
    is MutationResult.Failed -> MutationOutcome.Failed(cause.message ?: "unknown", rolledBack)
}

/** Renderable outcome of the last write. */
sealed interface MutationOutcome {
    /** Optimistic local write landed; the network leg is queued and retried by the syncer. */
    data object OptimisticApplied : MutationOutcome

    /** Applied AND confirmed by the server. */
    data object AppliedSynced : MutationOutcome

    /** Applied locally, network sync still queued (optimistic, offline). */
    data object AppliedQueued : MutationOutcome

    /** Online-required write refused — nothing was written. */
    data class Blocked(val reason: BlockReason) : MutationOutcome

    /** Server diverged; server-wins applied, the user's version is in the conflict inbox. */
    data class Conflicted(val conflictId: String) : MutationOutcome

    /** Permanent failure. [rolledBack] is true when the optimistic local write was undone. */
    data class Failed(val message: String, val rolledBack: Boolean) : MutationOutcome
}

/** One-shot actions accepted by [CloudTodoViewModel]. */
sealed interface CloudTodoAction {
    data class ToggleOptimistic(val todo: CloudTodo) : CloudTodoAction
    data class CompleteOnline(val todo: CloudTodo) : CloudTodoAction
    data object DismissOutcome : CloudTodoAction
}
