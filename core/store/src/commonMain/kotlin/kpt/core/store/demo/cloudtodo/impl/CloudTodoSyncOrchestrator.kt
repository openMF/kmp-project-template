/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.demo.cloudtodo.impl

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kpt.core.base.database.infra.dao.BookkeeperDao
import kpt.core.base.store.submit.RetryOnNetworkStatus
import kpt.core.base.store.submit.RetryPolicy
import kpt.core.model.demo.cloudtodo.CloudTodo
import org.mobilenativefoundation.store.store5.Bookkeeper

/**
 * Drains the cloud-todo write backlog when connectivity returns (S5-SYNC).
 *
 * The MUTABLE archetype records a failed write through its [Bookkeeper] — but recording is
 * only half of offline-first. Before this orchestrator existed, cloud-todo was the one demo
 * that writes to a real server, injected a `RoomBookkeeper`, faithfully recorded every write
 * that failed while offline, and then **never retried any of them**: the row sat in
 * `store_bookkeeper` forever and the user's toggle silently never reached the server. The
 * sibling features (loans, bill reminders, alerts) each drive an `OfflineSubmitSyncer`; the
 * one feature with a real network write had no equivalent.
 *
 * `OfflineSubmitSyncer` is not reusable here: it drains a `SubmitOutbox` of PAYLOADS, whereas
 * the MutableStore path records KEYS in the bookkeeper and rebuilds the payload from the
 * source of truth. So this is the store-side counterpart, not a second copy — it enumerates
 * [BookkeeperDao.pendingKeys], re-issues the write for each, and clears the bookkeeper entry
 * only on success. It reuses the SAME [RetryOnNetworkStatus] and [RetryPolicy] objects the
 * outbox syncer uses, so both drains agree on what "online" means and on how hard to push.
 *
 * @param scope Long-lived scope (the app scope) the watcher runs in.
 * @param networkMonitor Connectivity source — retries fire on the offline → online edge.
 * @param bookkeeperDao Enumerates the outstanding failures.
 * @param bookkeeper Consulted per key and cleared on a successful replay.
 * @param loadLocal Rebuilds the payload for a key from the source of truth.
 * @param writeBlock Re-issues the write (the same path the UI uses).
 * @param retryOnStatus Which statuses count as "can retry now".
 * @param retryPolicy Backoff between attempts within one drain.
 * @param onReplayError Error sink — a replay that throws is reported, never swallowed. Declared
 *   as a plain lambda rather than a `CrashReporter` parameter so `core/store` (the Store5
 *   keystone) does not take a dependency on `core-base/observability`; the DI site in
 *   `core/data` passes the real reporter in.
 */
class CloudTodoSyncOrchestrator(
    private val scope: CoroutineScope,
    private val networkMonitor: NetworkMonitor,
    private val bookkeeperDao: BookkeeperDao,
    private val bookkeeper: Bookkeeper<CloudTodoKey>,
    private val loadLocal: suspend (CloudTodoKey) -> CloudTodo?,
    private val writeBlock: suspend (CloudTodo) -> Unit,
    private val retryOnStatus: RetryOnNetworkStatus = RetryOnNetworkStatus.OnlineOnly,
    private val retryPolicy: RetryPolicy = RetryPolicy(),
    private val onReplayError: (Throwable) -> Unit = {},
) {

    /**
     * Watch connectivity and drain on every offline → online transition.
     *
     * Edge-triggered via [distinctUntilChanged] so a status flow that re-emits an online
     * status repeatedly does not restart the drain on each emission.
     */
    fun start() {
        scope.launch {
            networkMonitor.networkStatus
                .map { retryOnStatus.shouldRetry(it) }
                .distinctUntilChanged()
                .collect { canRetry -> if (canRetry) retryPending() }
        }
    }

    /**
     * Replay every outstanding cloud-todo write once, with exponential backoff between keys.
     *
     * A key whose local row has disappeared (deleted while offline) is dropped from the
     * bookkeeper rather than retried forever — the write it refers to no longer has a
     * payload. A key that fails again keeps its bookkeeper entry and is picked up by the
     * next online edge, so the backlog drains across reconnects instead of spinning here.
     *
     * @return the number of keys successfully replayed.
     */
    suspend fun retryPending(): Int {
        // Resolve the replayable keys FIRST. A key that belongs to another store, or whose failure
        // a concurrent successful write already cleared, never enters the replay loop — so that
        // loop has exactly one job and one exit instead of four interleaved jumps.
        val replayable = mutableListOf<CloudTodoKey>()
        for (raw in bookkeeperDao.pendingKeys()) {
            val key = raw.toCloudTodoKeyOrNull() ?: continue // another store's key — not ours
            // Re-check through the Bookkeeper itself: pendingKeys() is a snapshot, and a
            // concurrent successful write may have cleared this key since we read it.
            if (bookkeeper.getLastFailedSync(key) != null) replayable += key
        }

        var replayed = 0
        // Space the attempts (1s/2s/4s ±jitter) so a reconnect with a large backlog doesn't burst
        // the whole queue at the server in one tick. Capped at maxAttempts per online edge; the
        // remainder keeps its bookkeeper entry and drains on the next reconnect.
        replayable.take(retryPolicy.maxAttempts).forEachIndexed { index, key ->
            delay(retryPolicy.delayFor(index))
            if (replayOne(key)) replayed++
        }
        return replayed
    }

    /**
     * Replay a single key.
     *
     * @return true when the write landed. A row that has vanished (deleted while offline) clears
     *   its bookkeeper entry and returns false — nothing is owed, but nothing was replayed either.
     *   A throwing write KEEPS its entry so the next online edge picks it up.
     */
    private suspend fun replayOne(key: CloudTodoKey): Boolean = try {
        val local = loadLocal(key)
        if (local == null) {
            bookkeeper.clear(key) // nothing left to send
            false
        } else {
            writeBlock(local)
            bookkeeper.clear(key)
            true
        }
    } catch (t: Throwable) {
        onReplayError(t)
        false
    }
}

/**
 * Parse a bookkeeper key back into a [CloudTodoKey], or `null` when it belongs to a different
 * store. The bookkeeper table is shared across every MutableStore in the app, so the prefix
 * is what keeps one store's orchestrator from replaying another store's failures. Must stay
 * in step with the `keySerializer` registered in `ProjectRepositoryModule`.
 */
fun String.toCloudTodoKeyOrNull(): CloudTodoKey? =
    removePrefix(CLOUD_TODO_KEY_PREFIX)
        .takeIf { it != this }
        ?.toIntOrNull()
        ?.let(::CloudTodoKey)

const val CLOUD_TODO_KEY_PREFIX = "cloudTodo:"
