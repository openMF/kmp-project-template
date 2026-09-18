/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.banking

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.DataProvider
import kpt.core.base.data.annotation.FromQualifier
import kpt.core.base.data.infra.NetworkMonitor
import kpt.core.base.database.infra.dao.DraftDao
import kpt.core.base.store.infra.impl.RoomSubmitOutbox
import kpt.core.base.store.submit.OfflineSubmitSyncer
import kpt.core.base.store.submit.SubmitOutbox
import kpt.core.model.banking.BillReminder
import kpt.core.model.banking.Loan
import kpt.core.model.banking.LoanCalcScenario

/**
 * Banking's submit-path wiring, declared where the feature lives.
 *
 * Each form payload gets its OWN outbox so a formKey collision across features is impossible, and
 * each outbox declares a qualifier: Koin matches `single<SubmitOutbox<*>>` by raw class, not full
 * KType, so unqualified outboxes collapse to whichever registered last.
 */
@DataProvider(qualifier = "outbox.loan")
fun provideLoanOutbox(dao: DraftDao): SubmitOutbox<Loan> =
    RoomSubmitOutbox(dao = dao, serializer = Loan.serializer())

@DataProvider(qualifier = "outbox.billReminder")
fun provideBillReminderOutbox(dao: DraftDao): SubmitOutbox<BillReminder> =
    RoomSubmitOutbox(dao = dao, serializer = BillReminder.serializer())

@DataProvider(qualifier = "outbox.loanCalcScenario")
fun provideLoanCalcScenarioOutbox(dao: DraftDao): SubmitOutbox<LoanCalcScenario> =
    RoomSubmitOutbox(dao = dao, serializer = LoanCalcScenario.serializer())

/**
 * Marker wrapper around the Loan syncer.
 *
 * Exists so Koin resolves this binding by a unique type — a bare `OfflineSubmitSyncer<*, *>` erases
 * to one runtime class across every payload and would collide with the other syncers.
 */
class LoanSubmitSyncer internal constructor(
    @Suppress("unused") val syncer: OfflineSubmitSyncer<Loan, Loan>,
)

/** Marker wrapper around the BillReminder syncer — same erasure reason as [LoanSubmitSyncer]. */
class BillReminderSubmitSyncer internal constructor(
    @Suppress("unused") val syncer: OfflineSubmitSyncer<BillReminder, BillReminder>,
)

/**
 * Eager: a syncer built only on first injection never starts watching for reconnects, so the
 * backlog it exists to drain is never drained. For a purely-local feature the submit target is the
 * repository itself — `networkStatusFlow` is still required by the syncer contract.
 */
@DataProvider(createdAtStart = true)
fun provideLoanSubmitSyncer(
    scope: CoroutineScope,
    @FromQualifier("outbox.loan") outbox: SubmitOutbox<Loan>,
    networkMonitor: NetworkMonitor,
    repository: LoanRepository,
): LoanSubmitSyncer = LoanSubmitSyncer(
    syncer = OfflineSubmitSyncer<Loan, Loan>(
        scope = scope,
        outbox = outbox,
        networkStatusFlow = networkMonitor.networkStatus,
        submitBlock = { payload ->
            repository.upsert(payload)
            payload
        },
    ).also { it.start() },
)

@DataProvider(createdAtStart = true)
fun provideBillReminderSubmitSyncer(
    scope: CoroutineScope,
    @FromQualifier("outbox.billReminder") outbox: SubmitOutbox<BillReminder>,
    networkMonitor: NetworkMonitor,
    repository: BillReminderRepository,
): BillReminderSubmitSyncer = BillReminderSubmitSyncer(
    syncer = OfflineSubmitSyncer<BillReminder, BillReminder>(
        scope = scope,
        outbox = outbox,
        networkStatusFlow = networkMonitor.networkStatus,
        submitBlock = { payload ->
            repository.upsert(payload)
            payload
        },
    ).also { it.start() },
)
