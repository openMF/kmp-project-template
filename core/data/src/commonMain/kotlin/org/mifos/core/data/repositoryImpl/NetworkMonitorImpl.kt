/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.repositoryImpl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mifos.core.data.repository.NetworkMonitor
import org.mifos.core.data.util.connectivityProvider

class NetworkMonitorImpl : NetworkMonitor {
    override val isOnline: Flow<Boolean>
        get() = connectivityProvider.statusUpdates.map { it.isConnected }
}
