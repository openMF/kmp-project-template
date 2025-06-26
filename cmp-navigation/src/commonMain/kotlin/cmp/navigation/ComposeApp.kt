/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import cmp.navigation.navigation.RootNavGraph
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.data.utils.NetworkMonitor
import org.mifos.core.data.utils.TimeZoneMonitor
import org.mifos.core.designsystem.theme.MifosTheme

@Composable
fun ComposeApp(
    modifier: Modifier = Modifier,
    networkMonitor: NetworkMonitor = koinInject(),
    timeZoneMonitor: TimeZoneMonitor = koinInject(),
) {
    val viewModel: AppViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isSystemInDarkTheme = isSystemInDarkTheme()

    MifosTheme(
        darkTheme = uiState.shouldUseDarkTheme(isSystemInDarkTheme),
        androidTheme = uiState.shouldUseAndroidTheme,
        useDynamicColor = uiState.shouldDisplayDynamicTheming,
    ) {
        RootNavGraph(
            networkMonitor = networkMonitor,
            timeZoneMonitor = timeZoneMonitor,
            navHostController = rememberNavController(),
            modifier = modifier,
        )
    }
}
