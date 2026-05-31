package org.mifos.sync.di

import io.github.mobilebytelabs.worker.registry.WorkerRegistry
import io.github.mobilebytelabs.worker.scheduler.DefaultWorkScheduler
import io.github.mobilebytelabs.worker.scheduler.WorkScheduler
import io.github.mobilebytelabs.worker.scheduler.sync.SyncManager
import io.github.mobilebytelabs.worker.scheduler.sync.SyncStatePersister
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mifos.sync.DataSyncWorker
import org.mifos.sync.NotificationWorker

val SyncModule: Module = module {
    // Library's in-memory MutableStateFlow-backed persister. Swap for a DataStore-
    // backed impl by binding your own SyncStatePersister subclass to this qualifier.
    single { SyncStatePersister() }
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
