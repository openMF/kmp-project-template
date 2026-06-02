/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.bills.testing

import kpt.core.platform.notification.bill.BillReminderSchedule
import kpt.feature.bills.notification.BillNotificationGateway

/**
 * Recording fake [BillNotificationGateway] for ViewModel unit tests.
 *
 * Captures every call so the VM tests can assert on what the scheduling layer was asked
 * to do, without bringing WorkManager / UNUserNotificationCenter into the test classpath.
 */
internal class FakeBillReminderScheduler : BillNotificationGateway {

    /** Every `schedule(...)` call, newest last. */
    val scheduled: MutableList<BillReminderSchedule> = mutableListOf()

    /** Every `cancel(...)` call's billId, newest last. */
    val cancelledIds: MutableList<String> = mutableListOf()

    /** Number of `cancelAll()` invocations. */
    var cancelAllCount: Int = 0
        private set

    override suspend fun schedule(bill: BillReminderSchedule) {
        scheduled.add(bill)
    }

    override suspend fun cancel(billId: String) {
        cancelledIds.add(billId)
    }

    override suspend fun cancelAll() {
        cancelAllCount++
    }
}
