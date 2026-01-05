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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.core.common.DateHelper
import org.mifos.core.designsystem.icon.AppIcons
import org.mifos.core.model.TimeBasedTheme
import org.mifos.core.ui.scaffold.KptScaffold
import org.mifos.feature.settings.generated.resources.Res
import org.mifos.feature.settings.generated.resources.feature_settings_apply_theme
import org.mifos.feature.settings.generated.resources.feature_settings_cancel
import org.mifos.feature.settings.generated.resources.feature_settings_change_theme_placeholder_text
import org.mifos.feature.settings.generated.resources.feature_settings_change_theme_text
import org.mifos.feature.settings.generated.resources.feature_settings_choose_dark_mode_time
import org.mifos.feature.settings.generated.resources.feature_settings_dark_mode_ends_at
import org.mifos.feature.settings.generated.resources.feature_settings_dark_mode_starts_at
import org.mifos.feature.settings.generated.resources.feature_settings_ok
import org.mifos.feature.settings.generated.resources.feature_settings_title
import template.core.base.analytics.AnalyticsHelper
import template.core.base.analytics.TrackScreenView
import template.core.base.analytics.rememberAnalyticsHelper

@Composable
internal fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewmodel = koinViewModel(),
) {
    val analyticsHelper = rememberAnalyticsHelper()
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = {
                analyticsHelper.logSettingsDialogVisible(false)
                showSettingsDialog = false
            },
            viewModel = viewModel,
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
        title = stringResource(Res.string.feature_settings_title),
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
        modifier = modifier.fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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

            Icon(
                imageVector = AppIcons.ArrowRight,
                contentDescription = stringResource(Res.string.feature_settings_change_theme_placeholder_text),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeBasedThemeDialog(
    initialTheme: TimeBasedTheme,
    onDismiss: () -> Unit,
    onConfirm: (TimeBasedTheme) -> Unit,
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var startHour by remember { mutableStateOf(initialTheme.hourStart) }
    var startMinute by remember { mutableStateOf(initialTheme.minStart) }

    var endHour by remember { mutableStateOf(initialTheme.hourEnd) }
    var endMinute by remember { mutableStateOf(initialTheme.minEnd) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.feature_settings_choose_dark_mode_time),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TimeRow(
                    label = stringResource(Res.string.feature_settings_dark_mode_starts_at),
                    time = DateHelper.format(startHour, startMinute),
                    onClick = { showStartPicker = true },
                )

                TimeRow(
                    label = stringResource(Res.string.feature_settings_dark_mode_ends_at),
                    time = DateHelper.format(endHour, endMinute),
                    onClick = { showEndPicker = true },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        TimeBasedTheme(
                            hourStart = startHour,
                            minStart = startMinute,
                            hourEnd = endHour,
                            minEnd = endMinute,
                        ),
                    )
                },
            ) {
                Text(stringResource(Res.string.feature_settings_apply_theme))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(),
            ) {
                Text(stringResource(Res.string.feature_settings_cancel))
            }
        },
    )

    if (showStartPicker) {
        TimePickerDialog(
            initialHour = startHour,
            initialMinute = startMinute,
            onDismiss = { showStartPicker = false },
            onConfirm = { h, m ->
                startHour = h
                startMinute = m
                showStartPicker = false
            },
        )
    }

    if (showEndPicker) {
        TimePickerDialog(
            initialHour = endHour,
            initialMinute = endMinute,
            onDismiss = { showEndPicker = false },
            onConfirm = { h, m ->
                endHour = h
                endMinute = m
                showEndPicker = false
            },
        )
    }
}

@Composable
private fun TimeRow(
    label: String,
    time: String,
    onClick: () -> Unit,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 1.dp,
        ) {
            Text(
                text = time,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(state.hour, state.minute)
                },
            ) {
                Text(stringResource(Res.string.feature_settings_ok))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(),
            ) {
                Text(stringResource(Res.string.feature_settings_cancel))
            }
        },
        text = {
            TimePicker(state = state)
        },
    )
}

private fun AnalyticsHelper.logSettingsDialogVisible(visible: Boolean) {
    logEvent(
        type = "settings_dialog_visible",
        params = mapOf("visible" to visible.toString()),
    )
}
