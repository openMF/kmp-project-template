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

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import org.mifos.feature.home.catalog.KptAlertDialogCatalogScreen
import org.mifos.feature.home.catalog.KptBottomAppBarCatalogScreen
import org.mifos.feature.home.catalog.KptBottomSheetCatalogScreen
import org.mifos.feature.home.catalog.KptButtonCatalogScreen
import org.mifos.feature.home.catalog.KptCardCatalogScreen
import org.mifos.feature.home.catalog.KptCheckboxCatalogScreen
import org.mifos.feature.home.catalog.KptChipCatalogScreen
import org.mifos.feature.home.catalog.KptDatePickerCatalogScreen
import org.mifos.feature.home.catalog.KptDividerCatalogScreen
import org.mifos.feature.home.catalog.KptEmptyStateCatalogScreen
import org.mifos.feature.home.catalog.KptExpandableCardCatalogScreen
import org.mifos.feature.home.catalog.KptFloatingActionButtonCatalogScreen
import org.mifos.feature.home.catalog.KptListItemCatalogScreen
import org.mifos.feature.home.catalog.KptProgressIndicatorCatalogScreen
import org.mifos.feature.home.catalog.KptRadioButtonCatalogScreen
import org.mifos.feature.home.catalog.KptRadioGroupCatalogScreen
import org.mifos.feature.home.catalog.KptScaffoldCatalogScreen
import org.mifos.feature.home.catalog.KptShimmerLoadingBoxCatalogScreen
import org.mifos.feature.home.catalog.KptSimpleListItemCatalogScreen
import org.mifos.feature.home.catalog.KptSlideTransitionCatalogScreen
import org.mifos.feature.home.catalog.KptSliderCatalogScreen
import org.mifos.feature.home.catalog.KptSnackbarHostCatalogScreen
import org.mifos.feature.home.catalog.KptSwitchCatalogScreen
import org.mifos.feature.home.catalog.KptTabCatalogScreen
import org.mifos.feature.home.catalog.KptTabLayoutCatalogScreen
import org.mifos.feature.home.catalog.KptTextFieldCatalogScreen
import org.mifos.feature.home.catalog.KptTimePickerCatalogScreen
import org.mifos.feature.home.catalog.KptTopAppBarCatalogScreen

const val HOME_ROUTE = "home_route"
private const val DESIGN_SYSTEM_CATALOG_ROUTE = "design_system_catalog_route"
const val HOME_GRAPH = "home_graph"

fun NavController.navigateToHome(navOptions: NavOptions? = null) =
    navigate(HOME_GRAPH, navOptions)

fun NavGraphBuilder.homeGraph(
    navController: NavController,
    startDestination: String = HOME_ROUTE,
) {
    navigation(
        route = HOME_GRAPH,
        startDestination = startDestination,
    ) {
        composable(HOME_ROUTE) {
            HomeScreen(
                navigateToShowcase = {
                    navController.navigate(DESIGN_SYSTEM_CATALOG_ROUTE)
                },
            )
        }

        composable(DESIGN_SYSTEM_CATALOG_ROUTE) {
            DesignSystemCatalogScreen(
                navigateBack = navController::navigateUp,
                onNavigateToComponent = { component ->
                    navController.navigate(component.route)
                },
            )
        }

        DesignSystemComponent.entries.forEach { component ->
            composable(component.route) {
                when (component) {
                    DesignSystemComponent.BUTTON -> KptButtonCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.CHIP -> KptChipCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.CARD -> KptCardCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.CHECKBOX -> KptCheckboxCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.DATE_PICKER -> KptDatePickerCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.DIVIDER -> KptDividerCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.EMPTY_STATE -> KptEmptyStateCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.EXPANDABLE_CARD -> KptExpandableCardCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.FAB -> KptFloatingActionButtonCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.LIST_ITEM -> KptListItemCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.PROGRESS -> KptProgressIndicatorCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.RADIO_BUTTON -> KptRadioButtonCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.RADIO_GROUP -> KptRadioGroupCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.SCAFFOLD -> KptScaffoldCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.SHIMMER -> KptShimmerLoadingBoxCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.SIMPLE_LIST_ITEM -> KptSimpleListItemCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.SLIDER -> KptSliderCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.SNACKBAR -> KptSnackbarHostCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.SWITCH -> KptSwitchCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.TAB -> KptTabCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.TAB_LAYOUT -> KptTabLayoutCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.TEXT_FIELD -> KptTextFieldCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.TIME_PICKER -> KptTimePickerCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.TOP_APP_BAR -> KptTopAppBarCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.SLIDE_TRANSITION -> KptSlideTransitionCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.ALERT_DIALOG -> KptAlertDialogCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.BOTTOM_SHEET -> KptBottomSheetCatalogScreen(
                        navController::popBackStack,
                    )

                    DesignSystemComponent.BOTTOM_BAR -> KptBottomAppBarCatalogScreen(
                        navController::popBackStack,
                    )
                }
            }
        }
    }
}
