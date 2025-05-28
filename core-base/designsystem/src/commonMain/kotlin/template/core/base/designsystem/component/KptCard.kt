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

/**
 * The main KptCard composable function. This function serves as the core
 * component for displaying cards in various styles (Filled, Elevated,
 * Outlined, or Custom). It takes a [KptCardConfiguration] object which
 * defines all the properties of the card, such as its variant, content,
 * click behavior, and styling.
 *
 * This function handles the common logic for applying modifiers (including
 * test tags and content descriptions) and determining the shape of
 * the card. It then delegates to the appropriate Material Design card
 * composable (Card, ElevatedCard, OutlinedCard) or a custom renderer based
 * on the specified [CardVariant].
 *
 * The content of the card, including optional header and footer, is
 * rendered by the [CardContent] composable, which is called within the
 * chosen card type.
 *
 * @param configuration The [KptCardConfiguration] object that defines the
 *    appearance and behavior of the card. This includes:
 *    - `variant`: The type of card to display (Filled, Elevated, Outlined,
 *      Custom).
 *    - `modifier`: The modifier to be applied to the card.
 *    - `onClick`: Optional lambda to be invoked when the card is clicked.
 *    - `enabled`: Whether the card is interactive.
 *    - `colors`: Custom colors for the card.
 *    - `elevation`: Custom elevation for the card.
 *    - `shape`: Custom shape for the card.
 *    - `border`: Custom border for the card (primarily for Outlined
 *      variant).
 *    - `interactionSource`: MutableInteractionSource for observing and
 *      controlling interactions.
 *    - `testTag`: A tag for testing purposes.
 *    - `contentDescription`: A description for accessibility.
 *    - `header`: Optional composable function for the card's header.
 *    - `content`: Composable function for the card's main content.
 *    - `footer`: Optional composable function for the card's footer.
 *    - `contentPadding`: Padding to be applied around the content area.
 *
 * @see KptCardConfiguration
 * @see CardVariant
 * @see CardContent
 * @see androidx.compose.material3.Card
 * @see androidx.compose.material3.ElevatedCard
 */
@Composable
fun KptCard(configuration: KptCardConfiguration) {
    val finalModifier = configuration.modifier
        .testTag(configuration.testTag ?: KptTestTags.CARD)
        .let { mod ->
            if (configuration.contentDescription != null) {
                mod.semantics { contentDescription = configuration.contentDescription }
            } else {
                mod
            }
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

/**
 * A basic card component. This is a simplified version of [KptCard] that
 * takes only essential parameters.
 *
 * This composable provides a convenient way to create a card with a
 * specific visual style and content. It uses the [KptCardConfiguration]
 * internally to configure the underlying Material Design card.
 *
 * @param modifier Optional [Modifier] for this card.
 * @param variant The visual style of the card, determining its appearance
 *    (e.g., filled, outlined, elevated). Defaults to [CardVariant.Filled].
 * @param onClick Optional lambda to be invoked when the card is clicked.
 *    If null, the card will not be clickable.
 * @param content A composable lambda that defines the content to be
 *    displayed within the card. The content is placed inside a
 *    [ColumnScope], allowing for vertical arrangement of elements.
 * @sample template.core.base.designsystem.component.preview.KptFilledCardPreview
 * @sample template.core.base.designsystem.component.preview.KptElevatedCardPreview
 * @sample template.core.base.designsystem.component.preview.KptOutlinedCardPreview
 */
@Composable
fun KptCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            onClick = onClick,
            content = content,
        ),
    )
}

/**
 * A basic card component that displays a title and content.
 *
 * This version of `KptCard` is a convenience overload for simple
 * cards that only need a title and content. It uses the default
 * [CardVariant.Filled] and provides a standard text style for the title.
 *
 * @param title The text to be displayed as the card's title.
 * @param modifier Optional [Modifier] for this card.
 * @param variant The visual style of the card, defaulting to
 *    [CardVariant.Filled].
 * @param onClick Optional lambda to be invoked when the card is clicked.
 *    If null, the card will not be clickable.
 * @param content The main content of the card, provided as a composable
 *    lambda function that operates within a [ColumnScope].
 * @sample template.core.base.designsystem.component.preview.KptCardWithTitlePreview
 */
@Composable
fun KptCard(
    title: String,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )
            },
            content = content,
        ),
    )
}

/**
 * A card component that displays a title and a subtitle. This is a simpler
 * version of the card, suitable for displaying basic information.
 *
 * @param title The main title text to be displayed in the card.
 * @param subtitle The subtitle text to be displayed below the title.
 * @param modifier Optional [Modifier] for this card.
 * @param variant The visual style of the card, defaulting to
 *    [CardVariant.Filled].
 * @param onClick Optional lambda to be invoked when the card is clicked.
 * @param content The main content of the card, provided as a composable
 *    lambda that receives a [ColumnScope].
 * @sample template.core.base.designsystem.component.preview.KptCardWithTitleAndSubtitlePreview
 */
@Composable
fun KptCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
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
                        style = KptTheme.typography.titleLarge,
                    )
                    Text(
                        text = subtitle,
                        style = KptTheme.typography.bodyMedium,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            content = content,
        ),
    )
}

/**
 * A card component that displays a title, an icon, and content.
 *
 * This version of `KptCard` is a convenience overload for cards
 * that need an icon alongside the title in the header.
 * It uses the default [CardVariant.Filled] and standard text style for
 * the title.
 *
 * @param title The text to be displayed as the card's title.
 * @param icon The [ImageVector] to be displayed next to the title.
 * @param modifier Optional [Modifier] for this card.
 * @param variant The visual style of the card, defaulting to
 *    [CardVariant.Filled].
 * @param onClick Optional lambda to be invoked when the card is clicked.
 *    If null, the card will not be clickable.
 * @param content The main content of the card, provided as a composable
 *    lambda function that operates within a [ColumnScope].
 * @sample template.core.base.designsystem.component.preview.KptCardWithTitleAndIconPreview
 */
@Composable
fun KptCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            onClick = onClick,
            header = {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = KptTheme.typography.titleLarge,
                    )
                }
            },
            content = content,
        ),
    )
}

/**
 * A card component that displays a title, description, and an action
 * button. It's suitable for conveying information and prompting user
 * interaction.
 *
 * @param title The main title text to be displayed in the card.
 * @param description The descriptive text to be displayed below the title.
 * @param actionText The text for the action button.
 * @param onActionClick Lambda to be invoked when the action button is
 *    clicked.
 * @param modifier Optional [Modifier] for this card.
 * @param icon Optional [ImageVector] to be displayed next to the title. If
 *    null, no icon is shown.
 * @param variant The visual style of the card, defaulting to
 *    [CardVariant.Filled].
 * @sample template.core.base.designsystem.component.preview.KptInfoCardPreview
 * @sample template.core.base.designsystem.component.preview.KptInfoCardWithIconPreview
 */
@Composable
fun KptInfoCard(
    title: String,
    description: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    variant: CardVariant = CardVariant.Filled,
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            header = if (icon != null) {
                {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = title,
                            style = KptTheme.typography.titleLarge,
                        )
                    }
                }
            } else {
                {
                    Text(
                        text = title,
                        style = KptTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    )
                }
            },
            footer = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onActionClick) {
                        Text(actionText)
                    }
                }
            },
            content = {
                Text(
                    text = description,
                    style = KptTheme.typography.bodyMedium,
                )
            },
        ),
    )
}

/**
 * A card component designed to display a statistic or key performance
 * indicator (KPI). It typically shows a title, a prominent value, and
 * an optional subtitle. An icon can also be included to visually
 * represent the statistic.
 *
 * This card is useful for dashboards, summary screens, or any place
 * where a concise numerical or textual piece of information needs to be
 * highlighted.
 *
 * @param title The main label or title for the statistic (e.g., "Total
 *    Sales", "Active Users").
 * @param value The actual value of the statistic to be displayed
 *    prominently (e.g., "$10,000", "1500").
 * @param subtitle Optional additional context or description for the
 *    statistic, displayed below the value (e.g., "Last 30 days", "vs.
 *    last month").
 * @param modifier Optional [Modifier] for this card.
 * @param variant The visual style of the card, determining its appearance
 *    (e.g., filled, outlined, elevated). Defaults to [CardVariant.Filled].
 * @param icon Optional [ImageVector] to be displayed alongside the
 *    statistic, typically on the right side.
 * @param valueColor The color used for displaying the main `value` text.
 *    Defaults to the primary color from [KptTheme.colorScheme].
 * @param onClick Optional lambda to be invoked when the card is clicked.
 *    If null, the card will not be clickable.
 * @sample template.core.base.designsystem.component.preview.KptStatCardPreview
 * @sample template.core.base.designsystem.component.preview.KptStatCardWithIconPreview
 * @sample template.core.base.designsystem.component.preview.KptClickableStatCardPreview
 */
@Composable
fun KptStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    icon: ImageVector? = null,
    valueColor: Color = KptTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            onClick = onClick,
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = KptTheme.typography.bodyMedium,
                            color = KptTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = value,
                            style = KptTheme.typography.headlineMedium,
                            color = valueColor,
                        )
                        subtitle?.let {
                            Text(
                                text = it,
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = KptTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
        ),
    )
}

/**
 * A card component that features media content, a title, a description,
 * and optional actions. This card is ideal for displaying items that have
 * a visual representation, such as images or videos, along with
 * textual information and interactive elements.
 *
 * The `mediaContent` is typically placed at the top of the card.
 * The title and description are displayed below the media.
 * Optional action buttons can be placed in the footer of the card,
 * aligned to the end.
 *
 * @param title The main title text to be displayed in the card.
 * @param description The descriptive text to be displayed below the title.
 * @param mediaContent A composable lambda that defines the media to be
 *    displayed at the top of the card (e.g., an `Image` or `VideoPlayer`).
 * @param modifier Optional [Modifier] for this card.
 * @param variant The visual style of the card, defaulting to
 *    [CardVariant.Filled].
 * @param actions Optional composable lambda that defines actions to be
 *    displayed in the card's footer. This lambda operates within a
 *    [RowScope], allowing for horizontal arrangement of action buttons.
 * @param onClick Optional lambda to be invoked when the card is clicked.
 *    If null, the card will not be clickable.
 * @sample template.core.base.designsystem.component.preview.KptMediaCardPreview
 * @sample template.core.base.designsystem.component.preview.KptMediaCardWithActionsPreview
 */
@Composable
fun KptMediaCard(
    title: String,
    description: String,
    mediaContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    actions: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
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
                        horizontalArrangement = Arrangement.End,
                    ) {
                        actions()
                    }
                }
            },
            content = {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = KptTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = KptTheme.typography.bodyMedium,
                    )
                }
            },
        ),
    )
}

/**
 * A card component that displays a list item with a title, an optional
 * subtitle, and optional leading/trailing icons.
 *
 * This composable is designed to represent items in a list, offering a
 * clear and structured way to display information. It supports customization
 * through icons and click actions.
 *
 * @param title The main text to be displayed for the list item.
 * @param subtitle Optional secondary text displayed below the title,
 *    providing additional details.
 * @param leadingIcon Optional [ImageVector] to be displayed at the start of
 *    the list item.
 * @param trailingIcon Optional [ImageVector] to be displayed at the end of
 *    the list item.
 * @param onTrailingIconClick Optional lambda to be invoked when the
 *    trailing icon is clicked. If provided, the trailing icon will be
 *    rendered as an [IconButton].
 * @param modifier Optional [Modifier] for this card.
 * @param variant The visual style of the card, defaulting to
 *    [CardVariant.Filled].
 * @param onClick Optional lambda to be invoked when the card itself is
 *    clicked.
 * @sample template.core.base.designsystem.component.preview.KptListItemCardPreview
 * @sample template.core.base.designsystem.component.preview.KptListItemCardWithIconsPreview
 * @sample template.core.base.designsystem.component.preview.KptListItemCardClickablePreview
 */
@Composable
fun KptListItemCard(
    title: String,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Filled,
    onClick: (() -> Unit)? = null,
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = variant,
            onClick = onClick,
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leadingIcon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = KptTheme.typography.bodyLarge,
                        )
                        subtitle?.let {
                            Text(
                                text = it,
                                style = KptTheme.typography.bodyMedium,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    trailingIcon?.let { icon ->
                        if (onTrailingIconClick != null) {
                            IconButton(onClick = onTrailingIconClick) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                )
                            }
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            },
        ),
    )
}

@Composable
fun KptFilledCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    KptCard(
        modifier = modifier,
        variant = CardVariant.Filled,
        onClick = onClick,
        content = content,
    )
}

@Composable
fun KptElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    KptCard(
        modifier = modifier,
        variant = CardVariant.Elevated,
        onClick = onClick,
        content = content,
    )
}

@Composable
fun KptOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    KptCard(
        modifier = modifier,
        variant = CardVariant.Outlined,
        onClick = onClick,
        content = content,
    )
}

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
            content = content,
        ),
    )
}

@Composable
fun KptErrorCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = CardVariant.Filled,
            colors = CardDefaults.cardColors(
                containerColor = KptTheme.colorScheme.errorContainer,
            ),
            header = {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = KptTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = KptTheme.typography.titleMedium,
                        color = KptTheme.colorScheme.onErrorContainer,
                    )
                }
            },
            footer = if (actionText != null && onActionClick != null) {
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = onActionClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = KptTheme.colorScheme.onErrorContainer,
                            ),
                        ) {
                            Text(actionText)
                        }
                    }
                }
            } else {
                null
            },
            content = {
                Text(
                    text = message,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onErrorContainer,
                )
            },
        ),
    )
}

@Composable
fun KptWarningCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    KptCard(
        KptCardConfiguration(
            modifier = modifier,
            variant = CardVariant.Filled,
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF3CD),
            ),
            header = {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFF856404),
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = KptTheme.typography.titleMedium,
                        color = Color(0xFF856404),
                    )
                }
            },
            footer = if (actionText != null && onActionClick != null) {
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = onActionClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF856404),
                            ),
                        ) {
                            Text(actionText)
                        }
                    }
                }
            } else {
                null
            },
            content = {
                Text(
                    text = message,
                    style = KptTheme.typography.bodyMedium,
                    color = Color(0xFF856404),
                )
            },
        ),
    )
}
