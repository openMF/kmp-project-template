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
 * Specifies the type of alert dialog to be rendered in the CMP design system.
 *
 * This enum allows choosing between a fully featured Material3 dialog or a lightweight,
 * custom-content dialog depending on the use case.
 */
enum class AlertDialogVariant {
    /**
     * A customizable Material3 [AlertDialog] that supports title, text, buttons, and icon.
     * Ideal for standard alert or confirmation use cases with rich UI elements.
     */
    CUSTOM,

    /**
     * A minimal [BasicAlertDialog] that allows full control over the dialog content
     * via a single composable slot. Useful for simpler or highly customized layouts.
     */
    BASIC,
}
