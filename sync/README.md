# `sync/` — worker-kmp × openMF/kmp-project-template integration

> **NOT** in upstream `openMF/kmp-project-template` yet — see [`../PR_README.md`](../PR_README.md)
> for the drafted upstream PR. This module is the worker-kmp team's source-of-truth for what
> the integration looks like; the upstream PR is a separate human action staged in `PR_README.md`.

## What it does

Background sync via worker-kmp's cross-platform WorkManager. On Android, schedules a
`OneTimeWorkRequest<DataSyncWorker>` with unique work name `SyncWork` and
`ExistingWorkPolicy.KEEP` at `App.onCreate`. `DataSyncWorker` constructor-injects
`CurrencyRepository` + `MacroIndicatorsRepository` via Koin and calls `syncWith(synchronizer, payload)`
on each in parallel via `awaitAll(async { currencyRepo.syncWith(...) }, async { macroRepo.syncWith(...) })`.

`WorkScheduler` is the Koin-injectable façade exposing 7 scheduling methods:
- `enqueueDataSync(mode, payload)` — one-time immediate or expedited
- `scheduleNotification(content, delay, mode)` — one-time delayed notification
- `scheduleDailyDataSync(timeOfDay, tz, payload)` — periodic 24h repeat at exact time
- `schedulePeriodicDataSync(interval, initialDelay, payload)` — periodic with clamped interval
- `scheduleDataSyncAt(instant, mode, payload)` — one-time at instant (flex window)
- `scheduleDataSyncAtExact(instant, mode, payload)` — Android: AlarmManager exact; other: flex fallback
- `scheduleNotificationAt(instant, content, mode)` — notification at exact instant

On iOS / Desktop / Web (v1), `SyncManager` is a `MutableStateFlow<Boolean>` stub that
wraps an ad-hoc coroutine — the integration shape is exercised but no real periodic
background scheduling. Real cross-platform scheduling is a follow-up.

## Sequence (Android)

```
App.onCreate
  └─ initKoin { modules(allModules + androidWorkerModule) }
  └─ Sync.initialize(scheduler)
     └─ scheduler.enqueueDataSync(KEEP, OneTimeWorkRequest<DataSyncWorker>(SyncConstraints))
        └─ DataSyncWorker.doWork()
           ├─ persister.read()             → ChangeListVersions
           ├─ coroutineScope {
           │    async { currencyRepository.syncWith(this@DataSyncWorker, payload) }
           │    async { macroIndicatorsRepository.syncWith(this@DataSyncWorker, payload) }
           │    awaitAll(...)              → (currencyOk, macroOk)
           │  }
           ├─ persister.write(updatedVersions)
           └─ Result.success() | Result.retry()

UI observers
  └─ syncManager.isSyncing.collect { showProgress = it }
       (Backed by workManager.getWorkInfosByUniqueWorkNameFlow(SYNC_WORK_NAME) on Android)
```

## How to add a new Syncable repository

1. Mark the repository interface `: Syncable` and implement
   `suspend fun syncWith(synchronizer: Synchronizer, payload: WorkData): Boolean` in the impl.
2. Add the new repository as a constructor param to `DataSyncWorker` (and to `SyncModule.syncWorkers()`
   worker-registry registration). No service-locator `getAll<Syncable>()` — explicit constructor injection.
3. In the Koin module, ensure the new repo is bound as `single<YourRepository>`.

## WorkScheduler — cross-module usage

Any commonMain module can inject `WorkScheduler` without depending on `:sync` directly — it
resolves from `SyncModule` already in `KoinModules.allModules`. Example from `feature/loans`:

```kotlin
class LoanReminderUseCase(private val workScheduler: WorkScheduler) {
    fun installDailyRefresh() {
        workScheduler.scheduleDailyDataSync(timeOfDay = LocalTime(9, 0))
    }
    fun scheduleReminder(loanId: String, dueAt: Instant) {
        workScheduler.scheduleNotificationAt(dueAt, NotificationContent("Loan due", "..."))
        workScheduler.scheduleDataSyncAtExact(dueAt.minus(15.minutes))
    }
}
```

## v1 scope (intentional)

- Real periodic / cross-platform scheduling = follow-up
- `SyncSubscriber` / FCM topics = out of scope
- Per-repo progress / `SyncStatus` enum = preserves Nia's `Flow<Boolean>` constraint
- Single build variant (no demo/prod split)

## Upstream PR plan

See [`../PR_README.md`](../PR_README.md) for the full drafted PR text ready to copy into
a real `openMF/kmp-project-template` PR.
