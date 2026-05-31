package org.mifos.sync

import io.github.mobilebytelabs.worker.WorkerContext
import io.github.mobilebytelabs.worker.scheduler.sync.AbstractDataSyncWorker
import io.github.mobilebytelabs.worker.scheduler.sync.SyncStatePersister
import org.mifos.core.data.currency.CurrencyRepository
import org.mifos.core.data.economic.MacroIndicatorsRepository

/**
 * Sample-side worker: extends the library's [AbstractDataSyncWorker] which handles
 * the parallel-fan-out + persistence + retry semantics.
 *
 * The constructor lists the [io.github.mobilebytelabs.worker.scheduler.sync.Syncable]
 * adopters specific to this sample (Currency + Macro). The base class' `doWork` invokes
 * each `syncWith` in parallel via `awaitAll` and bumps `ChangeListVersions` on success.
 */
class DataSyncWorker(
    ctx: WorkerContext,
    currencyRepository: CurrencyRepository,
    macroIndicatorsRepository: MacroIndicatorsRepository,
    persister: SyncStatePersister,
) : AbstractDataSyncWorker(
    ctx,
    syncables = listOf(currencyRepository, macroIndicatorsRepository),
    persister = persister,
)
