/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.alerts

import kotlinx.coroutines.flow.Flow
import org.mifos.core.model.alerts.PriceAlert

/**
 * Repository for the Price Alerts feature.
 *
 * Read side: observable list of committed alerts (from the Store-backed [AlertsRepository.alertsStream]).
 * Pending drafts (in the framework outbox) are tracked at the form-handler
 * level via `DraftSubmitHandler` — the `framework_submit_drafts` row for
 * formKey `"price_alert"` indicates an in-flight or failed submission, surfaced
 * to the user via the form screen's `DraftResumeBanner` rather than the list.
 *
 * Write side: [submitAlert] is the suspend block passed to
 * `DraftSubmitHandler.submit { repo.submitAlert(payload) }`. On network failure
 * the handler persists the payload to the outbox; on reconnect, the
 * [template.core.base.store.submit.OfflineSubmitSyncer] retries it through the
 * same block.
 */
interface AlertsRepository {

    /** Reactive list of server-committed alerts, newest first. */
    fun alertsStream(): Flow<List<PriceAlert>>

    /**
     * Direct API submit — used by `DraftSubmitHandler`'s block parameter and by
     * `OfflineSubmitSyncer` for reconnect retries. Throws on failure; the
     * handler / syncer route the error to the outbox.
     */
    suspend fun submitAlert(alert: PriceAlert): PriceAlert

    /** Delete by id. */
    suspend fun deleteAlert(id: String)
}
