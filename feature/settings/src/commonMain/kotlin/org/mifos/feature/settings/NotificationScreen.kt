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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.mifos.core.designsystem.component.MifosScaffold

@Composable
internal fun NotificationScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NotificationScreenContent(
        modifier = modifier,
        onBackClick = onBackClick,
    )
}

@Composable
internal fun NotificationScreenContent(
    modifier: Modifier,
    onBackClick: () -> Unit,
) {
    MifosScaffold(
        backPress = onBackClick,
        topBarTitle = "Notification",
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // NotificationScreenContent
            Text(text = "Notification Screen", fontWeight = FontWeight.SemiBold)
        }
    }
}
