package template.core.base.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import template.core.base.designsystem.core.KptComponent
import template.core.base.designsystem.theme.KptTheme

@Composable
fun KptRadioGroup(
    configuration: RadioGroupConfiguration,
    colors: RadioButtonColors? = null,
    arrangement:  Arrangement.Vertical = Arrangement.spacedBy(KptTheme.spacing.sm),
) {
    Column(
        modifier = configuration.modifier
            .selectableGroup()
            .testTag(configuration.testTag ?: "KptRadioGroup"),
        verticalArrangement = arrangement,
    ) {
        configuration.options.forEach { option ->
            KptRadioButton(
                RadioButtonConfiguration(
                    selected = configuration.selectedOption == option.value,
                    onClick = { configuration.onOptionSelected(option.value) },
                    enabled = configuration.enabled && option.enabled,
                    colors = colors,
                    label = option.label,
                    description = option.description,
                ),
            )
        }
    }
}

@Immutable
data class RadioGroupConfiguration(
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    override val modifier: Modifier = Modifier,
    val options: List<RadioGroupOption>,
    val selectedOption: String?,
    val onOptionSelected: (String) -> Unit,
    val enabled: Boolean = true,
) : KptComponent

@Immutable
data class RadioGroupOption(
    val value: String,
    val label: String,
    val description: String? = null,
    val enabled: Boolean = true,
)