/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.screen

import kpt.core.base.store.screen.DataFreshness
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks the [deriveDisplayState] contract — the pure mapping from [DataFreshness]
 * to [DisplayState] used by [DataFreshnessIndicator].
 *
 * [DisplayState.Stale] and [DisplayState.Reconnected] were removed in the
 * staleness-ui-consumer-ownership refactor. Stale/offline UI is now owned by
 * feature screens via the `content { data, freshness -> }` lambda; this indicator
 * is UPDATING-only.
 *
 * Null [ScreenContent] `refreshingIndicator` slot suppression is verified by
 * inspection: the guard `if (freshness == UPDATING) refreshingIndicator?.invoke()`
 * is a no-op when the slot is null. Full Compose UI tests require compose.uiTest
 * wired into commonTest — tracked separately.
 */
class DataFreshnessIndicatorStateTest {

    @Test
    fun freshIsHidden() {
        assertEquals(DisplayState.Hidden, deriveDisplayState(DataFreshness.FRESH))
    }

    @Test
    fun staleIsHidden() {
        // Stale/offline UI is owned by feature screens — core-base shows nothing.
        assertEquals(DisplayState.Hidden, deriveDisplayState(DataFreshness.STALE))
    }

    @Test
    fun updatingIsUpdating() {
        assertEquals(DisplayState.Updating, deriveDisplayState(DataFreshness.UPDATING))
    }
}
