# `core/store` — Consumer Customization Seam

This module is the **single discoverable customization point** for consumer apps adopting
`kmp-project-template`. It scaffolds the per-app integration layer for the framework's
`core-base/store` (state model) and `core-base/ui` (ScreenState rendering).

## What you get for free (no per-screen code)

By calling `ScreenContent(state, onRetry) { data -> ... }` (or `PagingScreenContent`),
your screen automatically gets:

- ✅ Loading → Content / NoNetwork / Error / Empty transitions, animated (M3 fade)
- ✅ Captive-portal detection (hotel WiFi)
- ✅ Auto-refresh when network reconnects (debounced 300ms)
- ✅ `lastContent` preservation during refresh (no flicker)
- ✅ Pagination with cache-first reads + load-more trigger + footer + retry
- ✅ Branded visuals from `appScreenStateDefaults()` (empty / error / no-network)
- ✅ Skeleton loading (when `ScreenStateLoading.Skeleton` is the default)
- ✅ A11y semantics (TalkBack/VoiceOver state announcements + liveRegion)
- ✅ Default `messageFor` routes through `categorize()` (network / auth / server / generic)

You write **zero state-handling code**. Screens cannot break offline-first by misuse —
the decision logic lives in `core-base/store`'s `DecisionEngine`, used by every flow
(both single-key `asScreenStream` and paged `asPagingScreenStream`).

## Which framework API for which screen type?

`PagingScreenContent` is **not** a generic "all UI" wrapper — it's specifically for
infinite-scroll paginated lists driven by `asPagingScreenStream`. Detail pages,
non-paginated lists, forms, and dashboards each have their own canonical pattern:

| Screen type | Framework API | Notes |
|---|---|---|
| **Detail (1 entity)** | `ScreenContent(state) { item -> ... }` | Single-key flow via `asScreenStream(key)`. CoinDetail uses this. |
| **Detail (1 entity) + pull-to-refresh** | `RefreshableScreenContent(state, onRefresh) { item -> ... }` | Same as above wrapped in M3 `PullToRefreshBox`. Pull spinner appears while `state.freshness == UPDATING`. |
| **Detail (dynamic key)** | `ScreenContent(state) { item -> ... }` | ViewModel uses `asScreenStream(keyFlow, ...)` overload. Re-streams when key changes. |
| **Paginated list (infinite scroll)** | `PagingScreenContent { items(coins) { ... } }` | Auto LazyColumn + LoadMoreFooter + load-more trigger + **pull-to-refresh on by default** (`enablePullToRefresh = true`). CryptoWatchlist uses this. |
| **Paginated list (custom layout)** | `PagingScreenContent(...) { items, _ -> /* your LazyColumn */ }` | Slot-only overload — you own the LazyColumn (sticky headers, sectioned lists, etc.). |
| **Non-paginated list** | `ScreenContent(state) { list -> LazyColumn { items(list) { ... } } }` | `asScreenStream` returning `List<T>`. No load-more wiring. Wrap in `RefreshableScreenContent` for pull-to-refresh. |
| **Form / pure UI (no remote data)** | none — regular Compose | No state stream needed. |
| **Multi-source combined** | Manual `combine(s1.state, s2.state) { ... }` in ViewModel → expose `Flow<ScreenState<X>>` to screen | A `combineScreenStreams` framework helper is on the roadmap. Today: do it in the ViewModel. |
| **Dashboard with several independent panels** | One `ScreenContent` per panel, each with its own `asScreenStream` | Each panel owns its loading/empty/error UX independently. |

All variants share the same offline-first guarantees from `core-base/store`'s
`DecisionEngine`. Branded visuals come from `appScreenStateDefaults()` in this module
and apply to every screen automatically (wired into `MifosTheme`).

## What you customize here

- `AppScreenStateDefaults.kt` — brand visuals, copy, Lottie animations, telemetry
  callbacks (`onShown`). Already wired into `MifosTheme` via
  `LocalScreenStateDefaults` — every screen picks up your changes app-wide.
- `AppErrorMapper.kt` — domain error → user message (extends framework `categorize()`).
- `AppStoreRegistry.kt` — your named Store qualifiers.
- `di/StoreModule.kt` — Koin module for Store factories.

## What lives here

| File | Purpose | Forks customize by |
|---|---|---|
| `AppStoreRegistry.kt` | Single named-qualifier registry for every `Store` the app exposes | adding `val Foo = store("foo")` entries |
| `AppScreenStateDefaults.kt` | App-wide `ScreenStateDefaults` factory | swapping visuals (Vector → Lottie), tweaking copy, wiring telemetry hooks |
| `AppErrorMapper.kt` | `Throwable → user message` mapper | adding domain-error branches before falling back to `categorize()` |
| `di/StoreModule.kt` | Koin module that registers Stores | adding `single(qualifier = ...) { ... }` factories |

## What does NOT live here

- **`core-base/store`** — pure state model (`ScreenState`, `Store5` integration, `categorize`). Framework-shared, do not edit.
- **`core-base/ui`** — generic `ScreenContent`, default Material 3 visuals, `LocalScreenStateDefaults`. Framework-shared, do not edit.

If you find yourself wanting to change something in `core-base/*`, push the change to
this module instead — that's exactly what the seam is for. The framework's promise is
that `core-base/*` upgrades cleanly across template versions; `core/store` is yours to
diverge.

## Wiring it up

After adding your customizations, provide the defaults at the app's theme root:

```kotlin
@Composable
fun MifosApp(content: @Composable () -> Unit) {
    MifosTheme {
        CompositionLocalProvider(
            LocalScreenStateDefaults provides appScreenStateDefaults(),
        ) {
            content()
        }
    }
}
```

And register the Koin module at startup:

```kotlin
startKoin {
    modules(appStoreModule, /* ...other modules */)
}
```

## Dependency rule

`core/store` may depend on `core-base/store` and `core-base/ui` (both `api`-exposed so
consumers get the framework types transitively). **Nothing in `core-base/*` may depend
on `core/store`** — that would create a cycle and break the framework-vs-fork separation
the seam exists to provide. Enforced via `dependency-guard` baseline.
