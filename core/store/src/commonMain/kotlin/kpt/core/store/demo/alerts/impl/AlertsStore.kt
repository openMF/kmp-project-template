/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.alerts.impl

import kotlinx.coroutines.flow.map
import kpt.core.base.database.invalidation.daoFlow
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.demo.alerts.AlertDao
import kpt.core.database.demo.alerts.AlertEntity
import kpt.core.model.demo.alerts.PriceAlert
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Build an offline-only [Store] for price alerts.
 *
 * Backed exclusively by [AlertDao] — there is no remote fetcher. Alerts are
 * user-defined and stored locally; the repository layer writes them via the
 * DAO's [AlertDao.upsert] / [AlertDao.upsertAll] mutations and the Store
 * surfaces changes reactively.
 *
 * Key: [Unit] — always returns all alerts (no per-alert keyed filter here;
 * per-id lookup goes through the repository directly).
 *
 * The DAO reader is wrapped with [daoFlow] so wasmJs collectors re-emit after writes
 * even when Room 3 alpha05's async InvalidationTracker fails to fan out (see
 * `core-base/database/.../invalidation/README.md`). The paired write notification is
 * emitted by `AlertsRepositoryImpl`'s [kpt.core.base.database.invalidation.notifyingWrite]
 * on `alertDao.upsert` / `deleteById`. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 */
fun provideAlertsStore(dao: AlertDao): Store<Unit, List<PriceAlert>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        // Emit the DOMAIN model — the entity→domain map lives in the SourceOfTruth (read-path contract).
        reader = { _: Unit -> daoFlow(ALERTS_TABLE) { dao.observeAll() }.map { rows -> rows.map(AlertEntity::toPriceAlert) } },
        writer = { _: Unit, alerts: List<PriceAlert> -> dao.upsertAll(alerts.map(PriceAlert::toAlertEntity)) },
        delete = { _: Unit -> dao.deleteAll() },
        deleteAll = { dao.deleteAll() },
    ),
)

/** Room `@Entity(tableName = …)` for [AlertEntity]. Shared with the repository's writes. */
private const val ALERTS_TABLE = "alerts"
