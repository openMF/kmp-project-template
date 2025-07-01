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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kmp_project_template.feature.settings.generated.resources.Res
import kmp_project_template.feature.settings.generated.resources.feature_settings_change_theme_placeholder_text
import kmp_project_template.feature.settings.generated.resources.feature_settings_change_theme_text
import org.jetbrains.compose.resources.stringResource
import org.mifos.core.designsystem.icon.AppIcons
import org.mifos.core.ui.scaffold.KptScaffold
import template.core.base.analytics.AnalyticsHelper
import template.core.base.analytics.TrackScreenView
import template.core.base.analytics.rememberAnalyticsHelper

@Composable
internal fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val analyticsHelper = rememberAnalyticsHelper()
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = {
                analyticsHelper.logSettingsDialogVisible(false)
                showSettingsDialog = false
            },
        )
    }

    SettingsScreenContent(
        modifier = modifier.fillMaxSize(),
        onBackClick = onBackClick,
        onThemeCardClick = {
            analyticsHelper.logSettingsDialogVisible(true)
            showSettingsDialog = true
        },
    )

    TrackScreenView(screenName = "SettingsScreen")
}

@Composable
internal fun SettingsScreenContent(
    onBackClick: () -> Unit,
    onThemeCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KptScaffold(
        title = "Settings",
        onNavigationIconClick = onBackClick,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // SettingsScreenContent
            ThemeCard(onClick = onThemeCardClick)
        }
    }
}

@Composable
internal fun ThemeCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = AppIcons.Sun,
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clip(shape = RoundedCornerShape(50.dp)),
            )
            Text(
                text = stringResource(Res.string.feature_settings_change_theme_text),
                modifier = Modifier.weight(1F),
            )
            IconButton(
                onClick = onClick,
            ) {
                Icon(
                    imageVector = AppIcons.ArrowRight,
                    contentDescription = stringResource(Res.string.feature_settings_change_theme_placeholder_text),
                )
            }
        }
    }
}

private fun AnalyticsHelper.logSettingsDialogVisible(visible: Boolean) {
    logEvent(
        type = "settings_dialog_visible",
        params = mapOf("visible" to visible.toString()),
    )
}
