/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.feature.home.model.TaskEntity

/**
 * A Composable screen that displays the list of tasks along with various controls such as:
 * - A toolbar for navigating to settings.
 * - A floating action button to add new tasks.
 * - A year picker, month picker, and day picker to filter tasks based on date.
 * - A list of tasks with checkboxes to mark them as completed or incomplete.
 *
 * @param onSettingsClick A callback to navigate to the settings screen when the settings icon is clicked.
 * @param onAddNewTask A callback that triggers the action to add a new task when the floating action button is clicked.
 * @param onTaskClick A callback to handle task click events.
 * @param modifier Modifier for customizing the layout of this composable screen.
 */
@Composable
fun TasksScreen(
    onSettingsClick: () -> Unit,
    onAddEditNewTask: (id: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tasksViewModel: TasksViewModel = koinViewModel()
    val tasks by tasksViewModel.tasks.collectAsStateWithLifecycle()
    val tasksUiState by tasksViewModel.tasksUiState.collectAsStateWithLifecycle()

    TasksScreenContent(
        tasksUiState = tasksUiState,
        onAddNewTask = {
            onAddEditNewTask(null)
        },
        onSettingsClick = onSettingsClick,
        onTaskDelete = tasksViewModel::deleteTask,
        onTaskClick = onAddEditNewTask,
        onTaskCheckedChange = tasksViewModel::flagTask,
        tasks = tasks,
        onYearSelected = tasksViewModel::updateSelectedYear,
        onNextMonthClicked = tasksViewModel::selectNextMonth,
        onPreviousMonthClicked = tasksViewModel::selectPreviousMonth,
        onSelectedDayInMonthChange = tasksViewModel::updateSelectDayInMonth,
        modifier = modifier,
    )
}

/**
 * The content of the Tasks screen, which includes:
 * - A toolbar with settings navigation.
 * - Floating action button for adding a new task.
 * - Year, month, and day pickers to filter tasks by date.
 * - A list of tasks.
 *
 * @param tasksUiState UI state containing selected year, month, day, and tasks data.
 * @param onAddNewTask Callback triggered when the floating action button is clicked.
 * @param onSettingsClick Callback triggered when the settings icon is clicked.
 * @param onTaskClick Callback triggered when a task is clicked.
 * @param onTaskCheckedChange Callback triggered when the checkbox for a task is changed.
 * @param tasks List of tasks to display.
 * @param onYearSelected Callback triggered when a year is selected from the year picker.
 * @param onNextMonthClicked Callback triggered when the next month button is clicked.
 * @param onPreviousMonthClicked Callback triggered when the previous month button is clicked.
 * @param onSelectedDayInMonthChange Callback triggered when a day is selected from the day picker.
 * @param modifier Modifier for customizing the layout of this composable screen.
 */
@Composable
fun TasksScreenContent(
    tasksUiState: TasksUiState,
    onAddNewTask: () -> Unit,
    onSettingsClick: () -> Unit,
    onTaskDelete: (Int) -> Unit,
    onTaskClick: (Int) -> Unit,
    onTaskCheckedChange: (TaskEntity) -> Unit,
    tasks: List<TaskEntity>,
    onYearSelected: (Int) -> Unit,
    onNextMonthClicked: () -> Unit,
    onPreviousMonthClicked: () -> Unit,
    onSelectedDayInMonthChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TasksToolBar(
                onSettingsClicked = onSettingsClick,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewTask,
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Task",
                )
            }
        },
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = modifier.padding(innerPadding),
        ) {
            YearPicker(
                selectedYear = tasksUiState.selectedYear,
                minYear = 1900,
                maxYear = 2100,
                onYearSelected = onYearSelected,
                modifier = Modifier.padding(
                    start = 10.dp,
                    end = 10.dp,
                ),
            )
            MonthPicker(
                selectedMonth = tasksUiState.selectedMonth,
                onNextMonth = onNextMonthClicked,
                onPreviousMonth = onPreviousMonthClicked,
                modifier = Modifier.padding(
                    start = 10.dp,
                    end = 10.dp,
                ),
            )
            HorizontalDayPicker(
                selectedDayInMonth = tasksUiState.selectedDayInMonth,
                onSelectedDayInMonthChange = onSelectedDayInMonthChange,
                daysInMonth = tasksUiState.weekdaysAndDaysInMonth,
            )
            TaskItemList(
                tasks = tasks,
                onTaskDelete = onTaskDelete,
                onTaskClick = {
                    onTaskClick(it.id)
                },
                onTaskCheckedChange = onTaskCheckedChange,
                modifier = Modifier.padding(
                    start = 10.dp,
                    end = 10.dp,
                ),
            )
        }
    }
}

/**
 * The toolbar for the Tasks screen containing a title and settings icon.
 *
 * @param onSettingsClicked Callback triggered when the settings icon is clicked.
 * @param modifier Modifier for customizing the layout of the toolbar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksToolBar(
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                text = "Tasks",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        actions = {
            IconButton(onClick = onSettingsClicked) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Navigate to Settings",
                )
            }
        },
        modifier = modifier,
    )
}

/**
 * A year picker that allows users to select a year.
 *
 * @param selectedYear The currently selected year.
 * @param minYear The minimum year allowed for selection.
 * @param maxYear The maximum year allowed for selection.
 * @param onYearSelected Callback triggered when a year is selected.
 * @param modifier Modifier for customizing the layout of the year picker.
 */
@Composable
fun YearPicker(
    selectedYear: Int,
    minYear: Int,
    maxYear: Int,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var intentionallyDismissed by remember { mutableStateOf(false) }
    var rowHeight by remember { mutableIntStateOf(0) }
    val years = (minYear..maxYear).toList()

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.onGloballyPositioned { coordinates ->
                rowHeight = coordinates.size.height
            },
        ) {
            Text(
                text = selectedYear.toString(),
                modifier = Modifier,
            )
            IconButton(
                onClick = {
                    intentionallyDismissed = true
                    expanded = !expanded
                },
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Select year",
                )
            }
        }
        if (expanded) {
            Popup(
                onDismissRequest = {
                    if (!intentionallyDismissed) {
                        expanded = false
                    }
                    intentionallyDismissed = false
                },
                offset = IntOffset(0, rowHeight),
                alignment = Alignment.TopStart,
            ) {
                Surface(
                    modifier = Modifier.shadow(elevation = 4.dp).height(200.dp),
                ) {
                    YearGridSelector(
                        selectedYear = selectedYear,
                        years = years,
                        onYearSelected = { year ->
                            onYearSelected(year)
                            expanded = false
                        },
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
        }
    }
}

/**
 * A grid selector for picking a year from a list of years.
 *
 * @param years List of years to display.
 * @param selectedYear The currently selected year.
 * @param onYearSelected Callback triggered when a year is selected.
 * @param modifier Modifier for customizing the layout of the grid selector.
 */
@Composable
fun YearGridSelector(
    years: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
    ) {
        items(items = years) { year ->
            Box(
                modifier = Modifier.fillMaxWidth()
                    .selectable(selected = year == selectedYear, onClick = { onYearSelected(year) })
                    .background(
                        color = if (year == selectedYear) {
                            MaterialTheme.colorScheme.surfaceContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = MaterialTheme.shapes.small,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

/**
 * A month picker that allows users to navigate between months.
 *
 * @param selectedMonth The currently selected month.
 * @param onNextMonth Callback triggered when the next month button is clicked.
 * @param onPreviousMonth Callback triggered when the previous month button is clicked.
 * @param modifier Modifier for customizing the layout of the month picker.
 */
@Composable
fun MonthPicker(
    selectedMonth: String,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                contentDescription = "Navigate to previous month",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = selectedMonth,
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.weight(1F),
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = "Navigate to next month",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/**
 * A composable that displays a day of the month along with its corresponding weekday.
 * It shows a border when the day is selected and can be clicked to change the selected day.
 *
 * @param weekday The name of the weekday (e.g., "Mon", "Tue").
 * @param dayOfMonth The day of the month (e.g., "1", "15").
 * @param selectedDayInMonth The day of the month that is currently selected.
 * @param onDayOfMonthClick A callback triggered when the user clicks on a day of the month.
 * @param modifier Modifier for customizing the layout of the item.
 */
@Composable
fun WeekdayAndDayOfMonthItem(
    weekday: String,
    dayOfMonth: String,
    selectedDayInMonth: String,
    onDayOfMonthClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelected = dayOfMonth == selectedDayInMonth
    Surface(
        border = BorderStroke(
            width = if (isSelected) 2.dp else 0.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        ),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(10.dp).width(35.dp)
                .clickable { onDayOfMonthClick(dayOfMonth) },
        ) {
            Text(
                text = weekday,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = dayOfMonth,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/**
 * A composable that displays a horizontal list of days of the month, showing the weekday and day.
 * It allows the user to scroll through the days of the month and select a day.
 *
 * @param selectedDayInMonth The currently selected day of the month.
 * @param onSelectedDayInMonthChange A callback triggered when a user selects a new day.
 * @param daysInMonth A list of pairs containing the weekday and the day of the month.
 * @param modifier Modifier for customizing the layout of the horizontal day picker.
 */
@Composable
fun HorizontalDayPicker(
    selectedDayInMonth: String,
    onSelectedDayInMonthChange: (String) -> Unit,
    daysInMonth: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier,
    ) {
        items(daysInMonth) { (weekday, dayOfMonth) ->
            WeekdayAndDayOfMonthItem(
                onDayOfMonthClick = onSelectedDayInMonthChange,
                weekday = weekday,
                dayOfMonth = dayOfMonth,
                selectedDayInMonth = selectedDayInMonth,
            )
        }
    }
}

/**
 * A composable that represents a task item, displaying the task's title, due time, and a checkbox
 * that marks the task as completed or not.
 *
 * @param task The task to display.
 * @param onTaskClick A callback triggered when the task item is clicked.
 * @param onTaskCheckedChange A callback triggered when the task's completion checkbox is changed.
 * @param modifier Modifier for customizing the layout of the task item.
 */
@Composable
fun TaskItem(
    task: TaskEntity,
    onTaskDelete: () -> Unit,
    onTaskClick: () -> Unit,
    onTaskCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.clickable { onTaskClick() },
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(start = 20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1F).padding(15.dp),
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = task.dueTime,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row {
                    IconButton(
                        onClick = onTaskDelete,
                        modifier = Modifier.padding(end = 10.dp).border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape,
                        ).size(38.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                        )
                    }
                    IconButton(
                        onClick = onTaskCheckedChange,
                        modifier = Modifier.padding(end = 10.dp).border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape,
                        ).background(
                            color = if (task.completed) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            } else {
                                Color.Transparent
                            },
                            shape = CircleShape,
                        ).size(38.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = if (task.completed) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItemList(
    tasks: List<TaskEntity>,
    onTaskDelete: (Int) -> Unit,
    onTaskClick: (TaskEntity) -> Unit,
    onTaskCheckedChange: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 10.dp),
        modifier = modifier,
    ) {
        items(items = tasks, key = { task -> task.id }) { task ->
            TaskItem(
                task = task,
                onTaskDelete = { onTaskDelete(task.id) },
                onTaskClick = { onTaskClick(task) },
                onTaskCheckedChange = { onTaskCheckedChange(task) },
            )
        }
    }
}
