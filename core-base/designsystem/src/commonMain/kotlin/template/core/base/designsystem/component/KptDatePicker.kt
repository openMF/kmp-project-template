/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import template.core.base.designsystem.core.KptComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptDatePicker(configuration: DatePickerConfiguration) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = configuration.selectedDate,
        initialDisplayMode = configuration.initialDisplayMode,
        yearRange = configuration.yearRange,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return configuration.dateValidator(utcTimeMillis)
            }
        },
    )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        configuration.onDateSelected(datePickerState.selectedDateMillis)
    }

    DatePicker(
        state = datePickerState,
        modifier = configuration.modifier.testTag(configuration.testTag ?: "KptDatePicker"),
        dateFormatter = DatePickerDefaults.dateFormatter(),
        title = configuration.title?.let { { Text(it) } },
        headline = configuration.headline?.let { { Text(it) } },
        showModeToggle = configuration.showModeToggle,
        colors = configuration.colors ?: DatePickerDefaults.colors(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptDatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    selectedDate: Long? = null,
    title: String = "Select Date",
    dateValidator: (Long) -> Boolean = { true },
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = dateValidator(utcTimeMillis)
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier,
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptDatePickerField(
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Date",
    placeholder: String = "Choose a date",
    enabled: Boolean = true,
    dateFormatter: (Long) -> String,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate?.let(dateFormatter) ?: "",
        onValueChange = { },
        modifier = modifier,
        enabled = enabled,
        readOnly = true,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select date")
            }
        },
    )

    if (showDatePicker) {
        KptDatePickerDialog(
            onDateSelected = { date ->
                onDateSelected(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            selectedDate = selectedDate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Immutable
data class DatePickerConfiguration(
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    override val modifier: Modifier = Modifier,
    val selectedDate: Long? = null,
    val onDateSelected: (Long?) -> Unit = {},
    val dateValidator: (Long) -> Boolean = { true },
    val yearRange: IntRange = IntRange(1900, 2100),
    val initialDisplayMode: DisplayMode = DisplayMode.Picker,
    val showModeToggle: Boolean = true,
    val title: String? = null,
    val headline: String? = null,
    val colors: DatePickerColors? = null,
) : KptComponent
