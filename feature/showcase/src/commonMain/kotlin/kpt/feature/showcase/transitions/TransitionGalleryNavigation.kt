/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.showcase.transitions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.showcase.generated.resources.Res
import kpt.feature.showcase.generated.resources.screens_showcase_transition_demo_label
import org.jetbrains.compose.resources.stringResource

@Serializable
data object TransitionGalleryRoute

@Serializable
data class TransitionDemoRoute(val variantName: String)

fun NavGraphBuilder.transitionGalleryGraph(navController: NavController) {
    composableWithPushTransitions<TransitionGalleryRoute> {
        TransitionGalleryScreen(
            onNavigateToDemo = { variant ->
                navController.navigate(TransitionDemoRoute(variant.name))
            },
            onBackClick = { navController.popBackStackSafely() },
        )
    }

    composableWithPushTransitions<TransitionDemoRoute> { backStackEntry ->
        val variantName = backStackEntry.toRoute<TransitionDemoRoute>().variantName
        val variant = TransitionVariant.valueOf(variantName)
        TransitionDemoScreen(variant = variant, onAutoPop = { navController.popBackStackSafely() })
    }
}

/** Destination screen for each transition demo. Auto-pops after 2s. */
@Composable
private fun TransitionDemoScreen(variant: TransitionVariant, onAutoPop: () -> Unit) {
    LaunchedEffect(variant) {
        delay(2_000L)
        onAutoPop()
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(
                Res.string.screens_showcase_transition_demo_label,
                variant.displayName,
            ),
        )
    }
}
