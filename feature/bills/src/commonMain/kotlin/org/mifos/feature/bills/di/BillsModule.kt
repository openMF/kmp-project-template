/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.bills.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.mifos.core.data.di.OutboxQualifiers
import org.mifos.feature.bills.notification.BillNotificationGateway
import org.mifos.feature.bills.notification.BillNotificationGatewayImpl
import org.mifos.feature.bills.ui.BillRemindersListViewModel
import org.mifos.feature.bills.ui.EditBillReminderViewModel

/**
 * Koin module for the Bill Reminders feature.
 *
 * Bindings:
 *  - `BillNotificationGateway` — feature-local seam over the platform scheduler. Resolves
 *    the platform `BillReminderScheduler` from `template.core.base.platform.di.notificationModule`
 *    (included by the shared `platformModule`).
 *  - List ViewModel — parameter-less.
 *  - Edit ViewModel — takes the nullable `billId` via Koin's `parameters` channel so
 *    navigation passes the route argument straight through.
 */
val BillsModule = module {
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
