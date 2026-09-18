/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.alerts

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.DataProvider
import kpt.core.base.data.annotation.FromQualifier
import kpt.core.base.data.infra.NetworkMonitor
import kpt.core.base.database.infra.dao.DraftDao
import kpt.core.base.store.infra.impl.RoomSubmitOutbox
import kpt.core.base.store.submit.OfflineSubmitSyncer
import kpt.core.base.store.submit.SubmitOutbox
import kpt.core.model.alerts.PriceAlert

/** Outbox for PriceAlert payloads — RoomSubmitOutbox writes to `framework_submit_drafts`. */
@DataProvider(qualifier = "outbox.priceAlert")
fun providePriceAlertOutbox(dao: DraftDao): SubmitOutbox<PriceAlert> =
    RoomSubmitOutbox(dao = dao, serializer = PriceAlert.serializer())

/**
 * Eager: starts watching online events at Koin start and retries pending alerts on reconnect.
 *
 * Bound as the bare `OfflineSubmitSyncer` type rather than behind a marker class — it is the only
 * syncer declared at this type, so nothing collides with it. The banking syncers need markers
 * precisely because there are two of them.
 */
@DataProvider(createdAtStart = true)
fun providePriceAlertSubmitSyncer(
    scope: CoroutineScope,
    @FromQualifier("outbox.priceAlert") outbox: SubmitOutbox<PriceAlert>,
    networkMonitor: NetworkMonitor,
    repository: AlertsRepository,
): OfflineSubmitSyncer<PriceAlert, PriceAlert> = OfflineSubmitSyncer<PriceAlert, PriceAlert>(
    scope = scope,
    outbox = outbox,
    networkStatusFlow = networkMonitor.networkStatus,
    submitBlock = { payload -> repository.submitAlert(payload) },
).also { it.start() }
