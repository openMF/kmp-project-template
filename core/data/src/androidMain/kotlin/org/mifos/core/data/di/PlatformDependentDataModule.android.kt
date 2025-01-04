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

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mifos.core.common.di.AppDispatchers
import org.mifos.core.data.util.ConnectivityManagerNetworkMonitor
import org.mifos.core.data.util.TimeZoneBroadcastMonitor
import org.mifos.core.data.utils.NetworkMonitor
import org.mifos.core.data.utils.TimeZoneMonitor

val AndroidDataModule = module {
    single<NetworkMonitor> {
        ConnectivityManagerNetworkMonitor(androidContext(), get(named(AppDispatchers.IO.name)))
    }

    single<TimeZoneMonitor> {
        TimeZoneBroadcastMonitor(
            context = androidContext(),
            appScope = get(named("ApplicationScope")),
            ioDispatcher = get(named(AppDispatchers.IO.name)),
        )
    }

    single {
        AndroidPlatformDependentDataModule(
            context = androidContext(),
            dispatcher = get(named(AppDispatchers.IO.name)),
            scope = get(named("ApplicationScope")),
        )
    }
}

actual val platformModule: Module = AndroidDataModule

actual val getPlatformDataModule: PlatformDependentDataModule
    get() = org.koin.core.context.GlobalContext.get().get()
