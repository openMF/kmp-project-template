// File: samples/kmp-project-template/sync/src/desktopMain/kotlin/org/mifos/sync/DesktopSyncManager.kt
package org.mifos.sync

import io.github.mobilebytelabs.worker.scheduler.enqueueDataSync
import io.github.mobilebytelabs.worker.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.mobilebytelabs.worker.scheduler.sync.SyncManager

class DesktopSyncManager(private val workManager: WorkManager, private val scheduler: WorkScheduler) : SyncManager {
    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing = _isSyncing.asStateFlow()
    override fun requestSync() { scheduler.enqueueDataSync<DataSyncWorker>() }
}
actual fun provideSyncManager(workManager: WorkManager): SyncManager =
    DesktopSyncManager(workManager, org.koin.mp.KoinPlatform.getKoin().get())
