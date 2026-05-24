# `feature/_archive/watchlist/` — archived

> **Archived 2026-05-24.** No longer part of the default build.

Originally the canonical showcase for the framework's `SubmitHandler` — the
simple (non-draft-persistent) form-submit handler used to add a coin to a
personal watchlist.

## Why archived

The Money Toolkit pivot (sub-plan 08 of `banking-utility-toolkit`) replaces the
crypto-themed demo features with personal-finance utilities. The watchlist
module is preserved here as a **reference implementation**.

## Re-enable

1. **`settings.gradle.kts`** — uncomment:
   ```kotlin
   // include(":feature:_archive:watchlist")
   ```
2. **`cmp-navigation/build.gradle.kts`** — add:
   ```kotlin
   implementation(projects.feature.archive.watchlist)
   ```
3. **`cmp-navigation/.../KoinModules.kt`** — re-import and include
   `WatchlistModule`.
4. **`cmp-navigation/.../authenticated/AuthenticatedNavigation.kt`** — re-add
   `personalWatchlistDestination(...)` + the `navigateToPersonalWatchlist`
   import / nav-bar wiring.

## Canonical replacement showcase

The `SubmitHandler` showcase now lives in **`feature/loans`** —
`EditLoanViewModel` is the same pattern wired to the banking-domain
`LoanRepository` instead of the local-only watchlist DAO. The form UX
(`MutationScreenContent` + `SubmitHandler`) is structurally identical.

See the project-root `CLAUDE.md` for the full showcase mapping.

## Grace window

This module will be removed entirely **on or after 2026-08-23** (90 days from
archive). Forks must either migrate to `feature/loans` patterns or vendor this
module into their own fork before then.
