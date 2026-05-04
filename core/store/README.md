# `core/store` — Consumer Customization Seam

This module is the **single discoverable customization point** for consumer apps adopting
`kmp-project-template`. It scaffolds the per-app integration layer for the framework's
`core-base/store` (state model) and `core-base/ui` (ScreenState rendering).

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
