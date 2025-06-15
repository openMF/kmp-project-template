/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A base ViewModel for managing background tasks and handling exceptions in a centralized way.
 *
 * This ViewModel provides the `launchCatching` function, which launches a coroutine and handles exceptions
 * and logging the error using the Kermit logger.
 *
 */
open class TaskMinderViewModel : ViewModel() {

    /**
     * Launches a coroutine that catches any exceptions
     *
     * @param block The suspend block of code to execute within the coroutine.
     */
    fun launchCatching(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(
            context = CoroutineExceptionHandler { _, throwable ->
                println(throwable.message.toString())
            },
            block = block,
        )
}
