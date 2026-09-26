# `core/designsystem`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DESIGNSYSTEM.md`
> **Measured:** 37 Kotlin files, 4 test files

## Principal types

`AppIcons`, `BarDatum`, `Candle`, `ChartTokens`, `DonutSlice`, `Elevation`, `FinanceColors`, `MoneyTone`, `NonLetterColorVisualTransformation`, `RateColors`, `RateDirection`, `Spacing`, `StatusChipIntent`, `Urgency`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/designsystem sha=6da2cb3909fa0147077cd9fc984c697cae34453f -->
## API reference

_Generated from `core/designsystem` at tree `6da2cb3909fa` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/ChartTokens.kt`

```kotlin
object ChartTokens
```
Shared visual tokens for every chart in `core/designsystem/chart/`. Reads from `MaterialTheme.colorScheme`, `MaterialTheme.typography`, and `MaterialTheme.finance`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptSparkline.kt:44</code></summary>

```kotlin
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = ChartTokens.defaultStrokeWidth,
    markerAtEnd: Boolean = false,
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
```

</details>

- `fun multiSeriesColors(): List<Color>`
- `val finance = MaterialTheme.finance`
- `fun axisLabelStyle(): TextStyle = MaterialTheme.typography.bodySmall.copy(`
- `fun gridlineColor(): Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)`
- `fun areaFillBrush(strokeColor: Color): Brush = Brush.verticalGradient(`
- `val defaultStrokeWidth = 1.5.dp`
- `val defaultAxisStrokeWidth = 1.0.dp`

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptAreaChart.kt`

```kotlin
fun KptAreaChart(
```
Filled area chart for the detail-screen Hero. Same geometry contract as `KptSparkline` but adds a gradient fill under the line via `ChartTokens.areaFillBrush`.

<details><summary>Used in the template — <code>feature/rates/src/commonMain/kotlin/kpt/feature/rates/ui/InterestRateDetailScreen.kt:182</code></summary>

```kotlin
    AppCard {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).padding(sp.sm)) {
            KptAreaChart(
                values = series.observations.map { it.value },
                modifier = Modifier.fillMaxSize(),
            )
        }
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptBarChart.kt`

```kotlin
data class BarDatum(
```
One bar of `KptBarChart`. `value` is unitless — the chart normalizes each bar to a fraction of the running max.

```kotlin
fun KptBarChart(
```
Vertical bar chart with rounded tops and optional axis labels. Practical input cap: ~24 bars (2 years monthly). For longer series, caller decimates or buckets. Empty input → renders nothing.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptCandlestick.kt`

```kotlin
data class Candle(
```
One open-high-low-close bar for `KptCandlestick`. All values are in the same unit (price, rate, etc.) and rendered against a shared y-axis covering the series' min/max.

<details><summary>Used in the template — <code>core/designsystem/src/commonTest/kotlin/kpt/core/designsystem/chart/KptCandlestickTest.kt:25</code></summary>

```kotlin
    @Test
    fun closeAboveOpenIsUp() {
        val candle = Candle(open = 100f, high = 110f, low = 95f, close = 108f)
        assertTrue(candle.isUp)
    }

    @Test
```

</details>

```kotlin
fun KptCandlestick(
```
Classic OHLC candlestick chart for price-like time series. Up candles (close ≥ open) use `upColor`; down candles use `downColor`. Default up/down colors resolve from `MaterialTheme.finance` so they brand-shift with theme without code edits.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptDonutChart.kt`

```kotlin
data class DonutSlice(
```
One slice of `KptDonutChart`. `value` is unitless — the chart normalizes each slice to a fraction of the sum.

```kotlin
fun KptDonutChart(
```
Canvas-based donut chart. Renders concentric arcs around a hollow center, optionally hosting a centered Composable (e.g. summary number). Practical input cap: any size (rendering cost is per-slice, fixed).

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptSparkline.kt`

```kotlin
fun KptSparkline(
```
Minimal Canvas-based sparkline — a one-pass connected polyline whose y-axis is normalized to the series's own `[min, max]` range. No external chart dependency.

<details><summary>Used in the template — <code>feature/rates/src/commonMain/kotlin/kpt/feature/rates/ui/InterestRatesScreen.kt:231</code></summary>

```kotlin
        }

        KptSparkline(
            values = series.observations.map { it.value },
            modifier = Modifier.weight(1f).fillMaxSize(),
        )
    }
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/AmountDisplay.kt`

```kotlin
fun AmountDisplay(
```
Big-and-bold currency presentation used at the top of dashboards and detail screens. Layout: optional label (small, dimmed) → large amount → optional supporting metadata row.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailScreen.kt:177</code></summary>

```kotlin

        HeroCard {
            AmountDisplay(
                amountText = formatMoney(loan.principalRemaining),
                label = stringResource(Res.string.screens_loans_detail_principal_remaining_label),
                supporting = {
                    Text(
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/MoneyText.kt`

```kotlin
fun MoneyText(
```
Currency text that picks its color from `MaterialTheme.finance` based on the amount's sign (or a forced `MoneyTone`). Use everywhere a monetary value is rendered so the app has a single visual grammar for money.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanRowCard.kt:87</code></summary>

```kotlin
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                MoneyText(
                    text = formatMoney(loan.principalRemaining),
                    tone = kpt.core.designsystem.component.MoneyTone.Negative,
                    style = MaterialTheme.typography.titleLarge,
                )
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/MoneyTone.kt`

```kotlin
enum class MoneyTone { AutoFromSign, Positive, Negative, Neutral, Inherit }
```
Money tone — how a monetary amount should be colored regardless of the raw value's sign.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanRowCard.kt:89</code></summary>

```kotlin
                MoneyText(
                    text = formatMoney(loan.principalRemaining),
                    tone = kpt.core.designsystem.component.MoneyTone.Negative,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/RateBadge.kt`

```kotlin
fun RateBadge(delta: String, direction: RateDirection, modifier: Modifier = Modifier)
```
Compact rate-change indicator — directional icon + percentage / delta text, both colored from `MaterialTheme.finance` (rateUp / rateDown / rateFlat). Renders inside a tinted container so it reads as a single visual unit.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:712</code></summary>

```kotlin
        )
        Spacer(Modifier.size(MaterialTheme.spacing.sm))
        RateBadge(delta = delta, direction = direction)
    }
}

@Composable
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/RateDirection.kt`

```kotlin
enum class RateDirection { Up, Down, Flat }
```
Direction of a rate / price / metric change relative to the prior period.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:555</code></summary>

```kotlin
                        rates.fedFundsPercent.formatDecimal(2),
                    ),
                    direction = RateDirection.Flat,
                    delta = stringResource(Res.string.screens_home_rates_delta_flat),
                )
                RateRow(
                    label = stringResource(Res.string.screens_home_rates_mortgage_30y_label),
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/SectionHeader.kt`

```kotlin
fun SectionHeader(
```
Section header — title (+ optional supporting text) + optional trailing action button. Use to break dashboards into scannable groups (Loans, Bills, Rates, Currencies).

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:191</code></summary>

```kotlin

        // ── Quick stats grid (Bills + Rates) ─────────────────────────────
        SectionHeader(title = stringResource(Res.string.screens_home_section_this_week))

        BillsQuickCard(
            state = state.bills,
            onSeeAll = onNavigateToBills,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardLoadingSkeleton.kt`

```kotlin
fun CardLoadingSkeleton(modifier: Modifier = Modifier)
```
Whole-card shimmer placeholder. Drop in for `AppCard` / `Card` while the underlying data loads. Renders 3 stacked shimmer bars to suggest a typical card layout (title + 2 body lines).

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:128</code></summary>

```kotlin

            // ── CardLoadingSkeleton ─────────────────────────────────────
            SectionHeader("CardLoadingSkeleton")
            CardLoadingSkeleton()

            // ── CardStateBox ────────────────────────────────────────────
            SectionHeader("CardStateBox — every variant")
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardStateBox.kt`

```kotlin
fun <T> CardStateBox(
```
State-aware Card wrapper. Renders the right component-scale UI for each `ScreenState` variant *inside* the Card bounds — no full-screen overlay. Use for individual cards on a dashboard, where each card resolves its own data independently.

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:132</code></summary>

```kotlin

            // ── CardStateBox ────────────────────────────────────────────
            SectionHeader("CardStateBox — every variant")
            Text(
                stringResource(Res.string.screens_showcase_state_gallery_loading),
                style = MaterialTheme.typography.labelMedium,
            )
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/ErrorChip.kt`

```kotlin
fun ErrorChip(message: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null)
```
M3 AssistChip styled for error reporting. Slightly more prominent than `InlineErrorPill` — adds an icon and uses chip semantics (clickable by default if `onClick` is non-null).

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:103</code></summary>

```kotlin
            // interactive rather than a dead placeholder.
            var errorChipTaps by remember { mutableIntStateOf(0) }
            LabelRow("ErrorChip") {
                ErrorChip(message = "Failed to load", onClick = { errorChipTaps++ })
                if (errorChipTaps > 0) {
                    Text(
                        text = stringResource(
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/InlineErrorPill.kt`

```kotlin
fun InlineErrorPill(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null)
```
Pill-shaped inline error chip — for component-scale failures (a single row failing in an otherwise-loaded list, a stale field in a form, a card-local fetch failure).

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:91</code></summary>

```kotlin

            // ── InlineErrorPill ─────────────────────────────────────────
            LabelRow("InlineErrorPill (no retry)") {
                InlineErrorPill(message = "Couldn't refresh balance")
            }
            LabelRow("InlineErrorPill (with retry)") {
                InlineErrorPill(message = "Network error", onRetry = {})
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/RowLoadingShimmer.kt`

```kotlin
fun RowLoadingShimmer(modifier: Modifier = Modifier)
```
Single-row shimmer placeholder. Use inside LazyColumn `items()` while paged data loads, or as a one-off row placeholder for a single record waiting on a network response. Layout: circle avatar + 2 stacked text bars (title + subtitle).

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:117</code></summary>

```kotlin

            // ── RowLoadingShimmer ───────────────────────────────────────
            SectionHeader("RowLoadingShimmer")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    repeat(3) {
                        RowLoadingShimmer()
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/StatusChip.kt`

```kotlin
fun StatusChip(text: String, intent: StatusChipIntent, modifier: Modifier = Modifier)
```
Compact colored pill used to convey state at a glance — bill status, loan stage, rate direction, sync state. Stays one line; no icons (use `UrgencyDot` when you want a leading accent).

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailScreen.kt:170</code></summary>

```kotlin
                modifier = Modifier.weight(1f),
            )
            StatusChip(
                text = loanKindLabel(loan.kind),
                intent = StatusChipIntent.Info,
            )
        }
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/StatusChipIntent.kt`

```kotlin
enum class StatusChipIntent
```
Semantic intent of a `StatusChip`. Maps to a (container, content) color pair derived from the active Material color scheme + finance palette.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailScreen.kt:172</code></summary>

```kotlin
            StatusChip(
                text = loanKindLabel(loan.kind),
                intent = StatusChipIntent.Info,
            )
        }

        HeroCard {
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/Urgency.kt`

```kotlin
enum class Urgency { Overdue, Today, Upcoming, Distant }
```
Due-date urgency tier — informs the color of a leading dot on a list row.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:772</code></summary>

```kotlin

/** Heuristic mapping bill due-day to an urgency tier for the [UrgencyDot]. */
private fun urgencyForDay(dueDay: Int): Urgency = when {
    dueDay <= 1 -> Urgency.Today
    dueDay <= 3 -> Urgency.Upcoming
    dueDay <= 7 -> Urgency.Upcoming
    else -> Urgency.Distant
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/UrgencyDot.kt`

```kotlin
fun UrgencyDot(urgency: Urgency, modifier: Modifier = Modifier, size: Dp = 10.dp)
```
Solid colored dot used as the leading accent on a list row (bill reminder, loan due, task). Pairs cheaply with any list-item layout to encode urgency at a glance without stealing focus from the row's text content.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:499</code></summary>

```kotlin
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    ) {
                        UrgencyDot(urgency = urgencyForDay(bill.dueDay))
                        Text(
                            text = bill.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/icon/AppIcons.kt`

```kotlin
object AppIcons
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsScreenPreview.kt:105</code></summary>

```kotlin
    KptTheme {
        SettingsRowCard(
            icon = AppIcons.Language,
            title = "Change the application display language and region format",
            contentDescription = "Opens the language picker",
            accentColor = MaterialTheme.colorScheme.tertiary,
            onClick = {},
```

</details>

- `val Language: ImageVector = Icons.Default.ArrowOutward`
- `val CheckCircle: ImageVector = Icons.Filled.CheckCircle`
- `val OutlinedInfo = Icons.Outlined.Info`
- `val OutlinedLock = Icons.Outlined.Lock`
- `val OutlinedNotifications = Icons.Outlined.Notifications`
- `val ChevronRight: ImageVector = Icons.Filled.ChevronRight`
- `val QrCode: ImageVector = Icons.Filled.QrCode`
- `val Close: ImageVector = Icons.Filled.Close`
- `val AttachMoney: ImageVector = Icons.Filled.AttachMoney`
- `val OutlinedVisibilityOff: ImageVector = Icons.Outlined.VisibilityOff`
- `val OutlinedVisibility: ImageVector = Icons.Outlined.Visibility`
- `val VisibilityOff: ImageVector = Icons.Filled.VisibilityOff`
- `val Visibility: ImageVector = Icons.Filled.Visibility`
- `val Check: ImageVector = Icons.Default.Check`
  _…more members; read the file._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Color.kt`

```kotlin
val primaryLight = Color(0xFF4338CA)
```
_No KDoc at source._

```kotlin
val onPrimaryLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val primaryContainerLight = Color(0xFFE0E7FF)
```
_No KDoc at source._

```kotlin
val onPrimaryContainerLight = Color(0xFF1F1D75)
```
_No KDoc at source._

```kotlin
val secondaryLight = Color(0xFF059669)
```
_No KDoc at source._

```kotlin
val onSecondaryLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val secondaryContainerLight = Color(0xFFD1FAE5)
```
_No KDoc at source._

```kotlin
val onSecondaryContainerLight = Color(0xFF064E3B)
```
_No KDoc at source._

```kotlin
val tertiaryLight = Color(0xFFD97706)
```
_No KDoc at source._

```kotlin
val onTertiaryLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val tertiaryContainerLight = Color(0xFFFEF3C7)
```
_No KDoc at source._

```kotlin
val onTertiaryContainerLight = Color(0xFF78350F)
```
_No KDoc at source._

```kotlin
val errorLight = Color(0xFFF87171)
```
_No KDoc at source._

```kotlin
val onErrorLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val errorContainerLight = Color(0xFFFFE4E1)
```
_No KDoc at source._

```kotlin
val onErrorContainerLight = Color(0xFF9F1239)
```
_No KDoc at source._

```kotlin
val backgroundLight = Color(0xFFFAFAFB)
```
_No KDoc at source._

```kotlin
val onBackgroundLight = Color(0xFF0F172A)
```
_No KDoc at source._

```kotlin
val surfaceLight = Color(0xFFFAFAFB)
```
_No KDoc at source._

```kotlin
val onSurfaceLight = Color(0xFF0F172A)
```
_No KDoc at source._

```kotlin
val surfaceVariantLight = Color(0xFFE2E8F0)
```
_No KDoc at source._

```kotlin
val onSurfaceVariantLight = Color(0xFF475569)
```
_No KDoc at source._

```kotlin
val outlineLight = Color(0xFF94A3B8)
```
_No KDoc at source._

```kotlin
val outlineVariantLight = Color(0xFFCBD5E1)
```
_No KDoc at source._

```kotlin
val scrimLight = Color(0xFF000000)
```
_No KDoc at source._

```kotlin
val inverseSurfaceLight = Color(0xFF1E293B)
```
_No KDoc at source._

```kotlin
val inverseOnSurfaceLight = Color(0xFFF1F5F9)
```
_No KDoc at source._

```kotlin
val inversePrimaryLight = Color(0xFFA5B4FC)
```
_No KDoc at source._

```kotlin
val surfaceDimLight = Color(0xFFE2E8F0)
```
_No KDoc at source._

```kotlin
val surfaceBrightLight = Color(0xFFFAFAFB)
```
_No KDoc at source._

```kotlin
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
```
_No KDoc at source._

```kotlin
val surfaceContainerLowLight = Color(0xFFF8FAFC)
```
_No KDoc at source._

```kotlin
val surfaceContainerLight = Color(0xFFF1F5F9)
```
_No KDoc at source._

```kotlin
val surfaceContainerHighLight = Color(0xFFE2E8F0)
```
_No KDoc at source._

```kotlin
val surfaceContainerHighestLight = Color(0xFFCBD5E1)
```
_No KDoc at source._

```kotlin
val primaryDark = Color(0xFFA5B4FC)
```
_No KDoc at source._

```kotlin
val onPrimaryDark = Color(0xFF1F1D75)
```
_No KDoc at source._

```kotlin
val primaryContainerDark = Color(0xFF3730A3)
```
_No KDoc at source._

```kotlin
val onPrimaryContainerDark = Color(0xFFE0E7FF)
```
_No KDoc at source._

```kotlin
val secondaryDark = Color(0xFF6EE7B7)
```
_No KDoc at source._

```kotlin
val onSecondaryDark = Color(0xFF064E3B)
```
_No KDoc at source._

```kotlin
val secondaryContainerDark = Color(0xFF065F46)
```
_No KDoc at source._

```kotlin
val onSecondaryContainerDark = Color(0xFFD1FAE5)
```
_No KDoc at source._

```kotlin
val tertiaryDark = Color(0xFFFCD34D)
```
_No KDoc at source._

```kotlin
val onTertiaryDark = Color(0xFF78350F)
```
_No KDoc at source._

```kotlin
val tertiaryContainerDark = Color(0xFF92400E)
```
_No KDoc at source._

```kotlin
val onTertiaryContainerDark = Color(0xFFFEF3C7)
```
_No KDoc at source._

```kotlin
val errorDark = Color(0xFFFDBA74)
```
_No KDoc at source._

```kotlin
val onErrorDark = Color(0xFF7C2D12)
```
_No KDoc at source._

```kotlin
val errorContainerDark = Color(0xFFC2410C)
```
_No KDoc at source._

```kotlin
val onErrorContainerDark = Color(0xFFFFEDD5)
```
_No KDoc at source._

```kotlin
val backgroundDark = Color(0xFF0F172A)
```
_No KDoc at source._

```kotlin
val onBackgroundDark = Color(0xFFF1F5F9)
```
_No KDoc at source._

```kotlin
val surfaceDark = Color(0xFF0F172A)
```
_No KDoc at source._

```kotlin
val onSurfaceDark = Color(0xFFF1F5F9)
```
_No KDoc at source._

```kotlin
val surfaceVariantDark = Color(0xFF1E293B)
```
_No KDoc at source._

```kotlin
val onSurfaceVariantDark = Color(0xFFCBD5E1)
```
_No KDoc at source._

```kotlin
val outlineDark = Color(0xFF64748B)
```
_No KDoc at source._

```kotlin
val outlineVariantDark = Color(0xFF334155)
```
_No KDoc at source._

```kotlin
val scrimDark = Color(0xFF000000)
```
_No KDoc at source._

```kotlin
val inverseSurfaceDark = Color(0xFFF1F5F9)
```
_No KDoc at source._

```kotlin
val inverseOnSurfaceDark = Color(0xFF1E293B)
```
_No KDoc at source._

```kotlin
val inversePrimaryDark = Color(0xFF4338CA)
```
_No KDoc at source._

```kotlin
val surfaceDimDark = Color(0xFF0F172A)
```
_No KDoc at source._

```kotlin
val surfaceBrightDark = Color(0xFF374558)
```
_No KDoc at source._

```kotlin
val surfaceContainerLowestDark = Color(0xFF020617)
```
_No KDoc at source._

```kotlin
val surfaceContainerLowDark = Color(0xFF1E293B)
```
_No KDoc at source._

```kotlin
val surfaceContainerDark = Color(0xFF243044)
```
_No KDoc at source._

```kotlin
val surfaceContainerHighDark = Color(0xFF2D3B52)
```
_No KDoc at source._

```kotlin
val surfaceContainerHighestDark = Color(0xFF374558)
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Elevation.kt`

```kotlin
data class Elevation(
```
Shared elevation tier scale — five named tiers matching Material 3 elevation guidance. Access from any Composable via `MaterialTheme.elevation`.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptBottomBar.kt:33</code></summary>

```kotlin
        windowInsets = windowInsets,
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 0.dp,
    ) {
        navigationItems.forEach { navigationItem ->
            KptNavigationBarItem(
                contentDescriptionRes = navigationItem.contentDescriptionRes,
```

</details>

```kotlin
val LocalElevation = staticCompositionLocalOf { Elevation() }
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:153</code></summary>

```kotlin
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
        ) {
            content()
        }
```

</details>

```kotlin
val MaterialTheme.elevation: Elevation
```
Resolve the active `Elevation` tier scale from composition.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/FinanceColors.kt`

```kotlin
data class FinanceColors(
```
Semantic finance color palette — extends Material 3's `androidx.compose.material3.ColorScheme` with money/rate/freshness/urgency tokens specific to financial UIs. Access from any Composable via `MaterialTheme.finance`.

<details><summary>Example</summary>

```kotlin
CompositionLocalProvider(LocalFinanceColors provides myForkFinanceColors()) {
    KptTheme { App() }
}
```

</details>

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:136</code></summary>

```kotlin

    val screenStateDefaults = appScreenStateDefaults()
    val financeColors = if (darkTheme) darkFinanceColors() else lightFinanceColors()

    KptMaterialTheme(theme = themeProvider) {
        // Provide the design-system token CompositionLocals app-wide so every widget
        // built on `core/designsystem/component/`, `chart/`, and `motion/` resolves
```

</details>

```kotlin
fun lightFinanceColors(): FinanceColors = FinanceColors(
```
Light-theme finance palette. Contrast-verified against `surfaceLight` / `surfaceContainerLight` etc.

```kotlin
fun darkFinanceColors(): FinanceColors = FinanceColors(
```
Dark-theme finance palette. Contrast-verified against `surfaceDark` / `surfaceContainerDark`. Mirrors light values but brightened for legibility on the deep navy `#13131B` background.

```kotlin
val LocalFinanceColors = staticCompositionLocalOf<FinanceColors>
```
CompositionLocal for the active `FinanceColors`. Provided by `KptTheme`. Direct access discouraged — use `MaterialTheme.finance` extension instead.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:150</code></summary>

```kotlin
        // core/store/AppScreenStateDefaults.kt — that's the single fork seam.
        CompositionLocalProvider(
            LocalFinanceColors provides financeColors,
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
```

</details>

```kotlin
val MaterialTheme.finance: FinanceColors
```
Resolve the active `FinanceColors` from composition.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt`

```kotlin
val lightScheme = lightColorScheme(
```
_No KDoc at source._

```kotlin
val darkScheme = darkColorScheme(
```
_No KDoc at source._

```kotlin
fun KptTheme(
```
The main theme composable for the application. This composable uses KptMaterialTheme under the hood to provide seamless integration between KptTheme design tokens and Material3 theming system.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/AddOrEditLoanScreenPreview.kt:33</code></summary>

```kotlin
@Composable
internal fun LoanFormFieldsEnabledPreview() {
    KptTheme {
        Column {
            LoanKindDropdown(value = LoanKind.AUTO, onChange = {}, enabled = true)
            DoubleField(label = "Principal", value = 100_000.0, onChange = {}, enabled = true)
            IntField(label = "Tenure (months)", value = 60, onChange = {}, enabled = true)
```

</details>

```kotlin
expect fun platformColorScheme(useDarkTheme: Boolean, dynamicColor: Boolean): ColorScheme
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Spacing.kt`

```kotlin
data class Spacing(
```
Shared spacing scale — replaces raw `.dp` literals scattered across features with a disciplined 4 / 8 / 12 / 16 / 24 / 32 / 48 progression. Access from any Composable via `MaterialTheme.spacing`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptCandlestick.kt:72</code></summary>

```kotlin
        val priceRange = (priceMax - priceMin).takeIf { it > 0f } ?: 1f

        val candleSpacing = size.width / candles.size
        val candleBodyWidth = candleSpacing * 0.7f
        val wickStrokePx = wickWidth.toPx()

        candles.forEachIndexed { index, candle ->
```

</details>

```kotlin
val LocalSpacing = staticCompositionLocalOf { Spacing() }
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:152</code></summary>

```kotlin
            LocalFinanceColors provides financeColors,
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
        ) {
            content()
```

</details>

```kotlin
val MaterialTheme.spacing: Spacing
```
Resolve the active `Spacing` scale from composition.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Type.kt`

```kotlin
val fontFamily: FontFamily
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/utils/ModifierExt.kt`

```kotlin
fun Modifier.mirrorIfRtl() = composed
```
_No KDoc at source._

```kotlin
fun Modifier.tabNavigation() = composed
```
_No KDoc at source._

```kotlin
fun Modifier.onClick(
```
_No KDoc at source._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/utils/NonLetterColorVisualTransformation.kt`

```kotlin
fun nonLetterColorVisualTransformation(): VisualTransformation
```
_No KDoc at source._

---

_12 type(s), 124 function(s)/property(ies); 34 carry KDoc at source; 1 authored example(s); 27 live call site(s)._
<!-- api-docs:end -->
