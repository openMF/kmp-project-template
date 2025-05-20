/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.adaptivelayout

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AdaptiveNavigableSupportingPaneScaffold(
    mainPaneContent: @Composable ThreePaneScaffoldPaneScope.(() -> Unit) -> Unit,
    supportingPaneContent: @Composable ThreePaneScaffoldPaneScope.(() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    extraPaneContent: @Composable ThreePaneScaffoldPaneScope.() -> Unit = {},
    paneExpansionDragHandle: @Composable (ThreePaneScaffoldScope.(PaneExpansionState) -> Unit)? = null,
    paneExpansionState: PaneExpansionState? = null,
) {
    val scaffoldNavigator = rememberSupportingPaneScaffoldNavigator()
    val scope = rememberCoroutineScope()

    BackHandler(enabled = scaffoldNavigator.canNavigateBack()) {
        scope.launch {
            scaffoldNavigator.navigateBack()
        }
    }

    SupportingPaneScaffold(
        directive = scaffoldNavigator.scaffoldDirective,
        scaffoldState = scaffoldNavigator.scaffoldState,
        mainPane = {
            mainPaneContent {
                scope.launch {
                    scaffoldNavigator.navigateTo(SupportingPaneScaffoldRole.Supporting)
                }
            }
        },
        supportingPane = {
            supportingPaneContent {
                scope.launch {
                    if (scaffoldNavigator.canNavigateBack()) {
                        scaffoldNavigator.navigateBack()
                    }
                }
            }
        },
        modifier = modifier,
        extraPane = {
            extraPaneContent()
        },
        paneExpansionDragHandle = paneExpansionDragHandle,
        paneExpansionState = paneExpansionState
            ?: rememberPaneExpansionState(scaffoldNavigator.scaffoldValue),
    )
}
