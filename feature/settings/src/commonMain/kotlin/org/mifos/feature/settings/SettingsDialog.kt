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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.mifos.core.common.DateHelper
import org.mifos.core.model.DarkThemeConfig
import org.mifos.core.model.ThemeBrand
import org.mifos.core.model.TimeBasedTheme
import org.mifos.feature.settings.generated.resources.Res
import org.mifos.feature.settings.generated.resources.feature_settings_brand_android
import org.mifos.feature.settings.generated.resources.feature_settings_brand_default
import org.mifos.feature.settings.generated.resources.feature_settings_dark_mode_based_on_time
import org.mifos.feature.settings.generated.resources.feature_settings_dark_mode_config_dark
import org.mifos.feature.settings.generated.resources.feature_settings_dark_mode_config_light
import org.mifos.feature.settings.generated.resources.feature_settings_dark_mode_config_system_default
import org.mifos.feature.settings.generated.resources.feature_settings_dark_mode_label
import org.mifos.feature.settings.generated.resources.feature_settings_dark_mode_preference
import org.mifos.feature.settings.generated.resources.feature_settings_dismiss_dialog_button_text
import org.mifos.feature.settings.generated.resources.feature_settings_dynamic_color_no
import org.mifos.feature.settings.generated.resources.feature_settings_dynamic_color_preference
import org.mifos.feature.settings.generated.resources.feature_settings_dynamic_color_yes
import org.mifos.feature.settings.generated.resources.feature_settings_light_mode_label
import org.mifos.feature.settings.generated.resources.feature_settings_loading
import org.mifos.feature.settings.generated.resources.feature_settings_theme
import org.mifos.feature.settings.generated.resources.feature_settings_title

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewmodel,
) {
    val settingsUiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
    SettingsDialog(
        onDismiss = onDismiss,
        settingsUiState = settingsUiState,
        onChangeThemeBrand = viewModel::updateThemeBrand,
        onChangeDynamicColorPreference = viewModel::updateDynamicColorPreference,
        onChangeDarkThemeConfig = viewModel::updateDarkThemeConfig,
        onChangeTimeBasedTheme = viewModel::updateTimeBasedThemeConfig,
    )
}

@Composable
fun SettingsDialog(
    settingsUiState: SettingsUiState,
    onDismiss: () -> Unit,
    onChangeThemeBrand: (themeBrand: ThemeBrand) -> Unit,
    onChangeTimeBasedTheme: (timeBasedTheme: TimeBasedTheme) -> Unit,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
    modifier: Modifier = Modifier,
    supportDynamicColor: Boolean = supportsDynamicTheming(),
) {
    var timeBasedThemeDialogVisible by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier.fillMaxWidth(0.8f),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(resource = Res.string.feature_settings_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            HorizontalDivider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                when (settingsUiState) {
                    SettingsUiState.Loading -> {
                        Text(
                            text = stringResource(resource = Res.string.feature_settings_loading),
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }

                    is SettingsUiState.Success -> {
                        SettingsPanel(
                            settings = settingsUiState.settings,
                            supportDynamicColor = supportDynamicColor,
                            onChangeThemeBrand = onChangeThemeBrand,
                            onChangeDynamicColorPreference = onChangeDynamicColorPreference,
                            onChangeDarkThemeConfig = {
                                if (it == DarkThemeConfig.BASED_ON_TIME) {
                                    timeBasedThemeDialogVisible = true
                                } else {
                                    onChangeDarkThemeConfig(it)
                                }
                            },
                        )

                        if (timeBasedThemeDialogVisible) {
                            TimeBasedThemeDialog(
                                onDismiss = {
                                    timeBasedThemeDialogVisible = false
                                },
                                initialTheme = settingsUiState.settings.timeBasedTheme,
                                onConfirm = {
                                    onChangeTimeBasedTheme(it)
                                    onChangeDarkThemeConfig(DarkThemeConfig.BASED_ON_TIME)
                                    timeBasedThemeDialogVisible = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Text(
                    text = stringResource(resource = Res.string.feature_settings_dismiss_dialog_button_text),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

// [ColumnScope] is used for using the [ColumnScope.AnimatedVisibility] extension overload composable.
@Composable
private fun ColumnScope.SettingsPanel(
    settings: UserEditableSettings,
    supportDynamicColor: Boolean,
    onChangeThemeBrand: (themeBrand: ThemeBrand) -> Unit,
    onChangeDynamicColorPreference: (useDynamicColor: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
) {
    val platform = getPlatform()
    if (platform == Platform.Android) {
        SettingsDialogSectionTitle(text = stringResource(resource = Res.string.feature_settings_theme))
        Column(Modifier.selectableGroup()) {
            SettingsDialogThemeChooserRow(
                text = stringResource(resource = Res.string.feature_settings_brand_default),
                selected = settings.brand == ThemeBrand.DEFAULT,
                onClick = { onChangeThemeBrand(ThemeBrand.DEFAULT) },
            )
            SettingsDialogThemeChooserRow(
                text = stringResource(resource = Res.string.feature_settings_brand_android),
                selected = settings.brand == ThemeBrand.ANDROID,
                onClick = { onChangeThemeBrand(ThemeBrand.ANDROID) },
            )
        }
        AnimatedVisibility(visible = settings.brand == ThemeBrand.DEFAULT && supportDynamicColor) {
            Column {
                SettingsDialogSectionTitle(
                    text = stringResource(
                        resource = Res.string.feature_settings_dynamic_color_preference,
                    ),
                )
                Column(Modifier.selectableGroup()) {
                    SettingsDialogThemeChooserRow(
                        text = stringResource(resource = Res.string.feature_settings_dynamic_color_yes),
                        selected = settings.useDynamicColor,
                        onClick = { onChangeDynamicColorPreference(true) },
                    )
                    SettingsDialogThemeChooserRow(
                        text = stringResource(resource = Res.string.feature_settings_dynamic_color_no),
                        selected = !settings.useDynamicColor,
                        onClick = { onChangeDynamicColorPreference(false) },
                    )
                }
            }
        }
    }
    SettingsDialogSectionTitle(text = stringResource(resource = Res.string.feature_settings_dark_mode_preference))
    Column(Modifier.selectableGroup()) {
        SettingsDialogThemeChooserRow(
            text = stringResource(resource = Res.string.feature_settings_dark_mode_config_system_default),
            selected = settings.darkThemeConfig == DarkThemeConfig.FOLLOW_SYSTEM,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.FOLLOW_SYSTEM) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(resource = Res.string.feature_settings_dark_mode_config_light),
            selected = settings.darkThemeConfig == DarkThemeConfig.LIGHT,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.LIGHT) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(resource = Res.string.feature_settings_dark_mode_config_dark),
            selected = settings.darkThemeConfig == DarkThemeConfig.DARK,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.DARK) },
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(Res.string.feature_settings_dark_mode_based_on_time) +
                "\n" + stringResource(Res.string.feature_settings_dark_mode_label) +
                " [" +
                DateHelper.formatTimeRange(
                    settings.timeBasedTheme.hourStart,
                    settings.timeBasedTheme.minStart,
                    settings.timeBasedTheme.hourEnd,
                    settings.timeBasedTheme.minEnd,
                ) +
                "]\n" +
                stringResource(Res.string.feature_settings_light_mode_label) + " [" +
                DateHelper.formatTimeRange(
                    settings.timeBasedTheme.hourEnd,
                    settings.timeBasedTheme.minEnd,
                    settings.timeBasedTheme.hourStart,
                    settings.timeBasedTheme.minStart,
                ) +
                "]",
            selected = settings.darkThemeConfig == DarkThemeConfig.BASED_ON_TIME,
            onClick = { onChangeDarkThemeConfig(DarkThemeConfig.BASED_ON_TIME) },
        )
    }
}

@Composable
private fun SettingsDialogSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
fun SettingsDialogThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}
