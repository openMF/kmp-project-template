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

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import kpt.core.data.di.OutboxQualifiers
import kpt.core.platform.notification.bill.di.notificationModule
import kpt.feature.bills.notification.BillNotificationGateway
import kpt.feature.bills.notification.BillNotificationGatewayImpl
import kpt.feature.bills.ui.BillRemindersListViewModel
import kpt.feature.bills.ui.EditBillReminderViewModel

/**
 * Koin module for the Bill Reminders feature.
 *
 * Bindings:
 *  - `BillNotificationGateway` — feature-local seam over the platform scheduler. Resolves
 *    the platform `BillReminderScheduler` from
 *    `kpt.core.platform.notification.bill.di.notificationModule` (pulled in here via
 *    `includes()` rather than the shared `platformModule`, since bill-reminder wiring is
 *    banking-domain-specific and now lives in `core/platform`).
 *  - List ViewModel — parameter-less.
 *  - Edit ViewModel — takes the nullable `billId` via Koin's `parameters` channel so
 *    navigation passes the route argument straight through.
 */
val BillsModule = module {
    includes(notificationModule)

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
