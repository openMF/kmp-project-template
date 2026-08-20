/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.mutation

import kpt.core.base.database.invalidation.notifyingWrite
import kpt.core.base.store.mutation.conflict.ConflictInbox
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.StoreWriteRequest

/**
 * The default [MutationGateway] — composes the existing Store5 write machinery.
 *
 * Optimistic writes go through `MutableStore.write` (which persists to the Room source of truth, drives
 * the `Updater`, and records failures in the `Bookkeeper` for retry). Online-required writes await the
 * network first and are [MutationResult.Blocked] offline. Deletes clear the local store and sync via the
 * caller-supplied delete endpoint (Store5 has no network-DELETE path). Command mutations run the caller's
 * endpoint, apply optimistic local state, and record server conflicts in the [ConflictInbox].
 *
 * @param isOnline connectivity probe (DI wires it to the app's `NetworkMonitor`); kept as a lambda so the
 *   gateway is decoupled from the network API and trivially testable.
 * @param conflictInbox durable sink for write conflicts surfaced in Settings.
 */
class DefaultMutationGateway(
    private val isOnline: suspend () -> Boolean,
    private val conflictInbox: ConflictInbox,
) : MutationGateway {

    override suspend fun <K : Any, V : Any> upsert(
        store: MutableStore<K, V>,
        key: K,
        value: V,
        policy: MutationPolicy,
    ): MutationResult<V> {
        if (policy is MutationPolicy.OnlineRequired && !isOnline()) {
            return MutationResult.Blocked(BlockReason.OFFLINE)
        }
        return try {
            store.write(StoreWriteRequest.of<K, V, Any>(key = key, value = value))
            // Optimistic offline write is persisted locally + queued in the Bookkeeper (synced=false);
            // online (or online-required) writes reach the network now (synced=true).
            MutationResult.Applied(value = value, synced = isOnline())
        } catch (t: Throwable) {
            MutationResult.Failed(cause = t, rolledBack = false)
        }
    }

    override suspend fun <K : Any, V : Any> delete(
        store: MutableStore<K, V>,
        key: K,
        deleteEndpoint: suspend (K) -> Unit,
        policy: MutationPolicy,
    ): MutationResult<Unit> {
        if (policy is MutationPolicy.OnlineRequired && !isOnline()) {
            return MutationResult.Blocked(BlockReason.OFFLINE)
        }
        return try {
            // Clear the local row immediately (optimistic — it disappears from reads), then sync the
            // network DELETE. On an optimistic offline delete the row stays cleared and the caller's
            // SyncOrchestrator retries [deleteEndpoint] on reconnect.
            store.clear(key)
            if (isOnline()) deleteEndpoint(key)
            MutationResult.Applied(value = Unit, synced = isOnline())
        } catch (t: Throwable) {
            MutationResult.Failed(cause = t, rolledBack = false)
        }
    }

    override suspend fun <P : Any, R : Any> command(
        spec: CommandSpec<P, R>,
        policy: MutationPolicy,
    ): MutationResult<R> {
        if (policy is MutationPolicy.OnlineRequired && !isOnline()) {
            return MutationResult.Blocked(BlockReason.OFFLINE)
        }
        // Optimistic: apply the local pre-state before the call so the UI updates instantly.
        if (policy is MutationPolicy.Optimistic) {
            runCatching { spec.localApply?.invoke(null) }
        }
        return try {
            val result = spec.endpoint(spec.payload)
            spec.localApply?.invoke(result) // ingest the real server record (server-wins / key-remap)
            val report = spec.conflictOf?.invoke(spec.payload, result)
            if (report != null) {
                val id = conflictInbox.record(
                    entity = report.entity,
                    key = report.key,
                    localPayloadJson = report.localPayloadJson,
                    serverPayloadJson = report.serverPayloadJson,
                    formRoute = report.formRoute,
                )
                MutationResult.Conflicted(conflictId = id, server = result)
            } else {
                MutationResult.Applied(value = result, synced = true)
            }
        } catch (t: Throwable) {
            val rolledBack = runCatching { spec.rollback?.invoke() }.isSuccess && spec.rollback != null
            MutationResult.Failed(cause = t, rolledBack = rolledBack)
        }
    }

    override suspend fun localMutation(table: String, mutate: suspend () -> Unit): MutationResult<Unit> =
        try {
            notifyingWrite(table) { mutate() }
            MutationResult.Applied(value = Unit, synced = true)
        } catch (t: Throwable) {
            MutationResult.Failed(cause = t, rolledBack = false)
        }
}
