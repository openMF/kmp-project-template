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

import cmp.navigation.authenticatednavbar.AuthenticatedNavBarTabItem
import kpt.core.ui.NavigationItem

/**
 * TabRegistry — the FORK-OWNED white-label seam for the authenticated bottom-nav tabs.
 *
 * The template navbar (`AuthenticatedNavbarNavigationScreenContent`) reads [tabs] and renders them
 * generically, so adding/removing a tab is a ONE-file edit here — no longer a scattered change across a
 * sealed-class + item list + click `when` + NavHost. The backbone Home + Profile tabs are always present
 * (they are the app shell); a fork appends its own via [extraTabs]. Ownership: `owner: fork` in
 * customization-surface.yaml (S6 heal, epic pure-white-label-store5-network T7).
 */
object TabRegistry {
    /** Fork tabs appended after the backbone Home/Profile tabs. Template default = none. */
    val extraTabs: List<NavigationItem> = emptyList()

    /** The full ordered tab list the navbar renders: backbone shell tabs + [extraTabs]. */
    val tabs: List<NavigationItem> = buildList {
        add(AuthenticatedNavBarTabItem.HomeTab)
        add(AuthenticatedNavBarTabItem.ProfileTab)
        addAll(extraTabs)
    }
}
