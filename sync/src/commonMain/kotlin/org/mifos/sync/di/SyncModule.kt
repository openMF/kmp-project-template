// File: samples/kmp-project-template/sync/src/commonMain/kotlin/org/mifos/sync/di/SyncModule.kt
package org.mifos.sync.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.mifos.core.data.util.SyncManager
import org.mifos.core.datastore.SyncStatePersister
import org.mifos.sync.DataSyncWorker
import org.mifos.sync.DefaultWorkScheduler
import org.mifos.sync.NotificationWorker
import org.mifos.sync.WorkScheduler
import io.github.mobilebytelabs.worker.registry.WorkerRegistry

val SyncModule: Module = module {
    single { SyncStatePersister(dataStore = get()) }
    single<WorkScheduler> { DefaultWorkScheduler(workManager = get(), persister = get()) }
    single<SyncManager> { provideSyncManager(workManager = get()) }
}

/**
 * Worker-registry contribution — Phase 5's androidWorkerModule calls this.
 *
 * `DataSyncWorker`'s 3 `get()` calls resolve:
 *   1. CurrencyRepository (bound in Phase 3's RepositoryModule as single<CurrencyRepository>)
 *   2. MacroIndicatorsRepository (bound in Phase 3's RepositoryModule as single<MacroIndicatorsRepository>)
 *   3. SyncStatePersister (bound above in SyncModule)
 */
fun WorkerRegistry.Builder.syncWorkers() {
    register<DataSyncWorker> { ctx ->
        DataSyncWorker(
            ctx = ctx,
            currencyRepository = get(),
            macroIndicatorsRepository = get(),
            persister = get(),
        )
    }
    register<NotificationWorker> { ctx -> NotificationWorker(ctx) }
}

expect fun provideSyncManager(workManager: io.github.mobilebytelabs.worker.WorkManager): SyncManager
