package template.core.base.designsystem.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import template.core.base.designsystem.core.KptComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptTimePicker(configuration: TimePickerConfiguration) {
    val timePickerState = rememberTimePickerState(
        initialHour = configuration.initialHour,
        initialMinute = configuration.initialMinute,
        is24Hour = configuration.is24Hour
    )

    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        configuration.onTimeChanged(timePickerState.hour, timePickerState.minute)
    }

    TimePicker(
        state = timePickerState,
        modifier = configuration.modifier.testTag(configuration.testTag ?: "KptTimePicker"),
        colors = configuration.colors ?: TimePickerDefaults.colors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Immutable
data class TimePickerConfiguration(
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    override val modifier: Modifier = Modifier,
    val initialHour: Int = 0,
    val initialMinute: Int = 0,
    val is24Hour: Boolean = true,
    val onTimeChanged: (hour: Int, minute: Int) -> Unit = { _, _ -> },
    val colors: TimePickerColors? = null,
) : KptComponent