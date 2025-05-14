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
 * Defines the supported visual and behavioral styles for [CMPProgressIndicator] in the CMP design system.
 *
 * Each variant corresponds to a specific Material3 progress indicator type and determines
 * whether the indicator is determinate (shows exact progress) or indeterminate (shows ongoing activity).
 */
enum class ProgressIndicatorVariant {

    /**
     * A continuously animating horizontal bar that indicates indeterminate progress.
     * Corresponds to [androidx.compose.material3.LinearProgressIndicator] in indeterminate mode.
     * Best used when the duration or progress of an operation is unknown.
     */
    INDETERMINATE_LINEAR,

    /**
     * A spinning circular indicator that represents indeterminate progress.
     * Corresponds to [androidx.compose.material3.CircularProgressIndicator] in indeterminate mode.
     */
    INDETERMINATE_CIRCULAR,

    /**
     * A horizontal bar that fills from left to right based on a progress value between `0f` and `1f`.
     * Corresponds to [androidx.compose.material3.LinearProgressIndicator] in determinate mode.
     */
    DETERMINATE_LINEAR,

    /**
     * A circular arc that fills clockwise based on a progress value between `0f` and `1f`.
     * Corresponds to [androidx.compose.material3.CircularProgressIndicator] in determinate mode.
     */
    DETERMINATE_CIRCULAR,
}
