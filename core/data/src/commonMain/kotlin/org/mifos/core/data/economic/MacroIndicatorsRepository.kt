/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.data.economic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.mifos.core.data.infra.Syncable
import org.mifos.core.model.economic.MacroIndicator
import org.mifos.core.store.economic.impl.MacroIndicatorKey
import template.core.base.store.screen.ScreenDataStream

/**
 * Repository surface for World Bank macro-indicator series.
 *
 * Consumes the `MacroIndicator` Store5 cache under the hood — callers get a
 * [ScreenDataStream] that emits loading/empty/error/content transitions
 * automatically (per the toolkit's offline-first store contract).
 */
interface MacroIndicatorsRepository : Syncable {

    /**
     * Stream observations for a single static (country, indicator) pair.
     */
    fun macroIndicatorStream(
        key: MacroIndicatorKey,
        scope: CoroutineScope,
    ): ScreenDataStream<MacroIndicator>

    /**
     * Stream observations for a parameter-flow — typically driven by a UI
     * picker that lets the user switch countries / indicators.
     */
    fun macroIndicatorStream(
        keyFlow: Flow<MacroIndicatorKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<MacroIndicator>
}
