# `core-base/ui`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_UI.md`
> **Measured:** 78 Kotlin files, 13 test files

**Defines annotations:** `@FeatureDestination`

## Principal types

`AppInfo`, `BackgroundEvent`, `BaseMutationViewModel`, `BaseViewModel`, `DashboardProgressState`, `DefaultLottieAnimations`, `DisplayState`, `DraftPickerItem`, `FeatureDestination`, `FreshnessTint`, `FreshnessVisual`, `KptFadeThrough`, `KptSharedAxis`, `ListItemEnterMath`  …and 15 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/ui sha=255e300cb59150a1186b90f2ef9c32468c8c9f1a -->
## API reference

_Generated from `core-base/ui` at tree `255e300cb591` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/AppInfo.kt`

```kotlin
object AppInfo
```
The SINGLE common-code accessor for the app's user-facing display name.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsScreen.kt:145</code></summary>

```kotlin
    }
    Text(
        text = AppInfo.appDisplayName,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = rowModifier,
```

</details>

- `val appDisplayName: String get() = BuildKonfig.APP_DISPLAY_NAME`

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/captiveportal/CaptivePortalLauncher.kt`

```kotlin
expect fun rememberOpenCaptivePortalSignIn(): () -> Unit
```
Returns a stable `() -> Unit` lambda that opens the platform-native captive-portal sign-in flow. Per-platform behaviour: - **Android** — fires `android.settings.WIFI_SETTINGS` via `startActivity`.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/DashboardLoadingProgress.kt`

```kotlin
fun Flow<List<ScreenState<*>>>.aggregateDashboardProgress(): Flow<DashboardProgressState> =
```
Fan-in operator that converts a stream of per-card states into a single `DashboardProgressState` suitable for top-of-screen progress display.

<details><summary>Example</summary>

```kotlin
val cards: StateFlow<List<ScreenState<*>>> = combine(
    loansStream.state,
    billsStream.state,
    ratesStream.state,
    fxStream.state,
) { l, b, r, f -> listOf(l, b, r, f) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

val progress: StateFlow<DashboardProgressState> = cards
    .aggregateDashboardProgress()
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardProgressState(0, 0, false, false, false),
    )
DashboardProgressBar(state = progressState)
IndependentCardLayout(states = cards, onRetry = vm::onRetryCard) { i, content -> /* ... */ }
```

</details>

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
fun List<ScreenState<*>>.toDashboardProgressState(): DashboardProgressState =
```
Pure conversion from a list of per-card states to a single `DashboardProgressState`. Exposed so tests + non-Flow call sites can re-use the aggregation contract.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/PersonalLoansListViewModel.kt:75</code></summary>

```kotlin
 */
data class LoansListUiState(
    val loans: List<Loan>,
    val totalMonthlyEmi: Double,
    val totalPrincipalRemaining: Double,
)
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/DashboardProgressBar.kt`

```kotlin
fun DashboardProgressBar(
```
Top-of-dashboard **freshness** strip: shows *when* the data was last loaded — "Updated 5m ago" — NOT a "2 of 4 loaded" count.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:175</code></summary>

```kotlin
        // It shows a subtle "Loading…" bar until the first card has data and hides once
        // there is nothing to surface — the per-card independence + retry below is untouched.
        DashboardProgressBar(
            state = listOf(
                state.loans,
                state.bills,
                state.rates,
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/DashboardProgressState.kt`

```kotlin
data class DashboardProgressState(
```
Aggregate progress snapshot for a multi-card dashboard.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:181</code></summary>

```kotlin
                state.rates,
                state.exchangeRate,
            ).toDashboardProgressState(),
        )

        // ── Hero: at-a-glance financial snapshot ─────────────────────────
        HeroSnapshot(state.loans)
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/dashboard/IndependentCardLayout.kt`

```kotlin
fun <T> IndependentCardLayout(
```
Renders a vertical stack of cards where each card carries its OWN `ScreenState` — so a slow card can still spin while a fast one shows content, and a single failed fetch only ruins one card instead of blanking the whole dashboard.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreen.kt:192</code></summary>

```kotlin
            )

            IndependentCardLayout(
                states = macroStates,
                onRetry = onRetryIndicator,
                cardChrome = { index, card ->
                    MacroIndicatorCardChrome(
```

</details>

```kotlin
fun <T> IndependentCardLayout(
```
Direct `ScreenDataStream` overload of `IndependentCardLayout` — one Store5 read stream per independent card.

<details><summary>Example</summary>

```kotlin
IndependentCardLayout(
    streams = listOf(viewModel.loansStream, viewModel.billsStream, viewModel.ratesStream),
) { index, data, _ -> DashboardCard(index, data) }
```

</details>

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreen.kt:192</code></summary>

```kotlin
            )

            IndependentCardLayout(
                states = macroStates,
                onRetry = onRetryIndicator,
                cardChrome = { index, card ->
                    MacroIndicatorCardChrome(
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/draft/DraftPicker.kt`

```kotlin
fun DraftPickerList(
```
Full-screen picker for the **multi-pending** case — a form that can hold N concurrent drafts (one per `uniqueKey`, e.g. per-loan or per-bill).

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsPreview.kt:102</code></summary>

```kotlin
internal fun DraftPickerListPreview() {
    KptTheme {
        DraftPickerList(
            items = listOf(
                DraftPickerItem(1, "Rent · ₹1,200", "Saved offline — failed to sync", isFailed = true),
                DraftPickerItem(2, "Electricity", "Pending sync"),
                DraftPickerItem(3, "Internet · ₹800", "Pending sync"),
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/draft/DraftPickerItem.kt`

```kotlin
data class DraftPickerItem(
```
One selectable draft in the `DraftPickerList` — a **UI value object**, deliberately independent of the store/database types so this framework primitive never leaks `core-base/store` into its public API (consumers map their `DraftRecord` / outbox entry to this).

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsPreview.kt:104</code></summary>

```kotlin
        DraftPickerList(
            items = listOf(
                DraftPickerItem(1, "Rent · ₹1,200", "Saved offline — failed to sync", isFailed = true),
                DraftPickerItem(2, "Electricity", "Pending sync"),
                DraftPickerItem(3, "Internet · ₹800", "Pending sync"),
            ),
            onResume = {},
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/draft/DraftResolutionPrompt.kt`

```kotlin
fun DraftResolutionPrompt(
```
The **three-case** draft-resolution prompt shown when a user re-opens a form that already has a saved draft.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/AddOrEditLoanScreen.kt:122</code></summary>

```kotlin
    // resume it, discard it, or start fresh (the draft stays recoverable in Settings → Sync & Drafts).
    if (ui.hasResumableDraft) {
        DraftResolutionPrompt(
            onResume = viewModel::onResume,
            onDiscard = viewModel::onDiscardSavedDraft,
            onStartFresh = viewModel::onStartFresh,
            onDismiss = viewModel::onStartFresh,
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/effects/EventsEffect.kt`

```kotlin
fun <E> EventsEffect(
```
Convenience method for observing event flow from `BaseViewModel`. By default, events will only be consumed when the associated screen is resumed, to avoid bugs like duplicate navigation calls.

```kotlin
fun <E> EventsEffect(
```
Convenience method for observing event flow from `BaseViewModel`. By default, events will only be consumed when the associated screen is resumed, to avoid bugs like duplicate navigation calls.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/effects/LifecycleEventEffect.kt`

```kotlin
fun LivecycleEventEffect(
```
Creates a side effect to observe lifecycle events.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/effects/ReportDrawnExt.kt`

```kotlin
expect fun ReportDrawnWhen(block: () -> Boolean)
```
Reports to the composition system that content is considered drawn when the specified condition is true. Platform-specific implementation that affects rendering optimizations.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/freshness/ErrorCategoryMessages.kt`

```kotlin
fun ErrorCategory.toShortMessage(): String = when (this)
```
Short (≤ 20 chars) message per `ErrorCategory` variant — used by `FreshnessIndicator` tooltip titles and `RefreshStateChip` labels.

```kotlin
fun ErrorCategory.toLongMessage(): String = when (this)
```
Longer descriptive message per `ErrorCategory` variant — used by `FreshnessIndicator` `RichTooltip` body text. `AppErrorMapper.mapErrorToUserMessage` delegates here so the message catalogue lives in one place.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/freshness/FreshnessIndicator.kt`

```kotlin
fun FreshnessIndicator(
```
Per-card freshness indicator: a small Material 3 info / clock / warning icon anchored to a `TooltipBox`.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:537</code></summary>

```kotlin
        onSeeAll = onSeeAll,
        trailing = {
            FreshnessIndicator(
                signal = freshness,
                onRefresh = onRetry,
            )
        },
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/freshness/FreshnessStrings.kt`

```kotlin
fun humanizeDuration(d: Duration): String = when
```
Humanises a duration into a "X ago" tooltip phrase for `FreshnessIndicator`.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/freshness/RefreshStateChip.kt`

```kotlin
fun RefreshStateChip(
```
Inline dismissible "we're showing previous data" chip for **input-type screens** (Rate History period/currency picker, etc.) where the previous selection's data is preserved as a stale fallback while the new key's fetch is in-flight or has failed. Renders nothing unless `signal`.band is `FreshnessBand.VeryStale` — i.e. the preserved data is significantly out of date OR a fetch error has occurred. The chip's label categorises the error via `categorize` when `signal`.lastError is non-null ("No network · Showing previous data ↺", "Server error · Showing previous data ↺", etc.); falls back to a generic age-based label otherwise. Pair this with the per-card `FreshnessIndicator` on the title: the indicator surfaces the staleness, the chip explains it inline next to the content.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/GestureDetector.kt`

```kotlin
fun Modifier.detectMultiTapGesture(
```
Modifier that detects a multi-tap gesture within a configurable timeout window.

<details><summary>Example</summary>

```kotlin
Text(
    text = "Tap me 5 times!",
    modifier = Modifier
        .detectMultiTapGesture {
            // Show hidden feature
            showDebugMenu = true
        }
)
Text(
    text = "Tap me 3 times quickly!",
    modifier = Modifier
        .detectMultiTapGesture(
            tapCount = 3,
            tapTimeoutMs = 500L,
        ) {
            // Show hidden feature
        }
)
```

</details>

```kotlin
fun Modifier.detectLongPressGesture(
```
Modifier that detects a long press gesture.

<details><summary>Example</summary>

```kotlin
Box(
    modifier = Modifier
        .detectLongPressGesture {
            // Handle long press
        }
)
```

</details>

```kotlin
fun Modifier.detectDoubleTapGesture(
```
Modifier that detects a double tap gesture.

<details><summary>Example</summary>

```kotlin
Image(
    modifier = Modifier
        .detectDoubleTapGesture {
            // Handle double tap (e.g., like action)
        }
)
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/KptConnectivityBanner.kt`

```kotlin
fun KptConnectivityBanner(
```
Thin wrapper around `ConnectivityBanner` from `cmp-network-monitor-compose`.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/motion/KptFadeThrough.kt`

```kotlin
object KptFadeThrough
```
Fade-through enter/exit factories. Use for **sibling navigation** — bottom-nav tab switches, settings section switches, paged tabs.

- `fun enter(): EnterTransition = enter(MaterialTheme.motion)`
- `fun enter(motion: Motion): EnterTransition = fadeIn(`
- `fun exit(): ExitTransition = exit(MaterialTheme.motion)`
- `fun exit(motion: Motion): ExitTransition = fadeOut(`

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/motion/KptListItemEnter.kt`

```kotlin
fun Modifier.kptListItemEnter(
```
Staggered fade + translate-Y enter modifier for `LazyColumn` / `LazyRow` items.

<details><summary>Example</summary>

```kotlin
LazyColumn {
    itemsIndexed(loans) { index, loan ->
        LoanRowCard(
            loan,
            modifier = Modifier.kptListItemEnter(index),
        )
    }
}
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/motion/KptRefreshingPulse.kt`

```kotlin
fun Modifier.kptRefreshingPulse(active: Boolean): Modifier = composed
```
Infinite scale + alpha "breath" while `active` is true. Use to signal **"this surface is fetching live data right now"** — freshness dots, inline refresh badges, etc.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/motion/KptSharedAxis.kt`

```kotlin
object KptSharedAxis
```
Shared-axis-X enter/exit factories, per Material Motion. Use for **forward/back navigation in a single stack** — pushing onto the stack slides left-and-fades, popping reverses.

- `fun enterForward(): EnterTransition = enterForward(MaterialTheme.motion)` — Forward push — new screen slides in from the right, old screen slides out left.
- `fun enterForward(motion: Motion): EnterTransition`
- `val slide = motion.sharedAxisSlideDistance`
- `fun exitForward(): ExitTransition = exitForward(MaterialTheme.motion)` — Forward push — companion exit (the popped/replaced screen).
- `fun exitForward(motion: Motion): ExitTransition`
- `val slide = motion.sharedAxisSlideDistance`
- `fun enterBack(): EnterTransition = enterBack(MaterialTheme.motion)` — Back/pop — new screen slides in from the left, mirroring `enterForward`.
- `fun enterBack(motion: Motion): EnterTransition`
- `val slide = motion.sharedAxisSlideDistance`
- `fun exitBack(): ExitTransition = exitBack(MaterialTheme.motion)` — Back/pop — companion exit (the screen being popped).
- `fun exitBack(motion: Motion): ExitTransition`
- `val slide = motion.sharedAxisSlideDistance`

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/nav/FeatureDestination.kt`

```kotlin
public annotation class FeatureDestination
```
Marks a `NavGraphBuilder` extension as a TOP-LEVEL feature destination — one the app shell registers directly on the authenticated graph.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/navigation/LoansNavigation.kt:45</code></summary>

```kotlin
}

@FeatureDestination
fun NavGraphBuilder.loansGraph(navController: NavController) {
    navigation<LoansGraphRoute>(startDestination = PersonalLoansListRoute) {
        composableWithPushTransitions<PersonalLoansListRoute> {
            PersonalLoansListScreen(
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/nav/SafeNavController.kt`

```kotlin
fun NavController.popBackStackSafely()
```
Pops the back stack only when the current destination is fully `Lifecycle.State.RESUMED`.

<details><summary>Example</summary>

```kotlin
composableWithPushTransitions<MyRoute> {
    MyScreen(onBackClick = { navController.popBackStackSafely() })
}
```

</details>

```kotlin
fun NavController.rememberSafeBackPress(debounceMs: Long = 500L): () -> Unit
```
Returns a stable lambda that guards `NavController.popBackStack` with two layers: 1. **Lifecycle guard** — only fires when the current destination is `Lifecycle.State.RESUMED`.

<details><summary>Example</summary>

```kotlin
@Composable
fun MyScreen(navController: NavController) {
    val onBack = navController.rememberSafeBackPress()
    KptTopAppBar(title = "…", onNavigationIconClick = onBack)
}
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/paging/LoadMoreFooter.kt`

```kotlin
fun <T : Any> LoadMoreFooter(
```
Footer for paginated lists driven by a `PagingScreenStream`. Place as the last item in a LazyColumn. Shows loading indicator, error with retry, or end-of-list.

```kotlin
fun LoadMoreFooter(
```
Footer overload taking raw state values. Lets screens that drive paging without exposing `PagingScreenStream` directly still use the canonical footer rendering.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/paging/LoadMoreTrigger.kt`

```kotlin
fun rememberLoadMoreTrigger(
```
Returns a `State` that flips to `true` when the user has scrolled within `threshold` items of the end of the list AND more pages are available AND a load is not already in flight. Reads `listState.layoutInfo.totalItemsCount` directly.

<details><summary>Example</summary>

```kotlin
val shouldLoadMore by rememberLoadMoreTrigger(listState, hasMore, isLoadingMore)
LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) viewModel.onLoadMore() }
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/paging/PagingScreenContent.kt`

```kotlin
fun <T : Any> PagingScreenContent(
```
`ScreenContent` variant for paginated lists — slot-only overload for screens with custom layouts (sticky headers, sectioned lists, etc.).

<details><summary>Example</summary>

```kotlin
PagingScreenContent(
    pagingStream = viewModel.pagingStream,
    onRetry = viewModel::retry,
) { items, freshness ->
    LazyColumn {
        items(items) { item -> ItemRow(item) }
        item { LoadMoreFooter(pagingStream = viewModel.pagingStream) }
    }
}
```

</details>

<details><summary>Used in the template — <code>feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinMarketsScreen.kt:96</code></summary>

```kotlin
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        PagingScreenContent(
            pagingStream = viewModel.pagingStream,
            onRetry = viewModel::retry,
            modifier = Modifier
                .fillMaxSize()
```

</details>

```kotlin
fun <T : Any> PagingScreenContent(
```
Recommended `PagingScreenContent` overload — owns the `LazyColumn`, `LoadMoreFooter`, and load-more trigger. Screens just provide the per-item lazy content. Use this for "infinite scrolling list" screens.

<details><summary>Example</summary>

```kotlin
PagingScreenContent(
    pagingStream = viewModel.pagingStream,
    onRetry = viewModel::onRetry,
) { coins ->
    items(coins) { coin -> CoinItem(coin = coin) }
}
```

</details>

<details><summary>Used in the template — <code>feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinMarketsScreen.kt:96</code></summary>

```kotlin
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        PagingScreenContent(
            pagingStream = viewModel.pagingStream,
            onRetry = viewModel::retry,
            modifier = Modifier
                .fillMaxSize()
```

</details>

```kotlin
fun <T : Any> PagingScreenContent(
```
Non-stream (Store5-optional) `PagingScreenContent` overload — a caller with an already-materialized `List<T>` (static content, a test fixture, or a non-Store5 source) renders through the SAME paging chrome (owned `LazyColumn` + empty-state slot) without being forced to construct a `PagingScreenStream`. There are no further pages to fetch, so the `LoadMoreFooter` and load-more trigger are intentionally omitted — the whole list is present. The trailing `lazyContent` lambda is signature-identical to the recommended stream overload above, so a screen upgrades from a static list to a live Store5 paging stream by swapping `items = staticList` for `pagingStream = viewModel.pagingStream` with ZERO change to its per-item content (the D2 dual-input, Store5-optional contract). Usage: ``` PagingScreenContent(items = uiState.coins) { coins -> items(coins) { coin -> CoinItem(coin = coin) } } ```

<details><summary>Example</summary>

```kotlin
PagingScreenContent(items = uiState.coins) { coins ->
    items(coins) { coin -> CoinItem(coin = coin) }
}
```

</details>

<details><summary>Used in the template — <code>feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinMarketsScreen.kt:96</code></summary>

```kotlin
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        PagingScreenContent(
            pagingStream = viewModel.pagingStream,
            onRetry = viewModel::retry,
            modifier = Modifier
                .fillMaxSize()
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/DataFreshnessIndicator.kt`

```kotlin
fun DataFreshnessIndicator(
```
Compact updating banner shown above content when a background refresh is in flight.

```kotlin
fun DefaultRefreshingBanner(fetchedAt: Instant? = null)
```
Default refreshing banner used as the initial value of `ScreenContent`'s `refreshingIndicator` slot. Pass `null` for the slot to suppress all in-flight progress UI.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/DefaultLottieAnimations.kt`

```kotlin
object DefaultLottieAnimations
```
Convenience suspend loaders for the bundled default Lottie animations that ship with `core-base/ui`.

<details><summary>Example</summary>

```kotlin
ScreenStateEmpty(
    visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty),
)
ScreenStateVisual.Lottie(spec = {
    LottieCompositionSpec.JsonString(MyAppRes.readBytes("anim.json").decodeToString())
})
```

</details>

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/AppScreenStateDefaults.kt:86</code></summary>

```kotlin
            loading = ScreenStateLoading.Skeleton(rowCount = 5),
            empty = ScreenStateEmpty(
                visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty),
                title = emptyTitle,
                message = emptyMessage,
            ),
            error = ScreenStateError(
```

</details>

- `val empty: suspend () -> LottieCompositionSpec = { loadJson("files/screenstate/empty.json") }`
- `val error: suspend () -> LottieCompositionSpec = { loadJson("files/screenstate/error.json") }`
- `val noNetwork: suspend () -> LottieCompositionSpec = { loadJson("files/screenstate/no_network.json") }`
- `val loading: suspend () -> LottieCompositionSpec = { loadJson("files/screenstate/loading.json") }`

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/RefreshableScreenContent.kt`

```kotlin
fun <T> RefreshableScreenContent(
```
Direct `ScreenDataStream` overload — the ViewModel exposes the repository-built stream and Compose collects `stream.state` lifecycle-aware here, wiring the pull gesture to `stream.refresh()`. Mirrors `ScreenContent`'s stream overload.

<details><summary>Example</summary>

```kotlin
RefreshableScreenContent(stream = viewModel.rates) { rates, _ -> RatesList(rates) }
```

</details>

```kotlin
fun <T> RefreshableScreenContent(
```
Non-stream (Store5-optional) overload — a caller with a plain `ScreenState` value (static content, a test fixture, a projected/combined payload the ViewModel owns) renders through the SAME `PullToRefreshBox` chrome without being forced to construct a `ScreenDataStream`. The refreshing indicator tracks `ScreenState.Content.freshnessSignal``.isRefreshing`. ```kotlin RefreshableScreenContent(state = uiState, onRefresh = viewModel::onRefresh) { data, _ -> … } ```

<details><summary>Example</summary>

```kotlin
RefreshableScreenContent(state = uiState, onRefresh = viewModel::onRefresh) { data, _ -> … }
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/ScreenContent.kt`

```kotlin
fun <T> ScreenContent(
```
Generic composable that renders any `ScreenState` with sensible defaults. Animates between states with a fade transition. When `ScreenState.Content.isRefreshing` is true, invokes the `refreshingIndicator` slot above the content area.

<details><summary>Example</summary>

```kotlin
@Composable
fun SavingsScreen(vm: SavingsViewModel) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    ScreenContent(state = state, onRetry = vm::onRetry) { data, freshness ->
        SavingsList(data.accounts)
    }
}
```

</details>

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailScreen.kt:108</code></summary>

```kotlin
        },
    ) { padding ->
        ScreenContent(
            state = screenState,
            onRetry = viewModel::onRetry,
            modifier = Modifier
                .fillMaxSize()
```

</details>

```kotlin
fun <T> ScreenContent(
```
Direct `ScreenDataStream` overload — the ViewModel exposes the stream (built in the repository) and Compose consumes it here: `stream.state` is collected lifecycle-aware and retry is wired to `stream.retry()`.

<details><summary>Example</summary>

```kotlin
// ViewModel:  val alerts = repository.alertsStream(viewModelScope)   // just the stream
ScreenContent(stream = viewModel.alerts) { alerts, _ -> LazyColumn { … } }
```

</details>

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailScreen.kt:108</code></summary>

```kotlin
        },
    ) { padding ->
        ScreenContent(
            state = screenState,
            onRetry = viewModel::onRetry,
            modifier = Modifier
                .fillMaxSize()
```

</details>

```kotlin
fun DefaultLoadingContent(
```
The framework's loading rendering — spinner, skeleton or branded animation, per `config`.

```kotlin
fun DefaultEmptyContent(
```
The framework's empty rendering — for a successful read that returned nothing. Distinct from error on purpose: "no results" is a valid outcome, and showing a retry affordance for it invites the user to retry something that already worked.

```kotlin
fun DefaultNoNetworkContent(
```
The framework's offline rendering, shown when the failure was connectivity rather than the request.

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:191</code></summary>

```kotlin

            PreviewBox(label = stringResource(Res.string.screens_showcase_state_gallery_preview_nonet_captive)) {
                DefaultNoNetworkContent(
                    onRetry = {},
                    isCaptivePortal = true,
                    modifier = Modifier.fillMaxWidth().height(320.dp),
                )
```

</details>

```kotlin
fun DefaultErrorContent(
```
The framework's error rendering, with the retry affordance wired to the screen's stream.

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:183</code></summary>

```kotlin

            PreviewBox(label = stringResource(Res.string.screens_showcase_state_gallery_preview_error)) {
                DefaultErrorContent(
                    error = RuntimeException("Demo error: server unreachable"),
                    onRetry = {},
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                )
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/ScreenStateDefaults.kt`

```kotlin
data class ScreenStateDefaults(
```
App-level defaults for `ScreenContent` and `PagingScreenContent` slots. Provides three-tier overrides: 1. **Per-call**: pass a slot lambda to `ScreenContent(empty = { ... })`. 2.

<details><summary>Example</summary>

```kotlin
@Composable
fun KptTheme(content: @Composable () -> Unit) {
    val defaults = remember {
        ScreenStateDefaults(
            empty = ScreenStateEmpty(
                visual = ScreenStateVisual.Vector(MyBrandIcons.EmptyBox),
                title = "Nothing here yet",
                message = "When you have data, it'll show up here.",
            ),
            error = ScreenStateError(
                messageFor = ::mapErrorToUserMessage,
                onShown = { error -> AppTelemetry.recordError("screen_state_error", error) },
            ),
        )
    }
    CompositionLocalProvider(LocalScreenStateDefaults provides defaults) {
        MaterialTheme { content() }
    }
}
```

</details>

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:135</code></summary>

```kotlin
    )

    val screenStateDefaults = appScreenStateDefaults()
    val financeColors = if (darkTheme) darkFinanceColors() else lightFinanceColors()

    KptMaterialTheme(theme = themeProvider) {
        // Provide the design-system token CompositionLocals app-wide so every widget
```

</details>

```kotlin
data class ScreenStateEmpty(
```
Configuration for the empty state.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/AppScreenStateDefaults.kt:85</code></summary>

```kotlin
        ScreenStateDefaults(
            loading = ScreenStateLoading.Skeleton(rowCount = 5),
            empty = ScreenStateEmpty(
                visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty),
                title = emptyTitle,
                message = emptyMessage,
            ),
```

</details>

```kotlin
data class ScreenStateError(
```
Configuration for the error state.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/AppScreenStateDefaults.kt:90</code></summary>

```kotlin
                message = emptyMessage,
            ),
            error = ScreenStateError(
                visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.error),
                title = errorTitle,
                messageFor = errorMessageFor,
                retryText = errorRetry,
```

</details>

```kotlin
val DefaultErrorMessageFor: (Throwable) -> String = ::defaultErrorMessage
```
Singleton reference used as the default for `ScreenStateError.messageFor`.

```kotlin
fun defaultErrorMessage(error: Throwable): String = when (val cat = categorize(error))
```
Library default for `ScreenStateError.messageFor`. Routes through `categorize` so apps that don't supply a custom mapper still get sensible category-specific copy out of the box.

```kotlin
data class ScreenStateNoNetwork(
```
Configuration for the no-network state, including captive-portal variant. When the rendered state is `isCaptivePortal == true`, `DefaultNoNetworkContent` surfaces a primary CTA button labelled `captivePortalActionText`.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/AppScreenStateDefaults.kt:96</code></summary>

```kotlin
                retryText = errorRetry,
            ),
            noNetwork = ScreenStateNoNetwork(
                message = nonetMessage,
                captivePortalMessage = captiveMessage,
                captivePortalActionText = captiveAction,
                retryText = nonetRetry,
```

</details>

```kotlin
data class ScreenStateCta(
```
A primary call-to-action shown alongside an empty/error state.

```kotlin
sealed interface ScreenStateLoading
```
Loading-state strategy. Library ships `Spinner` and `Skeleton`; consumers can supply `Custom`.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/AppScreenStateDefaults.kt:84</code></summary>

```kotlin
    ) {
        ScreenStateDefaults(
            loading = ScreenStateLoading.Skeleton(rowCount = 5),
            empty = ScreenStateEmpty(
                visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty),
                title = emptyTitle,
                message = emptyMessage,
```

</details>

```kotlin
sealed interface ScreenStateVisual
```
Visual element for empty/error/no-network states. Sealed: extend by adding a new variant here and a branch in `ScreenStateVisualRenderer`.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/AppScreenStateDefaults.kt:86</code></summary>

```kotlin
            loading = ScreenStateLoading.Skeleton(rowCount = 5),
            empty = ScreenStateEmpty(
                visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty),
                title = emptyTitle,
                message = emptyMessage,
            ),
            error = ScreenStateError(
```

</details>

```kotlin
val LocalScreenStateDefaults = compositionLocalOf { ScreenStateDefaults() }
```
App-wide ScreenState defaults.

<details><summary>Example</summary>

```kotlin
CompositionLocalProvider(LocalScreenStateDefaults provides myDefaults) { App() }
```

</details>

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:70</code></summary>

```kotlin
fun StateGalleryScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val sp = MaterialTheme.spacing
    val defaults = LocalScreenStateDefaults.current
    KptScaffold(
        onNavigationIconClick = onBackClick,
        title = "State Gallery (dev)",
        modifier = modifier.testTag(TestTags.StateGallery.SCREEN),
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/screen/WithScreenStateDefaults.kt`

```kotlin
fun WithScreenStateDefaults(
```
Wraps `content` so that its screen-state widgets (`ScreenContent`, `PagingScreenContent`) see `overrides` merged on top of the ambient `LocalScreenStateDefaults`. Useful for per-screen tweaks (e.g.

<details><summary>Example</summary>

```kotlin
WithScreenStateDefaults(
    overrides = LocalScreenStateDefaults.current.copy(
        empty = ScreenStateEmpty(title = "No bills", message = "Add your first bill below."),
    ),
) {
    ScreenContent(state = state, onRetry = ::onRetry) { bills -> /* ... */ }
}
```

</details>

```kotlin
fun ScreenStateDefaults.mergeWith(other: ScreenStateDefaults): ScreenStateDefaults =
```
Returns a new `ScreenStateDefaults` where every field in `other` overrides the corresponding field in `this`. Used by `WithScreenStateDefaults`. Simple "other wins" merge: every nested field from `other` replaces `this`'s.

<details><summary>Example</summary>

```kotlin
val partial = LocalScreenStateDefaults.current.copy(
    empty = ScreenStateEmpty(title = "Custom"),
)
WithScreenStateDefaults(overrides = partial) { /* ... */ }
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/DraftResumeBanner.kt`

```kotlin
fun DraftResumeBanner(
```
Banner that surfaces a saved draft when `state` is `DraftResumeState.HasDraft`. Renders nothing when `state` is `DraftResumeState.None`. Place above the form in a Column, or inside the `MutationScreenContent` draft-aware overload.

<details><summary>Example</summary>

```kotlin
DraftResumeBanner(
    state = draftState,
    onResume  = viewModel::onResumeDraft,
    onDiscard = viewModel::onDiscardDraft,
)
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/DraftSavePrompt.kt`

```kotlin
fun DraftSavePrompt(
```
Out-of-box dialog shown when a form submission fails due to a network error.

<details><summary>Example</summary>

```kotlin
Box {
    ScreenContent(state) { data -> FormBody(data) }
    SubmitProgressOverlay(submitState)
    DraftSavePrompt(
        submitState = submitState,
        onSaveDraft = viewModel::onSaveDraft,
        onDismiss   = viewModel::onDismissDraftPrompt,
    )
}
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/MutationScreenContent.kt`

```kotlin
fun <T, R> MutationScreenContent(
```
Convenience composable for edit/mutation screens. Combines `ScreenContent` + `SubmitProgressOverlay` + `SubmitResultHandler` into a single call.

<details><summary>Example</summary>

```kotlin
MutationScreenContent(
    screenState = uiState.screen,
    submitState = uiState.submit,
    onRetry = viewModel::onRetry,
    onSubmitted = { navigateBack() },
    onFailed = { _, category -> showError(category) },
) { data, _ ->
    EditForm(data = data, enabled = uiState.canInteract, onSave = viewModel::onSave)
}
```

</details>

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/AddOrEditLoanScreen.kt:166</code></summary>

```kotlin
        // success). The persistent inline status line is supplied via the `submitStatus` slot so
        // the "Saved" / "Failed / retry / offline" affordances render in-place.
        MutationScreenContent(
            state = ui,
            onRetry = viewModel::onRetry,
            onSubmitted = { onSaved() },
            modifier = Modifier.padding(padding),
```

</details>

```kotlin
fun <T, R> MutationScreenContent(
```
Direct `ScreenDataStream` (Store5 read) overload of `MutationScreenContent`.

<details><summary>Example</summary>

```kotlin
MutationScreenContent(
    stream = viewModel.editStream,          // repository-built ScreenDataStream
    submitState = uiState.submit,
    onSubmitted = { navigateBack() },
) { data, _ ->
    EditForm(data = data, onSave = viewModel::onSave)
}
```

</details>

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/AddOrEditLoanScreen.kt:166</code></summary>

```kotlin
        // success). The persistent inline status line is supplied via the `submitStatus` slot so
        // the "Saved" / "Failed / retry / offline" affordances render in-place.
        MutationScreenContent(
            state = ui,
            onRetry = viewModel::onRetry,
            onSubmitted = { onSaved() },
            modifier = Modifier.padding(padding),
```

</details>

```kotlin
fun <T, R> MutationScreenContent(
```
`MutationUiState`-typed overload. Reduces boilerplate when the ViewModel exposes a single `MutationUiState` property.

<details><summary>Example</summary>

```kotlin
MutationScreenContent(
    state = uiState.mutation,
    onRetry = viewModel::onRetry,
    onSubmitted = { navigateBack() },
) { data, _ ->
    EditForm(data = data, enabled = uiState.mutation.canInteract, onSave = viewModel::onSave)
}
```

</details>

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/AddOrEditLoanScreen.kt:166</code></summary>

```kotlin
        // success). The persistent inline status line is supplied via the `submitStatus` slot so
        // the "Saved" / "Failed / retry / offline" affordances render in-place.
        MutationScreenContent(
            state = ui,
            onRetry = viewModel::onRetry,
            onSubmitted = { onSaved() },
            modifier = Modifier.padding(padding),
```

</details>

```kotlin
fun <T, R> MutationScreenContent(
```
Draft-aware overload of `MutationScreenContent`. Shows a `DraftResumeBanner` above the form content when `draftResumeState` is `DraftResumeState.HasDraft`. The banner is omitted when `draftResumeState` is `DraftResumeState.None` (default).

<details><summary>Example</summary>

```kotlin
MutationScreenContent(
    screenState = uiState.screen,
    submitState = uiState.submit,
    draftResumeState = draftState,
    onResumeClick = viewModel::onResumeDraft,
    onDiscardClick = viewModel::onDiscardDraft,
    onRetry = viewModel::onRetry,
    onSubmitted = { navigateBack() },
) { data, _ ->
    EditForm(data = data, enabled = uiState.canInteract)
}
```

</details>

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/AddOrEditLoanScreen.kt:166</code></summary>

```kotlin
        // success). The persistent inline status line is supplied via the `submitStatus` slot so
        // the "Saved" / "Failed / retry / offline" affordances render in-place.
        MutationScreenContent(
            state = ui,
            onRetry = viewModel::onRetry,
            onSubmitted = { onSaved() },
            modifier = Modifier.padding(padding),
```

</details>

```kotlin
fun <T, R> MutationScreenContent(
```
`MutationUiState`-typed draft-aware overload. Combines read state, write state, and draft resume into a single call.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/AddOrEditLoanScreen.kt:166</code></summary>

```kotlin
        // success). The persistent inline status line is supplied via the `submitStatus` slot so
        // the "Saved" / "Failed / retry / offline" affordances render in-place.
        MutationScreenContent(
            state = ui,
            onRetry = viewModel::onRetry,
            onSubmitted = { onSaved() },
            modifier = Modifier.padding(padding),
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/SubmitButton.kt`

```kotlin
fun <R> SubmitButton(
```
Submit button that auto-disables while a submission is in-flight. Wraps `KptButton` and derives `enabled` from `SubmitState` so callers never have to wire `enabled = !submitState.isSubmitting` manually.

<details><summary>Example</summary>

```kotlin
SubmitButton(state = submitState, onClick = viewModel::onSubmit) {
    Text("Save")
}
```

</details>

```kotlin
fun SubmitButton(
```
Boolean-driven overload for callers that already compute the enabled flag externally (e.g., `screenState.canInteract(submitState)`).

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/SubmitProgressOverlay.kt`

```kotlin
fun SubmitProgressOverlay(
```
Semi-transparent scrim + centered `KptProgress` circular spinner shown while a submission is in-flight. Render above form content inside a `Box`.

<details><summary>Example</summary>

```kotlin
Box(Modifier.fillMaxSize()) {
    FormContent(enabled = !submitState.isSubmitting, onSubmit = { … })
    SubmitProgressOverlay(visible = submitState.isSubmitting)
}
```

</details>

```kotlin
fun <R> SubmitProgressOverlay(
```
`SubmitState`-typed overload. Shows the overlay when `state` is `SubmitState.Submitting`.

<details><summary>Example</summary>

```kotlin
SubmitProgressOverlay(state = submitState)
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/submit/SubmitResultHandler.kt`

```kotlin
fun <R> SubmitResultHandler(
```
Side-effect composable that fires callbacks when `state` reaches a terminal state. - `onSubmitted` fires once when `SubmitState.Submitted` is observed. - `onFailed` fires once when `SubmitState.Failed` is observed.

<details><summary>Example</summary>

```kotlin
SubmitResultHandler(
    state = submitState,
    onSubmitted = { clientId ->
        onNavigateToDetail(clientId)
        // no reset needed — navigating away destroys the ViewModel
    },
    onFailed = { error, category ->
        viewModel.onDismiss()          // calls submit.reset()
        when (category) {
            ErrorCategory.Network -> showNoNetworkSheet()
            ErrorCategory.Auth    -> onNavigateToLogin()
            else                  -> showErrorDialog(error.message)
        }
    },
)
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/ImageLoaderExt.kt`

```kotlin
fun rememberImageLoader(): ImageLoader
```
Creates and remembers an instance of the default ImageLoader configured with platform-specific settings for caching and logging. The ImageLoader is initialized using the current platform context.

```kotlin
fun getDefaultImageLoader(context: PlatformContext): ImageLoader = ImageLoader
```
Creates and returns a default instance of an ImageLoader configured with common settings such as logging, caching policies, memory caching, and platform-specific component support.

```kotlin
fun LocalImageLoaderProvider(imageLoader: ImageLoader, content: @Composable () -> Unit)
```
Provides a composable local context for an `ImageLoader` to be used within a Composable hierarchy.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/JankStatsExtension.kt`

```kotlin
expect fun TrackScrollJank(scrollableState: ScrollableState, stateName: String)
```
Reports dropped frames during scrolling of `scrollableState`, tagged `stateName` so one screen's jank is attributable in a trace.

<details><summary>Used in the template — <code>core/ui/src/androidMain/kotlin/kpt/core/ui/utils/JankStatsExtensions.kt:71</code></summary>

```kotlin
 */
@Composable
fun TrackScrollJank(scrollableState: ScrollableState, stateName: String) {
    TrackJank(scrollableState) { metricsHolder ->
        snapshotFlow { scrollableState.isScrollInProgress }.collect { isScrollInProgress ->
            metricsHolder.state?.apply {
                if (isScrollInProgress) {
```

</details>

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/SharedElementExt.kt`

```kotlin
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
```
CompositionLocal that provides access to an `AnimatedVisibilityScope` within the composition. Default value is null, requiring an explicit provider upstream in the composition.

```kotlin
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
```
CompositionLocal that provides access to a `SharedTransitionScope` within the composition. Used for creating shared element transitions between composables. Default value is null, requiring an explicit provider upstream in the composition.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/ShareUtils.kt`

```kotlin
expect object ShareUtils
```
Platform-specific utility for sharing content with other applications. This expect class requires platform-specific implementations.

- `suspend fun shareText(text: String)` — Shares text content with other applications.
- `suspend fun shareImage(title: String, image: ImageBitmap)` — Shares an image with other applications.
- `suspend fun shareImage(title: String, byte: ByteArray)` — Shares an image with other applications using raw byte data.
- `fun openUrl(url: String)` — Opens the specified URL in the device's default web browser.
- `fun openAppInfo()` — Opens the application info screen in the device settings. Typically used to allow users to manage app permissions, storage, or other app-specific settings.
- `fun callPhone(number: String)` — Initiates a phone call using the platform dialer UI.
- `fun sendEmail(to: String, subject: String? = null, body: String? = null)` — Opens the platform email composer with the given parameters.
- `fun sendViaSMS(number: String, message: String)` — Opens the platform SMS composer with the given parameters.
- `fun copyText(text: String)` — Copies the given text to the system clipboard.
- `suspend fun shareApp(storeLink: String, message: String = "")` — Shares the app store link with a custom message. Opens the platform share sheet with the store link and message combined, allowing users to share the app with others via various apps.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/StringExt.kt`

```kotlin
val String.capitalizeEachWord: String
```
Extension property that returns a string with the first letter of each word capitalized. For example, "hello world" becomes "Hello World".

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/util/Transition.kt`

```kotlin
typealias EnterTransitionProvider =
```
Function type for providing nullable enter transitions in navigation. Used with `AnimatedContentTransitionScope` to define how a destination enters the screen.

```kotlin
typealias ExitTransitionProvider =
```
Function type for providing nullable exit transitions in navigation. Used with `AnimatedContentTransitionScope` to define how a destination exits the screen.

```kotlin
typealias NonNullEnterTransitionProvider =
```
Function type for providing non-null enter transitions in navigation. Used with `AnimatedContentTransitionScope` to define how a destination enters the screen.

```kotlin
typealias NonNullExitTransitionProvider =
```
Function type for providing non-null exit transitions in navigation. Used with `AnimatedContentTransitionScope` to define how a destination exits the screen.

```kotlin
val DEFAULT_STAY_TRANSITION_TIME_MS: Int =
```
The default transition time (in milliseconds) for all "stay"/no-op transitions in the `TransitionProviders`. This should be at least as large as any other transition that might also be happening during a navigation.

```kotlin
val AnimatedContentTransitionScope<NavBackStackEntry>.isSameGraphNavigation: Boolean
```
Checks if the parent of the destination before and after the navigation is the same. This is useful to ignore certain enter/exit transitions when navigating between distinct, nested flows.

```kotlin
object TransitionProviders
```
Contains standard "transition providers" that may be used to specify the `EnterTransition` and `ExitTransition` used when building a typical composable destination.

- `val fadeIn: EnterTransitionProvider =` — Fades the new screen in. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val pushLeft: EnterTransitionProvider =` — Slides the new screen in from the left of the screen.
- `val pushRight: EnterTransitionProvider =` — Slides the new screen in from the right of the screen.
- `val slideUp: EnterTransitionProvider =` — Slides the new screen in from the bottom of the screen. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val stay: EnterTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately appear while the other screen transitions away.
- `val fadeOut: ExitTransitionProvider =` — Fades the current screen out. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val pushLeft: ExitTransitionProvider =` — Slides the current screen out to the left of the screen.
- `val pushRight: ExitTransitionProvider =` — Slides the current screen out to the right of the screen.
- `val slideDown: ExitTransitionProvider =` — Slides the current screen down to the bottom of the screen. Note that this represents a `null` transition when navigating between different nested navigation graphs.
- `val stay: ExitTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately disappear while the other screen transitions into place.
- `val sharedAxisForward: EnterTransitionProvider = TransitionProviders.Enter.pushLeft` — Hardcoded aliases — kept for callers that don't have a `Motion` in scope.
- `val sharedAxisBack: EnterTransitionProvider = TransitionProviders.Enter.pushRight`
- `val fadeThrough: EnterTransitionProvider = TransitionProviders.Enter.fadeIn`
- `val slideUp: EnterTransitionProvider = TransitionProviders.Enter.slideUp`
  _…more members; read the file._

```kotlin
object RootTransitionProviders
```
Contains standard "transition providers" that may be used to specify the `EnterTransition` and `ExitTransition` used when building a root `NavHost`, which requires a non-null value.

- `val fadeIn: NonNullEnterTransitionProvider =` — Fades the new screen in.
- `val none: NonNullEnterTransitionProvider =` — There is no transition for the entering screen.
- `val pushLeft: NonNullEnterTransitionProvider =` — Slides the new screen in from the left of the screen.
- `val pushRight: NonNullEnterTransitionProvider =` — Slides the new screen in from the right of the screen.
- `val slideUp: NonNullEnterTransitionProvider =` — Slides the new screen in from the bottom of the screen.
- `val stay: NonNullEnterTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately appear while the other screen transitions away.
- `val fadeOut: NonNullExitTransitionProvider =` — Fades the current screen out.
- `val none: NonNullExitTransitionProvider =` — There is no transition for the exiting screen. Unlike the `stay` transition, this will immediately remove the outgoing screen even if there is an ongoing enter transition happening for the new screen.
- `val pushLeft: NonNullExitTransitionProvider =` — Slides the current screen out to the left of the screen.
- `val pushRight: NonNullExitTransitionProvider =` — Slides the current screen out to the right of the screen.
- `val slideDown: NonNullExitTransitionProvider =` — Slides the current screen down to the bottom of the screen.
- `val stay: NonNullExitTransitionProvider =` — A "no-op" transition: this changes nothing about the screen but "lasts" as long as other standard transitions in order to leave the screen in place such that it does not immediately disappear while the other screen transitions into place.
- `val sharedAxisForward: NonNullEnterTransitionProvider = RootTransitionProviders.Enter.pushLeft` — Hardcoded aliases — kept for callers that don't have a `Motion` in scope.
- `val sharedAxisBack: NonNullEnterTransitionProvider = RootTransitionProviders.Enter.pushRight`
  _…more members; read the file._

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/BackgroundEvent.kt`

```kotlin
interface BackgroundEvent
```
Almost all the events in the app involve navigation or toasts. To prevent accidentally navigating to the same view twice, by default, events are ignored if the view is not currently resumed.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/BaseMutationViewModel.kt`

```kotlin
abstract class BaseMutationViewModel<T, R>(
```
The single base class for edit/mutation-screen ViewModels — the one a feature module extends. It bundles a read stream (`mutableScreenState`) with a submit lifecycle, exposing both through the inherited MVI `stateFlow` as `MutationUiState`.

<details><summary>Example</summary>

```kotlin
// In-session (no persistence) — mode defaults to MutationMode.InSession:
class EditEntityViewModel(private val repo: EntityRepository, private val id: String) :
    BaseMutationViewModel<Entity, Unit>() {
    init { viewModelScope.launch { mutableScreenState.value = ScreenState.Content(repo.get(id)) } }
    override suspend fun performSubmit(payload: Entity) = repo.update(id, payload)
}

// Offline-resilient (durable draft + auto-retry + three-case resume):
class EditEntityViewModel(
    outbox: SubmitOutbox<Entity>,
    entityId: String?,
    private val repo: EntityRepository,
) : BaseMutationViewModel<Entity, Entity>(
    MutationMode.Draft(
        outbox = outbox,
        formKey = "entity",                // groups this form's drafts
        uniqueKey = entityId ?: "new",     // N concurrent drafts (one per entity); null = singleton
        autoSaveDraft = true,              // persist silently on network failure
    ),
) {
    override suspend fun performSubmit(payload: Entity): Entity = repo.upsert(payload)
}
```

</details>

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/EditLoanViewModel.kt:55</code></summary>

```kotlin
    outbox: SubmitOutbox<Loan>,
    val loanId: String?,
) : BaseMutationViewModel<Loan, Loan>(
    MutationMode.Draft(
        outbox = outbox,
        formKey = FORM_KEY,
        uniqueKey = loanId ?: NEW_LOAN_KEY,
```

</details>

- `val uiState: StateFlow<MutationUiState<T, R>> get() = stateFlow` — Backward-compat alias for the inherited `stateFlow`.
- `val resumeStream: Flow<DraftResumeState<T>> = draftMode?.let { m ->`
- `open fun onSubmit(payload: T) = trySendAction(MutationAction.Submit(payload))` — Trigger a form submission. No-op if already `SubmitState.Submitting`.
- `open fun onRetry() = trySendAction(MutationAction.Retry)` — Retry the last failed submission.
- `open fun onDismissResult() = trySendAction(MutationAction.Dismiss)` — Dismiss the result overlay and return to `SubmitState.Idle`. Override to add VM-local cleanup (e.g. reset form fields); always call `super.onDismissResult()` to keep the reset behaviour.
- `fun onSaveDraft()` — User-prompted draft save (Draft mode only) — persists the in-memory pending payload after the user confirms "Save Draft". No-op in `MutationMode.InSession` and in `autoSaveDraft = true` mode.
- `fun onDiscardDraft()` — Discard the in-memory pending payload and dismiss the save prompt (Draft mode only). Does NOT delete an already-persisted draft — use `onDiscardSavedDraft` for that.
- `open fun onResumeDraft() = trySendAction(MutationAction.ResumeDraft)` — Resume the saved draft — the screen reads `MutationUiState.resumableDraft` to pre-fill; hides the prompt.
- `open fun onStartFresh() = trySendAction(MutationAction.StartFreshDraft)` — Keep the saved draft (recoverable in Sync & Drafts) but start a blank new entry.
- `open fun onDiscardSavedDraft() = trySendAction(MutationAction.DiscardSavedDraft)` — Permanently delete the saved draft for this form, then edit blank.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/BaseViewModel.kt`

```kotlin
abstract class BaseViewModel<S, E, A>(
```
A base `ViewModel` that helps enforce the unidirectional data flow pattern and associated responsibilities of a typical ViewModel: - Maintaining and emitting a current state (of type `S`) with the given `initialState`.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailViewModel.kt:53</code></summary>

```kotlin
    private val repository: LoanRepository,
    private val loanId: String,
) : BaseViewModel<Unit, Nothing, LoanDetailAction>(Unit) {

    /**
     * Continuous reactive stream — emits every time the loan row changes in Room. Consumes the
     * repository's per-loan [kpt.core.base.store.screen.ScreenDataStream] (absent id → Empty); the
```

</details>

- `val stateFlow: StateFlow<S> = mutableStateFlow.asStateFlow()` — A `StateFlow` representing state updates.
- `val eventFlow: Flow<E> = eventChannel.receiveAsFlow()` — A `Flow` of one-shot events. These may be received and consumed by only a single consumer. Any additional consumers will receive no events.
- `val actionChannel: SendChannel<A> = internalActionChannel` — A `SendChannel` for sending actions to the ViewModel for processing.
- `fun trySendAction(action: A)` — Convenience method for sending an action to the `actionChannel`.

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/MutationAction.kt`

```kotlin
sealed interface MutationAction<out T>
```
MVI actions emitted by mutation-flow screens (form edit, settings save, etc.).

### `core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/MutationMode.kt`

```kotlin
sealed interface MutationMode<out T>
```
How a `BaseMutationViewModel` handles its submit — the single knob that replaces the old `BaseSubmitMutationViewModel` / `BaseDraftMutationViewModel` class split.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/EditLoanViewModel.kt:56</code></summary>

```kotlin
    val loanId: String?,
) : BaseMutationViewModel<Loan, Loan>(
    MutationMode.Draft(
        outbox = outbox,
        formKey = FORM_KEY,
        uniqueKey = loanId ?: NEW_LOAN_KEY,
        autoSaveDraft = true,
```

</details>

---

_26 type(s), 140 function(s)/property(ies); 144 carry KDoc at source; 32 authored example(s); 36 live call site(s)._
<!-- api-docs:end -->
