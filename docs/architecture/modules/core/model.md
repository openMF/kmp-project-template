# `core/model`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_MODEL.md`
> **Measured:** 24 Kotlin files, 1 test files

## Principal types

`AlertDirection`, `AmortizationBreakdown`, `AmortizationRow`, `AuthState`, `BillCategory`, `BillReminder`, `CloudTodo`, `CoinDetail`, `CoinMarket`, `Country`, `CountryFlagUtils`, `DarkThemeConfig`, `EmiResult`, `ExchangeRates`  …and 19 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/model sha=ee39068ed853b329912887ee2112944876713647 -->
## API reference

_Generated from `core/model` at tree `ee39068ed853` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/model/src/commonMain/kotlin/kpt/core/model/alerts/PriceAlert.kt`

```kotlin
data class PriceAlert(
```
A price alert configured by the user for a specific coin.

<details><summary>Used in the template — <code>feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/ui/AlertsListScreen.kt:92</code></summary>

```kotlin
        ) { alerts, _ ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = alerts, key = PriceAlert::id) { alert ->
                    AlertRow(alert = alert, onDelete = { viewModel.onDelete(alert.id) })
                }
            }
        }
```

</details>

```kotlin
enum class AlertDirection
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/ui/AlertsListScreen.kt:107</code></summary>

```kotlin
) {
    val directionLabel = when (alert.direction) {
        AlertDirection.ABOVE -> stringResource(Res.string.screens_alerts_row_above)
        AlertDirection.BELOW -> stringResource(Res.string.screens_alerts_row_below)
        AlertDirection.PCT_CHANGE -> stringResource(Res.string.screens_alerts_row_pct_change)
    }
    ListItem(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/banking/AmortizationRow.kt`

```kotlin
data class AmortizationRow(
```
A single monthly row in a reducing-balance amortization schedule. Every row satisfies: `payment` = `principal` + `interest` (within floating-point tolerance).

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/di/CalculatorsModule.kt:61</code></summary>

```kotlin
                    params.tenureMonths,
                ).map { row ->
                    AmortizationRow(
                        month = row.installmentNumber,
                        payment = row.principalPaid + row.interestPaid,
                        principal = row.principalPaid,
                        interest = row.interestPaid,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/banking/BillReminder.kt`

```kotlin
data class BillReminder(
```
A recurring (or one-time) bill the user wants to be reminded about. Purely local — no remote sync, no calendar export. The "reminder" itself is delivered by the in-app notification surface; this record is the declarative configuration.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsPreview.kt:88</code></summary>

```kotlin
            onPrune = {},
            conflicts = listOf(
                ConflictEntry("c1", "BillReminder", "rent-2026-08", "{}", "{}", null, 0),
                ConflictEntry("c2", "Loan", "home-loan", "{}", "{}", null, 0),
            ),
            onAcceptServer = {},
            onRetryLocal = {},
```

</details>

```kotlin
enum class Recurrence
```
How often a bill repeats.

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/demo/ui/HomeViewModelTest.kt:359</code></summary>

```kotlin
        amount = 75.0,
        dueDay = 15,
        recurrence = Recurrence.MONTHLY,
        category = BillCategory.UTILITIES,
        enabled = true,
        reminderDaysBefore = 1,
        createdAtMs = 0,
```

</details>

```kotlin
enum class BillCategory
```
Coarse spending category — drives icons and dashboard grouping.

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/demo/ui/HomeViewModelTest.kt:360</code></summary>

```kotlin
        dueDay = 15,
        recurrence = Recurrence.MONTHLY,
        category = BillCategory.UTILITIES,
        enabled = true,
        reminderDaysBefore = 1,
        createdAtMs = 0,
        updatedAtMs = 0,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/banking/Loan.kt`

```kotlin
data class Loan(
```
A personal loan tracked by the user — purely local, no remote sync.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailViewModel.kt:62</code></summary>

```kotlin
    private val detailStream = repository.loanDetailStream(loanId, viewModelScope)

    val screenState: StateFlow<ScreenState<Loan>> = detailStream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    /**
     * Re-fetch the loan detail — wired to the read-side retry affordance surfaced by
```

</details>

```kotlin
enum class LoanKind
```
High-level loan category used for grouping, icons, and analytics.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanLabels.kt:21</code></summary>

```kotlin
 * template we keep it inline so the multi-formKey showcase has zero infra dependencies.
 */
internal fun loanKindLabel(kind: LoanKind): String = when (kind) {
    LoanKind.PERSONAL -> "Personal"
    LoanKind.MORTGAGE -> "Mortgage"
    LoanKind.AUTO -> "Auto"
    LoanKind.STUDENT -> "Student"
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/banking/LoanCalcScenario.kt`

```kotlin
data class LoanCalcScenario(
```
Wizard payload — the serialized snapshot of the loan-calc wizard at the current step. Serializable so the offline-resilient `DraftSubmitHandler` can persist it across process death and the next session can resume in-place.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/di/CalculatorsModule.kt:79</code></summary>

```kotlin
    viewModel { (scenarioId: String?) ->
        LoanCalcWizardViewModel(
            outbox = get(qualifier = AppOutboxQualifiers.LoanCalcScenario),
            repository = get(),
            scenarioIdArg = scenarioId,
        )
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/calc/AmortizationBreakdown.kt`

```kotlin
data class AmortizationBreakdown(
```
One amortization calculation: the per-installment `rows` and the `summary` totals.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/di/CalculatorsModule.kt:55</code></summary>

```kotlin
    single<AmortizationCompute> {
        AmortizationCompute { params ->
            AmortizationBreakdown(
                rows = amortizationSchedule(
                    params.principal,
                    params.ratePercent,
                    params.tenureMonths,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/cloudtodo/CloudTodo.kt`

```kotlin
data class CloudTodo(
```
A cloud-synced todo — the toolkit's MUTABLE (offline-write) Store5 archetype showcase.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsScreenPreview.kt:146</code></summary>

```kotlin
            conflict = ConflictEntry(
                id = "c-1",
                entity = "CloudTodo",
                key = "1",
                localPayloadJson = """{"completed":true}""",
                serverPayloadJson = """{"completed":false}""",
                formRoute = null,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/crypto/CoinMarket.kt`

```kotlin
data class CoinMarket(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinMarketsScreen.kt:118</code></summary>

```kotlin

@Composable
internal fun CoinMarketRow(coin: CoinMarket, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
```

</details>

```kotlin
data class CoinDetail(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinDetailViewModel.kt:44</code></summary>

```kotlin

    /** The repository-built stream — the screen renders it directly via `ScreenContent(stream)`. */
    val detail: ScreenDataStream<CoinDetail> = repository.coinDetailStream(
        coinId = coinId,
        scope = viewModelScope,
    )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/currency/Country.kt`

```kotlin
data class Country(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryPickerViewModel.kt:50</code></summary>

```kotlin
data class CountryPickerState(
    val searchQuery: String,
    val results: List<Country>,
)

/** User intents the picker accepts. */
sealed interface CountryPickerAction {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/currency/CountryFlagUtils.kt`

```kotlin
object CountryFlagUtils
```
_No KDoc at source._

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
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/currency/ExchangeRates.kt`

```kotlin
data class ExchangeRates(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:574</code></summary>

```kotlin
@Composable
private fun ExchangeRateCard(
    state: ScreenState<kpt.core.model.currency.ExchangeRates>,
    freshness: FreshnessSignal,
    onRetry: () -> Unit,
    onSeeAll: () -> Unit,
) {
```

</details>

```kotlin
data class RateHistoryKey(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/demo/ui/HomeViewModelTest.kt:510</code></summary>

```kotlin

    override fun rateHistoryStream(
        keyFlow: Flow<RateHistoryKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<RateHistory> = throw UnsupportedOperationException(
        "Home dashboard does not subscribe to rate-history streams.",
    )
```

</details>

```kotlin
data class RateHistory(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/demo/ui/HomeViewModelTest.kt:512</code></summary>

```kotlin
        keyFlow: Flow<RateHistoryKey>,
        scope: CoroutineScope,
    ): ScreenDataStream<RateHistory> = throw UnsupportedOperationException(
        "Home dashboard does not subscribe to rate-history streams.",
    )
}
```

</details>

```kotlin
data class RatePoint(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/currency/mapper/RateHistoryEntityMapper.kt:34</code></summary>

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

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryPickerViewModel.kt:50</code></summary>

```kotlin
data class CountryPickerState(
    val searchQuery: String,
    val results: List<Country>,
)

/** User intents the picker accepts. */
sealed interface CountryPickerAction {
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/economic/InterestRateSeries.kt`

```kotlin
data class InterestRateSeries(
```
Domain representation of an interest-rate time series sourced from FRED (Federal Reserve Economic Data). The default consumer is the Banking Utility Toolkit's "B7 Interest Rate Tracker" screen.

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/demo/ui/HomeViewModelTest.kt:162</code></summary>

```kotlin
    @Test
    fun ratesWidgetIsLoadingUntilBothFredStreamsReachContent() = runTest {
        val fed = MutableStateFlow<ScreenState<InterestRateSeries>>(ScreenState.Loading)
        val mortgage = MutableStateFlow<ScreenState<InterestRateSeries>>(ScreenState.Loading)
        val rates = FakeEconomicRatesRepository(
            fed = fed,
            mortgage = mortgage,
```

</details>

```kotlin
data class RateObservation(
```
Single observation in an interest-rate time series.

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/demo/ui/HomeViewModelTest.kt:372</code></summary>

```kotlin
        current = current,
        unit = "%",
        observations = listOf(RateObservation(LocalDate(2026, 5, 23), current)),
        source = "FRED",
    )

    // endregion
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/economic/MacroIndicator.kt`

```kotlin
data class MacroIndicator(
```
Domain representation of a country-level macro indicator sourced from the World Bank Open Data API. The default consumer is the Banking Utility Toolkit's "B8 Country Macro Snapshot" screen.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroViewModel.kt:48</code></summary>

```kotlin
) {

    private val streams: MutableMap<IndicatorKind, ScreenDataStream<MacroIndicator>> =
        mutableMapOf()
    private val subscriptionJobs: MutableMap<IndicatorKind, Job> = mutableMapOf()

    init {
```

</details>

```kotlin
data class IndicatorObservation(
```
Single year-level macro observation.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreenPreview.kt:38</code></summary>

```kotlin
    indicator = kind,
    observations = listOf(
        IndicatorObservation(year = 2024, value = value),
        IndicatorObservation(year = 2025, value = value * 1.02),
    ),
)
```

</details>

```kotlin
enum class IndicatorKind(val worldBankCode: String)
```
Macro indicators surfaced by the toolkit. Each kind maps to a stable World Bank indicator code via `worldBankCode`.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreen.kt:84</code></summary>

```kotlin
    onBackClick: () -> Unit,
    onPickCountry: () -> Unit,
    onOpenIndicator: (IndicatorKind) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CountryMacroViewModel = koinViewModel { parametersOf(countryCode) },
) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/emi/EmiResult.kt`

```kotlin
data class EmiResult(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/emi-calculator/src/commonMain/kotlin/kpt/feature/emicalculator/ui/EmiCalculatorScreenPreview.kt:32</code></summary>

```kotlin
 */

private val sampleResult = EmiResult(
    emi = 8_722.61,
    totalPayment = 104_671.32,
    totalInterest = 4_671.32,
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/profile/ProfileInfo.kt`

```kotlin
data class ProfileInfo(
```
What the profile screen displays. In the template this carries only the app's display name — the demo profile is a local, signed-out placeholder.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/demo/ui/ProfileViewModel.kt:41</code></summary>

```kotlin

    /** The repository-built stream — the screen renders it directly. */
    val profile: ScreenDataStream<ProfileInfo> = repository.profileStream(viewModelScope)
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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsViewModel.kt:74</code></summary>

```kotlin
            is SettingsAction.UpdateDarkThemeConfig -> viewModelScope.launch {
                analyticsHelper.logThemeChanged(action.darkThemeConfig)
                settingsRepository.setDarkThemeConfig(action.darkThemeConfig)
            }
            is SettingsAction.UpdateDynamicColor -> viewModelScope.launch {
                analyticsHelper.logDynamicColorPreferences(action.useDynamicColor)
                settingsRepository.setDynamicColorPreference(action.useDynamicColor)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/LanguageConfig.kt`

```kotlin
enum class LanguageConfig(
```
Every language the app can be switched to, in the user's OWN language. GENERATED from core/registries/LOCALE_REGISTRY.yaml by `core/scripts/language-picker-sync.sh --write` — DO NOT HAND-EDIT.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:62</code></summary>

```kotlin
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onChangeLanguage: (language: LanguageConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/ThemeBrand.kt`

```kotlin
enum class ThemeBrand(val brandName: String)
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsViewModel.kt:70</code></summary>

```kotlin
            is SettingsAction.UpdateThemeBrand -> viewModelScope.launch {
                analyticsHelper.logThemeBrandChanged(action.themeBrand)
                settingsRepository.setThemeBrand(action.themeBrand)
            }
            is SettingsAction.UpdateDarkThemeConfig -> viewModelScope.launch {
                analyticsHelper.logThemeChanged(action.darkThemeConfig)
                settingsRepository.setDarkThemeConfig(action.darkThemeConfig)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/UnlockType.kt`

```kotlin
enum class UnlockType
```
_No KDoc at source._

### `core/model/src/commonMain/kotlin/kpt/core/model/user/UserData.kt`

```kotlin
data class UserData(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonTest/kotlin/kpt/feature/settings/SettingsViewModelTest.kt:78</code></summary>

```kotlin
     */
    private class FakeUserDataRepository(
        private val initial: UserData,
        private val stateFlowOverride: Flow<ScreenState<UserData>>? = null,
        private val refreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1),
    ) : UserDataRepository {
        val current = MutableStateFlow(initial)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/watchlist/WatchlistItem.kt`

```kotlin
data class WatchlistItem(
```
Domain model for a personal-watchlist row (the `read_local_list` demo).

<details><summary>Used in the template — <code>feature/add-to-watchlist/src/commonTest/kotlin/kpt/feature/addtowatchlist/ui/AddToWatchlistViewModelTest.kt:118</code></summary>

```kotlin
    }

    override fun watchlistStream(scope: CoroutineScope): ScreenDataStream<List<WatchlistItem>> =
        error("watchlistStream is the read-side feature's concern; not used by AddToWatchlistViewModel")
}
```

</details>

---

_34 type(s), 15 function(s)/property(ies); 26 carry KDoc at source; 0 authored example(s); 31 live call site(s)._
<!-- api-docs:end -->
