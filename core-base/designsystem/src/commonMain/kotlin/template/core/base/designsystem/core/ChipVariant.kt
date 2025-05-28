/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.designsystem.core

sealed interface ChipVariant : ComponentVariant {
    override val name: String

    data object Assist : ChipVariant {
        override val name: String = "assist"
    }

    data object Filter : ChipVariant {
        override val name: String = "filter"
    }

    data object Input : ChipVariant {
        override val name: String = "input"
    }

    data object Suggestion : ChipVariant {
        override val name: String = "suggestion"
    }
}
