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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import template.core.base.designsystem.core.ComponentConfigurationScope
import template.core.base.designsystem.core.ComponentDsl
import template.core.base.designsystem.core.KptComponent
import template.core.base.designsystem.theme.KptTheme

@Composable
fun KptSlider(configuration: SliderConfiguration) {
    Column {
        if (configuration.showLabel) {
            val labelText = configuration.labelFormatter?.invoke(configuration.value)
                ?: configuration.value.toString()
            Text(
                text = labelText,
                style = KptTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = KptTheme.spacing.sm),
            )
        }

        Slider(
            value = configuration.value,
            onValueChange = configuration.onValueChange,
            modifier = configuration.modifier.testTag(configuration.testTag ?: "KptSlider"),
            enabled = configuration.enabled,
            valueRange = configuration.valueRange,
            steps = configuration.steps,
            onValueChangeFinished = configuration.onValueChangeFinished,
            colors = configuration.colors ?: SliderDefaults.colors(),
//            interactionSource = configuration.interactionSource,
        )
    }
}

fun kptSlider(block: SliderBuilder.() -> Unit): SliderConfiguration {
    return SliderBuilder().apply(block).build()
}

@Immutable
data class SliderConfiguration(
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    override val modifier: Modifier = Modifier,
    val value: Float,
    val onValueChange: (Float) -> Unit,
    val enabled: Boolean = true,
    val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    val steps: Int = 0,
    val onValueChangeFinished: (() -> Unit)? = null,
    val colors: SliderColors? = null,
    val interactionSource: MutableInteractionSource? = null,
    val showLabel: Boolean = false,
    val labelFormatter: ((Float) -> String)? = null,
) : KptComponent

@ComponentDsl
class SliderBuilder : ComponentConfigurationScope {
    override var testTag: String? = null
    override var contentDescription: String? = null
    override var enabled: Boolean = true
    override var modifier: Modifier = Modifier

    var value: Float = 0f
    var onValueChange: (Float) -> Unit = {}
    var valueRange: ClosedFloatingPointRange<Float> = 0f..1f
    var steps: Int = 0
    var onValueChangeFinished: (() -> Unit)? = null
    var colors: SliderColors? = null
    var interactionSource: MutableInteractionSource? = null
    var showLabel: Boolean = false
    var labelFormatter: ((Float) -> String)? = null

    fun build(): SliderConfiguration = SliderConfiguration(
        testTag = testTag,
        contentDescription = contentDescription,
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        showLabel = showLabel,
        labelFormatter = labelFormatter,
    )
}
