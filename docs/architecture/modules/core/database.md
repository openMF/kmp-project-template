# `core/database`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATABASE.md`
> **Measured:** 53 Kotlin files, 10 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@DbEntity` | `database-ksp` | config/AppDatabase |
| `@DbDao` | `database-ksp` | di/GeneratedDaoBindings |
| `@DbConverters` | `database-ksp` | di/GeneratedConverterBindings |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

## Principal types

`AlertDao`, `AlertEntity`, `BankingTypeConverters`, `BillReminderDao`, `BillReminderEntity`, `ChargeTypeConverters`, `CloudTodoDao`, `CloudTodoEntity`, `CoinDetailDao`, `CoinDetailEntity`, `CoinMarketDao`, `CoinMarketEntity`, `DatabaseConfig`, `ExchangeRatesDao`  …and 13 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/database sha=79ec8dfbdce605a87b2f76b351a8bd222e4bc9b3 -->
## API reference

_Generated from `core/database` at tree `79ec8dfbdce6` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/database/src/commonMain/kotlin/kpt/core/database/alerts/AlertDao.kt`

```kotlin
interface AlertDao
```
Data-access object for the `alerts` table. All reads return reactive `Flow`s; all writes are `suspend` one-shots. Natural sort order is newest-first (`AlertEntity.createdAt` descending).

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/alerts/FakeAlertDao.kt:29</code></summary>

```kotlin
 * assert the absence of a mechanism production depends on.
 */
internal class FakeAlertDao : AlertDao {

    private val rows = MutableStateFlow<List<AlertEntity>>(emptyList())

    override fun observeAll(): Flow<List<AlertEntity>> =
```

</details>

- `fun observeAll(): Flow<List<AlertEntity>>` — Observe all alerts, ordered newest-first.
- `fun observeById(id: String): Flow<AlertEntity?>` — Observe a single alert by id — the per-item write store's SourceOfTruth reader.
- `suspend fun upsert(alert: AlertEntity)` — Insert or replace a single alert.
- `suspend fun upsertAll(alerts: List<AlertEntity>)` — Insert or replace a batch of alerts.
- `suspend fun deleteById(id: String)` — Delete a single alert by id. No-op if absent.
- `suspend fun deleteAll()` — Delete all alerts.

### `core/database/src/commonMain/kotlin/kpt/core/database/alerts/AlertEntity.kt`

```kotlin
data class AlertEntity(
```
Persistent row for a price alert. Stored in the `alerts` table (v9+). Each row represents a single threshold-based alert for a given symbol.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/alerts/AlertDao.kt:31</code></summary>

```kotlin
    /** Observe all alerts, ordered newest-first. */
    @Query("SELECT * FROM alerts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AlertEntity>>

    /** Observe a single alert by id — the per-item write store's SourceOfTruth reader. */
    @Query("SELECT * FROM alerts WHERE id = :id")
    fun observeById(id: String): Flow<AlertEntity?>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/banking/converter/BankingTypeConverters.kt`

```kotlin
class BankingTypeConverters
```
Room 3 `TypeConverter` collection for the banking-domain tables (`banking_loans`, `banking_bill_reminders`). - Enums (`LoanKind`, `Recurrence`, `BillCategory`) persist as their `name` (TEXT).

<details><summary>Used in the template — <code>core/database/src/commonTest/kotlin/kpt/core/database/banking/converter/BankingTypeConvertersTest.kt:28</code></summary>

```kotlin
class BankingTypeConvertersTest {

    private val converters = BankingTypeConverters()

    @Test
    fun loanKindRoundTripCoversAllValues() {
        LoanKind.entries.forEach { kind ->
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/banking/dao/BillReminderDao.kt`

```kotlin
interface BillReminderDao
```
Data-access object for the `banking_bill_reminders` table. Reads are reactive `Flow`s; writes are `suspend`. Natural sort order is by `BillReminderEntity.dueDay` ascending — bills due earliest in the month appear first in the dashboard.

<details><summary>Used in the template — <code>core/database/src/nonWebTest/kotlin/kpt/core/database/banking/dao/BillReminderDaoTest.kt:37</code></summary>

```kotlin

    private lateinit var database: AppDatabase
    private lateinit var dao: BillReminderDao

    @BeforeTest
    fun setup() {
        // Built through `testPlatformModule`, NOT a hand-rolled Room builder: the driver is
```

</details>

- `fun observeAll(): Flow<List<BillReminderEntity>>`
- `fun observeUpcoming(dueDays: Set<Int>): Flow<List<BillReminderEntity>>`
- `fun observeById(id: String): Flow<BillReminderEntity?>` — Observe a single bill reminder. Emits `null` after deletion.
- `suspend fun getById(id: String): BillReminderEntity?` — One-shot read for non-reactive callers.
- `fun count(): Flow<Int>` — Reactive row count — used by the dashboard badge.
- `suspend fun upsert(entity: BillReminderEntity)` — Insert-or-replace by primary key.
- `suspend fun deleteById(id: String)` — Delete a bill reminder by id. No-op if absent.
- `suspend fun deleteAll()`

### `core/database/src/commonMain/kotlin/kpt/core/database/banking/dao/LoanDao.kt`

```kotlin
interface LoanDao
```
Data-access object for the `banking_loans` table. Reads are reactive `Flow`s; writes are `suspend`. Standard Room 3 KMP shape.

<details><summary>Used in the template — <code>core/database/src/nonWebTest/kotlin/kpt/core/database/banking/dao/LoanDaoTest.kt:41</code></summary>

```kotlin

    private lateinit var database: AppDatabase
    private lateinit var dao: LoanDao

    @BeforeTest
    fun setup() {
        // Built through `testPlatformModule`, NOT a hand-rolled Room builder: the driver is
```

</details>

- `fun observeAll(): Flow<List<LoanEntity>>` — Observe all loans, soonest-due first. Emits on every change.
- `fun observeById(id: String): Flow<LoanEntity?>` — Observe a single loan by `id`. Emits `null` if it has been deleted.
- `suspend fun getById(id: String): LoanEntity?` — One-shot read for non-reactive callers (mappers, tests, migrations).
- `fun count(): Flow<Int>` — Reactive row count — used by the dashboard badge.
- `suspend fun upsert(entity: LoanEntity)` — Insert-or-replace. The `id` is the primary key, so re-saving overwrites.
- `suspend fun deleteById(id: String)` — Remove a loan by `id`. No-op if absent.
- `suspend fun deleteAll()`
- `fun observeAllByNextDue(): Flow<List<LoanEntity>> = observeAll()`
- `val UPSERT_STRATEGY = OnConflictStrategy.REPLACE`

### `core/database/src/commonMain/kotlin/kpt/core/database/banking/entity/BillReminderEntity.kt`

```kotlin
data class BillReminderEntity(
```
Persistent row for a recurring (or one-time) bill reminder. Mirrors `kpt.core.model.banking.BillReminder`; mapping lives in `core/data/banking/`. Stored locally only — no remote sync.

<details><summary>Used in the template — <code>core/database/src/jsTest/kotlin/kpt/core/database/infra/WebInvalidationProbeTest.kt:153</code></summary>

```kotlin
    }

    private fun bill(id: String): BillReminderEntity = BillReminderEntity(
        id = id,
        name = "Probe $id",
        amount = 100.0,
        dueDay = 15,
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/banking/entity/LoanEntity.kt`

```kotlin
data class LoanEntity(
```
Persistent row for a personal loan tracked by the user. Mirrors `kpt.core.model.banking.Loan`; mapping lives in the repository layer (`core/data/banking/`). Stored locally only — no remote sync.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/banking/dao/LoanDao.kt:33</code></summary>

```kotlin
    /** Observe all loans, soonest-due first. Emits on every change. */
    @Query("SELECT * FROM banking_loans ORDER BY nextDueDate ASC, createdAtMs ASC")
    fun observeAll(): Flow<List<LoanEntity>>

    /** Observe a single loan by [id]. Emits `null` if it has been deleted. */
    @Query("SELECT * FROM banking_loans WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<LoanEntity?>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/cloudtodo/CloudTodoDao.kt`

```kotlin
interface CloudTodoDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/cloudtodo/CloudTodoDataProviders.kt:52</code></summary>

```kotlin
    bookkeeperDao: BookkeeperDao,
    bookkeeper: Bookkeeper<CloudTodoKey>,
    todoDao: CloudTodoDao,
    repository: CloudTodoRepository,
    crashReporter: CrashReporter?,
): CloudTodoSyncOrchestrator = CloudTodoSyncOrchestrator(
    scope = scope,
```

</details>

- `fun observeById(id: Int): Flow<CloudTodoEntity?>`
- `suspend fun getById(id: Int): CloudTodoEntity?`
- `suspend fun upsert(entity: CloudTodoEntity)`
- `suspend fun deleteById(id: Int)`
- `suspend fun deleteAll()`

### `core/database/src/commonMain/kotlin/kpt/core/database/cloudtodo/CloudTodoEntity.kt`

```kotlin
data class CloudTodoEntity(
```
Room mirror of a `kpt.core.model.cloudtodo.CloudTodo` (the Store5 MutableStore SoT).

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/cloudtodo/CloudTodoDao.kt:23</code></summary>

```kotlin
interface CloudTodoDao {
    @Query("SELECT * FROM cloud_todos WHERE id = :id")
    fun observeById(id: Int): Flow<CloudTodoEntity?>

    @Query("SELECT * FROM cloud_todos WHERE id = :id")
    suspend fun getById(id: Int): CloudTodoEntity?
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/cloudtodo/CloudTodoEntityMapper.kt`

```kotlin
fun CloudTodoEntity.toDomain(): CloudTodo = CloudTodo(id = id, title = title, completed = completed)
```
_No KDoc at source._

```kotlin
fun CloudTodo.toEntity(): CloudTodoEntity = CloudTodoEntity(id = id, title = title, completed = completed)
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/config/DatabaseConfig.kt`

```kotlin
object DatabaseConfig
```
Fork-unique database naming — generated by syncForkConfig from app-profile/app.yaml.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/di/DatabaseModule.kt:24</code></summary>

```kotlin
 */
internal val appDatabaseNaming = DatabaseNaming(
    fileName = DatabaseConfig.NAME,
    desktopDirName = DatabaseConfig.DESKTOP_DIR_NAME,
)

/**
```

</details>

- `const val NAME = "org_mifos_kmp_template.db"` — On-disk SQLite file name (appId-derived).
- `const val DESKTOP_DIR_NAME = "MoneyToolkit"` — Desktop (JVM) data directory under the OS app-data root (app_name-derived).

### `core/database/src/commonMain/kotlin/kpt/core/database/config/ForkDatabaseConfig.kt`

```kotlin
object ForkDatabaseConfig
```
The fork's Room schema version. `owner: fork` — PRESERVED across `/kmp-project-template-sync`. GENERATED from `app-profile/migration-ledger.yaml#version` by `./gradlew syncForkConfig`; do not hand-edit.

- `const val VERSION = 13`

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/converter/FintechTypeConverters.kt`

```kotlin
class FintechTypeConverters
```
_No KDoc at source._

```kotlin
data class RatePointPair(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/currency/mapper/RateHistoryEntityMapper.kt:24</code></summary>

```kotlin
    startDate = startDate,
    endDate = endDate,
    ratesJson = Json.encodeToString(rates.map { RatePointPair(it.date, it.value) }),
    fetchedAt = Clock.System.now().toEpochMilliseconds(),
)

fun RateHistoryEntity.toDomain(): RateHistory = RateHistory(
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/dao/CoinDetailDao.kt`

```kotlin
interface CoinDetailDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/crypto/impl/CoinDetailStore.kt:35</code></summary>

```kotlin
    api: CoinGeckoApi,
    networkMonitor: NetworkMonitor,
    dao: CoinDetailDao,
): Store<String, CoinDetail> {
    val validator = DefaultValidator.withTtl<CoinDetail>(AppStoreRegistry.Ttl.COIN_DETAIL)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { coinId: String ->
```

</details>

- `suspend fun upsert(entity: CoinDetailEntity)`
- `fun getById(coinId: String): Flow<CoinDetailEntity?>`
- `suspend fun delete(coinId: String)`
- `suspend fun deleteAll()`

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/dao/CoinMarketDao.kt`

```kotlin
interface CoinMarketDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/crypto/impl/CoinMarketsStore.kt:36</code></summary>

```kotlin
    api: CoinGeckoApi,
    networkMonitor: NetworkMonitor,
    dao: CoinMarketDao,
): Store<PageKey, List<CoinMarket>> {
    val validator = DefaultValidator.withTtl<List<CoinMarket>>(AppStoreRegistry.Ttl.COIN_MARKETS)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { key: PageKey ->
```

</details>

- `suspend fun upsertAll(entities: List<CoinMarketEntity>)`
- `fun getPage(limit: Int, offset: Int): Flow<List<CoinMarketEntity>>`
- `fun getAll(): Flow<List<CoinMarketEntity>>`
- `suspend fun deleteByPage(page: Int)`
- `suspend fun deleteAll()`
- `suspend fun count(): Int`
- `suspend fun replacePage(page: Int, entities: List<CoinMarketEntity>)` — Atomically swap the rows of one page (S5-3 PAGINATION_RACE).

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/entity/CoinDetailEntity.kt`

```kotlin
data class CoinDetailEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/crypto/dao/CoinDetailDao.kt:24</code></summary>

```kotlin

    @Upsert
    suspend fun upsert(entity: CoinDetailEntity)

    @Query("SELECT * FROM coin_detail WHERE id = :coinId LIMIT 1")
    fun getById(coinId: String): Flow<CoinDetailEntity?>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/entity/CoinMarketEntity.kt`

```kotlin
data class CoinMarketEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/crypto/dao/CoinMarketDao.kt:25</code></summary>

```kotlin

    @Upsert
    suspend fun upsertAll(entities: List<CoinMarketEntity>)

    @Query("SELECT * FROM coin_markets ORDER BY marketCapRank ASC LIMIT :limit OFFSET :offset")
    fun getPage(limit: Int, offset: Int): Flow<List<CoinMarketEntity>>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/mapper/CoinDetailEntityMapper.kt`

```kotlin
fun CoinDetail.toEntity(): CoinDetailEntity = CoinDetailEntity(
```
_No KDoc at source._

```kotlin
fun CoinDetailEntity.toDomain(): CoinDetail = CoinDetail(
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/crypto/mapper/CoinMarketEntityMapper.kt`

```kotlin
fun CoinMarket.toEntity(page: Int): CoinMarketEntity = CoinMarketEntity(
```
_No KDoc at source._

```kotlin
fun CoinMarketEntity.toDomain(): CoinMarket = CoinMarket(
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/converter/ChargeTypeConverters.kt`

```kotlin
class ChargeTypeConverters
```
Room 3 `TypeConverter` collection for JSON-backed column types. Supports optional field-level encryption via `FieldEncryptor`.

<details><summary>Used in the template — <code>core/database/src/commonTest/kotlin/kpt/core/database/currency/ChargeTypeConvertersTest.kt:18</code></summary>

```kotlin
class ChargeTypeConvertersTest {

    private val converters = ChargeTypeConverters()

    @Test
    fun intListRoundTripPreservesData() {
        val original = arrayListOf<Int?>(1, 2, 3, null, 5)
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/dao/ExchangeRatesDao.kt`

```kotlin
interface ExchangeRatesDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/exchange/impl/SpotRateLookupStore.kt:45</code></summary>

```kotlin
    api: FrankfurterApi,
    networkMonitor: NetworkMonitor,
    dao: ExchangeRatesDao,
): Store<String, ExchangeRates> = StoreFactory.createStore(
    fetcher = Fetcher.of { baseCurrency: String ->
        networkMonitor.executeWithRetry(
            RetryPolicy { maxAttempts = 1 },
```

</details>

- `suspend fun upsert(entity: ExchangeRatesEntity)`
- `fun getByBase(currency: String): Flow<ExchangeRatesEntity?>`
- `suspend fun deleteByBase(currency: String)`
- `suspend fun deleteAll()`
- `suspend fun deleteOlderThan(epochMillis: Long)`

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/dao/RateHistoryDao.kt`

```kotlin
interface RateHistoryDao
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/currency/impl/RateHistoryStore.kt:45</code></summary>

```kotlin
    api: FrankfurterApi,
    networkMonitor: NetworkMonitor,
    dao: RateHistoryDao,
): Store<RateHistoryKey, RateHistory> {
    val validator = DefaultValidator.withTtl<RateHistory>(AppStoreRegistry.Ttl.RATE_HISTORY)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { key: RateHistoryKey ->
```

</details>

- `suspend fun upsert(entity: RateHistoryEntity)`
- `fun get(from: String, to: String, startDate: String, endDate: String): Flow<RateHistoryEntity?>`
- `suspend fun delete(from: String, to: String)`
- `suspend fun deleteAll()`

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/entity/ExchangeRatesEntity.kt`

```kotlin
data class ExchangeRatesEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/currency/dao/ExchangeRatesDao.kt:24</code></summary>

```kotlin

    @Upsert
    suspend fun upsert(entity: ExchangeRatesEntity)

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :currency LIMIT 1")
    fun getByBase(currency: String): Flow<ExchangeRatesEntity?>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/entity/RateHistoryEntity.kt`

```kotlin
data class RateHistoryEntity(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/currency/dao/RateHistoryDao.kt:24</code></summary>

```kotlin

    @Upsert
    suspend fun upsert(entity: RateHistoryEntity)

    @Query(
        """
        SELECT * FROM rate_history
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/mapper/ExchangeRatesEntityMapper.kt`

```kotlin
fun ExchangeRates.toEntity(baseCurrency: String): ExchangeRatesEntity = ExchangeRatesEntity(
```
_No KDoc at source._

```kotlin
fun ExchangeRatesEntity.toDomain(): ExchangeRates = ExchangeRates(
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/currency/mapper/RateHistoryEntityMapper.kt`

```kotlin
fun RateHistory.toEntity(): RateHistoryEntity = RateHistoryEntity(
```
_No KDoc at source._

```kotlin
fun RateHistoryEntity.toDomain(): RateHistory = RateHistory(
```
_No KDoc at source._

### `core/database/src/commonMain/kotlin/kpt/core/database/di/DatabaseModule.kt`

```kotlin
val DatabaseModule = module
```
Koin module that provides the `AppDatabase` instance and the framework-infra DAO singletons.

<details><summary>Used in the template — <code>core/database/src/desktopMain/kotlin/kpt/core/database/di/DatabaseModule.desktop.kt:19</code></summary>

```kotlin
// the desktop SQLite driver + IO dispatcher + fallback and resolves the OS data dir from
// appDatabaseNaming.desktopDirName.
actual val platformModule: Module = platformDatabaseModule<AppDatabase>(appDatabaseNaming)
```

</details>

```kotlin
expect val platformModule: Module
```
Platform-specific Koin module that provides the `AppDatabase` singleton.

### `core/database/src/commonMain/kotlin/kpt/core/database/di/ProjectDatabaseModule.kt`

```kotlin
val ProjectDatabaseModule = module
```
THE FORK'S database DI seam. Empty on the neutral template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
single { MyBackupScheduler(get<AppDatabase>()) }
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/economic/InterestRateSeriesDao.kt`

```kotlin
interface InterestRateSeriesDao
```
Data-access object for the `interest_rate_series` table. All reads return reactive `Flow`s; all writes are `suspend` one-shots.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/economic/impl/InterestRateSeriesStore.kt:55</code></summary>

```kotlin
    config: FredApiConfig,
    networkMonitor: NetworkMonitor,
    dao: InterestRateSeriesDao,
): Store<InterestRateSeriesKey, InterestRateSeries> {
    val validator = DefaultValidator.withTtl<InterestRateSeries>(AppStoreRegistry.Ttl.INTEREST_RATE_SERIES)
    return StoreFactory.createStore(
        fetcher = Fetcher.of { key: InterestRateSeriesKey ->
```

</details>

- `fun observeBySeriesId(seriesId: String): Flow<List<InterestRateSeriesEntity>>` — Observe all data-points for a given `seriesId`, ordered newest-first.
- `fun observeAll(): Flow<List<InterestRateSeriesEntity>>` — Observe all cached data-points across all series, ordered newest-first.
- `suspend fun upsertAll(entries: List<InterestRateSeriesEntity>)` — Insert or replace a batch of data-points (used during cache refresh).
- `suspend fun deleteBySeriesId(seriesId: String)` — Delete all cached data-points for a given series.
- `suspend fun upsert(entry: InterestRateSeriesEntity)` — Insert or replace a single data-point.
- `suspend fun deleteAll()` — Delete all cached data-points across all series.

### `core/database/src/commonMain/kotlin/kpt/core/database/economic/InterestRateSeriesEntity.kt`

```kotlin
data class InterestRateSeriesEntity(
```
Persistent row for a single data-point in an interest-rate time series. Stored in the `interest_rate_series` table (v10+).

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/economic/InterestRateSeriesDao.kt:36</code></summary>

```kotlin
     */
    @Query("SELECT * FROM interest_rate_series WHERE seriesId = :seriesId ORDER BY date DESC")
    fun observeBySeriesId(seriesId: String): Flow<List<InterestRateSeriesEntity>>

    /** Observe all cached data-points across all series, ordered newest-first. */
    @Query("SELECT * FROM interest_rate_series ORDER BY date DESC")
    fun observeAll(): Flow<List<InterestRateSeriesEntity>>
```

</details>

### `core/database/src/commonMain/kotlin/kpt/core/database/migrations/MigrationSpec8to10.kt`

```kotlin
class MigrationSpec8to10 : AutoMigrationSpec
```
Auto-migration spec for the demo showcase's v8 → v10 hop (collapsed from the never-shipped v8→9 + v9→10 path). Instructs Room to DROP the `samples` table that was present in v1–v8 and is no longer part of the schema.

### `core/database/src/commonMain/kotlin/kpt/core/database/watchlist/dao/WatchlistDao.kt`

```kotlin
interface WatchlistDao
```
Data-access object for the `personal_watchlist` table. Reads are reactive `Flow`s; writes are `suspend`. Standard Room 3 KMP shape.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/watchlist/impl/WatchlistRepositoryImpl.kt:45</code></summary>

```kotlin
    @FromStore(AppStoreIds.Watchlist) private val watchlistStore: Store<Unit, List<WatchlistItem>>,
    @FromStore(AppStoreIds.WatchlistMutable) private val watchlistWriteStore: MutableStore<String, WatchlistItem>,
    private val dao: WatchlistDao,
) : WatchlistRepository {

    override fun watchlistStream(scope: CoroutineScope): ScreenDataStream<List<WatchlistItem>> =
        watchlistStore.asScreenStream(
```

</details>

- `fun observeAll(): Flow<List<WatchlistEntity>>` — Observe the full watchlist, ordered by addition time (newest first).
- `fun observeContains(coinId: String): Flow<Boolean>` — Reactive in-membership check for a given coin — emits whenever the watchlist changes.
- `fun observeById(coinId: String): Flow<WatchlistEntity?>` — Observe a single watchlist row by coin id — the per-item write store's SourceOfTruth reader.
- `suspend fun insert(entry: WatchlistEntity)` — Add a coin to the watchlist. If already present, replaces the row (no-op effectively).
- `suspend fun delete(coinId: String)` — Remove a coin from the watchlist. No-op if absent.
- `suspend fun deleteAll()` — Clear the whole watchlist — used by the Store's `deleteAll` (logout cache clear).

### `core/database/src/commonMain/kotlin/kpt/core/database/watchlist/entity/WatchlistEntity.kt`

```kotlin
data class WatchlistEntity(
```
Persistent row representing a coin in the user's personal watchlist. Local-only: no Store5 caching layer, no remote sync.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/watchlist/dao/WatchlistDao.kt:31</code></summary>

```kotlin
    /** Observe the full watchlist, ordered by addition time (newest first). */
    @Query("SELECT * FROM personal_watchlist ORDER BY addedAtMs DESC")
    fun observeAll(): Flow<List<WatchlistEntity>>

    /** Reactive in-membership check for a given coin — emits whenever the watchlist changes. */
    @Query("SELECT EXISTS(SELECT 1 FROM personal_watchlist WHERE coinId = :coinId)")
    fun observeContains(coinId: String): Flow<Boolean>
```

</details>

---

_27 type(s), 76 function(s)/property(ies); 51 carry KDoc at source; 1 authored example(s); 25 live call site(s)._
<!-- api-docs:end -->
