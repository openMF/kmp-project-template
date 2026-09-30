# `core/data`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATA.md`
> **Measured:** 62 Kotlin files, 21 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@RepositoryBinding` | `data-ksp` | di/GeneratedRepositoryBindings |
| `@DataProvider` | `data-ksp` | di/GeneratedRepositoryBindings, config/AppOutboxQualifiers |
| `@FromStore` | `data-ksp` |  |
| `@FromQualifier` | `data-ksp` |  |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

## Principal types

`AlertsRepository`, `AlertsRepositoryImpl`, `AmortizationCalcRepository`, `AmortizationCalcRepositoryImpl`, `BillReminderRepository`, `BillReminderRepositoryImpl`, `BillReminderSubmitSyncer`, `CloudTodoRepository`, `CloudTodoRepositoryImpl`, `CryptoRepository`, `CryptoRepositoryImpl`, `CurrencyRepository`, `CurrencyRepositoryImpl`, `EconomicRatesRepository`  …and 20 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/data sha=4f801e6c686d8bb540918be22bfe792b945bb981 -->
## API reference

_Generated from `core/data` at tree `4f801e6c686d` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/data/src/commonMain/kotlin/kpt/core/data/alerts/AlertsDataProviders.kt`

```kotlin
fun providePriceAlertOutbox(dao: DraftDao): SubmitOutbox<PriceAlert> =
```
Outbox for PriceAlert payloads — RoomSubmitOutbox writes to `framework_submit_drafts`.

```kotlin
fun providePriceAlertSubmitSyncer(
```
Eager: starts watching online events at Koin start and retries pending alerts on reconnect.

### `core/data/src/commonMain/kotlin/kpt/core/data/alerts/AlertsRepository.kt`

```kotlin
interface AlertsRepository
```
Repository for the Price Alerts feature. Read side: observable list of committed alerts (from the Store-backed `AlertsRepository.alertsStream`).

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/alerts/AlertsDataProviders.kt:39</code></summary>

```kotlin
    @FromQualifier("outbox.priceAlert") outbox: SubmitOutbox<PriceAlert>,
    networkMonitor: NetworkMonitor,
    repository: AlertsRepository,
): OfflineSubmitSyncer<PriceAlert, PriceAlert> = OfflineSubmitSyncer<PriceAlert, PriceAlert>(
    scope = scope,
    outbox = outbox,
    networkStatusFlow = networkMonitor.networkStatus,
```

</details>

- `fun alertsStream(scope: CoroutineScope): ScreenDataStream<List<PriceAlert>>` — Reactive list of committed alerts as a Store5-backed `ScreenDataStream` (offline-local).
- `suspend fun submitAlert(alert: PriceAlert): PriceAlert` — Direct API submit — used by `DraftSubmitHandler`'s block parameter and by `OfflineSubmitSyncer` for reconnect retries. Throws on failure; the handler / syncer route the error to the outbox.
- `suspend fun deleteAlert(id: String)` — Delete by id.

### `core/data/src/commonMain/kotlin/kpt/core/data/banking/BankingDataProviders.kt`

```kotlin
fun provideLoanOutbox(dao: DraftDao): SubmitOutbox<Loan> =
```
Banking's submit-path wiring, declared where the feature lives.

```kotlin
fun provideBillReminderOutbox(dao: DraftDao): SubmitOutbox<BillReminder> =
```
Offline outbox for bill-reminder submissions — a Room-backed queue of drafts awaiting the network.

```kotlin
fun provideLoanCalcScenarioOutbox(dao: DraftDao): SubmitOutbox<LoanCalcScenario> =
```
Offline outbox for saved loan-comparison scenarios.

```kotlin
class LoanSubmitSyncer internal constructor(
```
Marker wrapper around the Loan syncer. Exists so Koin resolves this binding by a unique type — a bare `OfflineSubmitSyncer<*, *>` erases to one runtime class across every payload and would collide with the other syncers.

```kotlin
class BillReminderSubmitSyncer internal constructor(
```
Marker wrapper around the BillReminder syncer — same erasure reason as `LoanSubmitSyncer`.

```kotlin
fun provideLoanSubmitSyncer(
```
Eager: a syncer built only on first injection never starts watching for reconnects, so the backlog it exists to drain is never drained.

```kotlin
fun provideBillReminderSubmitSyncer(
```
Drains the bill-reminder outbox whenever connectivity returns.

### `core/data/src/commonMain/kotlin/kpt/core/data/banking/BillReminderRepository.kt`

```kotlin
interface BillReminderRepository
```
User's bill reminders — purely local persistence, no remote sync. Backs the B4 Bill Reminders feature. Reads are reactive `Flow`s; writes are `suspend`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/banking/BankingDataProviders.kt:106</code></summary>

```kotlin
    @FromQualifier("outbox.billReminder") outbox: SubmitOutbox<BillReminder>,
    networkMonitor: NetworkMonitor,
    repository: BillReminderRepository,
): BillReminderSubmitSyncer = BillReminderSubmitSyncer(
    syncer = OfflineSubmitSyncer<BillReminder, BillReminder>(
        scope = scope,
        outbox = outbox,
```

</details>

- `fun billRemindersStream(scope: CoroutineScope): ScreenDataStream<List<BillReminder>>` — Observe all reminders as a Store5-backed `ScreenDataStream` (offline-local) for read screens.
- `fun billReminderDetailStream(id: String, scope: CoroutineScope): ScreenDataStream<BillReminder>` — Store-backed detail read for ONE reminder — the read path an edit form hydrates from. Mirrors `kpt.core.data.banking.LoanRepository.loanDetailStream`; absent id → Empty.
- `fun observeUpcoming(maxDays: Int): Flow<List<BillReminder>>` — Observe enabled bill reminders whose `dueDay` falls within the next `maxDays` starting from today. The window wraps across the month boundary — a reminder on day 3 *is* "upcoming" when today is day 30 and `maxDays` >= 4.
- `suspend fun upsert(bill: BillReminder)` — Insert-or-replace. Idempotent.
- `suspend fun delete(id: String)` — Delete by id. No-op if absent.
- `fun observeTotalUpcomingAmount(maxDays: Int): Flow<Double>` — Sum of `BillReminder.amount` across upcoming reminders within the same window semantics as `observeUpcoming`. Powers the dashboard "Due in next X days" tile.

### `core/data/src/commonMain/kotlin/kpt/core/data/banking/LoanRepository.kt`

```kotlin
interface LoanRepository
```
User's personal loan portfolio — purely local persistence, no remote sync. Backs the B1 Loan Tracker feature. Reads are reactive `Flow`s; writes are `suspend` and go through `upsert` / `delete`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/banking/BankingDataProviders.kt:82</code></summary>

```kotlin
    @FromQualifier("outbox.loan") outbox: SubmitOutbox<Loan>,
    networkMonitor: NetworkMonitor,
    repository: LoanRepository,
): LoanSubmitSyncer = LoanSubmitSyncer(
    syncer = OfflineSubmitSyncer<Loan, Loan>(
        scope = scope,
        outbox = outbox,
```

</details>

- `fun loansStream(scope: CoroutineScope): ScreenDataStream<List<Loan>>` — Observe all loans as a Store5-backed `ScreenDataStream` (offline-local) for read screens.
- `fun loanDetailStream(id: String, scope: CoroutineScope): ScreenDataStream<Loan>` — Observe a single loan as a `ScreenDataStream` (absent id → Empty) for detail/projection screens.
- `suspend fun upsert(loan: Loan)` — Insert-or-replace by `Loan.id`. Idempotent.
- `suspend fun delete(id: String)` — Delete by id. No-op if absent.

### `core/data/src/commonMain/kotlin/kpt/core/data/calc/AmortizationCalcRepository.kt`

```kotlin
interface AmortizationCalcRepository
```
Read surface for the amortization calculator (`calculator_multi`, MEMORY_ONLY).

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/calc/impl/AmortizationCalcRepositoryImpl.kt:31</code></summary>

```kotlin
 * gate on and nothing to revalidate.
 */
@RepositoryBinding(binds = AmortizationCalcRepository::class)
internal class AmortizationCalcRepositoryImpl(
    @FromStore(AppStoreIds.AmortizationCalc) private val store: Store<AmortizationCalcParams, AmortizationBreakdown>,
) : AmortizationCalcRepository {
```

</details>

- `fun breakdownStream(` — A `ScreenDataStream` over the breakdown computed for `params`.

### `core/data/src/commonMain/kotlin/kpt/core/data/cloudtodo/CloudTodoDataProviders.kt`

```kotlin
fun provideCloudTodoBookkeeper(dao: BookkeeperDao): Bookkeeper<CloudTodoKey> =
```
MUTABLE (offline-write) archetype wiring. The bookkeeper records failed writes for retry-on-reconnect and is injected into the core/store MutableStore via Koin.

```kotlin
fun provideCloudTodoSyncOrchestrator(
```
Eager: drains the cloud-todo write backlog on the offline -> online edge.

### `core/data/src/commonMain/kotlin/kpt/core/data/cloudtodo/CloudTodoRepository.kt`

```kotlin
interface CloudTodoRepository
```
Read + offline-write surface over the cloud-todo `org.mobilenativefoundation.store.store5.MutableStore`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/cloudtodo/CloudTodoDataProviders.kt:53</code></summary>

```kotlin
    bookkeeper: Bookkeeper<CloudTodoKey>,
    todoDao: CloudTodoDao,
    repository: CloudTodoRepository,
    crashReporter: CrashReporter?,
): CloudTodoSyncOrchestrator = CloudTodoSyncOrchestrator(
    scope = scope,
    networkMonitor = networkMonitor,
```

</details>

- `fun todoStream(id: Int, scope: CoroutineScope): ScreenDataStream<CloudTodo>` — Offline-first stream for one todo.
- `suspend fun toggleCompleted(todo: CloudTodo)` — Flips `completed` and writes back through the MutableStore (Updater → server; Bookkeeper on failure).
- `suspend fun completeOnline(todo: CloudTodo): kpt.core.base.store.mutation.MutationResult<CloudTodo>` — Mark `todo` complete with the `OnlineRequired` policy — the network PUT is awaited first and the server record ingested; when offline the mutation is `kpt.core.base.store.mutation.MutationResult.Blocked` and nothing is written locally (unlike the optimistic `toggleCompleted`). The demo reference for network-first mutations that must not show an unconfirmed local state (payments, approvals).

### `core/data/src/commonMain/kotlin/kpt/core/data/cloudtodo/impl/CloudTodoRepositoryImpl.kt`

```kotlin
class CloudTodoRepositoryImpl(
```
The MUTABLE (offline-write) Store5 archetype, wired end to end: reads stream from Room, a toggle writes through the Updater, and a write that fails offline is left to the Bookkeeper to retry.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/cloudtodo/CloudTodoRepositoryTest.kt:47</code></summary>

```kotlin
    private val bookkeeper = FakeBookkeeper<CloudTodoKey>()

    private fun repo(online: Boolean) = CloudTodoRepositoryImpl(
        readStore = provideCloudTodoReadStore(api, dao),
        writeStore = provideCloudTodoStore(api, dao, bookkeeper),
        gateway = testMutationGateway(isOnline = online),
    )
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/crypto/CryptoRepository.kt`

```kotlin
interface CryptoRepository
```
Read surface for CoinGecko-backed crypto data. Returns `ScreenDataStream` / `PagingScreenStream`, never raw values: loading, empty, error and no-network are decided in the Store, so no screen re-derives them.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/crypto/impl/CryptoRepositoryImpl.kt:30</code></summary>

```kotlin
 * Default [CryptoRepository], wrapping the coin-market and coin-detail Stores.
 */
@RepositoryBinding(binds = CryptoRepository::class)
class CryptoRepositoryImpl(
    @FromStore(AppStoreIds.CoinMarkets) private val coinMarketsStore: Store<PageKey, List<CoinMarket>>,
    @FromStore(AppStoreIds.CoinDetail) private val coinDetailStore: Store<String, CoinDetail>,
) : CryptoRepository {
```

</details>

- `fun coinMarketsStream(scope: CoroutineScope, pageSize: Int = 20): PagingScreenStream<CoinMarket>` — Streams the CoinGecko coin-markets list as a paged screen stream.
- `fun coinDetailStream(coinId: String, scope: CoroutineScope): ScreenDataStream<CoinDetail>` — Offline-first stream for one coin's detail.

### `core/data/src/commonMain/kotlin/kpt/core/data/crypto/impl/CryptoRepositoryImpl.kt`

```kotlin
class CryptoRepositoryImpl(
```
Default `CryptoRepository`, wrapping the coin-market and coin-detail Stores.

### `core/data/src/commonMain/kotlin/kpt/core/data/currency/CurrencyRepository.kt`

```kotlin
interface CurrencyRepository : Syncable
```
Repository surface for exchange rates + historical rate data.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/currency/impl/CurrencyRepositoryImpl.kt:46</code></summary>

```kotlin
 * Default `CurrencyRepository` over the Frankfurter-backed exchange-rate and history Stores.
 */
@RepositoryBinding(binds = CurrencyRepository::class)
class CurrencyRepositoryImpl(
    @FromStore(AppStoreIds.ExchangeRates) private val exchangeRatesStore: Store<String, ExchangeRates>,
    @FromStore(AppStoreIds.RateHistory) private val rateHistoryStore: Store<RateHistoryKey, RateHistory>,
    @FromStore(AppStoreIds.SpotRate) private val spotRateStore: Store<String, ExchangeRates>,
```

</details>

- `fun exchangeRatesStream(` — Stream of exchange rates for `baseCurrency`.
- `fun rateHistoryStream(keyFlow: Flow<RateHistoryKey>, scope: CoroutineScope): ScreenDataStream<RateHistory>` — Offline-first stream for a historical series, re-keying whenever `keyFlow` emits. The key arrives as a Flow rather than a value so a pair or window change re-fetches without the screen rebuilding the stream.
- `fun spotRateStream(baseCurrency: String, online: Boolean, scope: CoroutineScope): ScreenDataStream<ExchangeRates>` — Spot conversion-rate stream with a connectivity-driven `FetchPolicy`: `online` `true` → `FetchPolicy.NETWORK_ONLY` (always fresh), `false` → `FetchPolicy.CACHE_ONLY` (no error flicker offline).

### `core/data/src/commonMain/kotlin/kpt/core/data/currency/impl/CurrencyRepositoryImpl.kt`

```kotlin
class CurrencyRepositoryImpl(
```
Default `CurrencyRepository` over the Frankfurter-backed exchange-rate and history Stores.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/currency/CurrencyRepositorySyncWithTest.kt:58</code></summary>

```kotlin
            .build()

        val repo = CurrencyRepositoryImpl(
            exchangeRatesStore = exchangeRatesStore,
            rateHistoryStore = rateHistoryStore,
            spotRateStore = spotRateStore,
        )
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/di/OutboxQualifiers.kt`

```kotlin
object OutboxQualifiers
```
Named qualifiers for the app's `SubmitOutbox<*>` bindings. Why qualifiers? Koin indexes `single<T>` definitions by `T::class` (the raw type), not by the full `KType`.

<details><summary>Example</summary>

```kotlin
val MyThing = named("outbox.myThing")
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/di/OutboxBindingVerifyTest.kt:86</code></summary>

```kotlin
            match,
            "GeneratedRepositoryBindings must register a SubmitOutbox<*> single<> under qualifier $expected — " +
                "missing or mis-qualified binding. See OutboxQualifiers KDoc.",
        )
        // Sanity — the qualifier value matches what callers use (e.g. `get(qualifier = X)`).
        assertTrue(
            match.beanDefinition.qualifier == expected,
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/di/ProjectRepositoryModule.kt`

```kotlin
val ProjectRepositoryModule = module
```
THE FORK'S repository DI seam. Empty on the neutral template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
single<MyRepository> { MyRepositoryImpl(get(), get()) }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt`

```kotlin
val DataModule = module
```
DataModule — the INFRA-ONLY (framework) data aggregator, `owner: template` (E1 / C1).

```kotlin
expect val platformModule: Module
```
Per-target repository bindings supplied by each `actual` — the pieces that cannot be expressed in common code.

### `core/data/src/commonMain/kotlin/kpt/core/data/economic/EconomicRatesRepository.kt`

```kotlin
interface EconomicRatesRepository
```
Repository surface for FRED-sourced interest-rate time series.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/economic/impl/EconomicRatesRepositoryImpl.kt:28</code></summary>

```kotlin
 * Default `EconomicRatesRepository` over the FRED-backed interest-rate series Store.
 */
@RepositoryBinding(binds = EconomicRatesRepository::class)
class EconomicRatesRepositoryImpl(
    @FromStore(AppStoreIds.InterestRateSeries)
    private val interestRateSeriesStore: Store<InterestRateSeriesKey, InterestRateSeries>,
) : EconomicRatesRepository {
```

</details>

- `fun interestRateSeriesStream(` — Stream observations for a single static series.
- `fun interestRateSeriesStream(` — Stream observations for a parameter-flow whose value can change at runtime (e.g. user switches series in the UI). Each new emission on `keyFlow` triggers a fresh load (cache-first).

### `core/data/src/commonMain/kotlin/kpt/core/data/economic/MacroIndicatorsRepository.kt`

```kotlin
interface MacroIndicatorsRepository : Syncable
```
Repository surface for World Bank macro-indicator series.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/economic/impl/MacroIndicatorsRepositoryImpl.kt:49</code></summary>

```kotlin
 * Default `MacroIndicatorsRepository` over the World Bank macro-indicator Store.
 */
@RepositoryBinding(binds = MacroIndicatorsRepository::class)
class MacroIndicatorsRepositoryImpl(
    @FromStore(AppStoreIds.MacroIndicator) private val macroIndicatorStore: Store<MacroIndicatorKey, MacroIndicator>,
) : MacroIndicatorsRepository {
```

</details>

- `fun macroIndicatorStream(` — Stream observations for a single static (country, indicator) pair.
- `fun macroIndicatorStream(keyFlow: Flow<MacroIndicatorKey>, scope: CoroutineScope): ScreenDataStream<MacroIndicator>` — Stream observations for a parameter-flow — typically driven by a UI picker that lets the user switch countries / indicators.

### `core/data/src/commonMain/kotlin/kpt/core/data/economic/SupportedCountries.kt`

```kotlin
object SupportedCountries
```
Curated list of countries the Banking Utility Toolkit's macro-snapshot screen ships with out of the box. Selection criteria: - The G20 plus a handful of other large economies the template's adopters are most likely to demo against.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/economic/SupportedCountriesTest.kt:35</code></summary>

```kotlin
        // protect against accidental whole-world inserts in a curated list.
        assertTrue(
            SupportedCountries.list.size in 20..250,
            "Expected ~30 curated countries, got ${SupportedCountries.list.size}",
        )
    }
```

</details>

- `val list: List<Country> = listOf(` — Curated G20-plus list — alphabetised by display name for stable UX.
- `fun findByCode(code: String): Country? = byCode[code.trim().uppercase()]` — Lookup a country by ISO code. Case-insensitive — the picker, intent deeplinks, or saved navigation args may all carry mixed case.
- `fun search(query: String): List<Country>` — Filter the list by a free-text query. Matches a country's ISO code as a prefix OR its localised name as a substring, both case-insensitively.
- `val trimmed = query.trim()`
- `val upper = trimmed.uppercase()`

### `core/data/src/commonMain/kotlin/kpt/core/data/economic/impl/EconomicRatesRepositoryImpl.kt`

```kotlin
class EconomicRatesRepositoryImpl(
```
Default `EconomicRatesRepository` over the FRED-backed interest-rate series Store.

### `core/data/src/commonMain/kotlin/kpt/core/data/economic/impl/MacroIndicatorsRepositoryImpl.kt`

```kotlin
class MacroIndicatorsRepositoryImpl(
```
Default `MacroIndicatorsRepository` over the World Bank macro-indicator Store.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/economic/MacroIndicatorsRepositorySyncWithTest.kt:54</code></summary>

```kotlin
            .build()

        val repo = MacroIndicatorsRepositoryImpl(
            macroIndicatorStore = macroIndicatorStore,
        )
        val synchronizer = RecordingSynchronizer()
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/emi/EmiCalculatorRepository.kt`

```kotlin
interface EmiCalculatorRepository
```
Read surface for the EMI calculator (`calculator_pure`, MEMORY_ONLY). The repository owns the Store and exposes exactly one function, matching the read-path contract every other feature follows — the ViewModel never touches a `Store`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/emi/impl/EmiCalculatorRepositoryImpl.kt:32</code></summary>

```kotlin
 * makes a repeated parameter set free; a new one computes once.
 */
@RepositoryBinding(binds = EmiCalculatorRepository::class)
internal class EmiCalculatorRepositoryImpl(
    @FromStore(AppStoreIds.Emi) private val emiStore: Store<EmiParams, EmiResult>,
) : EmiCalculatorRepository {
```

</details>

- `fun emiStream(params: EmiParams, scope: CoroutineScope): ScreenDataStream<EmiResult>` — A `ScreenDataStream` over the EMI computed for `params`.

### `core/data/src/commonMain/kotlin/kpt/core/data/profile/ProfileRepository.kt`

```kotlin
interface ProfileRepository
```
Read surface for the profile screen (`static_content`, MEMORY_ONLY).

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/profile/impl/ProfileRepositoryImpl.kt:30</code></summary>

```kotlin
 * changes this policy (and the store's fetcher); nothing above this line moves.
 */
@RepositoryBinding(binds = ProfileRepository::class)
internal class ProfileRepositoryImpl(
    @FromStore(AppStoreIds.Profile) private val profileStore: Store<Unit, ProfileInfo>,
) : ProfileRepository {
```

</details>

- `fun profileStream(scope: CoroutineScope): ScreenDataStream<ProfileInfo>` — A `ScreenDataStream` over the profile info.

### `core/data/src/commonMain/kotlin/kpt/core/data/user/AuthTokenBridge.kt`

```kotlin
fun provideAuthTokenSource(preferences: UserPreferencesRepository): AuthTokenSource =
```
Binds the network's `AuthTokenSource` port to where the credential actually lives.

```kotlin
fun provideAuthHeaderBridge(
```
Eager: starts collecting the credential at graph construction. `createdAtStart` is load-bearing. Built lazily, this would construct on first injection — and nothing injects it, because its whole job is a side effect.

### `core/data/src/commonMain/kotlin/kpt/core/data/user/LogoutEvent.kt`

```kotlin
data class LogoutEvent(
```
Result class to share the `loggedOutUserId` of a user that was successfully logged out.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/UserLogoutManager.kt:24</code></summary>

```kotlin
     * Observable flow of [LogoutEvent]s
     */
    val logoutEventFlow: SharedFlow<LogoutEvent>

    /**
     * Completely logs out the given [userId], removing all data. The [reason] indicates why the
     * user is being logged out.
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/user/LogoutReason.kt`

```kotlin
sealed class LogoutReason
```
Indicates the reason that the user is being logged out.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/UserLogoutManager.kt:30</code></summary>

```kotlin
     * user is being logged out.
     */
    fun logout(userId: Long, reason: LogoutReason)

    /**
     * Partially logs out the given [userId]. All data for the given [userId] will be removed with
     * the exception of basic account data. The [reason] indicates why the user is being logged out.
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/user/UserDataRepository.kt`

```kotlin
interface UserDataRepository
```
Repository interface for managing user preferences with reactive capabilities. This interface provides reactive access to user preferences including theme settings, dark mode configuration, and dynamic color preferences.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/impl/UserDataRepositoryImpl.kt:37</code></summary>

```kotlin
 * setting change behind every other for no benefit.
 */
@RepositoryBinding(binds = UserDataRepository::class)
class UserDataRepositoryImpl(
    private val preferencesRepository: UserPreferencesRepository,
    @FromStore(AppStoreIds.UserData) private val userDataStore: Store<Unit, UserData>,
) : UserDataRepository {
```

</details>

- `val userData: StateFlow<UserData>` — The whole preference aggregate as hot state, so a screen has a value at first composition.
- `fun userDataStream(scope: CoroutineScope): ScreenDataStream<UserData>` — Store5-backed read of the same preferences, as a `ScreenDataStream`. `userData` stays for the many call sites that just want the current value (auth, theme bootstrap).
- `val authToken: String?` — The stored credential, or null when signed out. Synchronous read; prefer a stream when the value can change under you.
- `val passcode: String` — The app-lock passcode.
- `val observeLanguage: Flow<LanguageConfig>` — Selected language, re-emitting on change.
- `val observeDarkThemeConfig: Flow<DarkThemeConfig>` — Dark-mode preference as a stream.
- `val observeDynamicColorPreference: Flow<Boolean>` — Whether to derive the palette from platform dynamic colour.
- `val observeScreenCapturePreference: Flow<Boolean>` — Whether screenshots and screen recording are permitted.
- `suspend fun setLanguage(language: LanguageConfig)` — Persists the app language.
- `suspend fun setThemeBrand(themeBrand: ThemeBrand)` — Persists the colour brand.
- `suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)` — Persists the dark-mode preference.
- `suspend fun setDynamicColorPreference(useDynamicColor: Boolean)` — Persists whether to use platform dynamic colour.
- `suspend fun setIsAuthenticated(isAuthenticated: Boolean)` — Persists session presence.
- `suspend fun setIsUnlocked(isUnlocked: Boolean)` — Persists whether the app-lock is currently satisfied.
  _…more members; read the file._

### `core/data/src/commonMain/kotlin/kpt/core/data/user/UserLogoutManager.kt`

```kotlin
interface UserLogoutManager
```
Coordinates logout across the app: wipes local state and notifies observers. Centralised so "what must be cleared on logout" is answered once. A feature clearing its own slice is how one gets missed.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt:66</code></summary>

```kotlin
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<UserLogoutManager> { UserLogoutManagerImpl(get(), get(), get()) }
}

/**
 * Per-target repository bindings supplied by each `actual` — the pieces that cannot be expressed in
```

</details>

- `val logoutEventFlow: SharedFlow<LogoutEvent>` — Observable flow of `LogoutEvent`s
- `fun logout(userId: Long, reason: LogoutReason)` — Completely logs out the given `userId`, removing all data. The `reason` indicates why the user is being logged out.
- `fun softLogout(userId: Long, reason: LogoutReason)` — Partially logs out the given `userId`. All data for the given `userId` will be removed with the exception of basic account data. The `reason` indicates why the user is being logged out.

### `core/data/src/commonMain/kotlin/kpt/core/data/user/impl/UserDataRepositoryImpl.kt`

```kotlin
class UserDataRepositoryImpl(
```
Default `UserDataRepository`.

### `core/data/src/commonMain/kotlin/kpt/core/data/user/impl/UserLogoutManagerImpl.kt`

```kotlin
class UserLogoutManagerImpl(
```
Default `UserLogoutManager`: clears every Store cache and draft row, then emits the logout event. Cache-clearing is the load-bearing half — a Store that survives logout would serve the previous user's data to the next one.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt:66</code></summary>

```kotlin
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<UserLogoutManager> { UserLogoutManagerImpl(get(), get(), get()) }
}

/**
 * Per-target repository bindings supplied by each `actual` — the pieces that cannot be expressed in
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/util/SharedFlowExtensions.kt`

```kotlin
fun <T> bufferedMutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = MutableSharedFlow(
```
Creates a `MutableSharedFlow` with a buffer of `Int.MAX_VALUE` and the given `replay` count.

### `core/data/src/commonMain/kotlin/kpt/core/data/watchlist/WatchlistRepository.kt`

```kotlin
interface WatchlistRepository
```
User's personal watchlist of coins — purely local persistence, no remote sync.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/watchlist/impl/WatchlistRepositoryImpl.kt:41</code></summary>

```kotlin
 * [contains] membership read.
 */
@RepositoryBinding(binds = WatchlistRepository::class)
internal class WatchlistRepositoryImpl(
    @FromStore(AppStoreIds.Watchlist) private val watchlistStore: Store<Unit, List<WatchlistItem>>,
    @FromStore(AppStoreIds.WatchlistMutable) private val watchlistWriteStore: MutableStore<String, WatchlistItem>,
    private val dao: WatchlistDao,
```

</details>

- `fun watchlistStream(scope: CoroutineScope): ScreenDataStream<List<WatchlistItem>>` — Observe the watchlist as a Store5-backed `ScreenDataStream` (offline-local, newest-added first).
- `fun contains(coinId: String): Flow<Boolean>` — Reactive in-membership check. Used by the star toggle to render filled/outline.
- `suspend fun add(coinId: String)` — Add a coin. Idempotent — re-adds touch the addedAtMs timestamp to "now".
- `suspend fun remove(coinId: String)` — Remove a coin. Idempotent — no-op if not present.

---

_27 type(s), 69 function(s)/property(ies); 94 carry KDoc at source; 2 authored example(s); 22 live call site(s)._
<!-- api-docs:end -->
