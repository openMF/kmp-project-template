/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package org.mifos.feature.home.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTimePicker
import template.core.base.designsystem.component.KptTopAppBar
import template.core.base.designsystem.component.TimePickerConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptTimePickerCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptTimePicker Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<TimePickerDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptTimePicker Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = TimePickerDemoType.entries.toList()) { demoType ->
                TimePickerCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected time picker demo
        when (selectedDemo) {
            TimePickerDemoType.BASIC -> BasicTimePickerExample()
            TimePickerDemoType.TWELVE_HOUR -> TwelveHourTimePickerExample()
            TimePickerDemoType.TWENTY_FOUR_HOUR -> TwentyFourHourTimePickerExample()
            TimePickerDemoType.CUSTOM_INITIAL -> CustomInitialTimePickerExample()
            null -> {}
        }
    }
}

@Composable
private fun TimePickerCatalogItem(
    demoType: TimePickerDemoType,
    onClick: () -> Unit,
) {
    KptCard(
        onClick = onClick,
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
                    text = demoType.title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = demoType.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class TimePickerDemoType(
    val title: String,
    val description: String,
) {
    BASIC("Basic Time Picker", "KptTimePicker with default settings (24-hour, 00:00)."),
    TWELVE_HOUR("12-Hour Time Picker", "KptTimePicker in 12-hour mode."),
    TWENTY_FOUR_HOUR("24-Hour Time Picker", "KptTimePicker in 24-hour mode."),
    CUSTOM_INITIAL("Custom Initial Time", "KptTimePicker with custom initial hour and minute."),
}

@Composable
private fun BasicTimePickerExample() {
    var hour by remember { mutableStateOf(0) }
    var minute by remember { mutableStateOf(0) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            KptTimePicker(
                configuration = TimePickerConfiguration(
                    initialHour = hour,
                    initialMinute = minute,
                    is24Hour = true,
                    onTimeChanged = { h, m ->
                        hour = h
                        minute = m
                    },
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Selected: $hour-$minute")
        }
    }
}

@Composable
private fun TwelveHourTimePickerExample() {
    var hour by remember { mutableStateOf(10) }
    var minute by remember { mutableStateOf(30) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            KptTimePicker(
                configuration = TimePickerConfiguration(
                    initialHour = hour,
                    initialMinute = minute,
                    is24Hour = false,
                    onTimeChanged = { h, m ->
                        hour = h
                        minute = m
                    },
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = if (hour == 0 || hour == 12) 12 else hour % 12
            Text("Selected: $displayHour:$minute $amPm")
        }
    }
}

@Composable
private fun TwentyFourHourTimePickerExample() {
    var hour by remember { mutableStateOf(14) }
    var minute by remember { mutableStateOf(45) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            KptTimePicker(
                configuration = TimePickerConfiguration(
                    initialHour = hour,
                    initialMinute = minute,
                    is24Hour = true,
                    onTimeChanged = { h, m ->
                        hour = h
                        minute = m
                    },
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Selected: $hour:$minute")
        }
    }
}

@Composable
private fun CustomInitialTimePickerExample() {
    var hour by remember { mutableStateOf(6) }
    var minute by remember { mutableStateOf(15) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            KptTimePicker(
                configuration = TimePickerConfiguration(
                    initialHour = hour,
                    initialMinute = minute,
                    is24Hour = true,
                    onTimeChanged = { h, m ->
                        hour = h
                        minute = m
                    },
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Selected: $hour:$minute")
        }
    }
}
