/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.datastore.model.AppLanguage
import org.mifos.core.datastore.model.AppSettings
import org.mifos.core.datastore.model.AppTheme
import org.mifos.core.designsystem.component.MifosScaffold

@Composable
internal fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsScreenContent(
        modifier = modifier.fillMaxSize(),
        onBackClick = onBackClick,
    )
}

@Composable
internal fun SettingsScreenContent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewmodel: SettingsViewmodel = koinViewModel()

    val uiState by viewmodel.settingsUiState.collectAsStateWithLifecycle()

    MifosScaffold(
        topBarTitle = "Settings",
        backPress = onBackClick,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // SettingsScreenContent
            Text(text = "Settings Screen", fontWeight = FontWeight.SemiBold)
            Button(onClick = {
                viewmodel.updateSettings(
                    settings = AppSettings(
                        theme = AppTheme.LIGHT.themeName,
                        language = AppLanguage.ENGLISH.code,
                    ),
                )
            }) {
                Text(text = "Light")
            }
            Button(onClick = {
                viewmodel.updateSettings(
                    settings = AppSettings(
                        theme = AppTheme.DARK.themeName,
                        language = AppLanguage.ENGLISH.code,
                    ),
                )
            }) {
                Text(text = "Dark")
            }
            Text(text = "Theme = ${uiState.theme}")
        }
    }
}
