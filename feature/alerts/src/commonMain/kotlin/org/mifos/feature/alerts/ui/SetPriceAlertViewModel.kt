/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.alerts.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.mifos.core.data.alerts.AlertsRepository
import org.mifos.core.model.alerts.AlertDirection
import org.mifos.core.model.alerts.PriceAlert
import template.core.base.store.submit.SubmitOutbox
import template.core.base.store.submit.SubmitState
import template.core.base.ui.viewmodel.BaseDraftMutationViewModel
import kotlin.random.Random
import kotlin.time.Clock

/**
 * **Canonical `BaseDraftMutationViewModel` showcase.**
 *
 * Demonstrates the framework's offline-resilient form submission via the unified mutation base
 * (introduced by `p1-framework-prerequisites/02-base-mutation-vm-param`). When the user taps
 * Save while online the alert is sent via [AlertsRepository.submitAlert]; when offline the
 * payload persists in `framework_submit_drafts` (`formKey = "price_alert"`) and the
 * `OfflineSubmitSyncer` retries it on the next online transition.
 *
 * Form state (`coinId`, `direction`, `targetValue`, `enabled`) lives in [formState] — pre-submit
 * ephemeral local state, NOT a derived `ScreenState<T>` from a stream. The screen edits via
 * `onCoinIdChange` etc. and triggers the snapshot-and-submit via [onSubmit].
 *
 * Submit lifecycle (Idle / Submitting / Submitted / Failed) is exposed via the inherited
 * [uiState] (a [template.core.base.store.submit.MutationUiState] whose `.submit` field
 * carries the [SubmitState]).
 *
 * **Single-pending mode**: `uniqueKey` is `null` (default) — one PENDING draft per `formKey`.
 * If we ever ship per-coin price alerts where N drafts can coexist offline, switch to
 * passing `uniqueKey = coinId` per ViewModel instance.
 */
class SetPriceAlertViewModel(
    private val repository: AlertsRepository,
    outbox: SubmitOutbox<PriceAlert>,
) : BaseDraftMutationViewModel<PriceAlert, PriceAlert>(
    outbox = outbox,
    formKey = "price_alert",
    autoSaveDraft = true,
) {

    private val _formState = MutableStateFlow(PriceAlertFormState())
    val formState: StateFlow<PriceAlertFormState> = _formState.asStateFlow()

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

    /**
     * Validate, snapshot to a [PriceAlert] payload, then delegate to the base's
     * `onSubmit(payload)`. Overload (parameter-less) — does NOT override `super.onSubmit(payload)`.
     */
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
        super.onSubmit(payload)
    }

    /** Reset both submit state (super) and form fields (VM-local). */
    override fun onDismissResult() {
        super.onDismissResult()
        _formState.value = PriceAlertFormState()
    }

    /** Repository implementation called by the inherited draft handler. */
    override suspend fun performSubmit(payload: PriceAlert): PriceAlert =
        repository.submitAlert(payload)

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
