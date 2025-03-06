/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.corebase.datastore

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.SuspendSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.coroutines.toSuspendSettings
import kotlinx.coroutines.CoroutineDispatcher

@OptIn(ExperimentalSettingsApi::class)
object SettingsFactory {

    fun createSuspendSettings(
        settings: Settings,
        dispatcher: CoroutineDispatcher,
    ): SuspendSettings {
        return settings.toSuspendSettings(dispatcher = dispatcher)
    }

    fun createFlowSettings(
        settings: Settings,
        dispatcher: CoroutineDispatcher,
    ): FlowSettings {
        val observableSettings: ObservableSettings = settings as ObservableSettings
        return observableSettings.toFlowSettings(dispatcher = dispatcher)
    }
}
