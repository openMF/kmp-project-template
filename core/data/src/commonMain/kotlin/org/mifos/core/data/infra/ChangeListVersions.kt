package org.mifos.core.data.infra

import kotlinx.serialization.Serializable

/**
 * Per-Syncable-name last-synced version map. Persisted by SyncStatePersister via DataStore.
 *
 * - For changeListSync adopters: stores a monotonic Int server-version (cast to Long for unified storage).
 * - For snapshotSync adopters: stores Clock.System.now().epochSeconds.
 *
 * Long-typed (D21 — amended 2026-05-30) so snapshotSync's epochSeconds doesn't overflow in ~2038.
 */
@Serializable
data class ChangeListVersions(val versions: Map<String, Long> = emptyMap()) {
    fun set(name: String, version: Long): ChangeListVersions =
        copy(versions = versions + (name to version))
}
