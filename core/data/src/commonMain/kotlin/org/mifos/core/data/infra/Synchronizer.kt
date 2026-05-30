package org.mifos.core.data.infra

import io.github.mobilebytelabs.worker.WorkData
import io.github.mobilebytelabs.worker.workDataOf

/**
 * Carries per-Syncable change-list state across a sync run.
 * Ported verbatim from NiA's core/data/SyncUtilities.kt.
 */
interface Synchronizer {
    suspend fun getChangeListVersions(): ChangeListVersions
    suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions)
    suspend fun Syncable.sync() = syncWith(this@Synchronizer)
}

/**
 * A repository (or other entity) that knows how to sync itself given a Synchronizer.
 *
 * Two overloads (D23 — amended 2026-05-30):
 *  - 1-arg: NiA verbatim; sync everything.
 *  - 2-arg: payload-aware; default impl discards payload and delegates to 1-arg.
 *    Adopters that understand specific keys override the 2-arg variant.
 */
interface Syncable {
    suspend fun syncWith(synchronizer: Synchronizer): Boolean

    suspend fun syncWith(synchronizer: Synchronizer, payload: WorkData): Boolean =
        syncWith(synchronizer)
}

/**
 * Server change-list entry for a single model id. Mirrors NiA's NetworkChangeList shape.
 */
interface NetworkChange {
    val id: String
    val changeListVersion: Int
    val isDelete: Boolean
}

/**
 * Changelist-based sync algorithm (D19 — verbatim port of NiA's `Synchronizer.changeListSync`).
 *
 * For each Syncable that wants delta-API semantics:
 *  1. Reads the last-synced version from ChangeListVersions via [versionReader].
 *  2. Fetches all server changes AFTER that version via [changeListFetcher].
 *  3. Splits into deletes (isDelete=true) and updates (isDelete=false).
 *  4. Invokes [modelDeleter] with the deleted ids; [modelUpdater] with the updated ids.
 *  5. On success, bumps ChangeListVersions to the highest server-version returned.
 *
 * Returns true on success, false on any throw.
 */
suspend fun <T : NetworkChange> Synchronizer.changeListSync(
    versionReader: (ChangeListVersions) -> Int,
    changeListFetcher: suspend (Int) -> List<T>,
    versionUpdater: ChangeListVersions.(Int) -> ChangeListVersions,
    modelDeleter: suspend (List<String>) -> Unit,
    modelUpdater: suspend (List<String>) -> Unit,
): Boolean = kotlin.runCatching {
    val currentVersion = versionReader(getChangeListVersions())
    val changes = changeListFetcher(currentVersion)
    if (changes.isEmpty()) return@runCatching true

    val (deletes, updates) = changes.partition { it.isDelete }
    modelDeleter(deletes.map { it.id })
    modelUpdater(updates.map { it.id })

    val latestVersion = changes.maxOf { it.changeListVersion }
    updateChangeListVersions { versionUpdater(latestVersion) }
    true
}.getOrElse { false }

/**
 * Snapshot-API sibling of `changeListSync` (D21). Used when the server returns the FULL
 * canonical set on every call (Frankfurter exchange rates, World Bank indicators).
 *
 *  1. Read last-sync `epochSeconds` from ChangeListVersions[name] (informational; used by staleness UI).
 *  2. Invoke [fetcher] — implementer fetches snapshot AND writes it to the relevant Store.
 *  3. On success, bump versions[name] = now.epochSeconds.
 *
 * Returns true on success, false on any throw.
 */
suspend fun Synchronizer.snapshotSync(
    name: String,
    fetcher: suspend () -> Unit,
): Boolean = kotlin.runCatching {
    fetcher()
    val now = kotlinx.datetime.Clock.System.now().epochSeconds
    updateChangeListVersions { copy(versions = versions + (name to now)) }
    true
}.getOrElse { false }
