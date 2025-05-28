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

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun KptSimpleListItem(
    text: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    KptListItem(
        ListItemConfiguration(
            headlineContent = { Text(text) },
            supportingContent = supportingText?.let { { Text(it) } },
            leadingContent = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            trailingContent = trailingIcon?.let { { Icon(it, contentDescription = null) } },
            onClick = onClick,
            modifier = modifier,
        ),
    )
}
