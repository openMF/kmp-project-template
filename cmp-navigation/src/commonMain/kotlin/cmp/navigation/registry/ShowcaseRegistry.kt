/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kpt.feature.settings.DevMenuEntry
// demo:begin
import kpt.core.base.security.isReleaseBuild
import kpt.feature.showcase.stategallery.StateGalleryRoute
import kpt.feature.showcase.stategallery.stateGalleryGraph
import kpt.feature.showcase.transitions.TransitionGalleryRoute
import kpt.feature.showcase.transitions.transitionGalleryGraph
// demo:end

/**
 * ShowcaseRegistry — template-shipped dev-only demo entry points (Transition Gallery,
 * State Gallery). Content is fully fenced `// demo:begin … // demo:end` so
 * `remove-demo.sh` / `scripts/white-label/customize.sh --clean` reduces the two members
 * to empty stubs (list returns empty, graph body is empty) — the shell still resolves
 * the object; the dev menu simply hides on the neutralized fork.
 *
 * Ownership: `owner: template` (dev-only shipped content — sync-reachable).
 */
object ShowcaseRegistry {
    /**
     * Dev-menu entries surfaced in `SettingsScreen`'s Developer section. Returns an
     * empty list on release builds (dev menu hidden) and, on a `--clean` fork where the
     * fenced block is stripped, the trailing `return emptyList()` is the whole body.
     */
    fun devSettingsEntries(navController: NavController): List<DevMenuEntry> {
        // demo:begin
        if (!isReleaseBuild()) {
            return listOf(
                DevMenuEntry("Transition Gallery") { navController.navigate(TransitionGalleryRoute) },
                DevMenuEntry("State Gallery") { navController.navigate(StateGalleryRoute) },
            )
        }
        // demo:end
        return emptyList()
    }

    /** Dev-only nav destinations registered inside the authenticated graph. */
    val devDestinations: NavGraphBuilder.(NavController) -> Unit = { navController ->
        // demo:begin
        transitionGalleryGraph(navController)
        stateGalleryGraph(navController)
        // demo:end
    }
}
