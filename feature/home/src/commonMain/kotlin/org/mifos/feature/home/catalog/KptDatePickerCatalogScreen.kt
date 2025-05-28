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
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import template.core.base.designsystem.component.DatePickerConfiguration
import template.core.base.designsystem.component.KptCard
import template.core.base.designsystem.component.KptDatePicker
import template.core.base.designsystem.component.KptDatePickerDialog
import template.core.base.designsystem.component.KptDatePickerField
import template.core.base.designsystem.component.KptScaffold
import template.core.base.designsystem.component.KptTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KptDatePickerCatalogScreen(
    navigateBack: () -> Unit,
) {
    KptScaffold(
        topBar = {
            KptTopAppBar(
                title = "KptDatePicker Catalog",
                onNavigationIconClick = navigateBack,
            )
        },
    ) {
        var selectedDemo by remember { mutableStateOf<DatePickerDemoType?>(null) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "KptDatePicker Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = DatePickerDemoType.entries.toList()) { demoType ->
                DatePickerCatalogItem(
                    demoType = demoType,
                    onClick = { selectedDemo = demoType },
                )
            }
        }

        // Show the selected date picker demo
        when (selectedDemo) {
            DatePickerDemoType.BASIC -> BasicDatePickerExample()
            DatePickerDemoType.DIALOG -> DatePickerDialogExample()
            DatePickerDemoType.FIELD -> DatePickerFieldExample()
            null -> {}
        }
    }
}

@Composable
private fun DatePickerCatalogItem(
    demoType: DatePickerDemoType,
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

private enum class DatePickerDemoType(
    val title: String,
    val description: String,
) {
    BASIC(
        title = "Basic DatePicker",
        description = "KptDatePicker with configuration",
    ),
    DIALOG(
        title = "DatePicker Dialog",
        description = "KptDatePickerDialog with confirm/cancel",
    ),
    FIELD(
        title = "DatePicker Field",
        description = "KptDatePickerField with text field and dialog",
    ),
}

@Composable
private fun BasicDatePickerExample() {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptDatePicker(
            configuration = DatePickerConfiguration(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                title = "Pick a Date",
                headline = "Choose a date for your event",
            ),
        )
    }
}

@Composable
private fun DatePickerDialogExample() {
    var showDialog by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = selectedDate?.let { "Selected: ${formatDate(it)}" } ?: "No date selected",
            style = MaterialTheme.typography.bodyLarge,
        )
        if (showDialog) {
            KptDatePickerDialog(
                onDateSelected = {
                    selectedDate = it
                    showDialog = false
                },
                onDismiss = { showDialog = false },
                selectedDate = selectedDate,
            )
        }
    }
}

@Composable
private fun DatePickerFieldExample() {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        KptDatePickerField(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            label = "Date of Birth",
            placeholder = "Select your birth date",
            dateFormatter = { millis -> formatDate(millis) },
        )
    }
}

private fun formatDate(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return dateTime.date.toString()
}
