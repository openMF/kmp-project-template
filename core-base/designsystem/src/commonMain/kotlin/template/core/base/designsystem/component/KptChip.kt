package template.core.base.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ChipElevation
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SelectableChipElevation
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import template.core.base.designsystem.core.ChipVariant
import template.core.base.designsystem.core.ComponentConfigurationScope
import template.core.base.designsystem.core.ComponentDsl
import template.core.base.designsystem.core.KptComponent
import template.core.base.designsystem.theme.KptTheme

@Composable
fun KptChip(configuration: ChipConfiguration){
    val finalModifier = configuration.modifier
        .testTag(configuration.testTag ?: "KptChip")

    when (configuration.variant) {
        ChipVariant.Assist -> AssistChip(
            onClick = configuration.onClick,
            label = { Text(configuration.label) },
            modifier = finalModifier,
            enabled = configuration.enabled,
            leadingIcon = configuration.leadingIcon?.let {
                {
                    Icon(
                        it,
                        contentDescription = null,
                    )
                }
            },
            trailingIcon = configuration.trailingIcon?.let {
                {
                    Icon(
                        it,
                        contentDescription = null,
                    )
                }
            },
            colors = configuration.colors ?: AssistChipDefaults.assistChipColors(),
            elevation = configuration.elevation ?: AssistChipDefaults.assistChipElevation(),
            border = configuration.border
                ?: AssistChipDefaults.assistChipBorder(configuration.enabled),
            shape = configuration.shape ?: KptTheme.shapes.small,
            interactionSource = configuration.interactionSource,
        )

        ChipVariant.Filter -> FilterChip(
            onClick = configuration.onClick,
            label = { Text(configuration.label) },
            selected = configuration.selected,
            modifier = finalModifier,
            enabled = configuration.enabled,
            leadingIcon = configuration.leadingIcon?.let {
                {
                    Icon(
                        it,
                        contentDescription = null,
                    )
                }
            },
            trailingIcon = configuration.trailingIcon?.let {
                {
                    Icon(
                        it,
                        contentDescription = null,
                    )
                }
            },
            colors = configuration.selectableColors ?: FilterChipDefaults.filterChipColors(),
            elevation = configuration.selectableElevation ?: FilterChipDefaults.filterChipElevation(),
            border = configuration.border ?: FilterChipDefaults.filterChipBorder(
                configuration.enabled,
                configuration.selected,
            ),
            shape = configuration.shape ?: KptTheme.shapes.small,
            interactionSource = configuration.interactionSource,
        )

        ChipVariant.Input -> InputChip(
            onClick = configuration.onClick,
            label = { Text(configuration.label) },
            selected = configuration.selected,
            modifier = finalModifier,
            enabled = configuration.enabled,
            leadingIcon = configuration.leadingIcon?.let {
                {
                    Icon(
                        it,
                        contentDescription = null,
                    )
                }
            },
            trailingIcon = configuration.trailingIcon?.let {
                {
                    Icon(
                        it,
                        contentDescription = null,
                    )
                }
            },
            colors = configuration.selectableColors ?: InputChipDefaults.inputChipColors(),
            elevation = configuration.selectableElevation ?: InputChipDefaults.inputChipElevation(),
            border = configuration.border
                ?: InputChipDefaults.inputChipBorder(configuration.enabled, configuration.selected),
            shape = configuration.shape ?: KptTheme.shapes.small,
            interactionSource = configuration.interactionSource,
        )

        ChipVariant.Suggestion -> SuggestionChip(
            onClick = configuration.onClick,
            label = { Text(configuration.label) },
            modifier = finalModifier,
            enabled = configuration.enabled,
            icon = configuration.leadingIcon?.let { { Icon(it, contentDescription = null) } },
            colors = configuration.colors ?: SuggestionChipDefaults.suggestionChipColors(),
            elevation = configuration.elevation ?: SuggestionChipDefaults.suggestionChipElevation(),
            border = configuration.border ?: SuggestionChipDefaults.suggestionChipBorder(
                configuration.enabled,
            ),
            shape = configuration.shape ?: KptTheme.shapes.small,
            interactionSource = configuration.interactionSource,
        )
    }
}

@Composable
fun KptAssistChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    KptChip(
        kptChip {
            this.variant = ChipVariant.Assist
            this.label = label
            this.onClick = onClick
            this.modifier = modifier
            this.leadingIcon = leadingIcon
        },
    )
}

@Composable
fun KptFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    KptChip(
        kptChip {
            this.variant = ChipVariant.Filter
            this.label = label
            this.selected = selected
            this.onClick = onClick
            this.modifier = modifier
            this.leadingIcon = leadingIcon
        },
    )
}

fun kptChip(block: ChipBuilder.() -> Unit): ChipConfiguration {
    return ChipBuilder().apply(block).build()
}

@Immutable
data class ChipConfiguration(
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    override val modifier: Modifier = Modifier,
    val onClick: () -> Unit = {},
    val enabled: Boolean = true,
    val variant: ChipVariant = ChipVariant.Assist,
    val selected: Boolean = false,
    val label: String,
    val leadingIcon: ImageVector? = null,
    val trailingIcon: ImageVector? = null,
    val colors: ChipColors? = null,
    val elevation: ChipElevation? = null,
    val selectableColors: SelectableChipColors? = null,
    val selectableElevation: SelectableChipElevation? = null,
    val border: BorderStroke? = null,
    val shape: Shape? = null,
    val interactionSource: MutableInteractionSource? = null,
) : KptComponent

@ComponentDsl
class ChipBuilder : ComponentConfigurationScope {
    override var testTag: String? = null
    override var contentDescription: String? = null
    override var enabled: Boolean = true
    override var modifier: Modifier = Modifier

    var onClick: () -> Unit = {}
    var variant: ChipVariant = ChipVariant.Assist
    var selected: Boolean = false
    var label: String = ""
    var leadingIcon: ImageVector? = null
    var trailingIcon: ImageVector? = null
    var colors: ChipColors? = null
    var elevation: ChipElevation? = null
    var selectableColors: SelectableChipColors? = null
    var selectableElevation: SelectableChipElevation? = null
    var border: BorderStroke? = null
    var shape: Shape? = null
    var interactionSource: MutableInteractionSource? = null

    fun build(): ChipConfiguration = ChipConfiguration(
        testTag = testTag,
        contentDescription = contentDescription,
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        variant = variant,
        selected = selected,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = colors,
        elevation = elevation,
        selectableColors = selectableColors,
        selectableElevation = selectableElevation,
        border = border,
        shape = shape,
        interactionSource = interactionSource,
    )
}