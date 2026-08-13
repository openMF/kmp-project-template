/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.bills.di

import kpt.core.data.di.OutboxQualifiers
import kpt.feature.bills.notification.BillNotificationGateway
import kpt.feature.bills.notification.BillNotificationGatewayImpl
import kpt.feature.bills.ui.BillRemindersListViewModel
import kpt.feature.bills.ui.EditBillReminderViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for the Bill Reminders feature.
 *
 * Bindings:
 *  - `BillNotificationGateway` — feature-local seam that maps bill reminders onto the cross-platform
 *    `sync` `WorkScheduler` (worker-kmp + KMPNotifier), provided by `SyncModule` at app level. No
 *    per-platform scheduler module is included here anymore.
 *  - List ViewModel — parameter-less.
 *  - Edit ViewModel — takes the nullable `billId` via Koin's `parameters` channel so
 *    navigation passes the route argument straight through.
 */
val BillsModule = module {
    // BillNotificationGateway maps bill reminders onto the cross-platform `sync` WorkScheduler
    // (provided by SyncModule) — no per-platform scheduler module to include here anymore.
    single<BillNotificationGateway> { BillNotificationGatewayImpl(get()) }

    viewModel { BillRemindersListViewModel(repository = get(), scheduler = get()) }
    viewModel { (billId: String?) ->
        EditBillReminderViewModel(
            repository = get(),
            scheduler = get(),
            billId = billId,
            outbox = get(qualifier = OutboxQualifiers.BillReminder),
        )
    } bind EditBillReminderViewModel::class
}
