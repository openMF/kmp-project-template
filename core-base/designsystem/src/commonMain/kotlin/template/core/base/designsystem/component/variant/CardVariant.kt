/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.component.variant

/**
 * Defines the visual style variants available for [CMPCard] in the CMP design system.
 *
 * Each variant maps to a specific Material3 card implementation and determines the
 * visual elevation, border, and background behavior.
 */
enum class CardVariant {

    /**
     * A standard filled card with background color and elevation.
     * Corresponds to [androidx.compose.material3.Card].
     * Recommended for most use cases where elevation and theming are desired.
     */
    FILLED,

    /**
     * A card with extra emphasis via elevation and contrast, but no border.
     * Corresponds to [androidx.compose.material3.ElevatedCard].
     * Useful for drawing attention to important or interactive content.
     */
    ELEVATED,

    /**
     * A low-emphasis card with a visible border and no elevation.
     * Corresponds to [androidx.compose.material3.OutlinedCard].
     * Suitable for lightweight, non-intrusive containers or when visual grouping is needed without elevation.
     */
    OUTLINED,
}
