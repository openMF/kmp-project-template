/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.store.submit

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import template.core.base.store.error.ErrorCategory
import template.core.base.store.error.categorize

/**
 * Out-of-box form submission handler that automatically persists the payload as a
 * PENDING draft when the network request fails.
 *
 * Drop-in replacement for [SubmitHandler] on screens where the user should be able
 * to resume an interrupted submission later (e.g. after connectivity is restored).
 *
 * **ViewModel usage:**
 * ```kotlin
 * private val draftHandler = viewModelScope.draftSubmitHandler(
 *     outbox     = roomSubmitOutbox,
 *     formKey    = "loan_application",
 * )
 * val submitState = draftHandler.state
 *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubmitState.Idle)
 *
 * fun onSubmit(payload: LoanPayload) = draftHandler.submit(payload) { repository.submit(it) }
 * fun onRetry()                      = draftHandler.retry()
 * fun onDismiss()                    = draftHandler.reset()
 * ```
 *
 * On network failure the payload is saved via [SubmitOutbox.save]. On any subsequent
 * [submit] or [retry] call the outbox row transitions to SUBMITTED or FAILED accordingly.
 *
 * @param P Serializable payload type (the data collected by the form).
 * @param R Result type returned by the server on success.
 */
class DraftSubmitHandler<P, R>(
    private val scope: CoroutineScope,
    private val outbox: SubmitOutbox<P>,
    private val formKey: String,
) {
    private val _state = MutableStateFlow<SubmitState<R>>(SubmitState.Idle)

    /** Observable state — expose via `.stateIn()` in the ViewModel. */
    val state: StateFlow<SubmitState<R>> = _state.asStateFlow()

    private var lastPayload: P? = null
    private var lastBlock: (suspend (P) -> R)? = null
    private var lastDraftId: Long? = null
    private var activeJob: Job? = null

    /**
     * Execute [block] with [payload] as a one-shot submission.
     *
     * - No-op if already [SubmitState.Submitting].
     * - On failure: saves [payload] to [SubmitOutbox] and transitions to [SubmitState.Failed].
     * - On success: marks the draft SUBMITTED (if one exists) and transitions to [SubmitState.Submitted].
     */
    fun submit(payload: P, block: suspend (P) -> R) {
        if (_state.value is SubmitState.Submitting) return
        lastPayload = payload
        lastBlock = block
        execute(payload, block)
    }

    /**
     * Re-run the last submission block with the last payload.
     * No-op if no prior submission or currently [SubmitState.Submitting].
     */
    fun retry() {
        val payload = lastPayload
        val block = lastBlock
        if (payload != null && block != null && _state.value !is SubmitState.Submitting) {
            execute(payload, block)
        }
    }

    /**
     * Return to [SubmitState.Idle] without clearing the outbox draft.
     *
     * **Cancellation behaviour:** if cancelled while [SubmitState.Submitting] and the network
     * call has not yet failed, no draft is saved — the `catch` block is only reached on an
     * exception, not on coroutine cancellation. If a draft was previously saved from an earlier
     * failure, it remains PENDING in the outbox unchanged.
     *
     * This is a UI-only reset: the [SubmitOutbox] is not touched. The draft stays PENDING so
     * [DraftResumeState] can surface it on the next screen visit.
     */
    fun reset() {
        activeJob?.cancel()
        _state.value = SubmitState.Idle
    }

    private fun execute(payload: P, block: suspend (P) -> R) {
        activeJob?.cancel()
        _state.value = SubmitState.Submitting
        activeJob = scope.launch {
            _state.value = try {
                val result = block(payload)
                lastDraftId?.let { outbox.markSubmitted(it) }
                lastDraftId = null
                SubmitState.Submitted(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val category = categorize(e)
                if (category == ErrorCategory.Network) {
                    val draftId = outbox.save(formKey, payload)
                    lastDraftId = draftId
                } else {
                    lastDraftId?.let { outbox.markFailed(it, e.message) }
                }
                SubmitState.Failed(error = e, category = category)
            }
        }
    }
}

/**
 * Creates a [DraftSubmitHandler] bound to this [CoroutineScope] (typically `viewModelScope`).
 *
 * ```kotlin
 * private val draftHandler = viewModelScope.draftSubmitHandler(
 *     outbox  = roomSubmitOutbox,
 *     formKey = "client_registration",
 * )
 * ```
 */
fun <P, R> CoroutineScope.draftSubmitHandler(
    outbox: SubmitOutbox<P>,
    formKey: String,
): DraftSubmitHandler<P, R> = DraftSubmitHandler(this, outbox, formKey)
