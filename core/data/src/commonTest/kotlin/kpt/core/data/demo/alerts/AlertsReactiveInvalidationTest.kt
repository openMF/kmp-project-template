/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.alerts

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kpt.core.data.demo.alerts.impl.AlertsRepositoryImpl
import kpt.core.model.demo.alerts.AlertDirection
import kpt.core.model.demo.alerts.PriceAlert
import kpt.core.store.demo.alerts.impl.provideAlertsStore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks the wasmJs invalidation-bridge wiring for the alerts surface — the ONE fixed surface
 * whose reactive read flows through a Store5 `SourceOfTruth` (`alertsStream()` →
 * `alertsStore.stream(...)`), not a direct DAO flow.
 *
 * [FakeAlertDao] returns a **cold snapshot** [kpt.core.database.demo.alerts.AlertDao.observeAll]
 * that never self-re-emits (modelling Room 3 alpha05 on wasmJs). These assertions can only pass
 * because:
 *  - `provideAlertsStore` wraps its SoT reader in `daoFlow(ALERTS_TABLE) { dao.observeAll() }`, and
 *  - `AlertsRepositoryImpl.submitAlert` / `deleteAlert` wrap their writes in
 *    `notifyingWrite(ALERTS_TABLE) { ... }`.
 *
 * The store reader and the repository writes must agree on the exact `"alerts"` table name — a
 * mismatch would leave the live collector stuck on its first emission. Regression guard proving
 * the fix propagates a re-emit all the way through Store5 to a long-lived dashboard collector.
 *
 * (Uses a cold fake + live Turbine collector — the combination that is race-free where the
 * banking hot-`MutableStateFlow` reactive tests are `@Ignore`d.)
 */
class AlertsReactiveInvalidationTest {

    private val dao = FakeAlertDao()
    private val repo: AlertsRepository = AlertsRepositoryImpl(
        alertsStore = provideAlertsStore(dao),
        alertDao = dao,
    )

    @Test
    fun alertsStreamReEmitsAcrossSubmitAndDelete() = runTest {
        repo.alertsStream().test {
            assertEquals(emptySet(), awaitItem().map { it.id }.toSet())
            repo.submitAlert(sampleAlert("a1"))
            assertEquals(setOf("a1"), awaitItem().map { it.id }.toSet())
            repo.submitAlert(sampleAlert("a2"))
            assertEquals(setOf("a1", "a2"), awaitItem().map { it.id }.toSet())
            repo.deleteAlert("a1")
            assertEquals(setOf("a2"), awaitItem().map { it.id }.toSet())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun sampleAlert(id: String): PriceAlert = PriceAlert(
        id = id,
        coinId = "coin-$id",
        direction = AlertDirection.ABOVE,
        targetValue = 100.0,
        createdAtMs = 1_700_000_000_000L,
    )
}
