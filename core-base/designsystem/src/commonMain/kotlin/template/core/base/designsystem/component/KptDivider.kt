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

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.core.KptComponent
import template.core.base.designsystem.theme.KptTheme

@Composable
fun KptDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color? = null,
    testTag: String? = null,
    contentDescription: String? = null,
    orientation: DividerOrientation = DividerOrientation.Horizontal,
) {
    KptDivider(
        configuration = DividerConfiguration(
            testTag = testTag,
            contentDescription = contentDescription,
            modifier = modifier,
            thickness = thickness,
            color = color,
            orientation = orientation,
        ),
    )
}

@Composable
fun KptDivider(configuration: DividerConfiguration) {
    when (configuration.orientation) {
        DividerOrientation.Horizontal -> HorizontalDivider(
            modifier = configuration.modifier.testTag(configuration.testTag ?: "KptDivider"),
            thickness = configuration.thickness,
            color = configuration.color ?: KptTheme.colorScheme.outline,
        )

        DividerOrientation.Vertical -> VerticalDivider(
            modifier = configuration.modifier.testTag(configuration.testTag ?: "KptDivider"),
            thickness = configuration.thickness,
            color = configuration.color ?: KptTheme.colorScheme.outline,
        )
    }
}

@Immutable
data class DividerConfiguration(
    override val testTag: String? = null,
    override val contentDescription: String? = null,
    override val modifier: Modifier = Modifier,
    val thickness: Dp = 1.dp,
    val color: Color? = null,
    val orientation: DividerOrientation = DividerOrientation.Horizontal,
) : KptComponent

enum class DividerOrientation { Horizontal, Vertical }
