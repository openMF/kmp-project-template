// File: samples/kmp-project-template/sync/src/androidMain/kotlin/org/mifos/sync/WorkManagerSyncManager.kt
package org.mifos.sync

import io.github.mobilebytelabs.worker.scheduler.enqueueDataSync

import io.github.mobilebytelabs.worker.WorkInfo
import io.github.mobilebytelabs.worker.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import io.github.mobilebytelabs.worker.scheduler.sync.SyncManager

class WorkManagerSyncManager(
    private val workManager: WorkManager,
    private val scheduler: WorkScheduler,
) : SyncManager {
    override val isSyncing: Flow<Boolean> =
        workManager.getWorkInfosByUniqueWorkNameFlow(SYNC_WORK_NAME)
            .map { infos -> infos.any { it.state == WorkInfo.State.RUNNING } }
            .conflate()
    override fun requestSync() { scheduler.enqueueDataSync<DataSyncWorker>() }
}

actual fun provideSyncManager(workManager: WorkManager): SyncManager =
    WorkManagerSyncManager(workManager, KoinPlatform.getKoin().get())
