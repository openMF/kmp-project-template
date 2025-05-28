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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTopAppBar

@Composable
fun DesignSystemCatalogScreen(
    navigateBack: () -> Unit,
    onNavigateToComponent: (DesignSystemComponent) -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "Designsystem Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "Design System Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            items(items = DesignSystemComponent.entries) { component ->
                KptCard(
                    onClick = { onNavigateToComponent(component) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = component.title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = component.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// List all catalog screens here
enum class DesignSystemComponent(
    val title: String,
    val description: String,
    val route: String,
) {
    BUTTON(
        title = "KptButton",
        description = "Filled, Outlined, Text, Icon, Loading, Disabled buttons.",
        route = "button_catalog_screen",
    ),
    CHIP(
        title = "KptChip",
        description = "Assist, Filter, Input, and Suggestion chips.",
        route = "chip_catalog_screen",
    ),
    CARD(
        title = "KptCard",
        description = "Filled, Elevated, Outlined, Info, Stat, Media, ListItem, Error",
        route = "card_catalog_screen",
    ),
    CHECKBOX(
        title = "KptCheckbox",
        description = "Basic, label, description, disabled, custom color checkboxes.",
        route = "checkbox_catalog_screen",
    ),
    DATE_PICKER(
        title = "KptDatePicker",
        description = "Date picker, dialog, and field.",
        route = "date_picker_catalog_screen",
    ),
    DIVIDER(
        title = "KptDivider",
        description = "Horizontal, vertical, custom thickness, custom color dividers.",
        route = "divider_catalog_screen",
    ),
    EMPTY_STATE(
        title = "KptEmptyState",
        description = "Basic, with action, custom icon empty states.",
        route = "empty_state_catalog_screen",
    ),
    EXPANDABLE_CARD(
        title = "KptExpandableCard",
        description = "Basic, with subtitle, with icon expandable cards.",
        route = "expandable_card_catalog_screen",
    ),
    FAB(
        title = "KptFloatingActionButton",
        description = "Regular, extended, loading, success, error, small, large, add, edit, etc",
        route = "fab_catalog_screen",
    ),
    LIST_ITEM(
        title = "KptListItem",
        description = "Basic, supporting content, leading/trailing icon, overline, etc.",
        route = "list_item_catalog_screen",
    ),
    PROGRESS(
        title = "KptProgressIndicator",
        description = "Circular/linear, loading dots, wave, pulse, ring, progress with label, etc",
        route = "progress_indicator_catalog_screen",
    ),
    RADIO_BUTTON(
        title = "KptRadioButton",
        description = "Basic, label, description, disabled, group radio buttons.",
        route = "radio_button_catalog_screen",
    ),
    RADIO_GROUP(
        title = "KptRadioGroup",
        description = "Basic, with descriptions, disabled, custom arrangement radio groups.",
        route = "radio_group_catalog_screen",
    ),
    SCAFFOLD(
        title = "KptScaffold",
        description = "Basic, with bottom bar, FAB, snackbar, pull-to-refresh.",
        route = "scaffold_catalog_screen",
    ),
    SHIMMER(
        title = "KptShimmerLoadingBox",
        description = "Basic shimmer, custom shape, shimmer list item, list of shimmer items.",
        route = "shimmer_loading_box_catalog_screen",
    ),
    SIMPLE_LIST_ITEM(
        title = "KptSimpleListItem",
        description = "Basic, supporting text, leading/trailing icon, clickable simple list items.",
        route = "simple_list_item_catalog_screen",
    ),
    SLIDER(
        title = "KptSlider",
        description = "Basic, label, custom range/steps, disabled, label formatter.",
        route = "slider_catalog_screen",
    ),
    SNACKBAR(
        title = "KptSnackbarHost",
        description = "Basic, with action, dismiss, custom duration snackbars.",
        route = "snackbar_host_catalog_screen",
    ),
    SWITCH(
        title = "KptSwitch",
        description = "Basic, label, description, disabled, custom thumb switches.",
        route = "switch_catalog_screen",
    ),
    TAB(
        title = "KptTab",
        description = "Basic tab row, custom colors, scrollable tab row.",
        route = "tab_catalog_screen",
    ),
    TAB_LAYOUT(
        title = "KptTabLayout",
        description = "Basic, with icons, dynamic content tab layouts.",
        route = "tab_layout_catalog_screen",
    ),
    TEXT_FIELD(
        title = "KptTextField",
        description = "Basic, outlined, filled, email, password, phone, search, etc.",
        route = "text_field_catalog_screen",
    ),
    TIME_PICKER(
        title = "KptTimePicker",
        description = "Basic, 12/24-hour, custom initial time pickers.",
        route = "time_picker_catalog_screen",
    ),
    TOP_APP_BAR(
        title = "KptTopAppBar",
        description = "Basic, navigation, subtitle, action, search, profile, settings, all variants.",
        route = "top_app_bar_catalog_screen",
    ),
    SLIDE_TRANSITION(
        title = "KptSlideTransition",
        description = "Slide in from left, right, up, down transitions.",
        route = "slide_transition_catalog_screen",
    ),
    BOTTOM_BAR(
        title = "KptBottomAppBar",
        description = "BottomAppBar",
        route = "bottom_app_bar_catalog_screen",
    ),
    BOTTOM_SHEET(
        title = "KptBottomSheet",
        description = "BottomSheet",
        route = "bottom_sheet_catalog_screen",
    ),
    ALERT_DIALOG(
        title = "KptAlertDialog",
        description = "Kpt Alert Dialog",
        route = "alert_dialog_catalog_screen",
    ),
    ;

    companion object {
        val entries: List<DesignSystemComponent> = DesignSystemComponent.entries
    }
}
