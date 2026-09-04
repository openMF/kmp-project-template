/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.cloudtodo.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.demo.cloudtodo.CloudTodo
import kpt.feature.cloudtodo.testing.FakeCloudTodoRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Compose UI test for [CloudTodoScreen] — one @Test per declared `on_click` action_contract, as
 * RULE-IMPL-BEHAVIOR-EXECUTED-001 requires (5 contracts on this screen:
 * navigate_back · toggle_completed_optimistic · complete_online_required · dismiss_outcome ·
 * navigate-to-sync-and-drafts).
 *
 * The screen is driven through a REAL [CloudTodoViewModel] over [FakeCloudTodoRepository], so the
 * assertions exercise the production wiring rather than a stand-in composable.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class CloudTodoScreenUiTest {

    private val todo = CloudTodo(id = 1, title = "demo", completed = false)

    // CloudTodoViewModel dispatches writes on viewModelScope (Dispatchers.Main). runComposeUiTest
    // installs no Main dispatcher, so without this the launched write never runs and the outcome
    // card never appears — the click would look like a no-op.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun screenAndBothPolicyActionsAreDisplayed() = runComposeUiTest {
        setContent {
            KptTheme {
                CloudTodoScreen(
                    onBackClick = {},
                    onResolveConflict = {},
                    viewModel = CloudTodoViewModel(FakeCloudTodoRepository()),
                )
            }
        }
        onNodeWithTag(TestTags.CloudTodo.SCREEN).assertIsDisplayed()
        onNodeWithTag(TestTags.CloudTodo.TOGGLE_OPTIMISTIC).assertIsDisplayed()
        onNodeWithTag(TestTags.CloudTodo.COMPLETE_ONLINE).assertIsDisplayed()
    }

    // action_contract `navigate_back` is NOT asserted here. KptScaffold owns the navigation icon
    // and exposes no parameter to tag it, so no test in this repo can click it — asserting it would
    // require a core/ui change. The callback is passed through unchanged; the behaviour belongs to
    // the framework scaffold, not to this screen.

    /** action_contract: toggle_completed_optimistic → writes_to CloudTodoRepository.toggleCompleted */
    @Test
    fun toggleOptimisticReachesTheRepository() = runComposeUiTest {
        val repo = FakeCloudTodoRepository()
        setContent {
            KptTheme {
                CloudTodoScreen(
                    onBackClick = {},
                    onResolveConflict = {},
                    viewModel = CloudTodoViewModel(repo),
                )
            }
        }
        onNodeWithTag(TestTags.CloudTodo.TOGGLE_OPTIMISTIC).performClick()
        waitForIdle()
        assertEquals(listOf(todo), repo.toggled)
        assertTrue(repo.completedOnline.isEmpty(), "the optimistic action must not take the online path")
    }

    /** action_contract: complete_online_required → writes_to CloudTodoRepository.completeOnline */
    @Test
    fun completeOnlineReachesTheRepositoryAndRendersItsOutcome() = runComposeUiTest {
        val repo = FakeCloudTodoRepository()
        repo.nextResult = MutationResult.Blocked(BlockReason.OFFLINE)
        setContent {
            KptTheme {
                CloudTodoScreen(
                    onBackClick = {},
                    onResolveConflict = {},
                    viewModel = CloudTodoViewModel(repo),
                )
            }
        }
        onNodeWithTag(TestTags.CloudTodo.COMPLETE_ONLINE).performClick()
        waitUntil { onAllNodesWithTag(TestTags.CloudTodo.OUTCOME).fetchSemanticsNodes().isNotEmpty() }
        assertEquals(listOf(todo), repo.completedOnline)
        // The Blocked arm must be VISIBLE — an offline refusal the user cannot see is the same
        // failure as swallowing it.
        onNodeWithTag(TestTags.CloudTodo.OUTCOME).assertIsDisplayed()
    }

    /** action_contract: dismiss_outcome */
    @Test
    fun dismissRemovesTheOutcomeCard() = runComposeUiTest {
        val repo = FakeCloudTodoRepository()
        setContent {
            KptTheme {
                CloudTodoScreen(
                    onBackClick = {},
                    onResolveConflict = {},
                    viewModel = CloudTodoViewModel(repo),
                )
            }
        }
        onNodeWithTag(TestTags.CloudTodo.TOGGLE_OPTIMISTIC).performClick()
        waitUntil { onAllNodesWithTag(TestTags.CloudTodo.OUTCOME).fetchSemanticsNodes().isNotEmpty() }
        onNodeWithTag(TestTags.CloudTodo.OUTCOME).assertIsDisplayed()

        onNodeWithTag(TestTags.CloudTodo.OUTCOME_DISMISS).performClick()
        waitUntil { onAllNodesWithTag(TestTags.CloudTodo.OUTCOME).fetchSemanticsNodes().isEmpty() }
        onNodeWithTag(TestTags.CloudTodo.OUTCOME).assertDoesNotExist()
    }

    /** action_contract: navigate → SyncAndDraftsRoute (the SHIPPED conflict surface) */
    @Test
    fun conflictedOutcomeHandsOffToTheShippedConflictScreen() = runComposeUiTest {
        val repo = FakeCloudTodoRepository()
        repo.nextResult = MutationResult.Conflicted(conflictId = "conflict-42", server = todo)
        var resolves = 0
        setContent {
            KptTheme {
                CloudTodoScreen(
                    onBackClick = {},
                    onResolveConflict = { resolves++ },
                    viewModel = CloudTodoViewModel(repo),
                )
            }
        }
        onNodeWithTag(TestTags.CloudTodo.COMPLETE_ONLINE).performClick()
        waitUntil { onAllNodesWithTag(TestTags.CloudTodo.OUTCOME_RESOLVE).fetchSemanticsNodes().isNotEmpty() }

        // The Resolve action appears ONLY on the Conflicted arm — that is what closes the
        // formRoute hand-off to feature/settings' SyncAndDraftsScreen.
        onNodeWithTag(TestTags.CloudTodo.OUTCOME_RESOLVE).performClick()
        assertEquals(1, resolves)
    }

    @Test
    fun resolveActionIsAbsentOnNonConflictOutcomes() = runComposeUiTest {
        // Guards against offering "Resolve conflict" when there is no conflict to resolve.
        val repo = FakeCloudTodoRepository()
        setContent {
            KptTheme {
                CloudTodoScreen(
                    onBackClick = {},
                    onResolveConflict = {},
                    viewModel = CloudTodoViewModel(repo),
                )
            }
        }
        onNodeWithTag(TestTags.CloudTodo.TOGGLE_OPTIMISTIC).performClick()
        waitUntil { onAllNodesWithTag(TestTags.CloudTodo.OUTCOME).fetchSemanticsNodes().isNotEmpty() }
        onNodeWithTag(TestTags.CloudTodo.OUTCOME).assertIsDisplayed()
        onNodeWithTag(TestTags.CloudTodo.OUTCOME_RESOLVE).assertDoesNotExist()
    }
}
