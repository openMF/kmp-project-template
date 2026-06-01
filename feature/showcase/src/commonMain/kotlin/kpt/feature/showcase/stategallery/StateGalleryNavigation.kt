/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MatchingDeclarationName")

package kpt.feature.showcase.stategallery

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely

@Serializable
data object StateGalleryRoute

fun NavGraphBuilder.stateGalleryGraph(navController: NavController) {
    composableWithPushTransitions<StateGalleryRoute> {
        StateGalleryScreen(
            onBackClick = { navController.popBackStackSafely() },
        )
    }
}
