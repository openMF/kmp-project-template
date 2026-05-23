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

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.mifos.core.model.alerts.PriceAlert

private const val SIMULATED_LATENCY_MS = 800L

/**
 * In-memory [AlertsApi] for template showcase purposes — no real backend.
 *
 * Holds alerts in a [MutableStateFlow]; mutations are simulated with an 800 ms
 * delay so the `DraftSubmitHandler` Submitting state is visible to the user.
 * Replace with a Ktorfit-backed implementation when a real backend is available.
 */
class FakeAlertsApi : AlertsApi {

    private val store = MutableStateFlow<Map<String, PriceAlert>>(emptyMap())

    override suspend fun create(alert: PriceAlert): PriceAlert {
        delay(SIMULATED_LATENCY_MS)
        store.update { it + (alert.id to alert) }
        return alert
    }

    override suspend fun update(alert: PriceAlert): PriceAlert {
        delay(SIMULATED_LATENCY_MS)
        store.update { it + (alert.id to alert) }
        return alert
    }

    override suspend fun delete(id: String) {
        delay(SIMULATED_LATENCY_MS)
        store.update { it - id }
    }

    override suspend fun list(): List<PriceAlert> = store.value.values.toList()

    override fun observe(): Flow<List<PriceAlert>> {
        return store.asStateFlow().let { snapshot ->
            kotlinx.coroutines.flow.flow {
                snapshot.collect { map ->
                    emit(map.values.toList().sortedByDescending { it.createdAtMs })
                }
            }
        }
    }
}
