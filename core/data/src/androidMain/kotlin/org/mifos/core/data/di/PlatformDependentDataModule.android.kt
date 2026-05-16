/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.mifos.core.data.infra.TimeZoneMonitor
import org.mifos.core.data.infra.impl.TimeZoneMonitorImpl
import template.core.base.common.di.CommonModule

actual val platformModule: Module = module {
    includes(CommonModule)

    singleOf(::TimeZoneMonitorImpl) bind TimeZoneMonitor::class
}
