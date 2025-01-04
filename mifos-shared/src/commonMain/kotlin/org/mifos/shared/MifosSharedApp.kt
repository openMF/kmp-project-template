/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import org.mifos.core.data.utils.NetworkMonitor
import org.mifos.core.data.utils.TimeZoneMonitor
import org.mifos.core.designsystem.theme.MifosTheme
import org.mifos.shared.navigation.MifosNavGraph.MAIN_GRAPH
import org.mifos.shared.navigation.RootNavGraph

@Composable
fun MifosSharedApp(
    networkMonitor: NetworkMonitor = koinInject(),
    timeZoneMonitor: TimeZoneMonitor = koinInject(),
    modifier: Modifier = Modifier,
) {
    MifosApp(
        networkMonitor = networkMonitor,
        timeZoneMonitor = timeZoneMonitor,
        modifier = modifier,
    )
}

@Composable
private fun MifosApp(
    networkMonitor: NetworkMonitor,
    timeZoneMonitor: TimeZoneMonitor,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    MifosTheme {
        RootNavGraph(
            networkMonitor = networkMonitor,
            timeZoneMonitor = timeZoneMonitor,
            navHostController = navController,
            startDestination = MAIN_GRAPH,
            modifier = modifier,
        )
    }
}
