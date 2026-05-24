# `feature/_archive/crypto/` — archived

> **Archived 2026-05-24.** No longer part of the default build.

Originally the canonical showcase for two framework patterns:

- **`PagingScreenStream`** — infinite-scroll lists backed by Store5
- **`ScreenDataStream`** — single-item network-backed state stream

## Why archived

The Money Toolkit pivot (sub-plan 08 of `banking-utility-toolkit`) replaces the
crypto-themed demo features with personal-finance utilities (loan tracking, bill
reminders, rate watching, calculators). The crypto module remains in-tree as a
**reference implementation** for forks that need its patterns, but it is no
longer wired into the navigation graph, Koin DI, or `settings.gradle.kts`.

## Re-enable

To bring this module back into your fork's build:

1. **`settings.gradle.kts`** — uncomment:
   ```kotlin
   // include(":feature:_archive:crypto")
   ```
2. **`cmp-navigation/build.gradle.kts`** — add:
   ```kotlin
   implementation(projects.feature.archive.crypto)
   ```
   (Gradle path `:feature:_archive:crypto`.)
3. **`cmp-navigation/.../KoinModules.kt`** — re-import and include
   `CryptoModule`.
4. **`cmp-navigation/.../authenticated/AuthenticatedNavigation.kt`** — re-add
   `cryptoGraph(navController)` + the `navigateToCrypto` import / nav-bar wiring.

No source code rewrite is required — the package coordinates and APIs are
unchanged.

## Canonical replacement showcase

The patterns this module demonstrated now live in the banking domain:

- `PagingScreenStream` → `feature/loans` `PersonalLoansListViewModel` (paginated
  loan list with framework-level Loading / Empty / Error / Content + load-more
  states).
- `ScreenDataStream` → `feature/rates` `InterestRatesViewModel` (the FRED-backed
  per-series stream).

See the project-root `CLAUDE.md` for the full showcase mapping.

## Grace window

This module will be removed entirely **on or after 2026-08-23** (90 days from
archive). Forks must either migrate to the canonical replacements or vendor
this module into their own fork before then.
