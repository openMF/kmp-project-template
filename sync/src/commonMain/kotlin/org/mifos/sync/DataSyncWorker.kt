// File: samples/kmp-project-template/sync/src/commonMain/kotlin/org/mifos/sync/DataSyncWorker.kt
package org.mifos.sync

import io.github.mobilebytelabs.worker.CoroutineWorker
import io.github.mobilebytelabs.worker.ForegroundInfo
import io.github.mobilebytelabs.worker.WorkData
import io.github.mobilebytelabs.worker.WorkResult
import io.github.mobilebytelabs.worker.WorkerContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.mifos.core.data.infra.ChangeListVersions
import org.mifos.core.data.infra.Synchronizer
import org.mifos.core.data.currency.CurrencyRepository
import org.mifos.core.data.economic.MacroIndicatorsRepository
import org.mifos.core.datastore.SyncStatePersister

/**
 * Single data-sync worker for the kmp-project-template sample.
 *
 * Both Syncable adopters are CONSTRUCTOR-INJECTED via Koin (D5/D8 amended 2026-05-30).
 * No `getAll<Syncable>()` service-locator, no `KoinComponent`.
 *
 * doWork explicitly awaits BOTH `syncWith` calls in parallel via `awaitAll` — the
 * fan-out shape is visible in the source (per D8 amended). Both must return true for
 * Result.success; any false → Result.retry; throw → Result.failure.
 */
class DataSyncWorker(
    ctx: WorkerContext,
    private val currencyRepository: CurrencyRepository,
    private val macroIndicatorsRepository: MacroIndicatorsRepository,
    private val persister: SyncStatePersister,
) : CoroutineWorker(ctx), Synchronizer {

    private var workingVersions: ChangeListVersions = ChangeListVersions()

    override suspend fun getChangeListVersions() = workingVersions
    override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
        workingVersions = workingVersions.update()
    }

    override suspend fun doWork(): WorkResult {
        workingVersions = persister.read()
        val payload: WorkData = inputData
        return try {
            val (currencyOk, macroOk) = coroutineScope {
                val currencyDeferred = async {
                    currencyRepository.syncWith(this@DataSyncWorker, payload)
                }
                val macroDeferred = async {
                    macroIndicatorsRepository.syncWith(this@DataSyncWorker, payload)
                }
                awaitAll(currencyDeferred, macroDeferred).let { it[0] to it[1] }
            }
            if (currencyOk && macroOk) {
                persister.write(workingVersions)
                WorkResult.success()
            } else WorkResult.retry()
        } catch (t: Throwable) {
            WorkResult.failure(t.message)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo =
        ForegroundInfo(
            notificationId = FOREGROUND_NOTIFICATION_ID_SYNC,
            title = "Syncing data",
            body = "Background sync in progress…",
        )
}
