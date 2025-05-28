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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import template.core.base.designsystem.config.KptTestTags
import template.core.base.designsystem.core.CardVariant
import template.core.base.designsystem.core.KptCardConfiguration
import template.core.base.designsystem.theme.KptTheme

// Main Enhanced Component
@Composable
fun KptCard(configuration: KptCardConfiguration) {
    val finalModifier = configuration.modifier
        .testTag(configuration.testTag ?: KptTestTags.Card)
        .let { mod ->
            if (configuration.contentDescription != null) {
                mod.semantics { contentDescription = configuration.contentDescription }
            } else mod
        }

    val finalShape = configuration.shape ?: KptTheme.shapes.medium

    when (configuration.variant) {
        CardVariant.Filled -> {
            if (configuration.onClick != null) {
                Card(
                    onClick = configuration.onClick,
                    modifier = finalModifier,
                    enabled = configuration.enabled,
                    colors = configuration.colors ?: CardDefaults.cardColors(),
                    elevation = configuration.elevation ?: CardDefaults.cardElevation(),
                    shape = finalShape,
                    border = configuration.border,
                    interactionSource = configuration.interactionSource,
                ) {
                    CardContent(configuration)
                }
            } else {
                Card(
                    modifier = finalModifier,
                    colors = configuration.colors ?: CardDefaults.cardColors(),
                    elevation = configuration.elevation ?: CardDefaults.cardElevation(),
                    shape = finalShape,
                    border = configuration.border,
                ) {
                    CardContent(configuration)
                }
            }
        }

        CardVariant.Elevated -> {
            if (configuration.onClick != null) {
                ElevatedCard(
                    onClick = configuration.onClick,
                    modifier = finalModifier,
                    enabled = configuration.enabled,
                    colors = configuration.colors ?: CardDefaults.elevatedCardColors(),
                    elevation = configuration.elevation ?: CardDefaults.elevatedCardElevation(),
                    shape = finalShape,
                    interactionSource = configuration.interactionSource,
                ) {
                    CardContent(configuration)
                }
            } else {
                ElevatedCard(
                    modifier = finalModifier,
                    colors = configuration.colors ?: CardDefaults.elevatedCardColors(),
                    elevation = configuration.elevation ?: CardDefaults.elevatedCardElevation(),
                    shape = finalShape,
                ) {
                    CardContent(configuration)
                }
            }
        }

        CardVariant.Outlined -> {
            if (configuration.onClick != null) {
                OutlinedCard(
                    onClick = configuration.onClick,
                    modifier = finalModifier,
                    enabled = configuration.enabled,
                    colors = configuration.colors ?: CardDefaults.outlinedCardColors(),
                    elevation = configuration.elevation ?: CardDefaults.outlinedCardElevation(),
                    shape = finalShape,
                    border = configuration.border
                        ?: CardDefaults.outlinedCardBorder(configuration.enabled),
                    interactionSource = configuration.interactionSource,
                ) {
                    CardContent(configuration)
                }
            } else {
                OutlinedCard(
                    modifier = finalModifier,
                    colors = configuration.colors ?: CardDefaults.outlinedCardColors(),
                    elevation = configuration.elevation ?: CardDefaults.outlinedCardElevation(),
                    shape = finalShape,
                    border = configuration.border
                        ?: CardDefaults.outlinedCardBorder(configuration.enabled),
                ) {
                    CardContent(configuration)
                }
            }
        }

        is CardVariant.Custom -> {
            configuration.variant.renderer(configuration)
        }
    }
}

@Composable
private fun CardContent(configuration: KptCardConfiguration) {
    Column {
        configuration.header?.let { header ->
            header()
            if (configuration.contentPadding != PaddingValues(0.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Column(modifier = Modifier.padding(configuration.contentPadding)) {
            configuration.content(this)
        }

        configuration.footer?.let { footer ->
            if (configuration.contentPadding != PaddingValues(0.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            footer()
        }
    }
}

// 1. Simple card with just content
@Composable
fun KptCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            onClick = onClick,
            content = content
        )
    )
}

// 2. Card with title
@Composable
fun KptCard(
    title: String,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            onClick = onClick,
            header = {
                Text(
                    text = title,
                    style = KptTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            },
            content = content
        )
    )
}

// 3. Card with title and subtitle
@Composable
fun KptCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            onClick = onClick,
            header = {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    Text(
                        text = title,
                        style = KptTheme.typography.titleLarge
                    )
                    Text(
                        text = subtitle,
                        style = KptTheme.typography.bodyMedium,
                        color = KptTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            content = content
        )
    )
}

// 4. Card with icon and title
@Composable
fun KptCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            onClick = onClick,
            header = {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = KptTheme.typography.titleLarge
                    )
                }
            },
            content = content
        )
    )
}

// 5. Info card with action
@Composable
fun KptInfoCard(
    title: String,
    description: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    variant: CardVariant = CardVariant.Filled
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            header = if (icon != null) {
                {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = title,
                            style = KptTheme.typography.titleLarge
                        )
                    }
                }
            } else {
                {
                    Text(
                        text = title,
                        style = KptTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                }
            },
            footer = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onActionClick) {
                        Text(actionText)
                    }
                }
            },
            content = {
                Text(
                    text = description,
                    style = KptTheme.typography.bodyMedium
                )
            }
        )
    )
}

// 6. Stat card
@Composable
fun KptStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    icon: ImageVector? = null,
    valueColor: Color = KptTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            onClick = onClick,
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = KptTheme.typography.bodyMedium,
                            color = KptTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            style = KptTheme.typography.headlineMedium,
                            color = valueColor
                        )
                        subtitle?.let {
                            Text(
                                text = it,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = KptTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )
    )
}

// 7. Media card
@Composable
fun KptMediaCard(
    title: String,
    description: String,
    mediaContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    actions: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            onClick = onClick,
            contentPadding = PaddingValues(0.dp),
            header = {
                mediaContent()
            },
            footer = actions?.let {
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        actions()
                    }
                }
            },
            content = {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = KptTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = KptTheme.typography.bodyMedium
                    )
                }
            }
        )
    )
}

// 8. List item card
@Composable
fun KptListItemCard(
    title: String,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    onClick: (() -> Unit)? = null
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            onClick = onClick,
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    leadingIcon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = KptTheme.typography.bodyLarge
                        )
                        subtitle?.let {
                            Text(
                                text = it,
                                style = KptTheme.typography.bodyMedium,
                                color = KptTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    trailingIcon?.let { icon ->
                        if (onTrailingIconClick != null) {
                            IconButton(onClick = onTrailingIconClick) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null
                                )
                            }
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        )
    )
}

// 9. Specific variant convenience functions
@Composable
fun KptFilledCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    KptCard(
        modifier = modifier,
        variant = CardVariant.Filled,
        onClick = onClick,
        content = content
    )
}

@Composable
fun KptElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    KptCard(
        modifier = modifier,
        variant = CardVariant.Elevated,
        onClick = onClick,
        content = content
    )
}

@Composable
fun KptOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    KptCard(
        modifier = modifier,
        variant = CardVariant.Outlined,
        onClick = onClick,
        content = content
    )
}

// 10. Original component compatibility
@Composable
fun KptCard(
    variant: CardVariant,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    borderStroke: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
    testTag: String? = null,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            onClick = if (onClick != {}) onClick else null,
            enabled = enabled,
            variant = variant,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = borderStroke,
            interactionSource = interactionSource,
            testTag = testTag,
            contentDescription = contentDescription,
            content = content
        )
    )
}

// 11. Error and warning cards
@Composable
fun KptErrorCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = CardVariant.Filled,
            colors = CardDefaults.cardColors(
                containerColor = KptTheme.colorScheme.errorContainer
            ),
            header = {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = KptTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = KptTheme.typography.titleMedium,
                        color = KptTheme.colorScheme.onErrorContainer
                    )
                }
            },
            footer = if (actionText != null && onActionClick != null) {
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onActionClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = KptTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text(actionText)
                        }
                    }
                }
            } else null,
            content = {
                Text(
                    text = message,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onErrorContainer
                )
            }
        )
    )
}

@Composable
fun KptWarningCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = CardVariant.Filled,
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF3CD) // Warning color
            ),
            header = {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFF856404), // Warning text color
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = KptTheme.typography.titleMedium,
                        color = Color(0xFF856404)
                    )
                }
            },
            footer = if (actionText != null && onActionClick != null) {
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onActionClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF856404)
                            )
                        ) {
                            Text(actionText)
                        }
                    }
                }
            } else null,
            content = {
                Text(
                    text = message,
                    style = KptTheme.typography.bodyMedium,
                    color = Color(0xFF856404)
                )
            }
        )
    )
}