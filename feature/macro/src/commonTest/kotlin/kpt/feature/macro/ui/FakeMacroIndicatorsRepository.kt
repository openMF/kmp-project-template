/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.macro.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.demo.economic.MacroIndicatorsRepository
import kpt.core.data.infra.Synchronizer
import kpt.core.model.demo.economic.MacroIndicator
import kpt.core.store.demo.economic.impl.MacroIndicatorKey

/**
 * Minimal [MacroIndicatorsRepository] fake for Compose UI tests.
 *
 * Returns a persistent [MutableSharedFlow] (replay=1) per [MacroIndicatorKey] so the
 * ViewModel's initial [ScreenState.Loading] emission is always available, and the
 * Scaffold root renders immediately regardless of the load state. Used by
 * [CountryMacroScreenUiTest] and [MacroIndicatorDetailScreenUiTest].
 *
 * The VM-test-level fakes ([KeyedFakeMacroIndicatorsRepository] /
 * [SingleKeyFakeRepository]) are `private` to their VM test files; this `internal`
 * sibling exists solely to serve the UI-layer tests without duplicating complex
 * recording logic.
 */
@OptIn(ExperimentalScreenDataStreamTestingApi::class)
internal class FakeMacroIndicatorsRepository : MacroIndicatorsRepository {

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    private val buses =
        mutableMapOf<MacroIndicatorKey, MutableSharedFlow<ScreenState<MacroIndicator>>>()

    private fun bus(key: MacroIndicatorKey): MutableSharedFlow<ScreenState<MacroIndicator>> =
        buses.getOrPut(key) { MutableSharedFlow(replay = 1, extraBufferCapacity = 16) }

    override fun macroIndicatorStream(
        key: MacroIndicatorKey,
        scope: CoroutineScope,
        fetchPolicy: FetchPolicy,
    ): ScreenDataStream<MacroIndicator> = screenDataStreamForTesting(state = bus(key))

    override fun macroIndicatorStream(
        keyFlow: Flow<MacroIndicatorKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<MacroIndicator> = error("dynamic-key overload not used in UI tests")
}
