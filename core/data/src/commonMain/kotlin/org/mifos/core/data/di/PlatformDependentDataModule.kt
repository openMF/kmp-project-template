/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.di

import org.koin.core.module.Module
import org.mifos.core.data.utils.NetworkMonitor
import org.mifos.core.data.utils.TimeZoneMonitor

interface PlatformDependentDataModule {
    val networkMonitor: NetworkMonitor

    val timeZoneMonitor: TimeZoneMonitor
}

expect val platformModule: Module

expect val getPlatformDataModule: PlatformDependentDataModule
