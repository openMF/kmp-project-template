package org.mifos.core.data.util

import kotlinx.coroutines.flow.Flow

/**
 * Cross-platform sync observer + trigger surface.
 * Ported verbatim from NiA's `core/data/util/SyncManager.kt` (then adapted: commonMain).
 *
 * Android impl wraps `WorkManager.getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)` to derive isSyncing.
 * Other platforms ship MutableStateFlow stubs.
 */
interface SyncManager {
    /** Flow that emits `true` while a sync is in progress, `false` otherwise. */
    val isSyncing: Flow<Boolean>

    /** Schedule an immediate sync (idempotent — uses unique work name on Android). */
    fun requestSync()
}
