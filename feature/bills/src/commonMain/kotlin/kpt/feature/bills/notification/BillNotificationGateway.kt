/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.bills.notification

import kpt.core.platform.notification.bill.BillReminderSchedule
import kpt.core.platform.notification.bill.BillReminderScheduler

/**
 * Feature-local boundary over [BillReminderScheduler].
 *
 * The platform scheduler is an `expect class`, which:
 *  - cannot be subclassed (so tests can't use a custom impl),
 *  - exposes platform-specific constructor shapes (Android needs a Context).
 *
 * `BillNotificationGateway` is a tiny `interface` over the same three methods so the
 * ViewModel layer can depend on something MOCKABLE in tests while production wiring
 * still routes to the real platform actual via [BillNotificationGatewayImpl].
 *
 * Keeping this interface scoped to `feature/bills` (instead of pushing it into
 * `core-base/platform`) keeps the framework primitive lean and the abstraction
 * cost owned by the feature that needs the seam.
 */
interface BillNotificationGateway {

    /** Schedule (or replace) a notification for the given bill. */
    suspend fun schedule(bill: BillReminderSchedule)

    /** Cancel a single bill's pending notification. */
    suspend fun cancel(billId: String)

    /** Cancel every pending bill-reminder notification. */
    suspend fun cancelAll()
}

/**
 * Production gateway — thin delegate to the platform actual. Wired in [kpt.feature.bills.di.BillsModule].
 */
class BillNotificationGatewayImpl(
    private val delegate: BillReminderScheduler,
) : BillNotificationGateway {
    override suspend fun schedule(bill: BillReminderSchedule) = delegate.schedule(bill)
    override suspend fun cancel(billId: String) = delegate.cancel(billId)
    override suspend fun cancelAll() = delegate.cancelAll()
}
