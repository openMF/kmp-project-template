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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.SubmitOutbox
import kpt.core.base.ui.viewmodel.BaseMutationViewModel
import kpt.core.base.ui.viewmodel.MutationMode
import kpt.core.data.demo.alerts.AlertsRepository
import kpt.core.model.demo.alerts.AlertDirection
import kpt.core.model.demo.alerts.PriceAlert
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Create-Price-Alert ViewModel — the toolkit's canonical `submit_offline_write` demo.
 *
 * Offline-first WRITE: the submit is routed through [BaseMutationViewModel]'s (`MutationMode.Draft`)
 * `DraftSubmitHandler`, which persists the [PriceAlert] payload to the
 * `framework_submit_drafts` outbox (`SubmitOutbox<PriceAlert>`, DI-qualified
 * `OutboxQualifiers.PriceAlert`) BEFORE hitting the repository. On network failure the
 * shipped `OfflineSubmitSyncer` retries it on reconnect. [performSubmit] delegates to the
 * pre-existing [AlertsRepository.submitAlert] — this feature is the missing UI over a
 * backing that already existed (repurpose, not rebuild).
 *
 * The editable body lives in [formState]; `mutableScreenState` is a "form ready" sentinel
 * (Content immediately — create has nothing to hydrate). `MutationScreenContent`'s content
 * slot renders [formState], not the read payload.
 */
class AlertCreateViewModel(
    private val repository: AlertsRepository,
    outbox: SubmitOutbox<PriceAlert>,
    private val clock: Clock = Clock.System,
) : BaseMutationViewModel<PriceAlert, PriceAlert>(
    MutationMode.Draft(
        outbox = outbox,
        formKey = FORM_KEY,
        uniqueKey = FORM_KEY,
        autoSaveDraft = true,
    ),
) {

    private val _formState = MutableStateFlow(AlertFormState())
    val formState: StateFlow<AlertFormState> = _formState.asStateFlow()

    init {
        mutableScreenState.value = ScreenState.Content(draftPayload())
    }

    override suspend fun performSubmit(payload: PriceAlert): PriceAlert =
        repository.submitAlert(payload)

    /** Update the coin id field. */
    fun onCoinIdChange(coinId: String) = _formState.update { it.copy(coinId = coinId) }

    /** Toggle the alert direction (ABOVE / BELOW). */
    fun onDirectionChange(direction: AlertDirection) = _formState.update { it.copy(direction = direction) }

    /** Update the target price field. */
    fun onTargetValueChange(target: String) = _formState.update { it.copy(targetValueText = target) }

    /** Submit the current form as a new [PriceAlert] through the offline draft handler. */
    fun submitForm() = onSubmit(draftPayload())

    /** Materialize the current [formState] into a fresh [PriceAlert] payload. */
    private fun draftPayload(): PriceAlert {
        val f = _formState.value
        return PriceAlert(
            id = "alert-${Random.nextLong().toString(RADIX_HEX)}",
            coinId = f.coinId.trim(),
            direction = f.direction,
            targetValue = f.targetValueText.toDoubleOrNull() ?: 0.0,
            enabled = true,
            createdAtMs = clock.now().toEpochMilliseconds(),
        )
    }

    private companion object {
        const val FORM_KEY = "alert-create"
        const val RADIX_HEX = 16
    }
}

/**
 * Editable form body for the create-alert screen. Kept separate from the [PriceAlert]
 * payload so partial/invalid input (`targetValueText` mid-type) never has to round-trip
 * through the domain model.
 */
data class AlertFormState(
    val coinId: String = "",
    val direction: AlertDirection = AlertDirection.ABOVE,
    val targetValueText: String = "",
) {
    /** A submit is only allowed once the coin id and a parseable positive target exist. */
    val canSubmit: Boolean
        get() = coinId.isNotBlank() && (targetValueText.toDoubleOrNull() ?: 0.0) > 0.0
}
