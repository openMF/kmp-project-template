// File: samples/kmp-project-template/sync/src/wasmJsMain/kotlin/org/mifos/sync/WebSyncManager.kt
package org.mifos.sync

import io.github.mobilebytelabs.worker.scheduler.enqueueDataSync
import io.github.mobilebytelabs.worker.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.mobilebytelabs.worker.scheduler.sync.SyncManager

class WebSyncManager(private val workManager: WorkManager, private val scheduler: WorkScheduler) : SyncManager {
    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing = _isSyncing.asStateFlow()
    override fun requestSync() { scheduler.enqueueDataSync<DataSyncWorker>() }
}
actual fun provideSyncManager(workManager: WorkManager): SyncManager =
    WebSyncManager(workManager, org.koin.mp.KoinPlatform.getKoin().get())
