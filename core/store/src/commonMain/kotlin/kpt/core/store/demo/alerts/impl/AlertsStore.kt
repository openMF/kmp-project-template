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

import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.demo.alerts.AlertDao
import kpt.core.database.demo.alerts.AlertEntity
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
 */
fun provideAlertsStore(dao: AlertDao): Store<Unit, List<AlertEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.observeAll() },
        writer = { _: Unit, alerts: List<AlertEntity> -> dao.upsertAll(alerts) },
        delete = { _: Unit -> dao.deleteAll() },
        deleteAll = { dao.deleteAll() },
    ),
)
