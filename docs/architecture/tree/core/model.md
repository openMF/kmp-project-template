# `core/model`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_MODEL.md`
> **Measured:** 24 Kotlin files, 1 test files

## Principal types

`AlertDirection`, `AmortizationBreakdown`, `AmortizationRow`, `AuthState`, `BillCategory`, `BillReminder`, `CloudTodo`, `CoinDetail`, `CoinMarket`, `Country`, `CountryFlagUtils`, `DarkThemeConfig`, `EmiResult`, `ExchangeRates`  …and 19 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/model sha=114b9e0a8fb52539a66c3e076020f08b3f655b2c -->
## API reference

_Generated from `core/model` at tree `114b9e0a8fb5` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/model/src/commonMain/kotlin/kpt/core/model/alerts/PriceAlert.kt`

```kotlin
data class PriceAlert(
```
A price alert configured by the user for a specific coin.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/alerts/AlertsDataProviders.kt:24</code></summary>

```kotlin
/** Outbox for PriceAlert payloads — RoomSubmitOutbox writes to `framework_submit_drafts`. */
@DataProvider(qualifier = "outbox.priceAlert")
fun providePriceAlertOutbox(dao: DraftDao): SubmitOutbox<PriceAlert> =
    RoomSubmitOutbox(dao = dao, serializer = PriceAlert.serializer())

/**
 * Eager: starts watching online events at Koin start and retries pending alerts on reconnect.
```

</details>

```kotlin
enum class AlertDirection
```
Which way a price must cross the threshold to fire the alert (above or below).

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/alerts/AlertsReactiveInvalidationTest.kt:93</code></summary>

```kotlin
        id = id,
        coinId = "coin-$id",
        direction = AlertDirection.ABOVE,
        targetValue = 100.0,
        createdAtMs = 1_700_000_000_000L,
    )
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/banking/AmortizationRow.kt`

```kotlin
data class AmortizationRow(
```
A single monthly row in a reducing-balance amortization schedule. Every row satisfies: `payment` = `principal` + `interest` (within floating-point tolerance).

<details><summary>Used in the template — <code>core/domain/src/commonMain/kotlin/kpt/core/domain/calc/EmiCalculator.kt:76</code></summary>

```kotlin
 * pipe straight into a `LazyColumn` without guarding.
 */
fun amortizationSchedule(principal: Double, annualRatePercent: Double, tenureMonths: Int): List<AmortizationRow> {
    if (principal <= 0.0 || tenureMonths <= 0) return emptyList()
    val monthlyRate = annualRatePercent / 12.0 / 100.0
    val emi = computeEmi(principal, annualRatePercent, tenureMonths).emi
    val rows = ArrayList<AmortizationRow>(tenureMonths)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/banking/BillReminder.kt`

```kotlin
data class BillReminder(
```
A recurring (or one-time) bill the user wants to be reminded about. Purely local — no remote sync, no calendar export. The "reminder" itself is delivered by the in-app notification surface; this record is the declarative configuration.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/banking/BankingDataProviders.kt:42</code></summary>

```kotlin
 */
@DataProvider(qualifier = "outbox.billReminder")
fun provideBillReminderOutbox(dao: DraftDao): SubmitOutbox<BillReminder> =
    RoomSubmitOutbox(dao = dao, serializer = BillReminder.serializer())

/**
 * Offline outbox for saved loan-comparison scenarios.
```

</details>

```kotlin
enum class Recurrence
```
How often a bill repeats.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/banking/BillReminderRepositoryTest.kt:224</code></summary>

```kotlin
        dueDay: Int = 15,
        amount: Double = 100.0,
        recurrence: Recurrence = Recurrence.MONTHLY,
        category: BillCategory = BillCategory.UTILITIES,
        enabled: Boolean = true,
    ): BillReminder = BillReminder(
        id = id,
```

</details>

```kotlin
enum class BillCategory
```
Coarse spending category — drives icons and dashboard grouping.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/banking/BillReminderRepositoryTest.kt:225</code></summary>

```kotlin
        amount: Double = 100.0,
        recurrence: Recurrence = Recurrence.MONTHLY,
        category: BillCategory = BillCategory.UTILITIES,
        enabled: Boolean = true,
    ): BillReminder = BillReminder(
        id = id,
        name = "Bill $id",
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/banking/Loan.kt`

```kotlin
data class Loan(
```
A personal loan tracked by the user — purely local, no remote sync.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/banking/BankingDataProviders.kt:32</code></summary>

```kotlin
 */
@DataProvider(qualifier = "outbox.loan")
fun provideLoanOutbox(dao: DraftDao): SubmitOutbox<Loan> =
    RoomSubmitOutbox(dao = dao, serializer = Loan.serializer())

/**
 * Offline outbox for bill-reminder submissions — a Room-backed queue of drafts awaiting the network.
```

</details>

```kotlin
enum class LoanKind
```
High-level loan category used for grouping, icons, and analytics.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/banking/LoanRepositoryTest.kt:147</code></summary>

```kotlin
    private fun sampleLoan(
        id: String,
        kind: LoanKind = LoanKind.MORTGAGE,
        principal: Double = 250_000.0,
        principalRemaining: Double = 200_000.0,
        monthlyPayment: Double = 1_580.17,
        nextDueDate: LocalDate = LocalDate(2026, 6, 1),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/banking/LoanCalcScenario.kt`

```kotlin
data class LoanCalcScenario(
```
Wizard payload — the serialized snapshot of the loan-calc wizard at the current step. Serializable so the offline-resilient `DraftSubmitHandler` can persist it across process death and the next session can resume in-place.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/banking/BankingDataProviders.kt:49</code></summary>

```kotlin
 */
@DataProvider(qualifier = "outbox.loanCalcScenario")
fun provideLoanCalcScenarioOutbox(dao: DraftDao): SubmitOutbox<LoanCalcScenario> =
    RoomSubmitOutbox(dao = dao, serializer = LoanCalcScenario.serializer())

/**
 * Marker wrapper around the Loan syncer.
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/calc/AmortizationBreakdown.kt`

```kotlin
data class AmortizationBreakdown(
```
One amortization calculation: the per-installment `rows` and the `summary` totals.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/calc/AmortizationCalcRepository.kt:24</code></summary>

```kotlin
        params: AmortizationCalcParams,
        scope: CoroutineScope,
    ): ScreenDataStream<AmortizationBreakdown>
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/cloudtodo/CloudTodo.kt`

```kotlin
data class CloudTodo(
```
A cloud-synced todo — the toolkit's MUTABLE (offline-write) Store5 archetype showcase.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/cloudtodo/CloudTodoRepository.kt:28</code></summary>

```kotlin
     * @param scope scope the underlying Store shares — usually the ViewModel's, so the stream ends with the screen.
     */
    fun todoStream(id: Int, scope: CoroutineScope): ScreenDataStream<CloudTodo>

    /** Flips `completed` and writes back through the MutableStore (Updater → server; Bookkeeper on failure). */
    suspend fun toggleCompleted(todo: CloudTodo)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/crypto/CoinMarket.kt`

```kotlin
data class CoinMarket(
```
One row of the coin market list — the fields a list item renders, nothing more.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/crypto/CryptoRepository.kt:26</code></summary>

```kotlin
interface CryptoRepository {
    /** Streams the CoinGecko coin-markets list as a paged screen stream. */
    fun coinMarketsStream(scope: CoroutineScope, pageSize: Int = 20): PagingScreenStream<CoinMarket>

    /**
     * Offline-first stream for one coin's detail.
     *
```

</details>

```kotlin
data class CoinDetail(
```
A single coin's full detail, as shown on its own screen.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/crypto/CryptoRepository.kt:34</code></summary>

```kotlin
     * @param scope scope the underlying Store shares.
     */
    fun coinDetailStream(coinId: String, scope: CoroutineScope): ScreenDataStream<CoinDetail>
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/currency/Country.kt`

```kotlin
data class Country(
```
A currency-bearing country: ISO code, display name and the currency it uses.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/economic/SupportedCountries.kt:33</code></summary>

```kotlin

    /** Curated G20-plus list — alphabetised by display name for stable UX. */
    val list: List<Country> = listOf(
        Country(code = "AR", name = "Argentina", flagEmoji = "🇦🇷"),
        Country(code = "AU", name = "Australia", flagEmoji = "🇦🇺"),
        Country(code = "BR", name = "Brazil", flagEmoji = "🇧🇷"),
        Country(code = "CA", name = "Canada", flagEmoji = "🇨🇦"),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/currency/CountryFlagUtils.kt`

```kotlin
object CountryFlagUtils
```
Maps an ISO country code to its flag emoji, by offsetting ASCII letters into the Unicode regional-indicator range.

- `fun detectCountryFromPhoneNumber(phoneNumber: String): Country?` — Get country by phone number (auto-detect)
- `val cleanedNumber = phoneNumber.filter { it.isDigit() || it == '+' }`
- `val sortedCountries = worldCountries.sortedByDescending { it.phoneCode.length }`
- `val mobileRegex = Regex(country.mobilePattern)`
- `val landlineRegex = country.landlinePattern?.let { Regex(it) }`
- `fun getAllCountriesForSelection(): List<Country>` — Get all countries for dropdown/picker
- `fun getCountriesWithPopularFirst(): List<Country>` — Get popular countries first, then alphabetical
- `val popularCodes = listOf("US", "GB", "ES", "FR", "DE", "IT", "CA", "AU")`
- `val popular = worldCountries.filter { it.code in popularCodes }`
- `val others = worldCountries.filter { it.code !in popularCodes }`
- `fun searchCountries(query: String): List<Country>` — Search countries by name or code
- `val lowercaseQuery = query.lowercase()`
- `fun getFlagEmoji(countryCode: String): String?` — Get flag emoji by country code
- `fun getFlagResourceName(countryCode: String): String?` — Get flag resource name by country code

```kotlin
val worldCountries: List<Country> = listOf(
```
The static country/currency catalogue the picker offers. Static because it changes on the order of once a decade and a network round-trip for it would be absurd.

### `core/model/src/commonMain/kotlin/kpt/core/model/currency/ExchangeRates.kt`

```kotlin
data class ExchangeRates(
```
FX rates for one base currency on one day — the `rates` map is quote-code → rate.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/currency/CurrencyRepository.kt:42</code></summary>

```kotlin
        scope: CoroutineScope,
        fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
    ): ScreenDataStream<ExchangeRates>

    /**
     * Offline-first stream for a historical series, re-keying whenever [keyFlow] emits.
     *
```

</details>

```kotlin
data class RateHistoryKey(
```
Store key for a historical series: currency pair plus window length. The window is PART of the key, so widening it is a different key and a full re-fetch. That is the `read_windowed_series` contract — a widened window is not a page append.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/currency/CurrencyRepository.kt:53</code></summary>

```kotlin
     * @param scope scope the underlying Store shares.
     */
    fun rateHistoryStream(keyFlow: Flow<RateHistoryKey>, scope: CoroutineScope): ScreenDataStream<RateHistory>

    /**
     * Spot conversion-rate stream with a connectivity-driven [FetchPolicy]: [online] `true` →
     * [FetchPolicy.NETWORK_ONLY] (always fresh), `false` → [FetchPolicy.CACHE_ONLY] (no error
```

</details>

```kotlin
data class RateHistory(
```
A historical FX series for one pair over a date range.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/currency/CurrencyRepository.kt:53</code></summary>

```kotlin
     * @param scope scope the underlying Store shares.
     */
    fun rateHistoryStream(keyFlow: Flow<RateHistoryKey>, scope: CoroutineScope): ScreenDataStream<RateHistory>

    /**
     * Spot conversion-rate stream with a connectivity-driven [FetchPolicy]: [online] `true` →
     * [FetchPolicy.NETWORK_ONLY] (always fresh), `false` → [FetchPolicy.CACHE_ONLY] (no error
```

</details>

```kotlin
data class RatePoint(
```
One (date, rate) sample within a `RateHistory`.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/currency/mapper/RateHistoryEntityMapper.kt:40</code></summary>

```kotlin
    endDate = endDate,
    rates = Json.decodeFromString<List<RatePointPair>>(ratesJson)
        .map { RatePoint(it.date, it.value) },
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/economic/Country.kt`

```kotlin
data class Country(
```
Country reference for the Banking Utility Toolkit's macro-indicator screens. Distinct from `kpt.core.model.currency.Country` which is a phone-number-formatting model.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/economic/SupportedCountries.kt:33</code></summary>

```kotlin

    /** Curated G20-plus list — alphabetised by display name for stable UX. */
    val list: List<Country> = listOf(
        Country(code = "AR", name = "Argentina", flagEmoji = "🇦🇷"),
        Country(code = "AU", name = "Australia", flagEmoji = "🇦🇺"),
        Country(code = "BR", name = "Brazil", flagEmoji = "🇧🇷"),
        Country(code = "CA", name = "Canada", flagEmoji = "🇨🇦"),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/economic/InterestRateSeries.kt`

```kotlin
data class InterestRateSeries(
```
Domain representation of an interest-rate time series sourced from FRED (Federal Reserve Economic Data). The default consumer is the Banking Utility Toolkit's "B7 Interest Rate Tracker" screen.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/economic/EconomicRatesRepository.kt:36</code></summary>

```kotlin
        key: InterestRateSeriesKey,
        scope: CoroutineScope,
    ): ScreenDataStream<InterestRateSeries>

    /**
     * Stream observations for a parameter-flow whose value can change at
     * runtime (e.g. user switches series in the UI). Each new emission on
```

</details>

```kotlin
data class RateObservation(
```
Single observation in an interest-rate time series.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/fred/dto/FredObservationsDto.kt:103</code></summary>

```kotlin
     * Callers should treat `null` as "drop this row" — never as "value = 0".
     */
    fun toDomainOrNull(): RateObservation? {
        val numeric = if (value == NO_DATA_MARKER) null else value.toDoubleOrNull()
        val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
        return if (numeric != null && parsedDate != null) {
            RateObservation(date = parsedDate, value = numeric)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/economic/MacroIndicator.kt`

```kotlin
data class MacroIndicator(
```
Domain representation of a country-level macro indicator sourced from the World Bank Open Data API. The default consumer is the Banking Utility Toolkit's "B8 Country Macro Snapshot" screen.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/economic/MacroIndicatorsRepository.kt:43</code></summary>

```kotlin
        scope: CoroutineScope,
        fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
    ): ScreenDataStream<MacroIndicator>

    /**
     * Stream observations for a parameter-flow — typically driven by a UI
     * picker that lets the user switch countries / indicators.
```

</details>

```kotlin
data class IndicatorObservation(
```
Single year-level macro observation.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/worldbank/dto/WorldBankResponseDto.kt:65</code></summary>

```kotlin
            ?: countryCode
        val parsed = observations
            .map { IndicatorObservation(year = it.dateAsYear(), value = it.value) }
            .filter { it.year > 0 }
            .sortedBy { it.year }
        return MacroIndicator(
            countryCode = countryCode,
```

</details>

```kotlin
enum class IndicatorKind(val worldBankCode: String)
```
Macro indicators surfaced by the toolkit. Each kind maps to a stable World Bank indicator code via `worldBankCode`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/economic/impl/MacroIndicatorsRepositoryImpl.kt:42</code></summary>

```kotlin
 */
private val PINNED_MACRO_KEYS = listOf(
    MacroIndicatorKey(countryCode = "US", indicator = IndicatorKind.GDP),
    MacroIndicatorKey(countryCode = "IN", indicator = IndicatorKind.GDP),
)

/**
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/emi/EmiResult.kt`

```kotlin
data class EmiResult(
```
Output of an EMI calculation: the monthly instalment plus the totals it implies.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/emi/EmiCalculatorRepository.kt:26</code></summary>

```kotlin

    /** A [ScreenDataStream] over the EMI computed for [params]. */
    fun emiStream(params: EmiParams, scope: CoroutineScope): ScreenDataStream<EmiResult>
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/profile/ProfileInfo.kt`

```kotlin
data class ProfileInfo(
```
What the profile screen displays. In the template this carries only the app's display name — the demo profile is a local, signed-out placeholder.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/profile/ProfileRepository.kt:20</code></summary>

```kotlin

    /** A [ScreenDataStream] over the profile info. */
    fun profileStream(scope: CoroutineScope): ScreenDataStream<ProfileInfo>
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/AuthState.kt`

```kotlin
sealed class AuthState
```
Models high level auth state for the application.

### `core/model/src/commonMain/kotlin/kpt/core/model/user/DarkThemeConfig.kt`

```kotlin
enum class DarkThemeConfig(val configName: String, val osValue: Int)
```
The user's dark-mode preference: follow the system, or force light/dark. `osValue` maps to the platform's own night-mode constant, so the choice can be handed straight to the OS rather than re-interpreted per platform.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/UserDataRepository.kt:55</code></summary>

```kotlin

    /** Dark-mode preference as a stream. */
    val observeDarkThemeConfig: Flow<DarkThemeConfig>

    /** Whether to derive the palette from platform dynamic colour. */
    val observeDynamicColorPreference: Flow<Boolean>
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/LanguageConfig.kt`

```kotlin
enum class LanguageConfig(
```
Every language the app can be switched to, in the user's OWN language. GENERATED from core/registries/LOCALE_REGISTRY.yaml by `core/scripts/language-picker-sync.sh --write` — DO NOT HAND-EDIT.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/UserDataRepository.kt:52</code></summary>

```kotlin

    /** Selected language, re-emitting on change. */
    val observeLanguage: Flow<LanguageConfig>

    /** Dark-mode preference as a stream. */
    val observeDarkThemeConfig: Flow<DarkThemeConfig>
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/ThemeBrand.kt`

```kotlin
enum class ThemeBrand(val brandName: String)
```
The selected colour brand. A fork extends this to offer its own palettes.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/UserDataRepository.kt:67</code></summary>

```kotlin

    /** Persists the colour brand. */
    suspend fun setThemeBrand(themeBrand: ThemeBrand)

    /** Persists the dark-mode preference. */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/UnlockType.kt`

```kotlin
enum class UnlockType
```
How the user unlocks the app — passcode or biometric.

### `core/model/src/commonMain/kotlin/kpt/core/model/user/UserData.kt`

```kotlin
data class UserData(
```
Everything the app persists about the current user — theme, language, onboarding progress and the lock state.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/UserDataRepository.kt:31</code></summary>

```kotlin

    /** The whole preference aggregate as hot state, so a screen has a value at first composition. */
    val userData: StateFlow<UserData>

    /**
     * Store5-backed read of the same preferences, as a [ScreenDataStream].
     *
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/watchlist/WatchlistItem.kt`

```kotlin
data class WatchlistItem(
```
Domain model for a personal-watchlist row (the `read_local_list` demo).

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/watchlist/WatchlistRepository.kt:29</code></summary>

```kotlin

    /** Observe the watchlist as a Store5-backed [ScreenDataStream] (offline-local, newest-added first). */
    fun watchlistStream(scope: CoroutineScope): ScreenDataStream<List<WatchlistItem>>

    /** Reactive in-membership check. Used by the star toggle to render filled/outline. */
    fun contains(coinId: String): Flow<Boolean>
```

</details>

---

_34 type(s), 15 function(s)/property(ies); 41 carry KDoc at source; 0 authored example(s); 31 live call site(s)._
<!-- api-docs:end -->
