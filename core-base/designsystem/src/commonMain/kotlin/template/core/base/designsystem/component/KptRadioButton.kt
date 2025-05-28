package template.core.base.designsystem.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import template.core.base.designsystem.core.KptComponent
import template.core.base.designsystem.theme.KptTheme

@Composable
fun KptRadioButton(configuration: RadioButtonConfiguration) {
    if (configuration.label != null) {
        Row(
            modifier = configuration.modifier
                .selectable(
                    selected = configuration.selected,
                    onClick = { configuration.onClick?.invoke() },
                    enabled = configuration.enabled,
                )
                .padding(horizontal = KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = configuration.selected,
                onClick = configuration.onClick,
                modifier = Modifier.testTag(configuration.testTag ?: "KptRadioButton"),
                enabled = configuration.enabled,
                colors = configuration.colors ?: RadioButtonDefaults.colors(),
                interactionSource = configuration.interactionSource,
            )
            Spacer(modifier = Modifier.width(KptTheme.spacing.sm))
            Column {
                Text(
                    text = configuration.label,
                    style = KptTheme.typography.bodyLarge,
                )
                if (configuration.description != null) {
                    Text(
                        text = configuration.description,
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    } else {
        RadioButton(
            selected = configuration.selected,
            onClick = configuration.onClick,
            modifier = configuration.modifier.testTag(configuration.testTag ?: "KptRadioButton"),
            enabled = configuration.enabled,
            colors = configuration.colors ?: RadioButtonDefaults.colors(),
            interactionSource = configuration.interactionSource,
        )
    }
}

@Immutable
data class RadioButtonConfiguration(
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    override val modifier: Modifier = Modifier,
    val selected: Boolean,
    val onClick: (() -> Unit)?,
    val enabled: Boolean = true,
    val colors: RadioButtonColors? = null,
    val interactionSource: MutableInteractionSource? = null,
    val label: String? = null,
    val description: String? = null,
) : KptComponent
