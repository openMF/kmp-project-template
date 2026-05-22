/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.watchlist.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import org.mifos.feature.watchlist.ui.PersonalWatchlistScreen
import template.core.base.ui.nav.composableWithPushTransitions

@Serializable
data object PersonalWatchlistRoute

fun NavController.navigateToPersonalWatchlist(navOptions: NavOptions? = null) {
    navigate(route = PersonalWatchlistRoute, navOptions = navOptions)
}

fun NavGraphBuilder.personalWatchlistDestination(
    onBackClick: () -> Unit,
    onCoinClick: (String) -> Unit,
) {
    composableWithPushTransitions<PersonalWatchlistRoute> {
        PersonalWatchlistScreen(
            onBackClick = onBackClick,
            onCoinClick = onCoinClick,
        )
    }
}
