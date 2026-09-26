# `core-base/data`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_DATA.md`
> **Measured:** 9 Kotlin files, 0 test files

**Defines annotations:** `@DataProvider`, `@FromQualifier`, `@FromStore`, `@RepositoryBinding`

## Principal types

`DataProvider`, `FromQualifier`, `FromStore`, `NetworkChange`, `NetworkMonitorContract`, `NetworkMonitorImpl`, `RepositoryBinding`, `SyncManager`, `Syncable`, `Synchronizer`, `TimeZoneMonitor`, `TimeZoneMonitorImpl`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/data sha=46953f4ae629eca14c1aa9d67f2784748bdf0ff4 -->
## API reference

_Generated from `core-base/data` at tree `46953f4ae629` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/annotation/RepositoryBinding.kt`

```kotlin
annotation class RepositoryBinding(val binds: KClass<*>)
```
Marks a repository implementation so its Koin binding is DERIVED, not hand-written.

<details><summary>Example</summary>

```kotlin
@RepositoryBinding(binds = LoanRepository::class)
internal class LoanRepositoryImpl(
    @FromStore("loans") private val loansStore: Store<Unit, List<Loan>>,
    @FromStore("loansMutable") private val loansWriteStore: MutableStore<String, Loan>,
    private val loanDao: LoanDao,
) : LoanRepository
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/crypto/impl/CryptoRepositoryImpl.kt:27</code></summary>

```kotlin
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = CryptoRepository::class)
class CryptoRepositoryImpl(
    @FromStore(AppStoreIds.CoinMarkets) private val coinMarketsStore: Store<PageKey, List<CoinMarket>>,
    @FromStore(AppStoreIds.CoinDetail) private val coinDetailStore: Store<String, CoinDetail>,
) : CryptoRepository {
```

</details>

```kotlin
annotation class FromStore(val id: String)
```
Resolves this parameter from the store registry rather than by bare type.

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
annotation class DataProvider(
```
Marks a factory function whose Koin `single` is DERIVED from its signature.

<details><summary>Example</summary>

```kotlin
@DataProvider(qualifier = "outbox.loan")
fun provideLoanOutbox(dao: DraftDao): SubmitOutbox<Loan> =
    RoomSubmitOutbox(dao = dao, serializer = Loan.serializer())
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/AuthTokenBridge.kt:31</code></summary>

```kotlin
 * `accessPointId` — the parameter exists precisely so that is a substitution, not a rewrite.
 */
@DataProvider
fun provideAuthTokenSource(preferences: UserPreferencesRepository): AuthTokenSource =
    AuthTokenSource { _: String -> preferences.observeAuthToken }

/**
```

</details>

```kotlin
annotation class FromQualifier(val name: String)
```
Resolves this parameter from a NAMED qualifier rather than by bare type. The counterpart to `FromStore` for bindings the data layer itself qualifies — chiefly a `SubmitOutbox<T>`, whose erased type is shared by every outbox in the graph.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/alerts/AlertsDataProviders.kt:37</code></summary>

```kotlin
fun providePriceAlertSubmitSyncer(
    scope: CoroutineScope,
    @FromQualifier("outbox.priceAlert") outbox: SubmitOutbox<PriceAlert>,
    networkMonitor: NetworkMonitor,
    repository: AlertsRepository,
): OfflineSubmitSyncer<PriceAlert, PriceAlert> = OfflineSubmitSyncer<PriceAlert, PriceAlert>(
    scope = scope,
```

</details>

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/impl/NetworkMonitorImpl.kt`

```kotlin
class NetworkMonitorImpl : NetworkMonitor by NetworkMonitorProvider.install()
```
Singleton NetworkMonitor backed by cmp-network-monitor. Auto-initializes on first access via NetworkMonitorProvider.

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/NetworkMonitor.kt`

```kotlin
typealias NetworkMonitor = io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
```
Backward-compatible typealias — existing consumers keep their import. Delegates to cmp-network-monitor's full-featured NetworkMonitor interface.

<details><summary>Used in the template — <code>feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/ui/CurrencyRatesViewModel.kt:43</code></summary>

```kotlin
class CurrencyRatesViewModel(
    private val currencyRepository: CurrencyRepository,
    private val networkMonitor: NetworkMonitor,
) : BaseViewModel<RatesLocalState, Nothing, RatesAction>(RatesLocalState()) {

    private val stream = currencyRepository.exchangeRatesStream(
        baseCurrency = "USD",
```

</details>

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/NetworkMonitorContract.kt`

```kotlin
object NetworkMonitorContract
```
Framework contract for `NetworkMonitor` implementations. The bundled `cmp-network-monitor` (v3.3.1+, from MobileByteLabs KmpToolkit) satisfies this contract; forks substituting their own implementation MUST also satisfy it.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/NetworkMonitorContractTest.kt:47</code></summary>

```kotlin
        assertTrue(
            monitor.isOnline is StateFlow<*>,
            "NetworkMonitor.isOnline MUST be StateFlow per NetworkMonitorContract invariant 1",
        )
    }

    // TODO: re-enable when cmp-network-monitor provides a JVM-friendly default
```

</details>

- `const val MIN_DEBOUNCE_MS: Long = 100L` — Minimum sane reconnect-debounce window (anything lower thrashes on flaps).
- `const val DEFAULT_DEBOUNCE_MS: Long = 300L` — Default reconnect-debounce window — sensible balance for most apps.
- `const val MAX_DEBOUNCE_MS: Long = 5_000L` — Upper bound — beyond this, the user perceives the app as unresponsive to network changes.

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/Synchronizer.kt`

```kotlin
interface Synchronizer
```
Synchronization contract — ports Now in Android's `core/data/SyncUtilities.kt`.

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/demo/ui/HomeViewModelTest.kt:483</code></summary>

```kotlin
@OptIn(ExperimentalScreenDataStreamTestingApi::class)
private class FakeCurrencyRepository : CurrencyRepository {
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true
    private val source = MutableStateFlow<ScreenState<ExchangeRates>>(ScreenState.Loading)
    var refreshCount: Int = 0
        private set
```

</details>

- `suspend fun getChangeListVersions(): ChangeListVersions`
- `suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions)`
- `suspend fun Syncable.sync(): Boolean = this.syncWith(this@Synchronizer)` — Convenience: call `someSyncable.sync()` to run `Syncable.syncWith` against this.

```kotlin
interface NetworkChange
```
Identified network record. Used by `changeListSync` to partition deletes from updates and to advance the per-feature version pointer.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/SynchronizerExtensionsTest.kt:44</code></summary>

```kotlin
        override val changeListVersion: Int,
        override val isDelete: Boolean,
    ) : NetworkChange

    @Test
    fun changeListSync_partitions_deletes_and_updates_and_advances_the_version() = runTest {
        val sync = InMemorySynchronizer()
```

</details>

- `val id: String`
- `val changeListVersion: Int`
- `val isDelete: Boolean`

```kotlin
interface Syncable
```
Adopter contract. A `Syncable` knows how to bring its slice of local state up to date with the network. At v1 the contract is intentionally minimal (no payload arg) — the worker enqueues all-pinned-keys per adopter at fixed defaults.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/economic/MacroIndicatorsRepository.kt:30</code></summary>

```kotlin
 * [syncWith] to force-refresh the macro cache on a schedule.
 */
interface MacroIndicatorsRepository : Syncable {

    /**
     * Stream observations for a single static (country, indicator) pair.
     *
```

</details>

- `suspend fun syncWith(synchronizer: Synchronizer): Boolean`

```kotlin
suspend fun <T : NetworkChange> Synchronizer.changeListSync(
```
Delta-API algorithm. NiA-port. Used when the server returns `[{id, version, isDelete}, ...]` from `?since=N` semantics — partitions deletes/updates, fans out body fetch, bumps the version pointer.

```kotlin
suspend fun Synchronizer.snapshotSync(name: String, fetcher: suspend () -> Unit): Boolean = coroutineScope
```
Snapshot-API algorithm. Used by both canonical adopters (`CurrencyRepository` over Frankfurter; `MacroIndicatorsRepository` over World Bank).

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/SyncManager.kt`

```kotlin
interface SyncManager
```
Observer surface for the in-flight sync state. NiA-port. Single binary signal — `isSyncing` — collected by composables that want to surface "Refreshing…" indicators globally (typically the top app bar, the home dashboard hero, etc.).

- `val isSyncing: Flow<Boolean>`
- `fun requestSync()`

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/TimeZoneMonitor.kt`

```kotlin
interface TimeZoneMonitor
```
Utility for reporting current timezone the device has set. It always emits at least once with default setting and then for each TZ change.

<details><summary>Used in the template — <code>core/data/src/nonAndroidMain/kotlin/kpt/core/data/di/PlatformModule.kt:19</code></summary>

```kotlin
actual val platformModule: Module
    get() = module {
        single<TimeZoneMonitor> { TimeZoneMonitorImpl() }
    }
```

</details>

- `val currentTimeZone: Flow<TimeZone>`

---

_12 type(s), 15 function(s)/property(ies); 18 carry KDoc at source; 2 authored example(s); 10 live call site(s)._
<!-- api-docs:end -->
