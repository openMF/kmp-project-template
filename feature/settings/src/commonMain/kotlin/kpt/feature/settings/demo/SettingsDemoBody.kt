/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsHelper
import io.github.mobilebytelabs.kmptoolkit.firebase.compose.TrackScreenView
import io.github.mobilebytelabs.kmptoolkit.firebase.compose.rememberAnalyticsHelper
import kpt.core.designsystem.theme.spacing
import kpt.feature.settings.DevMenuEntry
import kpt.feature.settings.LanguageDialog
import kpt.feature.settings.SettingsDialog
import kpt.feature.settings.SettingsScreenContent
import kpt.feature.settings.TestTags
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_dev_close
import kpt.feature.settings.generated.resources.feature_settings_dev_tools_title
import org.jetbrains.compose.resources.stringResource

/**
 * The settings tab's fork-owned INNER content (the default demo body). `SettingsScreen` (the
 * template-owned shell) forwards this opaque body; the fork owns what renders here via
 * `cmp-navigation`'s `BackboneRegistry.settingsBody`, mirroring the shipped `homeBody` seam. It owns the
 * dialog state (theme / language / dev-menu) and renders the golden-locked [SettingsScreenContent]; a
 * `customizer --clean` fork strips the `BackboneRegistry` fenced block, so the shell keeps its chrome and
 * this demo body drops out. (WS01 base-feature seam, epic AC7.)
 */
@Composable
fun SettingsDemoBody(
    onBackClick: () -> Unit,
    onSyncAndDraftsClick: () -> Unit,
    onConflictsClick: () -> Unit,
    modifier: Modifier = Modifier,
    devMenuEntries: List<DevMenuEntry> = emptyList(),
) {
    val analyticsHelper = rememberAnalyticsHelper()
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showDevMenu by rememberSaveable { mutableStateOf(false) }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = {
                analyticsHelper.logSettingsDialogVisible(false)
                showSettingsDialog = false
            },
        )
    }

    if (showLanguageDialog) {
        LanguageDialog(
            onDismiss = {
                analyticsHelper.logLanguageDialogVisible(false)
                showLanguageDialog = false
            },
        )
    }

    if (showDevMenu) {
        DevMenuDialog(
            entries = devMenuEntries,
            onDismiss = { showDevMenu = false },
        )
    }

    // Footer long-press surfaces the dev menu when at least one dev entry is available.
    val hasDevEntries = devMenuEntries.isNotEmpty()
    val onFooterLongClick: (() -> Unit)? = if (hasDevEntries) {
        { showDevMenu = true }
    } else {
        null
    }

    SettingsScreenContent(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.Settings.SCREEN),
        onBackClick = onBackClick,
        onThemeCardClick = {
            analyticsHelper.logSettingsDialogVisible(true)
            showSettingsDialog = true
        },
        onLanguageCardClick = {
            analyticsHelper.logLanguageDialogVisible(true)
            showLanguageDialog = true
        },
        onSyncAndDraftsClick = onSyncAndDraftsClick,
        onConflictsClick = onConflictsClick,
        onFooterLongClick = onFooterLongClick,
    )

    TrackScreenView(screenName = "SettingsScreen")
}

/**
 * Dev-menu dialog surfaced by long-pressing the settings version footer. Renders one row per
 * [DevMenuEntry] supplied by the caller (`cmp-navigation`'s `ShowcaseRegistry.devSettingsEntries`).
 * Reachable only when the list is non-empty — release builds / neutralized forks pass an empty list
 * and the footer long-press handler is `null`.
 */
@Composable
private fun DevMenuDialog(
    entries: List<DevMenuEntry>,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.feature_settings_dev_tools_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                entries.forEach { entry ->
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onDismiss()
                            entry.onClick()
                        },
                    ) { Text(entry.label) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.feature_settings_dev_close))
            }
        },
    )
}

private fun AnalyticsHelper.logSettingsDialogVisible(visible: Boolean) {
    logEvent(
        type = "settings_dialog_visible",
        params = mapOf("visible" to visible.toString()),
    )
}

private fun AnalyticsHelper.logLanguageDialogVisible(visible: Boolean) {
    logEvent(
        type = "language_dialog_visible",
        params = mapOf("visible" to visible.toString()),
    )
}
