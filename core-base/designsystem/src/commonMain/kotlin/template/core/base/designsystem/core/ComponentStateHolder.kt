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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class ComponentStateHolder<T>(initialValue: T) : ComponentState<T> {
    override var value by mutableStateOf(initialValue)
        private set

    override fun update(newValue: T) {
        value = newValue
    }
}

@Composable
fun <T> rememberComponentState(initialValue: T): ComponentState<T> {
    return remember { ComponentStateHolder(initialValue) }
}
