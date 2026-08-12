/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsHelper
import io.github.mobilebytelabs.kmptoolkit.firebase.compose.TrackScreenView
import io.github.mobilebytelabs.kmptoolkit.firebase.compose.rememberAnalyticsHelper
import kpt.core.base.designsystem.component.AppCard
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.spacing
import kpt.core.ui.scaffold.KptScaffold
import kpt.feature.settings.BuildKonfig
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_change_language_placeholder_text
import kpt.feature.settings.generated.resources.feature_settings_change_language_text
import kpt.feature.settings.generated.resources.feature_settings_change_theme_placeholder_text
import kpt.feature.settings.generated.resources.feature_settings_change_theme_text
import kpt.feature.settings.generated.resources.feature_settings_dev_close
import kpt.feature.settings.generated.resources.feature_settings_dev_state_gallery
import kpt.feature.settings.generated.resources.feature_settings_dev_tools_title
import kpt.feature.settings.generated.resources.feature_settings_dev_transition_gallery
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTransitionGalleryClick: (() -> Unit)? = null,
    onStateGalleryClick: (() -> Unit)? = null,
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
            onTransitionGalleryClick = onTransitionGalleryClick,
            onStateGalleryClick = onStateGalleryClick,
            onDismiss = { showDevMenu = false },
        )
    }

    // Footer long-press surfaces the dev menu when either gallery is available.
    val hasDevEntries = onTransitionGalleryClick != null || onStateGalleryClick != null
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
        onFooterLongClick = onFooterLongClick,
    )

    TrackScreenView(screenName = "SettingsScreen")
}

@Composable
internal fun SettingsScreenContent(
    onBackClick: () -> Unit,
    onThemeCardClick: () -> Unit,
    onLanguageCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFooterLongClick: (() -> Unit)? = null,
) {
    val sp = MaterialTheme.spacing
    KptScaffold(
        title = "Settings",
        onNavigationIconClick = onBackClick,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = sp.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            ThemeCard(onClick = onThemeCardClick)
            LanguageCard(onClick = onLanguageCardClick)
            Spacer(modifier = Modifier.fillMaxWidth().padding(sp.sm))
            VersionLabel(onLongClick = onFooterLongClick)
        }
    }
}

/**
 * Dev-menu dialog surfaced by long-pressing the [VersionLabel] footer. Lists every
 * available debug-only gallery (Transition Gallery, State Gallery, …). Each entry is
 * shown only when its callback is non-null — release-build wiring nulls them all out and
 * the dialog is never reachable.
 */
@Composable
private fun DevMenuDialog(
    onTransitionGalleryClick: (() -> Unit)?,
    onStateGalleryClick: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.feature_settings_dev_tools_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                if (onTransitionGalleryClick != null) {
                    androidx.compose.material3.TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onDismiss()
                            onTransitionGalleryClick()
                        },
                    ) { Text(stringResource(Res.string.feature_settings_dev_transition_gallery)) }
                }
                if (onStateGalleryClick != null) {
                    androidx.compose.material3.TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onDismiss()
                            onStateGalleryClick()
                        },
                    ) { Text(stringResource(Res.string.feature_settings_dev_state_gallery)) }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.feature_settings_dev_close))
            }
        },
    )
}

/**
 * Static version footer label. In debug builds, long-pressing opens the Dev tools menu
 * (Transition Gallery, State Gallery, etc.). In release builds the long-press handler is
 * wired to `null` by the navigation graph builder, so the label behaves as a plain text
 * footer.
 *
 * Wire point: [cmp.navigation.authenticated.authenticatedGraph] passes non-null
 * gallery click handlers only when `!isReleaseBuild()`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VersionLabel(onLongClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    // App-name footer rendered from BuildKonfig.APP_DISPLAY_NAME (the fork's gradle/fork.properties
    // app.display.name), not a hardcoded string resource — so a fork rebrands in one place (S9/T10).
    val rowModifier = if (onLongClick != null) {
        modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* no-op short-tap */ },
                onLongClick = onLongClick,
            )
    } else {
        modifier.fillMaxWidth()
    }
    Text(
        text = BuildKonfig.APP_DISPLAY_NAME,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = rowModifier,
    )
}

@Composable
internal fun ThemeCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsRowCard(
        icon = AppIcons.Sun,
        title = stringResource(Res.string.feature_settings_change_theme_text),
        contentDescription = stringResource(Res.string.feature_settings_change_theme_placeholder_text),
        accentColor = MaterialTheme.colorScheme.tertiary,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
internal fun LanguageCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsRowCard(
        icon = AppIcons.Language,
        title = stringResource(Res.string.feature_settings_change_language_text),
        contentDescription = stringResource(Res.string.feature_settings_change_language_placeholder_text),
        accentColor = MaterialTheme.colorScheme.primary,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun SettingsRowCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    contentDescription: String,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    AppCard(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        accentColor = accentColor,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sp.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor)
            Text(text = title, modifier = Modifier.weight(1f))
            IconButton(onClick = onClick) {
                Icon(imageVector = AppIcons.ArrowRight, contentDescription = contentDescription)
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

private fun AnalyticsHelper.logLanguageDialogVisible(visible: Boolean) {
    logEvent(
        type = "language_dialog_visible",
        params = mapOf("visible" to visible.toString()),
    )
}
