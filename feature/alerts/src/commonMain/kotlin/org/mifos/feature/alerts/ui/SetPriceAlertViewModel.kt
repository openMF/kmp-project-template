/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.alerts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.random.Random
import kotlin.time.Clock
import org.mifos.core.data.alerts.AlertsRepository
import org.mifos.core.model.alerts.AlertDirection
import org.mifos.core.model.alerts.PriceAlert
import template.core.base.store.submit.SubmitOutbox
import template.core.base.store.submit.SubmitState
import template.core.base.store.submit.draftSubmitHandler

/**
 * **Canonical `DraftSubmitHandler` showcase.**
 *
 * This ViewModel demonstrates the framework's offline-resilient form submission
 * pattern: when the user taps Save while online, the alert is sent via
 * [AlertsRepository.submitAlert] immediately. When offline, the payload persists
 * in the framework outbox (`framework_submit_drafts` table, formKey =
 * `"price_alert"`); the `OfflineSubmitSyncer` (wired in `DataModule`) retries
 * it automatically on the next online transition.
 *
 * The ViewModel does NOT extend `BaseMutationViewModel` — instead it uses
 * `draftSubmitHandler` directly because the canonical Mutation base supports
 * `SubmitHandler` (one-shot fire-and-forget), not `DraftSubmitHandler`
 * (persistent outbox). A future framework refactor could unify both, but
 * right now this is the cleaner shape.
 *
 * Form state lives in [formState] (mutable while typing). On Save, the form
 * is snapshotted into a [PriceAlert] payload and submitted via
 * [draftHandler.submit]; [submitState] exposes the Idle/Submitting/Submitted/Failed
 * lifecycle for the screen to render feedback overlays.
 *
 * @param autoSaveDraft When `true`, network failures silently persist the
 *   payload to the outbox (no user prompt). The plan's recommended default for
 *   showcase clarity — fork apps may flip to `false` for explicit user consent.
 */
class SetPriceAlertViewModel(
    private val repository: AlertsRepository,
    private val outbox: SubmitOutbox<PriceAlert>,
) : ViewModel() {

    private val _formState = MutableStateFlow(PriceAlertFormState())
    val formState: StateFlow<PriceAlertFormState> = _formState.asStateFlow()

    private val draftHandler = viewModelScope.draftSubmitHandler<PriceAlert, PriceAlert>(
        outbox = outbox,
        formKey = "price_alert",
        autoSaveDraft = true,
    )

    val submitState: StateFlow<SubmitState<PriceAlert>> = draftHandler.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SubmitState.Idle,
    )

    fun onCoinIdChange(value: String) {
        _formState.update { it.copy(coinId = value) }
    }

    fun onDirectionChange(direction: AlertDirection) {
        _formState.update { it.copy(direction = direction) }
    }

    fun onTargetValueChange(value: Double) {
        _formState.update { it.copy(targetValue = value) }
    }

    fun onEnabledChange(enabled: Boolean) {
        _formState.update { it.copy(enabled = enabled) }
    }

    /** Validate, snapshot to a [PriceAlert] payload, and submit through the draft handler. */
    fun onSubmit() {
        val form = _formState.value
        if (form.coinId.isBlank() || form.targetValue <= 0.0) return
        val payload = PriceAlert(
            id = randomId(),
            coinId = form.coinId.trim(),
            direction = form.direction,
            targetValue = form.targetValue,
            enabled = form.enabled,
            createdAtMs = Clock.System.now().toEpochMilliseconds(),
        )
        draftHandler.submit(payload) { repository.submitAlert(it) }
    }

    fun onRetry() {
        draftHandler.retry()
    }

    fun onDismissResult() {
        draftHandler.reset()
        _formState.value = PriceAlertFormState()
    }

    private fun randomId(): String {
        // Compact client-side ID; replace with UUID4 once kotlinx.uuid or okio.UUID is wired.
        val chars = ('a'..'z') + ('0'..'9')
        return (1..16).map { chars[Random.nextInt(chars.size)] }.joinToString("")
    }
}

/** UI form state — observable so the screen can re-render on each keystroke. */
data class PriceAlertFormState(
    val coinId: String = "",
    val direction: AlertDirection = AlertDirection.ABOVE,
    val targetValue: Double = 0.0,
    val enabled: Boolean = true,
)
