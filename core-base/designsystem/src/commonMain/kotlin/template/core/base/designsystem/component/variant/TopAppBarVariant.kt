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
 * Defines the visual style variants available for [CMPTopAppBar] in the CMP design system.
 *
 * Each variant corresponds to a different Material3 top app bar layout with specific visual
 * hierarchy, alignment, and scrolling behavior.
 */
enum class TopAppBarVariant {

    /**
     * A compact top app bar with minimal height and left-aligned title.
     * Corresponds to [androidx.compose.material3.TopAppBar].
     * Ideal for default or non-scrollable use cases.
     */
    SMALL,

    /**
     * A small top app bar with a center-aligned title.
     * Corresponds to [androidx.compose.material3.CenterAlignedTopAppBar].
     * Suitable for branding-focused UIs or when title alignment is centered by design.
     */
    CENTER_ALIGNED,

    /**
     * A medium-sized top app bar that expands and collapses on scroll.
     * Corresponds to [androidx.compose.material3.MediumTopAppBar].
     * Recommended for screens with moderate visual emphasis and scrollable content.
     */
    MEDIUM,

    /**
     * A large, scrollable top app bar with the greatest visual emphasis.
     * Corresponds to [androidx.compose.material3.LargeTopAppBar].
     * Best for top-level screens where visual hierarchy or immersive headers are needed.
     */
    LARGE,
}
