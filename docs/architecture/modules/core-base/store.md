# `core-base/store`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_STORE.md`
> **Measured:** 108 Kotlin files, 50 test files

**Defines annotations:** `@CacheKey`, `@ExperimentalScreenDataStreamTestingApi`, `@StoreProvider`

**Consumes contracts:** `@ExperimentalScreenDataStreamTestingApi`

## Principal types

`BatchResult`, `BatchSubmitHandler`, `BatchSubmitMode`, `BlockReason`, `CacheKey`, `CombinedState`, `CommandSpec`, `ConflictEntry`, `ConflictInbox`, `ConflictReport`, `ConflictResolution`, `ConflictStrategy`, `DataOrigin`, `DecisionEngine`  …and 52 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/store sha=44f1d50182d1812bf7ea381fe195b12e4d92d4fc -->
## API reference

_Generated from `core-base/store` at tree `44f1d50182d1` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/annotation/StoreProvider.kt`

```kotlin
annotation class StoreProvider(
```
Marks a Store5 provider function.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/prefs/impl/UserDataStore.kt:51</code></summary>

```kotlin
 * is deliberately not part of this store's contract.
 */
@StoreProvider(id = "userData")
@CacheKey(name = "KEY", key = "userData")
fun provideUserDataStore(source: UserDataSource): Store<Unit, UserData> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
```

</details>

```kotlin
annotation class CacheKey(
```
A stream cache key for the annotated store — the string that keys per-stream freshness tracking.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/prefs/impl/UserDataStore.kt:52</code></summary>

```kotlin
 */
@StoreProvider(id = "userData")
@CacheKey(name = "KEY", key = "userData")
fun provideUserDataStore(source: UserDataSource): Store<Unit, UserData> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { _: Unit -> source.observe() },
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/combine/CombinedState.kt`

```kotlin
data class CombinedState<R, W>(
```
Snapshot pairing the read-side `ScreenState` with the write-side `SubmitState` — the single value a screen needs to render every read-write cycle (form preload, submission progress, post-submit outcome, offline outbox indicator).

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailViewModel.kt:115</code></summary>

```kotlin
    private val editSubmitHandler = viewModelScope.submitHandler<Loan>()

    val combinedState: StateFlow<CombinedState<Loan, Loan>> = combine(
        loadOnceScreenState,
        editSubmitHandler.state,
    ) { read, mutation ->
        CombinedState(read = read, mutation = mutation)
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/combine/ScreenWithMutationStream.kt`

```kotlin
interface ScreenWithMutationStream<R, W>
```
Unified read + write + sync seam for screens that both display data and submit mutations against the same domain object (edit forms, settings panels, in-place record updates).

<details><summary>Example</summary>

```kotlin
private val stream = StoreFactory.createScreenWithMutation(
    store = loanStore,
    key = loanId,
    networkMonitor = networkMonitor,
    fetchedAtRepository = fetchedAtRepo,
    cacheKey = "loans:detail:$loanId",
    submitHandler = viewModelScope.submitHandler<LoanForm>(),
    submitBlock = { form -> loanRepository.update(loanId, form); form },
    scope = viewModelScope,
)
val state = stream.state

fun onSave(form: LoanForm) = stream.submit(form)
fun onRetry() = stream.refresh()
val combined by viewModel.state.collectAsStateWithLifecycle()
ScreenContent(state = combined.read) { data ->
    LoanForm(initial = data, onSubmit = viewModel::onSave)
    SubmitProgressOverlay(visible = combined.mutation.isSubmitting)
    OutboxBadge(pending = combined.outboxPending, syncing = combined.isSyncing)
}
```

</details>

- `val state: StateFlow<CombinedState<R, W>>` — Combined read + write + outbox state. Backed by a hot `StateFlow` so the screen can `collectAsStateWithLifecycle()` without rebuilding the pipeline on each subscription.
- `fun refresh()` — Trigger a network refresh of the read pipeline. No-op if read-policy is `kpt.core.base.store.screen.FetchPolicy.CACHE_ONLY`. Mirrors `kpt.core.base.store.screen.ScreenDataStream.refresh`.
- `fun submit(payload: W)` — Submit `payload` through the wired write pipeline.
- `fun cancelPendingSubmit()` — Cancel an in-flight submission and return `CombinedState.mutation` to `kpt.core.base.store.submit.SubmitState.Idle`. Called by screens after the user dismisses an error dialog or navigates away.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/di/StoreModule.kt`

```kotlin
val StoreModule = module
```
Koin module providing base Store infrastructure. Consumer apps should include this module and add their own store bindings in their `core/data` DI module using `StoreFactory` to create store instances.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/di/StoreModule.kt:55</code></summary>

```kotlin
 * ```
 */
val appStoreModule: Module = module {
    // Framework write-SoT: the base-store module provides the single write door (MutationGateway)
    // + its Room-backed ConflictInbox. Every repo migrated onto `gateway.*` resolves `get()` here,
    // so a fork wiring `appStoreModule` gets the gateway for free (needs ConflictDao from
    // DatabaseModule + NetworkMonitor on the graph — both present in KoinModules.allModules).
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/error/ErrorCategory.kt`

```kotlin
sealed interface ErrorCategory
```
High-level classification of a `Throwable` for state-aware UI rendering. Pure logic — no platform calls, no side effects. Lives in `core-base/store` (state layer) so both UI and non-UI consumers can branch on the same categorization.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/AddOrEditLoanScreenPreview.kt:83</code></summary>

```kotlin
            submit = SubmitState.Failed(
                error = IllegalStateException("no connection"),
                category = ErrorCategory.Network,
            ),
            onRetry = {},
            onDismiss = {},
        )
```

</details>

```kotlin
fun categorize(error: Throwable): ErrorCategory
```
Classifies `error` into an `ErrorCategory` by inspecting its class name, message, and cause chain.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/error/OfflineException.kt`

```kotlin
class OfflineException(message: String = "Device is offline") : RuntimeException(message)
```
Synthetic signal that the device was offline at the moment a Store load was attempted. Used by `PagingScreenStream` (and any other framework loader) to fail fast without burning network retry budget when there is clearly no point trying.

<details><summary>Used in the template — <code>feature/alerts/src/commonTest/kotlin/kpt/feature/alerts/ui/AlertCreateViewModelTest.kt:157</code></summary>

```kotlin
        // reconnect. No row = the user's alert is simply gone the moment they were offline.
        val outbox = InMemorySubmitOutbox<PriceAlert>()
        val vm = vmWith(FakeAlertsRepository(failWith = OfflineException()), outbox)
        backgroundScope.launch { vm.uiState.collect { } }
        drain()

        vm.onCoinIdChange("bitcoin")
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/freshness/FreshnessBand.kt`

```kotlin
enum class FreshnessBand
```
Pure time-relative staleness band for cached data — independent of network state. Computed by `FreshnessBands.bandFor` from `(now, lastSyncedAt, ttl, lastError)` alone.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/ui/HomeViewModel.kt:181</code></summary>

```kotlin
     * Stale < VeryStale (higher = worse).
     */
    private fun FreshnessBand.severity(): Int = when (this) {
        FreshnessBand.Initial -> 0
        FreshnessBand.Fresh -> 1
        FreshnessBand.Stale -> 2
        FreshnessBand.VeryStale -> 3
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/freshness/FreshnessBands.kt`

```kotlin
object FreshnessBands
```
Pure computation of `FreshnessBand` from time + last-error inputs only. Decision table (first match wins): 1. `lastSyncedAt == null && lastError == null` → `FreshnessBand.Initial` 2.

- `fun bandFor(`
- `val age = now - lastSyncedAt`

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/freshness/FreshnessSignal.kt`

```kotlin
data class FreshnessSignal(
```
Pure-staleness signal carried by `kpt.core.base.store.screen.ScreenDataStream.freshness` alongside `state`. Decouples cache age from network connectivity.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:528</code></summary>

```kotlin
private fun RatesQuickCard(
    state: ScreenState<RatesQuickView>,
    freshness: FreshnessSignal,
    onRetry: () -> Unit,
    onSeeAll: () -> Unit,
) {
    SectionCard(
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/ConflictStrategy.kt`

```kotlin
sealed interface ConflictStrategy<O>
```
Policy for reconciling a server-side value with a local-side value when both have diverged — the classic "I edited offline, the server also changed" conflict that surfaces during outbox sync, optimistic-UI rollback, and eventually-consistent reads. Wired into `StoreFactory.createMutableStore` as an optional parameter so forks can pick a default. The strategy itself is plain Kotlin — Store5's `org.mobilenativefoundation.store.store5.Updater` interface does not directly consume a `ConflictStrategy`; instead, the recommended pattern is for the fork's `Updater` block to call `conflictStrategy.resolve(server, client)` and write back the resolved value. The `StoreFactory` parameter is therefore exposed for *future* automated wiring + as a discovery hook (and the forks' Updater code documents which strategy is in use). Four shipping strategies cover the dominant cases — see the per-class KDoc. Forks needing bespoke merge logic should implement their own `ConflictStrategy<O>` directly.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/cloudtodo/impl/CloudTodoConflictResolver.kt:31</code></summary>

```kotlin
 */
class CloudTodoConflictResolver(
    private val strategy: ConflictStrategy<CloudTodo> = ConflictStrategy.ClientWins(),
) {

    /** Outcome of reconciling one server echo against the client value. */
    data class Resolution(
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/DecisionEngine.kt`

```kotlin
object DecisionEngine
```
Pure function combining StoreData metadata + NetworkStatus into ScreenState. No side effects, no coroutines — exhaustively unit testable. Uses full `NetworkStatus` (not just Boolean) to detect captive portals.

- `fun <T> decide(` — Maps `storeData` + `networkStatus` to the correct `ScreenState`.
- `val noData = storeData.isEmpty`
- `val error = storeData.error`
- `val isOnline = networkStatus is NetworkStatus.Available`
- `val isCaptivePortal = networkStatus is NetworkStatus.CaptivePortal`
- `val fetchedAt = storeData.fetchedAtInstant`
- `val networkStateNoLongerConsumedHere = isOnline || isCaptivePortal || (error != null)`
- `fun <T> decideFreshness(` — Pure sibling function: maps `storeData` + `ttl` to a `FreshnessSignal`. Decoupled from `decide`; runs in parallel and outputs the per-card freshness signal consumed by `FreshnessIndicator`.
- `val now = Clock.System.now()`
- `val lastSyncedAt = storeData.fetchedAtInstant`
- `val lastError = storeData.error`
- `val band = FreshnessBands.bandFor(`

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/DefaultValidator.kt`

```kotlin
class DefaultValidator<Output : Any>(
```
A TTL-based `Validator` that marks cached data as stale after a given duration. Tracks when data was last fetched using `TimeSource.Monotonic` and considers it invalid once the `ttl` has elapsed. Call `markFresh` when fresh data arrives.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/crypto/impl/CoinMarketsStore.kt:38</code></summary>

```kotlin
    dao: CoinMarketDao,
): Store<PageKey, List<CoinMarket>> {
    val validator = DefaultValidator.withTtl<List<CoinMarket>>(AppStoreRegistry.Ttl.COIN_MARKETS)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { key: PageKey ->
            networkMonitor.executeWithRetry(
                // Retry policy: 1 attempt per fetch. HTTP 401 is handled transparently
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/DraftInventory.kt`

```kotlin
interface DraftInventory
```
Framework-shared, **cross-form** view over every draft the app is holding. `SubmitOutbox` is generic in a single payload type `P` (one instance per form).

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsViewModel.kt:35</code></summary>

```kotlin
 */
class SyncAndDraftsViewModel(
    private val draftInventory: DraftInventory,
) : BaseViewModel<SyncAndDraftsUiState, Nothing, SyncAndDraftsAction>(SyncAndDraftsUiState.Loading) {

    init {
        draftInventory.observeAll()
```

</details>

- `fun observeAll(): Flow<List<DraftRecord>>` — Live cross-form feed of every non-terminal draft (PENDING / RETRYING / FAILED), newest-first by last-update. Terminal SUBMITTED rows are excluded — they are not actionable and are pruned by `StoreCacheManager.pruneExpiredDrafts`.
- `suspend fun discard(id: Long)` — Permanently discards a single draft by `id` (the per-row Discard). Irreversible — the caller confirms intent in the UI.
- `suspend fun retry(id: Long)` — Re-queues a draft by `id` for sync: transitions it back to PENDING and clears its error, so the per-form `kpt.core.base.store.submit.OfflineSubmitSyncer` re-attempts it on the next online transition.
- `suspend fun pruneExpired()` — Manual counterpart to the app-start `StoreCacheManager.pruneExpiredDrafts` — deletes SUBMITTED/FAILED rows older than `StoreCacheManager.DEFAULT_DRAFT_TTL_MS`. PENDING drafts are never pruned.

```kotlin
data class DraftRecord(
```
One draft row as seen by the cross-form `DraftInventory` — untyped (no deserialized payload), because the Sync & Drafts screen renders every form's drafts side-by-side and cannot know each `P`.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsScreen.kt:188</code></summary>

```kotlin
private fun androidx.compose.foundation.lazy.LazyListScope.draftSection(
    titleRes: StringResource,
    rows: List<DraftRecord>,
    onRetry: ((Long) -> Unit)?,
    onDiscardRequest: (Long) -> Unit,
) {
    if (rows.isEmpty()) return
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/FetchedAtRepository.kt`

```kotlin
interface FetchedAtRepository
```
Persists "when was this Store's data last successfully fetched from network", keyed by `storeKey`.

<details><summary>Used in the template — <code>feature/crypto/src/commonTest/kotlin/kpt/feature/crypto/ui/FakeCryptoRepository.kt:144</code></summary>

```kotlin
 */
@OptIn(ExperimentalTime::class)
private object NoOpFetchedAtRepository : FetchedAtRepository {
    override suspend fun read(storeKey: String): Instant? = null
    override suspend fun write(storeKey: String, instant: Instant) = Unit
}
```

</details>

- `suspend fun read(storeKey: String): Instant?` — Returns the last persisted timestamp for `storeKey`, or null if none.
- `suspend fun write(storeKey: String, instant: Instant)` — Persists `instant` as the latest fetch time for `storeKey`.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/impl/DraftInventoryImpl.kt`

```kotlin
class DraftInventoryImpl(
```
Room-backed `DraftInventory`. A thin, framework-owned read/action facade over `DraftDao` that exposes the cross-form draft feed the Sync & Drafts screen renders.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/di/StoreModule.kt:72</code></summary>

```kotlin
    // Cross-form drafts inventory — the live feed + actions behind the template-level
    // Settings → "Sync & Drafts" screen. Framework infra (not a demo store); survives sync.
    single<DraftInventory> { DraftInventoryImpl(draftDao = get()) }

    // Every declared store: its qualifier binding AND its logout registration.
    includes(GeneratedStoreBindings)
}
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/impl/RoomBookkeeper.kt`

```kotlin
class RoomBookkeeper<Key : Any>(
```
Room-backed `Bookkeeper` that persists sync-failure tracking across process restarts. Use this with `org.mobilenativefoundation.store.store5.MutableStore` to enable reliable offline-first writes that retry on connectivity restore.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/cloudtodo/CloudTodoDataProviders.kt:34</code></summary>

```kotlin
@DataProvider
fun provideCloudTodoBookkeeper(dao: BookkeeperDao): Bookkeeper<CloudTodoKey> =
    RoomBookkeeper(dao = dao, keySerializer = { "$CLOUD_TODO_KEY_PREFIX${it.id}" })

/**
 * Eager: drains the cloud-todo write backlog on the offline -> online edge.
 *
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/impl/RoomFetchedAtRepository.kt`

```kotlin
class RoomFetchedAtRepository(
```
Production `FetchedAtRepository` backed by Room (`framework_fetched_at` table). Wired into Koin in `RepositoryModule`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt:56</code></summary>

```kotlin
    // Framework FetchedAtRepository — durable lastFetchedAt persistence backing
    // DataFreshnessIndicator timestamps. Room-only by design (no in-memory fallback).
    single<FetchedAtRepository> { RoomFetchedAtRepository(get<AppDatabase>().fetchedAtDao) }

    // Framework DraftDao — backing store for SubmitOutbox / DraftSubmitHandler
    single { get<AppDatabase>().draftDao }
    // Framework BookkeeperDao — backing store for the MutableStore retry ledger.
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/impl/RoomSubmitOutbox.kt`

```kotlin
class RoomSubmitOutbox<P>(
```
Room-backed `SubmitOutbox` that persists form payloads across process death. Serializes payloads to JSON via `kotlinx.serialization` and stores them in the `framework_submit_drafts` table in `kpt.core.database.AppDatabase`.

<details><summary>Example</summary>

```kotlin
single<SubmitOutbox<LoanApplicationPayload>> {
    RoomSubmitOutbox(get<AppDatabase>().draftDao, LoanApplicationPayload.serializer())
}
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/alerts/AlertsDataProviders.kt:25</code></summary>

```kotlin
@DataProvider(qualifier = "outbox.priceAlert")
fun providePriceAlertOutbox(dao: DraftDao): SubmitOutbox<PriceAlert> =
    RoomSubmitOutbox(dao = dao, serializer = PriceAlert.serializer())

/**
 * Eager: starts watching online events at Koin start and retries pending alerts on reconnect.
 *
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/impl/StoreCacheManagerImpl.kt`

```kotlin
class StoreCacheManagerImpl(
```
Registration-based cache manager. Feature DI modules call `register` for each Store that should be cleared on logout; foundation infrastructure stays free of feature knowledge.

<details><summary>Example</summary>

```kotlin
(get<StoreCacheManager>() as StoreCacheManagerImpl)
    .register(get(AppStoreRegistry.YourStore))
```

</details>

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/di/StoreModule.kt:64</code></summary>

```kotlin
    // Store cache manager — clears all registered caches on logout (registration-based).
    single<StoreCacheManager> {
        StoreCacheManagerImpl(
            bookkeeperDao = get(),
            draftDao = get(),
        )
    }
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/StoreCacheManager.kt`

```kotlin
interface StoreCacheManager
```
Manages Store cache lifecycle. - Call `clearAll` on logout to prevent stale data leaking across user sessions. - Call `pruneExpiredDrafts` on app start to clean up old SUBMITTED/FAILED draft rows.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/impl/UserLogoutManagerImpl.kt:28</code></summary>

```kotlin
class UserLogoutManagerImpl(
    private val repository: UserPreferencesRepository,
    private val storeCacheManager: StoreCacheManager,
    dispatcherManager: DispatcherManager,
) : UserLogoutManager {

    private val scope = CoroutineScope(dispatcherManager.unconfined)
```

</details>

- `suspend fun clearAll()` — Clears all store caches (in-memory + database). Call on logout.
- `suspend fun pruneExpiredDrafts(maxAgeMs: Long = DEFAULT_DRAFT_TTL_MS)` — Removes SUBMITTED and FAILED draft rows older than `maxAgeMs` milliseconds. PENDING drafts are never removed — users can still resume them offline. Default retention is 30 days. Call once on app start.
- `const val DEFAULT_DRAFT_TTL_MS: Long = 30L * 24 * 60 * 60 * 1000` — 30 days in milliseconds.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/StoreFactory.kt`

```kotlin
object StoreFactory
```
Factory for creating `Store` and `MutableStore` instances with sensible defaults.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/prefs/impl/UserDataStore.kt:54</code></summary>

```kotlin
@CacheKey(name = "KEY", key = "userData")
fun provideUserDataStore(source: UserDataSource): Store<Unit, UserData> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { _: Unit -> source.observe() },
            // Writes go through UserDataRepository's typed setters (see KDoc above); this
            // writer exists only to satisfy SourceOfTruth's shape and is never invoked,
```

</details>

- `fun <Key : Any, Input : Any, Output : Any> createStore(` — Creates a read-only `Store` where the fetcher output type matches the source of truth input type (no conversion needed).
- `fun <Key : Any, Output : Any> createMemoryStore(` — Creates a read-only `Store` backed only by a `Fetcher` (no local persistence). Data is cached in-memory only. Useful for transient data that doesn't need to survive process death.
- `fun <Key : Any, Output : Any> createOfflineStore(` — Creates a read-only `Store` backed only by a `SourceOfTruth` (no network fetcher).
- `fun <Key : Any, Output : Any> createOfflineMutableStore(` — Creates a `MutableStore` for a LOCAL-ONLY entity — every mutation flows through `store.write` / `store.clear` (→ the `sourceOfTruth` writer/delete, i.e. Room), but there is NO network.
- `val identity = Converter.Builder<Output, Output, Output>()`
- `val noopUpdater = Updater.by<Key, Output, Output>(`
- `fun <Key : Any, Network : Any, Local : Any, Output : Any> createMutableStore(` — Creates a `MutableStore` that supports reads, writes, and offline sync. Uses a `Converter` to transform between network, local, and output types.
- `fun <Key : Any, R : Any, W : Any> createScreenWithMutation(` — Creates a `ScreenWithMutationStream` — a fused read + write + sync seam for screens that both display data and submit mutations against the same domain object (edit forms, settings panels, in-place record updates).
- `val readStream = store.asScreenStream(`

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/StoreRegistry.kt`

```kotlin
abstract class StoreRegistry
```
Base registry for Store DI qualifiers (Koin).

<details><summary>Example</summary>

```kotlin
object AppStoreRegistry : StoreRegistry() {
    val ExchangeRates = store("exchangeRates")
    val CoinMarkets = store("coinMarkets")
}
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/conflict/ConflictInbox.kt`

```kotlin
interface ConflictInbox
```
Durable inbox of write conflicts surfaced to the user in Settings.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/ConflictInboxViewModel.kt:29</code></summary>

```kotlin
 */
class ConflictInboxViewModel(
    private val conflictInbox: ConflictInbox,
) : BaseViewModel<ConflictInboxUiState, Nothing, ConflictInboxAction>(ConflictInboxUiState.Loading) {

    init {
        conflictInbox.observePending()
```

</details>

- `suspend fun record(` — Record a conflict; returns the new conflict id.
- `fun observePending(): Flow<List<ConflictEntry>>` — Observe the pending (unresolved) conflicts, newest first — drives the Settings badge + list.
- `suspend fun resolve(conflictId: String, resolution: ConflictResolution)` — Resolve a recorded conflict; the entry is cleared once resolution is applied by the caller.

```kotlin
enum class ConflictResolution
```
How the user chose to settle a `ConflictEntry`.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/ConflictInboxViewModel.kt:41</code></summary>

```kotlin
        when (action) {
            is ConflictInboxAction.AcceptServer ->
                viewModelScope.launch { conflictInbox.resolve(action.id, ConflictResolution.ACCEPT_SERVER) }
            is ConflictInboxAction.RetryLocal ->
                viewModelScope.launch { conflictInbox.resolve(action.id, ConflictResolution.RETRY_LOCAL) }
        }
    }
```

</details>

```kotlin
data class ConflictReport(
```
A conflict the caller detected between its local payload and the server result — returned by a command's `conflictOf` so the gateway can record it.

```kotlin
data class ConflictEntry(
```
A single recorded write conflict awaiting user resolution.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsScreen.kt:108</code></summary>

```kotlin
    onPrune: () -> Unit,
    modifier: Modifier = Modifier,
    conflicts: List<ConflictEntry> = emptyList(),
    onAcceptServer: (String) -> Unit = {},
    onRetryLocal: (String) -> Unit = {},
) {
    val sp = MaterialTheme.spacing
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/conflict/impl/RoomConflictInbox.kt`

```kotlin
class RoomConflictInbox(
```
Room-backed `ConflictInbox` — persists write conflicts in the `framework_write_conflicts` table so they survive process death and are surfaced in Settings.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/DefaultMutationGateway.kt`

```kotlin
class DefaultMutationGateway(
```
The default `MutationGateway` — composes the existing Store5 write machinery.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/TestMutationGateway.kt:27</code></summary>

```kotlin
 */
fun testMutationGateway(isOnline: Boolean = true): MutationGateway =
    DefaultMutationGateway(isOnline = { isOnline }, conflictInbox = NoopConflictInbox)

private object NoopConflictInbox : ConflictInbox {
    override suspend fun record(
        entity: String,
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/delete/DeleteSync.kt`

```kotlin
class DeleteSync<K : Any>(
```
The network-DELETE-with-sync primitive Store5 lacks (its `Updater` is write-only).

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/MutationGateway.kt`

```kotlin
interface MutationGateway
```
The single WRITE door for every mutation in the app.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/cloudtodo/impl/CloudTodoRepositoryImpl.kt:38</code></summary>

```kotlin
    @FromStore(AppStoreIds.CloudTodo) private val readStore: Store<CloudTodoKey, CloudTodo>,
    @FromStore(AppStoreIds.CloudTodoMutable) private val writeStore: MutableStore<CloudTodoKey, CloudTodo>,
    private val gateway: MutationGateway,
) : CloudTodoRepository {

    override fun todoStream(id: Int, scope: CoroutineScope): ScreenDataStream<CloudTodo> =
        readStore.asScreenStream(
```

</details>

- `suspend fun <K : Any, V : Any> upsert(` — Upsert `value` under `key` through `store`. - `MutationPolicy.Optimistic`: the local write lands first (UI updates instantly); the network sync is queued via the store's `Updater` + `Bookkeeper` and retried on reconnect.
- `suspend fun <K : Any, V : Any> delete(` — Delete the entity for `key` through `store`, syncing the delete to the network via `deleteEndpoint`.
- `suspend fun <P : Any, R : Any> command(` — Run an RPC / command / form mutation described by `spec` (approve, submit, pay, …) — an operation that is not a plain keyed value write. Composes the durable submit outbox for offline retry.
- `suspend fun localMutation(table: String, mutate: suspend () -> Unit): MutationResult<Unit>` — Local-only mutation for a CACHE_ONLY feature (no `MutableStore` / `Updater`): runs `mutate` as a write-through to Room inside a plain DAO write, so the table's reactive readers (Store streams AND Dao the Room `Flow` queries) re-emit.

```kotlin
sealed interface MutationPolicy
```
How a mutation reaches the network relative to the local write. Two policies only — "optimistic" already means queue-and-retry; there is no separate queue-only mode.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/cloudtodo/impl/CloudTodoRepositoryImpl.kt:69</code></summary>

```kotlin
            key = CloudTodoKey(todo.id),
            value = todo.copy(completed = true),
            policy = MutationPolicy.OnlineRequired,
        )
}
```

</details>

```kotlin
sealed interface MutationResult<out T>
```
The exhaustive outcome of a mutation. The caller (ViewModel) must handle every arm, so an offline write or a conflict can never be silently swallowed.

<details><summary>Used in the template — <code>feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/ui/CloudTodoViewModel.kt:82</code></summary>

```kotlin
 * result arm must not compile until it has somewhere to render.
 */
internal fun MutationResult<CloudTodo>.toOutcome(): MutationOutcome = when (this) {
    is MutationResult.Applied ->
        if (synced) MutationOutcome.AppliedSynced else MutationOutcome.AppliedQueued
    is MutationResult.Blocked -> MutationOutcome.Blocked(reason)
    is MutationResult.Conflicted -> MutationOutcome.Conflicted(conflictId)
```

</details>

```kotlin
enum class BlockReason
```
Why an `MutationPolicy.OnlineRequired` mutation was `MutationResult.Blocked`.

<details><summary>Used in the template — <code>feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/ui/CloudTodoScreen.kt:245</code></summary>

```kotlin
    MutationOutcome.AppliedQueued -> stringResource(Res.string.screens_cloudtodo_outcome_applied_queued_body)
    is MutationOutcome.Blocked -> when (reason) {
        BlockReason.OFFLINE -> stringResource(Res.string.screens_cloudtodo_outcome_blocked_offline)
        BlockReason.UNAUTHENTICATED -> stringResource(Res.string.screens_cloudtodo_outcome_blocked_unauthenticated)
        BlockReason.PRECONDITION_FAILED -> stringResource(Res.string.screens_cloudtodo_outcome_blocked_precondition)
    }
    is MutationOutcome.Conflicted -> stringResource(Res.string.screens_cloudtodo_outcome_conflicted_body)
```

</details>

```kotlin
class CommandSpec<P : Any, R : Any>(
```
Describes a command / RPC mutation for `MutationGateway.command`.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/paging/PagingScreenStream.kt`

```kotlin
class PagingScreenStream<T : Any> internal constructor(
```
Paginated variant of `kpt.core.base.store.screen.ScreenDataStream`. Manages page loading, appending, error surfacing, and unified state for infinite lists.

<details><summary>Example</summary>

```kotlin
val pagingStream = clientStore.asPagingScreenStream(
    networkMonitor = networkMonitor,
    scope = viewModelScope,
    pageSize = 20,
)
val uiState = pagingStream.state  // Flow<ScreenState<List<Client>>>
fun loadMore() = pagingStream.loadNextPage()
```

</details>

<details><summary>Used in the template — <code>feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinMarketsViewModel.kt:30</code></summary>

```kotlin
) : BaseViewModel<Unit, Nothing, CoinMarketsAction>(Unit) {

    val pagingStream: PagingScreenStream<CoinMarket> = repository.coinMarketsStream(
        scope = viewModelScope,
        pageSize = DEFAULT_PAGE_SIZE,
    )
```

</details>

```kotlin
fun <Value : Any> Store<PageKey, List<Value>>.asPagingScreenStream(
```
Creates a `PagingScreenStream` with network-fused state via cmp-network-monitor.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/crypto/impl/CryptoRepositoryImpl.kt:29</code></summary>

```kotlin
@RepositoryBinding(binds = CryptoRepository::class)
class CryptoRepositoryImpl(
    @FromStore(AppStoreIds.CoinMarkets) private val coinMarketsStore: Store<PageKey, List<CoinMarket>>,
    @FromStore(AppStoreIds.CoinDetail) private val coinDetailStore: Store<String, CoinDetail>,
) : CryptoRepository {

    override fun coinMarketsStream(scope: CoroutineScope, pageSize: Int): PagingScreenStream<CoinMarket> =
```

</details>

```kotlin
fun <Value : Any> Store<PageKey, List<Value>>.asPagingScreenStream(
```
`asPagingScreenStream` overload taking a bundled `ScreenStreamContext` instead of the two infra deps — so a paginated repository reads `store.asPagingScreenStream(screen, cacheKey, scope, …)` with `screen` its one injected `ScreenStreamContext`. Delegates to the primary overload.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/crypto/impl/CryptoRepositoryImpl.kt:29</code></summary>

```kotlin
@RepositoryBinding(binds = CryptoRepository::class)
class CryptoRepositoryImpl(
    @FromStore(AppStoreIds.CoinMarkets) private val coinMarketsStore: Store<PageKey, List<CoinMarket>>,
    @FromStore(AppStoreIds.CoinDetail) private val coinDetailStore: Store<String, CoinDetail>,
) : CryptoRepository {

    override fun coinMarketsStream(scope: CoroutineScope, pageSize: Int): PagingScreenStream<CoinMarket> =
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/paging/StorePagingSource.kt`

```kotlin
data class PageKey(
```
Key for paginated Store requests. Use this as the Store key type when the data source supports pagination. The Store will cache each page independently.

<details><summary>Used in the template — <code>feature/crypto/src/commonTest/kotlin/kpt/feature/crypto/ui/FakeCryptoRepository.kt:88</code></summary>

```kotlin
        lastPageSize = pageSize
        val store = StoreBuilder
            .from<PageKey, List<CoinMarket>>(
                fetcher = Fetcher.of {
                    fetches.value += 1
                    emptyList()
                },
```

</details>

```kotlin
suspend fun <Value : Any> Store<PageKey, List<Value>>.loadPage(
```
Triggers a Store refresh for a specific page and suspends until data arrives. Use this inside a PagingSource.load() implementation to let Store handle caching while Paging handles the pagination UX.

<details><summary>Example</summary>

```kotlin
class ClientPagingSource(
    private val store: Store<PageKey, List<Client>>,
) : PagingSource<Int, Client>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Client> {
        val page = params.key ?: 0
        val pageKey = PageKey(page = page, pageSize = params.loadSize)
        return store.loadPage(pageKey)
    }
}
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/crypto/impl/CryptoRepositoryImpl.kt:29</code></summary>

```kotlin
@RepositoryBinding(binds = CryptoRepository::class)
class CryptoRepositoryImpl(
    @FromStore(AppStoreIds.CoinMarkets) private val coinMarketsStore: Store<PageKey, List<CoinMarket>>,
    @FromStore(AppStoreIds.CoinDetail) private val coinDetailStore: Store<String, CoinDetail>,
) : CryptoRepository {

    override fun coinMarketsStream(scope: CoroutineScope, pageSize: Int): PagingScreenStream<CoinMarket> =
```

</details>

```kotlin
sealed class StorePageResult<out T>
```
Result of a paginated Store load, matching PagingSource.LoadResult shape.

<details><summary>Example</summary>

```kotlin
when (result) {
    is StorePageResult.Success -> LoadResult.Page(result.items, result.prevKey, result.nextKey)
    is StorePageResult.Error -> LoadResult.Error(result.error)
}
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/push/PushEvent.kt`

```kotlin
sealed interface PushEvent<K, V>
```
Server-originated push notification that mutates a `org.mobilenativefoundation.store.store5.MutableStore`.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/push/PushMergeStrategy.kt`

```kotlin
sealed interface PushMergeStrategy<out V>
```
How a `PushEvent.Update` should be combined with the existing local value held by the `org.mobilenativefoundation.store.store5.MutableStore`.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/push/PushStoreAdapter.kt`

```kotlin
class PushStoreAdapter<K : Any, V : Any>(
```
Bridges a transport-side `Flow` of `PushEvent`s into a Store5 `MutableStore`, applying the configured `PushMergeStrategy` per `Update` event.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/FetchPolicy.kt`

```kotlin
sealed interface FetchPolicy
```
Controls whether a screen stream reads from cache, hits the network, or both. Pass to `ScreenDataStream.asScreenStream`, `LoadOnceStream.asLoadOnceStream`, or `PagingScreenStream` to override that entry point's default.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/ui/HomeViewModel.kt:73</code></summary>

```kotlin
        baseCurrency = "USD",
        scope = viewModelScope,
        fetchPolicy = FetchPolicy.PERIODIC(intervalMillis = EXCHANGE_RATE_REFRESH_INTERVAL_MS),
    )

    private val fedFundsStream = economicRatesRepository.interestRateSeriesStream(
        key = FedFundsKey,
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/LoadOnceStream.kt`

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.asLoadOnceStream(
```
Load-once variant of `asScreenStream` for edit/mutation screens. Transitions: `Loading → Content(initialData)` — then stops. Further Store updates are ignored so in-progress user edits are never overwritten by background refreshes.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/ScreenDataStream.kt`

```kotlin
class ScreenDataStream<T> internal constructor(
```
One screen's read surface: a cold `ScreenState` flow plus the refresh/retry controls that drive it.

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/FakeLoanRepository.kt:43</code></summary>

```kotlin
    }

    override fun loansStream(scope: CoroutineScope): ScreenDataStream<List<Loan>> =
        // Mirror production: the DAO sorts `nextDueDate ASC, createdAtMs ASC`, so the stream must too.
        screenDataStreamForTesting(
            state.map { rows ->
                if (rows.isEmpty()) {
```

</details>

```kotlin
annotation class ExperimentalScreenDataStreamTestingApi
```
Opt-in marker for the testing-only `ScreenDataStream` constructors.

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/FakeLoanRepository.kt:10</code></summary>

```kotlin
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.feature.loans.ui

import kotlinx.coroutines.CoroutineScope
```

</details>

```kotlin
fun <T> screenDataStreamForTesting(
```
Construct a `ScreenDataStream` from an arbitrary `state` flow and a caller-owned `refreshTrigger`.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
```
Creates a `ScreenDataStream` from this Store, fusing network state via cmp-network-monitor.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
```
Overload accepting a Flow<Key> for dynamic keys (e.g., selected client from DataStore). Re-streams from Store when key changes. Resets lastContent on key change.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/ScreenState.kt`

```kotlin
sealed interface ScreenState<out T>
```
Unified UI state produced by `ScreenDataStream`. Replaces per-ViewModel ScreenUiState + isFromCache + isRefreshing + networkStatus.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailViewModel.kt:62</code></summary>

```kotlin
    private val detailStream = repository.loanDetailStream(loanId, viewModelScope)

    val screenState: StateFlow<ScreenState<Loan>> = detailStream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    /**
     * Re-fetch the loan detail — wired to the read-side retry affordance surfaced by
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/ScreenStateExtensions.kt`

```kotlin
fun <T, R> Flow<ScreenState<T>>.mapContent(
```
Transforms only `ScreenState.Content` data, passing through all other states unchanged.

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/InMemorySubmitOutbox.kt:87</code></summary>

```kotlin
        }

    override fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?> = _entries.map { list ->
        list.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }
    }
```

</details>

```kotlin
fun <T, S, R> Flow<ScreenState<T>>.combineContent(
```
Combines ScreenState with a local state flow for reactive filter/sort/preferences.

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/InMemorySubmitOutbox.kt:87</code></summary>

```kotlin
        }

    override fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?> = _entries.map { list ->
        list.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }
    }
```

</details>

```kotlin
fun <T> Flow<ScreenState<T>>.emptyIfContent(
```
Converts Content to Empty when business-level predicate says data is empty. Applied AFTER DecisionEngine (which only handles structural empty from Store).

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/InMemorySubmitOutbox.kt:87</code></summary>

```kotlin
        }

    override fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?> = _entries.map { list ->
        list.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }
    }
```

</details>

```kotlin
val <T> ScreenState<T>.dataOrNull: T?
```
Extracts data from Content state or null for other states.

```kotlin
val <T> ScreenState<T>.hasContent: Boolean
```
True if state has displayable content.

```kotlin
fun <T> Flow<ScreenState<T>>.mapError(
```
Maps Error throwable to a user-facing type.

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/InMemorySubmitOutbox.kt:87</code></summary>

```kotlin
        }

    override fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?> = _entries.map { list ->
        list.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }
    }
```

</details>

```kotlin
fun <T, R> SubmitHandler<R>.submitWhenContent(
```
Execute `block` only when `screenState` has loaded content, passing its data. No-op when state is Loading / Error / NoNetwork / Empty — safe to call at any time. Guards against premature submits before data arrives on edit screens.

```kotlin
fun <T, R> ScreenState<T>.canInteract(submitState: SubmitState<R>): Boolean =
```
True when the screen has content AND no submission is in-flight.

<details><summary>Example</summary>

```kotlin
Button(enabled = screenState.canInteract(submitState)) { … }
```

</details>

```kotlin
fun <T> T.asLocalScreenState(): ScreenState<T> =
```
Wraps any value in a `ScreenState.Content` with `DataFreshness.FRESH`. Use for utility or settings screens that hold local state and don't use Store5.

<details><summary>Example</summary>

```kotlin
val screenState = stateFlow
    .map { it.asLocalScreenState() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)
```

</details>

```kotlin
fun <T> Flow<T>.asLocalScreenStream(): Flow<ScreenState<T>> =
```
Turns any `Flow`<T> into a `Flow`<`ScreenState`<T>> for use with `ScreenContent`.

```kotlin
fun <A, B, R> combineScreenStates(
```
Combines two independent `ScreenState` flows into one, merging their `ScreenState.Content` data via `transform` when both sources have loaded.

<details><summary>Example</summary>

```kotlin
val screenState: Flow<ScreenState<DashboardUiState>> = combineScreenStates(
    a = accountStore.asScreenStream(Unit),
    b = transactionStore.asScreenStream(Unit),
) { account, transactions ->
    DashboardUiState(account = account, transactions = transactions)
}
```

</details>

```kotlin
fun <A, B, C, R> combineScreenStates(
```
Combines three independent `ScreenState` flows. See `combineScreenStates` for priority rules.

```kotlin
fun <A, B, C, D, R> combineScreenStates(
```
Combines four independent `ScreenState` flows. See `combineScreenStates` for priority rules.

```kotlin
fun <A, B, C, D, E, R> combineScreenStates(
```
Combines five independent `ScreenState` flows. See `combineScreenStates` for priority rules.

```kotlin
fun combineScreenStates(
```
Combines N independent `ScreenState` flows (vararg form). The resulting `ScreenState.Content` holds a `List<Any?>` of the per-source `Content.data` values in input order.

<details><summary>Example</summary>

```kotlin
val combined: Flow<ScreenState<List<Any?>>> = combineScreenStates(
    accountStream, transactionStream, ratesStream, billsStream, loansStream, alertsStream,
)
```

</details>

```kotlin
fun combineScreenStates(
```
Combines N independent `ScreenState` flows (list form). Mirror of the vararg overload — convenient when callers already have a `List<Flow<ScreenState<*>>>` (e.g. a dynamic collection built per-screen).

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/ScreenStreamContext.kt`

```kotlin
class ScreenStreamContext(
```
Bundles the app-infra dependencies that `asScreenStream` needs — the `NetworkMonitor` and the `FetchedAtRepository` — so a repository injects ONE screen-stream context instead of threading two framework singletons through every read method.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/alerts/AlertsReactiveInvalidationTest.kt:64</code></summary>

```kotlin
            modules(
                module {
                    single { ScreenStreamContext(onlineNetworkMonitor(), InMemoryFetchedAtRepository()) }
                },
            )
        }
    }
```

</details>

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
```
`asScreenStream` overload taking a bundled `ScreenStreamContext` instead of the two infra deps — so a repository reads `store.asScreenStream(key, screen, cacheKey, scope, …)` with `screen` its one injected `ScreenStreamContext`.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
```
Dynamic-key `asScreenStream` overload taking a bundled `ScreenStreamContext` — for repositories whose key changes over time (`keyFlow`) with a per-key `cacheKeyFor`.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/StoreData.kt`

```kotlin
data class StoreData<out T>(
```
Wraps data with metadata about its origin, freshness, and refresh state.

```kotlin
enum class DataOrigin
```
Indicates where a `StoreData` emission originated.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/StoreDataExtensions.kt`

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.streamData(
```
Streams data from a `Store` with full `StoreData` metadata. This is the primary API for repositories.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.streamDataWithErrors(
```
Like `streamData` but also emits on errors with last known data preserved.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.freshData(
```
Forces a fresh network fetch, ignoring any cached data. Useful for pull-to-refresh or explicit "reload" actions.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.refreshFresh(
```
Sibling fresh-fetch path for Store5 S5-5 — the counterpart to `FetchPolicy.CACHE_FIRST_SWR`'s cache-first read.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.localData(
```
Reads only from local cache/database, no network. Useful for offline-only screens or pre-fetched data.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.streamDataNoFallback(
```
Like `streamDataWithErrors` but without requiring a fallback value. Emits StoreData with isEmpty=true when error arrives before any data. Used by ScreenDataStream where DecisionEngine handles the no-data case.

```kotlin
fun <T, R> StoreData<T>.map(transform: (T) -> R): StoreData<R>
```
Maps `StoreData` content while preserving all metadata.

```kotlin
fun <T, R> Flow<StoreData<T>>.mapData(transform: (T) -> R): Flow<StoreData<R>>
```
Maps a Flow of `StoreData` content while preserving all metadata.

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/InMemorySubmitOutbox.kt:87</code></summary>

```kotlin
        }

    override fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?> = _entries.map { list ->
        list.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }
    }
```

</details>

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.skipMemoryData(
```
Bypasses in-memory cache, reads from SourceOfTruth + optional network refresh. Useful when you know in-memory state may be stale but disk is authoritative.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/StoreDataMapper.kt`

```kotlin
fun <Output : Any> Flow<StoreReadResponse<Output>>.mapToStoreData(
```
Maps a `StoreReadResponse` flow into `Flow<StoreData<Output>>`.

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/InMemorySubmitOutbox.kt:87</code></summary>

```kotlin
        }

    override fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?> = _entries.map { list ->
        list.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }
    }
```

</details>

```kotlin
fun <Output : Any> Flow<StoreReadResponse<Output>>.mapToStoreDataWithErrors(
```
Like `mapToStoreData` but also emits on errors, carrying the last known data.

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/InMemorySubmitOutbox.kt:87</code></summary>

```kotlin
        }

    override fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?> = _entries.map { list ->
        list.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }
    }
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/StoreResponseMapper.kt`

```kotlin
fun <Output : Any> Flow<StoreReadResponse<Output>>.mapToResult(): Flow<Result<Output>>
```
Maps a `StoreReadResponse` flow to `Flow<Result<Output>>`. - `StoreReadResponse.Data` → `Result.success` - `StoreReadResponse.Error` → `Result.failure` - `StoreReadResponse.Loading` and `StoreReadResponse.NoNewData` are filtered out.

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/InMemorySubmitOutbox.kt:87</code></summary>

```kotlin
        }

    override fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?> = _entries.map { list ->
        list.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }
    }
```

</details>

```kotlin
fun <Output : Any> Flow<StoreReadResponse<Output>>.mapToData(): Flow<Output>
```
Maps a `StoreReadResponse` flow to `Flow<Output>`, emitting only data values. Loading, error, and no-data responses are silently filtered out. Use `mapToResult` when error handling is needed.

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/InMemorySubmitOutbox.kt:87</code></summary>

```kotlin
        }

    override fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?> = _entries.map { list ->
        list.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }
    }
```

</details>

```kotlin
suspend fun <Output : Any> StoreReadResponse<Output>.requireData(): Output
```
Returns the first `StoreReadResponse.Data` value or throws on error.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/BatchSubmitHandler.kt`

```kotlin
class BatchSubmitHandler<P>(
```
Orchestrator for submitting a list of payloads via a single per-payload `submitBlock`. Modes: - `BatchSubmitMode.AllOrNothing` — abort on first failure; remaining payloads are not attempted.

```kotlin
data class BatchResult(
```
Aggregate outcome of `BatchSubmitHandler.submitBatch`.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/BatchSubmitMode.kt`

```kotlin
sealed interface BatchSubmitMode
```
Semantics for `BatchSubmitHandler.submitBatch` when one or more payloads fail. - `AllOrNothing` — fail-fast: the first failure aborts the remaining submissions and the resulting `BatchResult` records only the payloads attempted so far.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/DraftResumeState.kt`

```kotlin
sealed interface DraftResumeState<out P>
```
The UI state emitted by a draft-resume stream. Screens collect this to show a "Resume previous submission?" banner when a PENDING draft exists, or to silently prefill a form field set from a saved draft.

```kotlin
fun <P> SubmitOutbox<P>.resumeStateFor(formKey: String): Flow<DraftResumeState<P>> =
```
Converts a `SubmitOutbox.observePending` flow into a typed `DraftResumeState` flow.

<details><summary>Example</summary>

```kotlin
val draftState: StateFlow<DraftResumeState<LoanPayload>> =
    outbox.resumeStateFor("loan_application")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DraftResumeState.None)
```

</details>

```kotlin
fun <P> SubmitOutbox<P>.resumeStateFor(formKey: String, uniqueKey: String?): Flow<DraftResumeState<P>> =
```
Multi-pending-aware overload. When `uniqueKey` is non-null, observes the PENDING draft for that exact `(formKey, uniqueKey)` pair (one of N concurrent drafts); when null, behaves exactly like the singleton `resumeStateFor` above.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/DraftSubmitHandler.kt`

```kotlin
class DraftSubmitHandler<P, R>(
```
Out-of-box form submission handler with user-prompted draft save and restore/discard support. **Two save modes — controlled by `autoSaveDraft`:** `autoSaveDraft = false` (default) — **user-prompted save:** 1.

<details><summary>Example</summary>

```kotlin
// User-prompted (default):
private val draftHandler = viewModelScope.draftSubmitHandler<LoanPayload, Unit>(
    outbox  = roomSubmitOutbox,
    formKey = "loan_application",
)
// Auto-save:
private val draftHandler = viewModelScope.draftSubmitHandler<LoanPayload, Unit>(
    outbox         = roomSubmitOutbox,
    formKey        = "loan_application",
    autoSaveDraft  = true,
)
val submitState = draftHandler.state
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubmitState.Idle)

fun onSubmit(payload: LoanPayload) = draftHandler.submit(payload) { repository.submitLoan(it) }
fun onRetry()              = draftHandler.retry()
fun onSaveDraft()          = draftHandler.saveDraft()          // user-prompted mode only
fun onDismissDraftPrompt() = draftHandler.discardDraft()       // user-prompted mode only
```

</details>

```kotlin
fun <P, R> CoroutineScope.draftSubmitHandler(
```
Creates a `DraftSubmitHandler` bound to this `CoroutineScope` (typically `viewModelScope`).

<details><summary>Example</summary>

```kotlin
// User-prompted save (default):
private val draftHandler = viewModelScope.draftSubmitHandler<LoanPayload, Unit>(
    outbox  = roomSubmitOutbox,
    formKey = "client_registration",
)
// Silent auto-save on network failure:
private val draftHandler = viewModelScope.draftSubmitHandler<LoanPayload, Unit>(
    outbox        = roomSubmitOutbox,
    formKey       = "client_registration",
    autoSaveDraft = true,
)
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/IdempotencyKey.kt`

```kotlin
object IdempotencyKey
```
Idempotency-key generator for client-side de-duplication of retried submissions. The contract: a caller generates one key per logical user intent (e.g.

- `fun generate(): String = Uuid.random().toString()` — Generate a fresh, cryptographically-random idempotency key.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/MutationUiState.kt`

```kotlin
data class MutationUiState<out T, out R>(
```
Combined UI state for edit/mutation screens that load existing data before allowing changes.

<details><summary>Example</summary>

```kotlin
data class EditClientState(
    val mutation: MutationUiState<ClientDetail, Unit> = MutationUiState(),
)
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/OfflineSubmitSyncer.kt`

```kotlin
class OfflineSubmitSyncer<P, R>(
```
Retries all PENDING outbox entries whenever the device regains connectivity. Wire this once in the app's DI scope (e.g.

<details><summary>Example</summary>

```kotlin
val syncer = coroutineScope.offlineSubmitSyncer(
    outbox             = roomSubmitOutbox,
    networkStatusFlow  = networkMonitor.networkStatus,
    submitBlock        = { payload -> repository.submitLoanApplication(payload) },
    retryOnStatus      = RetryOnNetworkStatus.OnlineOnly,   // default
)
syncer.start()
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/alerts/AlertsDataProviders.kt:40</code></summary>

```kotlin
    networkMonitor: NetworkMonitor,
    repository: AlertsRepository,
): OfflineSubmitSyncer<PriceAlert, PriceAlert> = OfflineSubmitSyncer<PriceAlert, PriceAlert>(
    scope = scope,
    outbox = outbox,
    networkStatusFlow = networkMonitor.networkStatus,
    submitBlock = { payload -> repository.submitAlert(payload) },
```

</details>

```kotlin
fun <P, R> CoroutineScope.offlineSubmitSyncer(
```
Creates an `OfflineSubmitSyncer` bound to this `CoroutineScope`.

<details><summary>Example</summary>

```kotlin
val syncer = viewModelScope.offlineSubmitSyncer(
    outbox             = roomSubmitOutbox,
    networkStatusFlow  = networkMonitor.networkStatus,
    submitBlock        = { payload -> api.submit(payload) },
)
syncer.start()
```

</details>

```kotlin
typealias OfflineSubmitSyncerCrashSeverity = CrashSeverity
```
Re-exported for callers that want to set retry-classification severity in their own code. Currently informational only — `OfflineSubmitSyncer` records exceptions at the default severity (no `CrashSeverity` argument needed).

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/OptimisticSubmit.kt`

```kotlin
suspend fun <P, R> SubmitHandler<R>.optimisticSubmit(
```
Optimistic-UI submission orchestration: mutate the local store immediately, then perform the network call, and roll back if the network call fails.

<details><summary>Example</summary>

```kotlin
private val submit = viewModelScope.submitHandler<Item>()

fun onLike(item: Item) = viewModelScope.launch {
    submit.optimisticSubmit(
        payload  = item,
        apply    = { p -> localStore.update(p.copy(liked = true)) },
        network  = { p -> api.like(p.id) },
        rollback = { p, _ -> localStore.update(p.copy(liked = false)) },
    )
}
```

</details>

```kotlin
suspend fun <Key : Any, Value : Any, P, R> MutableStore<Key, Value>.optimisticSubmit(
```
`MutableStore` variant of `optimisticSubmit` — identical semantics, just a different receiver for IDE discoverability when the caller is holding a `MutableStore` rather than a `SubmitHandler`.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/RetryOnNetworkStatus.kt`

```kotlin
sealed interface RetryOnNetworkStatus
```
Policy controlling when `OfflineSubmitSyncer` retries pending outbox entries. Default (`OnlineOnly`) preserves the pre-2026-05-27 behavior — retry only when fully online. Forks whose API is reachable behind a captive portal (e.g.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/cloudtodo/impl/CloudTodoSyncOrchestrator.kt:62</code></summary>

```kotlin
    private val loadLocal: suspend (CloudTodoKey) -> CloudTodo?,
    private val writeBlock: suspend (CloudTodo) -> Unit,
    private val retryOnStatus: RetryOnNetworkStatus = RetryOnNetworkStatus.OnlineOnly,
    private val retryPolicy: RetryPolicy = RetryPolicy(),
    private val onReplayError: (Throwable) -> Unit = {},
) {
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/RetryPolicy.kt`

```kotlin
data class RetryPolicy(
```
Exponential-backoff-with-jitter retry policy for outbox-style retries. Pure data — no scheduler, no side effects. Consumers compute the next delay via `delayFor` and apply it themselves (e.g. `delay(policy.delayFor(attempt))`).

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/crypto/impl/CoinMarketsStore.kt:45</code></summary>

```kotlin
                // by the Ktor Auth interceptor (refresh-and-retry once). All other
                // failures propagate immediately to PagingScreenStream → DecisionEngine.
                RetryPolicy { maxAttempts = 1 },
            ) {
                api.getMarkets(page = key.page + 1, perPage = key.pageSize)
                    .map { it.toDomain() }
            }
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/SubmitHandler.kt`

```kotlin
class SubmitHandler<R> internal constructor(
```
Reusable one-shot executor for a single form/action submission lifecycle.

<details><summary>Example</summary>

```kotlin
private val submit = viewModelScope.submitHandler<ClientId>()
val submitState    = submit.state
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubmitState.Idle)

fun onSave(form: ClientForm) = submit.submit { repository.createClient(form) }
fun onRetry()               = submit.retry()
fun onDismiss()             = submit.reset()
Box(Modifier.fillMaxSize()) {
    FormContent(enabled = !submitState.isSubmitting, onSubmit = { viewModel.onSave(it) })
    SubmitProgressOverlay(visible = submitState.isSubmitting)
    SubmitResultHandler(
        state = submitState,
        onSubmitted = { onNavigateBack() },
        onFailed = { error, category -> viewModel.onDismiss(); showError(error, category) },
    )
}
```

</details>

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailViewModel.kt:113</code></summary>

```kotlin
     * used directly. For Store-backed entities, prefer `StoreFactory.createScreenWithMutation`.
     */
    private val editSubmitHandler = viewModelScope.submitHandler<Loan>()

    val combinedState: StateFlow<CombinedState<Loan, Loan>> = combine(
        loadOnceScreenState,
        editSubmitHandler.state,
```

</details>

```kotlin
fun <R> CoroutineScope.submitHandler(): SubmitHandler<R> = SubmitHandler(this)
```
Creates a `SubmitHandler` bound to this `CoroutineScope` (typically `viewModelScope`).

<details><summary>Example</summary>

```kotlin
private val submit = viewModelScope.submitHandler<Unit>()
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/SubmitMessages.kt`

```kotlin
data class SubmitMessages(
```
Per-mutation copy overrides. Pass to `SubmitHandler.submit` to surface action-specific messages instead of the generic "Saving…" / "Saved" / "Failed" defaults wired at the theme level.

<details><summary>Example</summary>

```kotlin
submitHandler.submit(
    messages = SubmitMessages(
        submitting = "Creating loan…",
        submitted = "Loan created",
        failed = "Couldn't create loan",
    ),
) { repository.createLoan(form) }
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/SubmitOutbox.kt`

```kotlin
interface SubmitOutbox<P>
```
Durable outbox for form payloads that failed to reach the server. Persist a payload on network failure → the user can resume later from any session.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/EditLoanViewModel.kt:53</code></summary>

```kotlin
class EditLoanViewModel(
    private val repository: LoanRepository,
    outbox: SubmitOutbox<Loan>,
    val loanId: String?,
) : BaseMutationViewModel<Loan, Loan>(
    MutationMode.Draft(
        outbox = outbox,
```

</details>

- `suspend fun save(formKey: String, payload: P): Long` — Save `payload` as the singleton PENDING draft under `formKey`. If a PENDING draft already exists for `formKey` with `uniqueKey IS NULL`, this method updates it in-place and returns its existing id (idempotent upsert — no duplicate rows).
- `suspend fun saveByUniqueKey(formKey: String, uniqueKey: String, payload: P): Long` — Multi-pending companion to `save`. Saves `payload` as a PENDING draft scoped on `(formKey, uniqueKey)`. N concurrent drafts can coexist under one `formKey` as long as each carries a distinct `uniqueKey` (e.g.
- `suspend fun getPending(formKey: String): SubmitOutboxEntry<P>?` — Retrieve the current singleton PENDING entry for `formKey`, or `null` if none.
- `suspend fun getPendingByUniqueKey(formKey: String, uniqueKey: String): SubmitOutboxEntry<P>?` — Retrieve the PENDING entry for a specific `(formKey, uniqueKey)` pair, or `null` if none.
- `fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?>` — Hot flow that emits the current singleton PENDING entry whenever it changes.
- `fun observePendingByUniqueKey(formKey: String, uniqueKey: String): Flow<SubmitOutboxEntry<P>?>` — Streaming variant of `getPendingByUniqueKey`.
- `fun observeAllByFormKey(formKey: String): Flow<List<SubmitOutboxEntry<P>>>` — Observes ALL non-terminal drafts (PENDING / RETRYING / FAILED) for `formKey`, ordered newest-first. Used by feature UIs that need to display "N drafts pending sync" across multiple uniqueKeys (e.g. Portfolio: "3 holdings pending").
- `suspend fun getAllPending(): List<SubmitOutboxEntry<P>>` — All entries currently in PENDING status (excludes RETRYING) — used by `OfflineSubmitSyncer`.
- `suspend fun markRetrying(id: Long)` — Claim this entry for retry — transitions PENDING → RETRYING; prevents concurrent UI double-submit.
- `suspend fun markSubmitted(id: Long)` — Transition the entry identified by `id` to SUBMITTED.
- `suspend fun markFailed(id: Long, error: String?)` — Transition the entry identified by `id` to FAILED with an optional `error` message.
- `suspend fun deleteByFormKey(formKey: String)` — Delete all drafts for `formKey` (e.g. on screen close or after successful submit).
- `suspend fun deleteByUniqueKey(formKey: String, uniqueKey: String)` — Delete a single `(formKey, uniqueKey)` row. Use after a multi-pending draft successfully submits. Does NOT affect the singleton draft (uniqueKey IS NULL) under the same `formKey`.
- `suspend fun deleteAll()` — Delete every draft — called from `StoreCacheManager.clearAll` on logout.

```kotlin
data class SubmitOutboxEntry<out P>(
```
A single outbox record as seen by the framework.

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/InMemorySubmitOutbox.kt:29</code></summary>

```kotlin
internal class InMemorySubmitOutbox<P> : SubmitOutbox<P> {

    private val _entries = MutableStateFlow<List<SubmitOutboxEntry<P>>>(emptyList())
    private var nextId = 1L

    val entries: List<SubmitOutboxEntry<P>> get() = _entries.value
```

</details>

```kotlin
enum class SubmitOutboxStatus
```
Lifecycle states for a `SubmitOutboxEntry`.

<details><summary>Used in the template — <code>feature/loans/src/commonTest/kotlin/kpt/feature/loans/ui/InMemorySubmitOutbox.kt:36</code></summary>

```kotlin
    override suspend fun save(formKey: String, payload: P): Long {
        val existing = _entries.value.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }
        if (existing != null) {
            _entries.value = _entries.value.map {
                if (it.id == existing.id) it.copy(payload = payload) else it
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/SubmitState.kt`

```kotlin
sealed interface SubmitState<out R>
```
State machine for a single form/action submission lifecycle.

<details><summary>Example</summary>

```kotlin
  Idle  ──submit()──▶  Submitting  ──success──▶  Submitted<R>
                                   ──failure──▶  Failed
  Submitted / Failed  ──reset()──▶  Idle
  Failed              ──retry()──▶  Submitting  (re-runs last block)
```

</details>

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailViewModel.kt:123</code></summary>

```kotlin
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CombinedState(read = ScreenState.Loading, mutation = SubmitState.Idle),
    )

    /**
     * Submit an edit of the current loan. Transitions [combinedState.mutation] through
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/SubmitStateExtensions.kt`

```kotlin
val <R> SubmitState<R>.isSubmitting: Boolean
```
True while the API call is in-flight. Use to disable the submit button.

```kotlin
val <R> SubmitState<R>.isIdle: Boolean
```
True when in the initial or post-reset state.

```kotlin
val <R> SubmitState<R>.isSubmitted: Boolean
```
True when the last submission completed successfully.

```kotlin
val <R> SubmitState<R>.isFailed: Boolean
```
True when the last submission failed. `SubmitHandler.retry` is available.

```kotlin
val <R> SubmitState<R>.resultOrNull: R?
```
Returns the result payload for `SubmitState.Submitted`, or null for all other states.

```kotlin
val <R> SubmitState<R>.errorOrNull: Throwable?
```
Returns the error for `SubmitState.Failed`, or null for all other states.

```kotlin
val <R> SubmitState<R>.categoryOrNull: ErrorCategory?
```
Returns the `ErrorCategory` for `SubmitState.Failed`, or null for all other states.

---

_65 type(s), 113 function(s)/property(ies); 163 carry KDoc at source; 22 authored example(s); 55 live call site(s)._
<!-- api-docs:end -->
