/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation

import cmp.navigation.testing.FakeUserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.platform.garbage.GarbageCollectionManager
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.LanguageConfig
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Locks the [AppViewModel] contract — the app-root ViewModel that turns preference changes into
 * theme / locale / screen-capture state plus the platform events that apply them.
 *
 * The load-bearing case is the locale one. A language change must update BOTH `state.localeName`
 * AND emit [AppEvent.UpdateAppLocale]: the event drives the per-platform locale switch, while the
 * state drives Compose's `LayoutDirection` at the app root. Dropping the state half is invisible in
 * LTR and silently renders every RTL language's translated strings inside a left-to-right layout
 * on desktop / iOS / web — so [languageChangeUpdatesBothStateAndEvent] asserts both halves.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class RecordingGarbageCollector : GarbageCollectionManager {
        var collections = 0
            private set

        override fun tryCollect() {
            collections++
        }
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.drain() {
        repeat(3) {
            runCurrent()
            advanceUntilIdle()
        }
    }

    @Test
    fun darkThemeConfigDrivesTheDarkThemeFlagAndTheOsEvent() = runTest(dispatcher) {
        val repo = FakeUserDataRepository()
        val gc = RecordingGarbageCollector()
        val events = mutableListOf<AppEvent>()
        val vm = AppViewModel(repo, gc)
        backgroundScope.launch { vm.eventFlow.collect { events += it } }
        drain()

        repo.darkThemeConfig.value = DarkThemeConfig.DARK
        drain()

        assertTrue(vm.stateFlow.value.darkTheme)
        assertTrue(
            events.contains(AppEvent.UpdateAppTheme(osValue = DarkThemeConfig.DARK.osValue)),
            "events were $events",
        )
    }

    @Test
    fun onlyTheDarkConfigCountsAsDark() = runTest(dispatcher) {
        // FOLLOW_SYSTEM and LIGHT are both "not dark" here — the OS resolves FOLLOW_SYSTEM via the
        // emitted osValue, so treating it as dark in state would double-apply the preference.
        val repo = FakeUserDataRepository()
        val vm = AppViewModel(repo, RecordingGarbageCollector())
        drain()

        repo.darkThemeConfig.value = DarkThemeConfig.LIGHT
        drain()
        assertEquals(false, vm.stateFlow.value.darkTheme)

        repo.darkThemeConfig.value = DarkThemeConfig.FOLLOW_SYSTEM
        drain()
        assertEquals(false, vm.stateFlow.value.darkTheme)
    }

    @Test
    fun dynamicColorAndScreenCapturePreferencesReachState() = runTest(dispatcher) {
        val repo = FakeUserDataRepository()
        val vm = AppViewModel(repo, RecordingGarbageCollector())
        drain()

        repo.dynamicColor.value = true
        repo.screenCapture.value = true
        drain()

        assertTrue(vm.stateFlow.value.isDynamicColorsEnabled)
        assertTrue(vm.stateFlow.value.isScreenCaptureAllowed)
    }

    @Test
    fun languageChangeUpdatesBothStateAndEvent() = runTest(dispatcher) {
        // Both halves are required — see the class doc. Asserting only the event would let an RTL
        // layout regression through.
        val repo = FakeUserDataRepository()
        val events = mutableListOf<AppEvent>()
        val vm = AppViewModel(repo, RecordingGarbageCollector())
        backgroundScope.launch { vm.eventFlow.collect { events += it } }
        drain()

        repo.language.value = LanguageConfig.HINDI
        drain()

        assertEquals(LanguageConfig.HINDI.localeName, vm.stateFlow.value.localeName)
        assertTrue(
            events.contains(AppEvent.UpdateAppLocale(LanguageConfig.HINDI.localeName)),
            "events were $events",
        )
    }

    @Test
    fun appSpecificLanguageUpdateWritesBackToPreferences() = runTest(dispatcher) {
        val repo = FakeUserDataRepository()
        val vm = AppViewModel(repo, RecordingGarbageCollector())
        drain()

        vm.trySendAction(AppAction.AppSpecificLanguageUpdate(LanguageConfig.SPANISH))
        drain()

        assertEquals(listOf(LanguageConfig.SPANISH), repo.languageWrites)
    }

    @Test
    fun userStateChangesRecreateTheUiAndCollectGarbage() = runTest(dispatcher) {
        // Both arms exist so a locale/user switch tears down the old Compose tree AND releases it;
        // emitting Recreate without collecting leaks the previous tree across every switch.
        val gc = RecordingGarbageCollector()
        val events = mutableListOf<AppEvent>()
        val vm = AppViewModel(FakeUserDataRepository(), gc)
        backgroundScope.launch { vm.eventFlow.collect { events += it } }
        drain()

        vm.trySendAction(AppAction.Internal.CurrentUserStateChange)
        drain()
        vm.trySendAction(AppAction.Internal.UserUnlockStateChange)
        drain()

        assertEquals(2, events.count { it == AppEvent.Recreate }, "events were $events")
        assertEquals(2, gc.collections)
    }
}
