# `feature/_archive/alerts/` — archived

> **Archived 2026-05-24.** No longer part of the default build.

Originally the canonical showcase for the framework's `DraftSubmitHandler` —
the offline-resilient form-submit handler that persists the draft payload
across process death.

## Why archived

The Money Toolkit pivot (sub-plan 08 of `banking-utility-toolkit`) replaces the
crypto-themed demo features with personal-finance utilities. The price-alerts
module is preserved here as a **reference implementation**.

## Re-enable

1. **`settings.gradle.kts`** — uncomment:
   ```kotlin
   // include(":feature:_archive:alerts")
   ```
2. **`cmp-navigation/build.gradle.kts`** — add:
   ```kotlin
   implementation(projects.feature.archive.alerts)
   ```
3. **`cmp-navigation/.../KoinModules.kt`** — re-import and include
   `AlertsModule`.
4. **`cmp-navigation/.../authenticated/AuthenticatedNavigation.kt`** — re-add
   `alertsGraph(navController)` + the `navigateToAlerts` import / nav-bar
   wiring.

## Canonical replacement showcase

The `DraftSubmitHandler` showcase now lives in **`feature/bills`** —
`EditBillReminderViewModel` is the same pattern wired to the banking-domain
`BillReminderRepository`. The form UX (`MutationScreenContent` + draft
persistence + offline retry polish) is structurally identical.

See the project-root `CLAUDE.md` for the full showcase mapping.

## Grace window

This module will be removed entirely **on or after 2026-08-23** (90 days from
archive). Forks must either migrate to `feature/bills` patterns or vendor this
module into their own fork before then.
