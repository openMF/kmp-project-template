// File: samples/kmp-project-template/sync/src/commonMain/kotlin/org/mifos/sync/SyncInitializer.kt
package org.mifos.sync

/** Convenience initializer — enqueues a startup data sync via the injected scheduler. */
object Sync {
    fun initialize(scheduler: WorkScheduler) {
        scheduler.enqueueDataSync(mode = WorkMode.Background)
    }
}
