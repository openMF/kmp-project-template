/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.feature.home.model.ActionToolbar
import org.mifos.feature.home.model.TaskEntity
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Composable for the Edit Task screen.
 *
 * @param navigateBack A lambda triggered when the user navigates back from this screen.
 * @param onTaskSaved A lambda triggered when the task is successfully saved.
 * @param modifier A [Modifier] for custom styling or layout adjustments.
 */
@Composable
fun EditTaskScreen(
    navigateBack: () -> Unit,
    onTaskSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val editTaskViewModel: EditTaskViewModel = koinViewModel()
    val task by editTaskViewModel.task.collectAsStateWithLifecycle()
    val isTaskSaved by editTaskViewModel.isTaskSaved.collectAsStateWithLifecycle()

    val onDateChangeMemoized: (Long) -> Unit = remember { editTaskViewModel::onDateChange }
    val onTimeChangeMemoized: (Int, Int) -> Unit = remember { editTaskViewModel::onTimeChange }
    val onPriorityChangeMemoized: (String) -> Unit =
        remember { editTaskViewModel::onPriorityChange }
    val onSaveTaskMemoized: () -> Unit = remember { editTaskViewModel::onSaveTask }

    LaunchedEffect(isTaskSaved) {
        if (isTaskSaved) {
            onTaskSaved()
            editTaskViewModel.resetTaskSaved()
        }
    }

    EditTaskScreenContent(
        isEditing = task.id != 0,
        task = task,
        onNavigateBack = navigateBack,
        onDateChange = onDateChangeMemoized,
        onTimeChange = onTimeChangeMemoized,
        onTitleChange = editTaskViewModel::onTitleChange,
        onDescriptionChange = editTaskViewModel::onDescriptionChange,
        onPriorityChange = onPriorityChangeMemoized,
        onSaveTask = onSaveTaskMemoized,
        modifier = modifier,
    )
}

/**
 * The core UI for editing task details.
 *
 * @param isEditing Indicates if the screen is in editing mode.
 * @param task The [Task] object containing current task details.
 * @param onNavigateBack A lambda triggered when the user navigates back.
 * @param onDateChange Lambda to handle date changes in the task.
 * @param onTimeChange Lambda to handle time changes in the task.
 * @param onTitleChange Lambda to handle changes in the task title.
 * @param onDescriptionChange Lambda to handle changes in the task description.
 * @param onPriorityChange Lambda to handle priority changes in the task.
 * @param isAlertEnabled Whether the alert toggle is enabled for the task.
 * @param onAlertChange Lambda to handle alert enable/disable changes.
 * @param onSaveTask Lambda to handle task saving action.
 * @param modifier A [Modifier] for custom styling or layout adjustments.
 */
@Composable
fun EditTaskScreenContent(
    isEditing: Boolean,
    task: TaskEntity,
    onNavigateBack: () -> Unit,
    onDateChange: (Long) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onSaveTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            ActionToolbar(
                title = if (isEditing) {
                    "Edit Task"
                } else {
                    "Create New Task"
                },
                onNavigateUp = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier
                .padding(innerPadding)
                .padding(10.dp)
                .verticalScroll(state = rememberScrollState()),
        ) {
            OutlinedTextField(
                value = task.dueDate,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        text = "MM/dd/yyyy",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showDatePickerDialog = !showDatePickerDialog }) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "select date",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            TaskTitleAndDescription(
                title = task.title,
                description = task.description,
                onTitleChange = onTitleChange,
                onDescriptionChange = onDescriptionChange,
            )
            OutlinedTextField(
                value = task.dueTime,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        text = "Pick a time",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showTimePickerDialog = !showTimePickerDialog }) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = "select time",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            TaskPriority(
                selectedPriority = task.priority,
                onPriorityChange = onPriorityChange,
            )
            Button(onClick = onSaveTask, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isEditing) {
                        "Edit your Task"
                    } else {
                        "Create Task"
                    },
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }

    if (showDatePickerDialog) {
        DateSelectionDialog(
            onDismissRequest = {
                showDatePickerDialog = false
            },
            onConfirmClicked = { currentDateInMillis ->
                onDateChange(currentDateInMillis)
                showDatePickerDialog = false
            },
            onDismissButtonClicked = {
                showDatePickerDialog = false
            },
        )
    }

    if (showTimePickerDialog) {
        TimeSelectionDialog(
            onDismiss = {
                showTimePickerDialog = false
            },
            onConfirm = { hour, minute ->
                onTimeChange(hour, minute)
                showTimePickerDialog = false
            },
        )
    }
}

/**
 * A dialog for selecting a date using a date picker.
 *
 * @param onDismissRequest Lambda to dismiss the dialog.
 * @param onConfirmClicked Lambda triggered when the user confirms a date.
 * @param onDismissButtonClicked Lambda triggered when the user cancels the dialog.
 * @param modifier A [Modifier] for custom styling or layout adjustments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionDialog(
    onDismissRequest: () -> Unit,
    onConfirmClicked: (Long) -> Unit,
    onDismissButtonClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = {
                    onConfirmClicked(datePickerState.selectedDateMillis!!)
                },
                enabled = datePickerState.selectedDateMillis != null,
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismissButtonClicked) {
                Text(text = "Cancel")
            }
        },
        modifier = modifier,
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.verticalScroll(rememberScrollState()),
        )
    }
}

/**
 * A dialog for selecting a time using a time picker.
 *
 * @param onConfirm Lambda triggered when the user confirms the selected time, providing the hour and minute.
 * @param onDismiss Lambda triggered when the user dismisses or cancels the dialog.
 * @param modifier A [Modifier] for custom styling or layout adjustments.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun TimeSelectionDialog(
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = true,
    )
    TimePickerDialog(
        onDismiss = onDismiss,
        onConfirm = {
            onConfirm(timePickerState.hour, timePickerState.minute)
        },
        modifier = modifier,
    ) {
        TimePicker(
            state = timePickerState,
        )
    }
}

/**
 * A reusable dialog for displaying time picker or other custom content.
 *
 * @param onDismiss Lambda triggered when the dialog is dismissed.
 * @param onConfirm Lambda triggered when the user confirms the dialog action.
 * @param modifier A [Modifier] for custom styling or layout adjustments.
 * @param content The composable content to display within the dialog.
 */
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dismissText = "Cancel"
    val confirmText = "Confirm"
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText)
            }
        },
        text = { content() },
        modifier = modifier,
    )
}

/**
 * A composable for entering the task's title and description.
 *
 * @param title The current value of the task's title.
 * @param onTitleChange Lambda triggered when the title is updated.
 * @param onDescriptionChange Lambda triggered when the description is updated.
 * @param description The current value of the task's description.
 * @param modifier A [Modifier] for custom styling or layout adjustments.
 */
@Composable
fun TaskTitleAndDescription(
    title: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            maxLines = 2,
            placeholder = {
                Text(
                    text = "Title",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = {
                Text(
                    text = "Description",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            minLines = 3,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * A composable to select the priority of the task.
 *
 * @param selectedPriority The currently selected priority as a [String].
 * @param onPriorityChange Lambda triggered when the user selects a new priority.
 * @param modifier A [Modifier] for custom styling or layout adjustments.
 */
@Composable
fun TaskPriority(
    selectedPriority: String,
    onPriorityChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Priority",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val priorities = listOf("Low", "Medium", "High")
            priorities.forEach { priority ->
                PriorityChip(
                    text = priority,
                    selected = priority == selectedPriority,
                    onSelected = {
                        onPriorityChange(priority)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * A chip for selecting task priority.
 *
 * @param text The display text of the chip.
 * @param selected Whether the chip is currently selected.
 * @param onSelected Lambda triggered when the chip is selected.
 * @param modifier A [Modifier] for custom styling or layout adjustments.
 */
@Composable
fun PriorityChip(
    text: String,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        onClick = onSelected,
        label = {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        selected = selected,
        leadingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "selected",
                )
            }
        },
        modifier = modifier,
    )
}
