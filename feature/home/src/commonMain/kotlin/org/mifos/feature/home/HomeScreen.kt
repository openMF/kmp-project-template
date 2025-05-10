/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.mifos.core.designsystem.component.MifosButton
import org.mifos.core.designsystem.component.MifosScaffold
import template.core.base.platform.LocalAppReviewManager
import template.core.base.platform.LocalAppUpdateManager
import template.core.base.platform.LocalIntentManager
import template.core.base.platform.intent.IntentManager
import template.core.base.platform.review.AppReviewManager
import template.core.base.platform.update.AppUpdateManager

/**
 * Home Screen composable.
 * @param modifier Modifier
 * @return Composable
 */
@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    reviewManager: AppReviewManager = LocalAppReviewManager.current,
    updateManager: AppUpdateManager = LocalAppUpdateManager.current,
    intentManager: IntentManager = LocalIntentManager.current,
) {
    HomeScreenContent(
        modifier = modifier,
        showReviewPrompt = reviewManager::promptForReview,
        checkForUpdate = updateManager::checkForAppUpdate,
        shareText = {
            intentManager.shareText("Share Home Screen")
        },
    )
}

/**
 * Home Screen content.
 *
 * @param modifier Modifier
 * @return Composable
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun HomeScreenContent(
    showReviewPrompt: () -> Unit,
    checkForUpdate: () -> Unit,
    modifier: Modifier = Modifier,
    shareText: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val permissionState = rememberPermissionState(Permission.Notification)

    MifosScaffold(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            MifosButton(
                onClick = showReviewPrompt,
            ) {
                Text(text = "Prompt Review")
            }

            MifosButton(
                onClick = checkForUpdate,
            ) {
                Text(text = "Check for Update")
            }

            MifosButton(
                onClick = {
                    scope.launch {
                        permissionState.launchPermissionRequest()
                    }
                },
            ) {
                Text(text = "Check Permissions")
            }

            MifosButton(
                onClick = shareText,
            ) {
                Text(text = "Launch Intent")
            }
        }
    }
}
