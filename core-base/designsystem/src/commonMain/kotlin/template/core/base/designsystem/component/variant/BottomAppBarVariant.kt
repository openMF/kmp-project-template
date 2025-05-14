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
 * Defines the layout variants for the CMP bottom app bar component.
 *
 * This enum allows selecting between a standard bottom app bar with predefined action slots
 * or a fully custom bottom app bar with developer-defined content.
 */
enum class BottomAppBarVariant {
    /**
     * A standard bottom app bar layout that includes action slots and optionally a floating action button (FAB).
     * This is the default variant used in [CMPBottomAppBar].
     */
    BOTTOM_WITH_ACTIONS,

    /**
     * A fully custom bottom app bar layout. The content is provided via the [customContent] parameter
     * in [CMPBottomAppBar], allowing for complete control over the row layout.
     *
     * Use this when the standard actions/FAB layout does not meet your design requirements.
     */
    BOTTOM_CUSTOM,
}
