# `feature/_archive/`

This directory holds **archived feature modules** — code that's no longer part
of the default build but is preserved in-tree so forks that adopted these
features can keep using them without forking history.

## What's here

| Module                      | Original purpose                                 | Canonical replacement                                                  |
|-----------------------------|--------------------------------------------------|------------------------------------------------------------------------|
| `_archive/crypto/`          | CryptoWatchlist / CoinDetail — `PagingScreenStream` + `ScreenDataStream` showcase | `feature/loans` (list pattern), `feature/rates` (network-backed stream) |
| `_archive/watchlist/`       | Personal coin watchlist — `SubmitHandler` showcase | `feature/loans` `EditLoanViewModel` (same `SubmitHandler` pattern, banking domain) |
| `_archive/alerts/`          | Price alerts — `DraftSubmitHandler` showcase     | `feature/bills` `EditBillReminderViewModel` (same `DraftSubmitHandler` pattern, banking domain) |

## How re-enable

Each archived module ships as a fully-formed gradle module — its `build.gradle.kts`,
sources, manifest, and tests are intact. To re-enable any of them:

1. Uncomment the matching `include(":feature:_archive:crypto")` (or `watchlist`,
   `alerts`) line in the root `settings.gradle.kts`.
2. Add the module back as an `implementation(projects.feature.<name>)` dep in
   `cmp-navigation/build.gradle.kts`.
3. Re-register the Koin module in `cmp-navigation/.../KoinModules.kt`.
4. Re-add the nav graph wiring in
   `cmp-navigation/.../authenticated/AuthenticatedNavigation.kt`.

The package coordinates (`org.mifos.feature.crypto`, `org.mifos.feature.watchlist`,
`org.mifos.feature.alerts`) are unchanged — no source rewrite is required.

## Grace window

These modules will remain in-tree until **2026-08-23** (90 days from the pivot to
Money Toolkit). Forks that depend on them should either:

- Migrate to the banking-domain equivalents (recommended), **or**
- Vendor the archived modules into their own fork before the grace window
  closes.

See the project root `CLAUDE.md` for the toolkit identity and the canonical
showcase mapping post-pivot.
