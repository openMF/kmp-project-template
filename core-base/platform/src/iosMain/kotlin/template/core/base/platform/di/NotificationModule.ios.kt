/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.platform.di

import org.koin.core.module.Module
import org.koin.dsl.module
import template.core.base.platform.notification.BillReminderScheduler

/** iOS wiring — the scheduler talks to `UNUserNotificationCenter.currentNotificationCenter()`. */
actual val notificationModule: Module = module {
    single { BillReminderScheduler() }
}
