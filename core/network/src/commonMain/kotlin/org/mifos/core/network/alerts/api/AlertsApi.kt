/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.network.alerts.api

import kotlinx.coroutines.flow.Flow
import org.mifos.core.model.alerts.PriceAlert

/**
 * Server-side API for managing price alerts.
 *
 * The template ships a [FakeAlertsApi] in-memory implementation since no real
 * backend exists. Real forks substitute a Ktorfit-backed implementation pointing
 * at their server. The fake retains a 800 ms simulated latency so the showcase
 * exercises the `DraftSubmitHandler` Submitting state visibly.
 */
interface AlertsApi {

    /** Persist a new alert (or upsert an existing one with the same [PriceAlert.id]). */
    suspend fun create(alert: PriceAlert): PriceAlert

    /** Update an existing alert (e.g., toggle enabled). */
    suspend fun update(alert: PriceAlert): PriceAlert

    /** Delete by id. */
    suspend fun delete(id: String)

    /** Snapshot of all committed alerts. */
    suspend fun list(): List<PriceAlert>

    /** Reactive committed alerts — emits whenever the server-side store changes. */
    fun observe(): Flow<List<PriceAlert>>
}
