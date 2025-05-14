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
 * Defines the visual style variants available for [CMPTextField] in the CMP design system.
 *
 * Each variant maps to a specific Material3 text field component and affects the appearance
 * of borders, background, and elevation.
 */
enum class TextFieldVariant {

    /**
     * A filled text field with a background and no border outline.
     * Corresponds to [androidx.compose.material3.TextField].
     * Recommended for most form inputs and primary text entry.
     */
    FILLED,

    /**
     * An outlined text field with a visible border and transparent background.
     * Corresponds to [androidx.compose.material3.OutlinedTextField].
     * Suitable for use cases where less visual weight or contrast is desired.
     */
    OUTLINED,
}
