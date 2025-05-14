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
 * Defines the visual style variants available for [CMPButton] in the CMP design system.
 *
 * Each variant maps to a corresponding Material3 button component and provides a different level
 * of emphasis and interaction style.
 */
enum class ButtonVariant {

    /**
     * A high-emphasis button using the default filled style.
     * Corresponds to [androidx.compose.material3.Button].
     * Recommended for primary actions.
     */
    FILLED,

    /**
     * A medium-emphasis button using a filled tonal background.
     * Corresponds to [androidx.compose.material3.FilledTonalButton].
     * Suitable for secondary actions.
     */
    TONAL,

    /**
     * A button with elevation to emphasize visual hierarchy.
     * Corresponds to [androidx.compose.material3.ElevatedButton].
     */
    ELEVATED,

    /**
     * A low-emphasis button outlined with a border.
     * Corresponds to [androidx.compose.material3.OutlinedButton].
     */
    OUTLINED,

    /**
     * A minimal, flat button with no background or border.
     * Corresponds to [androidx.compose.material3.TextButton].
     * Ideal for inline actions or low-priority tasks.
     */
    TEXT,
}
