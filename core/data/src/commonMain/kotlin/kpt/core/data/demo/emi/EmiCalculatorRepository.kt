/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.demo.emi

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.demo.emi.EmiResult
import kpt.core.store.demo.emi.impl.EmiParams

/**
 * Read surface for the EMI calculator (`calculator_pure`, MEMORY_ONLY).
 *
 * The repository owns the Store and exposes exactly one function, matching the read-path
 * contract every other feature follows — the ViewModel never touches a `Store`.
 */
interface EmiCalculatorRepository {

    /** A [ScreenDataStream] over the EMI computed for [params]. */
    fun emiStream(params: EmiParams, scope: CoroutineScope): ScreenDataStream<EmiResult>
}
