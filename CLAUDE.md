# Claude Code - App Toolkit (KMP white-label template)

**Last Updated:** 2026-08-14
**Project Type:** Kotlin Multiplatform (KMP) — brand-neutral white-label template
**Platforms:** Android | iOS | macOS | Desktop (Windows/macOS/Linux) | Web

---

## Quick Links

🚀 **New fork? Start here:**
- [Fork Quickstart](docs/setup/FORK_QUICKSTART.md) - Day-1 customization checklist for new forks

📖 **Domain-Specific Guides:**
- [GitHub Actions & CI/CD](.github/CLAUDE.md) - Workflows, custom actions, secrets
- [Fastlane Deployment](deployment/BOOTSTRAP.md) - Deployment architecture, secrets bootstrap, all 18 targets
- [Bash Scripts](scripts/CLAUDE.md) - Setup, deployment, and verification scripts

📚 **Deep-Dive Documentation:**
- [Troubleshooting Guide](docs/claude/troubleshooting.md)
- [Onboarding Guide](docs/claude/onboarding.md)
- [Deployment Playbook](docs/claude/deployment-playbook.md)
- [Patterns & Best Practices](docs/claude/patterns.md)
- [Independent Cards Pattern](docs/claude/PATTERN-independent-cards.md) - Multi-card dashboards where each card has its own ScreenState (loading / error / empty / content) — `IndependentCardLayout` + `DashboardProgressBar` + `aggregateDashboardProgress`
- [Store Implementation Guide](docs/claude/store-implementation.md) - Offline-first streams, mutations, FetchPolicy, cache lifecycle
- [Room Invalidation Bridge](core-base/database/src/commonMain/kotlin/kpt/core/base/database/invalidation/README.md) - `RoomChangeBus` + `daoFlow{}` + `notifyingWrite{}` — absorbs Room 3 alpha05's wasmJs async-fan-out gap so DAO Flow consumers re-emit after writes; no-op on Android/Desktop/iOS
- [Motion + Transitions](core-base/ui/MOTION.md) - Symmetric durations, M3 patterns, debug Transition Gallery
- [GitHub Actions Deep Dive](docs/claude/github-actions-deep-dive.md)
- [Secrets Management](docs/claude/secrets-management.md)
- [Version Handling](docs/claude/version-handling.md)

🐛 **Known Issues:**
- [Infrastructure Bugs & Workarounds](docs/analysis/BUGS_AND_ISSUES.md)

---

## Project Overview

This is the **App Toolkit** — a brand-neutral, open-source white-label template
built on Kotlin Multiplatform. Every brand-touching value ships as a neutral
placeholder (`com.example.app` / "App Toolkit" / placeholder Firebase); a fork
fills `app-profile/`, runs `./gradlew syncForkConfig`, and the identity flows to
every platform surface.

The bundled demo features (loan tracking, bill reminders, interest-rate watching,
calculators, country-level macro indicators) exist to **showcase the 8 Store5
archetypes** end-to-end — each demo is the canonical proof of one or more archetypes
wired through the same offline-first store contract. No login. No backend of its own.
Keep the demos, swap their branding, or remove the ones you don't need.

The project doubles as a **reference implementation** for every architectural
pattern in `core-base/store` and `core-base/ui` — each shipped demo is the
canonical showcase for one or more framework archetypes (see "Store Archetype
Showcases" below).

CI/CD infrastructure spans **5 platforms** and **18 deployment targets** (see `deployment/DEPLOYMENT_MANIFEST.yaml`).

### Architecture

```
kmp-project-template/
├── cmp-android/          # Android application
├── cmp-ios/             # iOS Xcode project
├── cmp-desktop/         # Desktop (JVM) application
├── cmp-web/             # Web (Kotlin/JS) application
├── cmp-shared/          # Shared KMP business logic
├── core/                # Core modules (data, domain, network, etc.)
├── core-base/           # Base platform implementations
├── feature/             # Feature modules
├── deployment/          # Deployment automation — 18 targets across 5 platforms
├── .github/workflows/   # GitHub Actions CI/CD
└── scripts/             # Bash automation scripts
```

### Toolkit feature showcase

Every shipped feature exists for two reasons: it's a working tool, AND it's the
canonical demo of one or more framework patterns. Forks can keep the lot, swap
the per-feature branding, or selectively remove features they don't need.

| Feature                   | What it does                                              | Pattern showcased                                          |
|---------------------------|-----------------------------------------------------------|------------------------------------------------------------|
| **B1 Loan Tracker**       | Personal loans — track principal, EMI, due dates locally  | `PagingScreenStream` list + `SubmitHandler` edit form      |
| **B2 EMI Calculator**     | Compute monthly EMI for any loan                          | Pure local state (no Store)                                |
| **B3 Affordability**      | "How much loan can I afford?" calculator                  | Pure local state + derived multi-input math                |
| **B4 Bill Reminders**     | Recurring bills + in-app notification scheduler           | `DraftSubmitHandler` (offline-resilient form)              |
| **B5 Amortization**       | Full payment schedule for any loan                        | Read-side projection of `LoanRepository`                   |
| **B6 Loan Comparison**    | Side-by-side total-cost comparison wizard                 | Multi-step wizard state machine                            |
| **B7 Interest Rates**     | FRED-backed federal funds / mortgage / treasury series    | `NETWORK_WITH_CACHE` `ScreenDataStream` + 4-stream combine |
| **B8 Country Macro**      | GDP / CPI / unemployment from World Bank                  | Multi-source combine + country picker                      |
| **Home dashboard**        | Loans summary + upcoming bills + rates + USD exchange     | `combineScreenStates` 4-way fan-in                         |
| **Currency Rates**        | Live FX rates by base currency                            | `Store` + search filter + emptyIfContent                   |
| **Rate History**          | Historical FX charts                                      | Dynamic-key flow + auto-refresh                            |
| **Amortization Schedule** | Month-by-month payment breakdown for any loan             | OFFLINE_LOCAL_ONLY projection via `ScreenDataStream`       |

## Store Archetype Showcases

The generator routes on **`feature_profile.store_archetype`** — the primary key that picks the
`core/store` factory and the module chain. There are **8 archetypes**; every row below resolves to
a real demo `*Store.kt` / `*ViewModel.kt` / `*Test.kt`. The decision matrix + module chain is
`FEATURE_AUTHORING.md` (in-repo summary) and `docs/architecture/STORE_DATA_API.md` (canonical).

> **The archetype ↔ showcase contract is enforced, not just documented.**
> `core/store/STORE_ARCHETYPES.yaml` is the machine-readable source of truth; the table below is a
> human projection of it. `scripts/product-health/checks/store-archetype-coverage.sh` fails the build
> if any archetype loses its last showcase, or if a showcase stops calling its declared factory.
>
> That registry also declares **`cache_first`** per archetype. Two are non-cache-first *by
> definition* — **MEMORY_ONLY** (no SourceOfTruth, so the cache dies with the process) and
> **NETWORK_ONLY** (network-first, cache only as a failure fallback). Do not "fix" them to be
> cache-first: that deletes the archetype demo. This is not hypothetical — it happened, the two
> guarding tests were `assertTrue(true, …)` and passed, and the gates above exist because of it.

| `store_archetype` | Store factory | Store | ViewModel | Test |
|---|---|---|---|---|
| OFFLINE_LOCAL_ONLY | `createOfflineStore` | `AlertsStore.kt`, `LoansStore.kt`, `BillRemindersStore.kt` | `AmortizationScheduleViewModel.kt` | `AlertsStoreTest.kt`, `LoansStoreTest.kt`, `AmortizationScheduleViewModelTest.kt` |
| NETWORK_WITH_CACHE | `createStore` | `ExchangeRatesStore.kt`, `InterestRateSeriesStore.kt` | `CurrencyRatesViewModel.kt`, `InterestRatesViewModel.kt` | `store-archetype-coverage.sh` (AC-3) |
| NETWORK_ONLY | `createStore` + `FetchPolicy.NETWORK_ONLY` | `SpotRateLookupStore.kt` | `CurrencyRatesViewModel.kt` (online) | `SpotRateLookupStoreTest.kt` |
| CACHE_ONLY | `createStore` + `FetchPolicy.CACHE_ONLY` | `SpotRateLookupStore.kt` | `CurrencyRatesViewModel.kt` (offline) | `CurrencyConverterViewModelTest.kt` |
| PERIODIC | `createStore` + TTL in `AppStoreRegistry` | `ExchangeRatesStore.kt` | `HomeViewModel.kt` tile | `HomeDashboardViewModelTest.kt` |
| MEMORY_ONLY | `createMemoryStore` | `MacroIndicatorStore.kt` | `CountryMacroViewModel.kt` | `store-archetype-coverage.sh` (AC-3) |
| LOAD_ONCE | `createStore` + `asLoadOnceStream` | `LoansStore.kt` | `LoanDetailViewModel.kt` | `LoanDetailViewModelTest.kt` |
| MUTABLE | `createMutableStore` + `Bookkeeper` | `CloudTodoStore.kt` | `EditBillReminderViewModel.kt` | `EditBillReminderViewModelTest.kt`, `OfflineSubmitSyncerTest.kt` |

### Write side — one unified mutation ViewModel

The write path uses a **single** base view-model, `BaseMutationViewModel<T, R>`
(`core-base/ui/.../viewmodel/BaseMutationViewModel.kt`), parameterized by **`MutationMode`**:

- **`MutationMode.InSession`** — single-shot submit, no persistence.
- **`MutationMode.Draft`** — offline-resilient draft with 3-case resume (fresh / resume-in-progress /
  resume-after-crash), persisting the payload across restarts.

An earlier design split these into two separate base view-models; they were collapsed into this one,
and `MutationMode` now expresses the mode. The **Sync & Drafts** surface
(`feature/settings/.../SyncAndDraftsViewModel.kt`) lists in-flight drafts from both modes. Wire the
screen with `MutationScreenContent` + `SubmitHandler` / `DraftSubmitHandler`.

### Customization seams (real registry names)

The app shell reads features + backbone + tabs + stores + network from registries — a fork adds one
line per surface, never edits the shell:

- **`FeatureRegistry`** (`cmp-navigation/.../registry/FeatureRegistry.kt`) — registers demo/fork
  features into `AuthenticatedNavigation`.
- **`BackboneRegistry`** (`cmp-navigation/.../registry/BackboneRegistry.kt`) — home/profile/settings
  backbone graph.
- **`TabRegistry`** (`cmp-navigation/.../registry/TabRegistry.kt`) — bottom-nav tab set.
- **`AppStoreRegistry`** (`core/store/.../AppStoreRegistry.kt`) — feature-tagged Store5 factories.
- **`AppAccessPoints`** + **`AccessPointRegistry`** (`core-base/network/.../AccessPointRegistry.kt`) —
  the declared network endpoints (see Network below).
- **`core/store`** — `AppScreenStateDefaults`, `AppErrorMapper`, `appStoreModule` (branded state
  visuals + error mapping + DI).

### Network — N REST + N Supabase access points

Every endpoint the app talks to is declared once in
`app-profile/app.yaml#network.access_points` (`type: rest | supabase`) and generated into
`AppAccessPoints.points`, which the fork registers as `AccessPointRegistry(AppAccessPoints.points)`
in its `NetworkModule`. The registry resolves any number of REST **and** Supabase points:

- **REST** — `restApi<T>("<id>")` DSL + `AccessPointRegistry.restBaseUrl(type)`; `core-base/network`
  owns the transport, so a fork writes only the API interface + one `restApi("<id>")` line.
- **Supabase** — `AccessPointRegistry.supabasePoints()` returns every declared Supabase point;
  a per-point `SupabaseConfigClient` factory builds the client (URL from the registry, key from
  secrets by id). `supabasePoints()` supports N Supabase projects, not a single hardcoded client.

### Tech Stack

**Languages:**
- Kotlin (shared business logic)
- Kotlin/Native (iOS, macOS)
- Kotlin/JVM (Android, Desktop)
- Kotlin/JS (Web)
- Swift (iOS platform code)
- Ruby (Fastlane)
- Bash (automation scripts)

**Frameworks:**
- Compose Multiplatform (UI framework for all platforms)
- Ktor (networking)
- Room 3 (database)
- Koin (dependency injection)

**CI/CD:**
- GitHub Actions with the **v2 reusable workflows** from `openMF/mifos-x-actionhub` (per-workflow pins — the wrapper file is authoritative; see `.github/CLAUDE.md`)
- **13 custom actions** (4 Android, 4 iOS, 2 macOS, 1 Desktop, 1 Web, 1 Static Analysis)
- **Fastlane** (8 lanes across 8 deployment targets in `deployment/<platform>/<target>/lane.rb`)
- **17 bash scripts** for setup, deployment, and verification

**Code Quality:**
- Spotless (code formatting)
- Detekt (Kotlin static analysis & linting)
- Dependency Guard (dependency validation)

---

## Deployment Targets

### Android (3 targets)
1. **Firebase App Distribution** (Prod & Demo variants)
2. **Play Store Internal/Beta** (auto-promotion)
3. **Play Store Production** (manual promotion)

### iOS (3 targets)
4. **Firebase App Distribution**
5. **TestFlight** (beta testing)
6. **App Store** (production)

### macOS (2 targets)
7. **TestFlight** (macOS beta)
8. **App Store** (macOS production)

### Desktop (1 target)
9. **GitHub Releases** (Windows EXE/MSI, macOS DMG, Linux DEB)

### Web
- **GitHub Pages** (continuous deployment)

---

## First-time Fork Setup

> **Canonical guide:** [`deployment/BOOTSTRAP.md`](deployment/BOOTSTRAP.md) — Path A
> (manual mode, OSS forks) and Path B (vault mode, framework maintainers) with a
> decision matrix at the top. Start there for the full step-by-step.

The `template_version: "2.6.0"` epic (fastlane-modernization) replaced the
legacy `.env.local.example` pattern with a structured secrets pipeline. Pick
the path that matches your team:

- **Path A — OSS fork (manual mode):** copy `secrets/sample/` into `secrets/live/`
  and fill in real values; CI consumes them via the per-target
  `deployment/<platform>/<target>/workflow-snippet.yml` manual flavor.
- **Path B — Vault mode (maintainers):** run `/secrets pull` from a
  framework-bound session; secrets materialize to canonical filesystem
  locations from the SOPS+age vault.

**FRED (Federal Reserve Economic Data)** — free developer key required for the
B7 Interest Rate Tracker + B8 Country Macro Snapshot screens:

1. Sign up: https://fred.stlouisfed.org/docs/api/api_key.html (30 seconds)
2. Provide the key one of two ways:
   - **Path A:** add `FRED_API_KEY=<your-key>` to `local.properties` (gitignored,
     matches the KMP ecosystem convention) — no shared env bundle needed.
   - **Path B:** run `/secrets request mifos_x_fred_api_key` from a
     project-bound session; the framework opens a vault PR proposing the
     new alias row. After it merges, `/secrets pull` materializes it.
3. Wire it into Koin in your fork's app module:
   ```kotlin
   single { FredApiConfig(apiKey = System.getenv("FRED_API_KEY")) }
   ```
   (Or load via BuildKonfig / Gradle property — whichever your fork prefers.)

Leave the key unset and the FRED-backed screens render an explicit "FRED key
not configured" empty state rather than crashing.

**World Bank Open Data** — no setup. Fully open API.

---

## Development Workflow

### 1. Initial Setup

```bash
# For new contributors:
./setup-project.sh  # Master setup script

# OR follow detailed setup:
scripts/white-label/keystore.sh generate  # Generate Android keystores
scripts/white-label/firebase.sh             # Configure Firebase projects
./scripts/ios/setup_ios_complete.sh # iOS code signing setup
```

### 2. Daily Development

```bash
# Checkout feature branch
git checkout -b feature/my-feature

# Make changes, format code
./gradlew spotlessApply

# Run checks locally
./gradlew check spotlessCheck detekt dependencyGuard

# Commit (pre-commit hooks run automatically)
git add .
git commit -m "feat(android): add new feature"
```

### 3. Before Deploying

```bash
# Run tests
./gradlew test

# Verify iOS deployment configuration (iOS only)
./scripts/ios/verify_ios_deployment.sh

# Check version sanitization (iOS only)
./scripts/ios/check_ios_version.sh
```

### 4. Deployment

**Via GitHub Actions (Recommended):**
1. Trigger the **`Release · Multi-Platform`** workflow (`release-multi-platform.yml`)
2. For each platform pick the top **rung** to reach (`<platform>_rung`: internal → beta → production); lower rungs auto-fire
3. Production-facing stages pause for approval if the environment has required reviewers (set up via `scripts/configure-release-environments.sh`)

See `.github/CLAUDE.md` for the full rung-ladder + environment-gate model.

**Via Fastlane (Local/Manual):**
```bash
# Invocation: (cd deployment && bundle exec fastlane <platform> <lane>)
# All lanes live in the canonical deployment/fastlane/ dir — cd into deployment/, no dir flag.

# Android
(cd deployment && bundle exec fastlane android deployReleaseApkOnFirebase)
(cd deployment && bundle exec fastlane android deployInternal)
(cd deployment && bundle exec fastlane android promoteToBeta)
(cd deployment && bundle exec fastlane android promote_to_production)

# iOS
(cd deployment && bundle exec fastlane ios deploy_on_firebase)
(cd deployment && bundle exec fastlane ios beta)
(cd deployment && bundle exec fastlane ios release)

# macOS / Desktop
(cd deployment && bundle exec fastlane mac desktop_testflight)
(cd deployment && bundle exec fastlane mac desktop_release)
```

**Via Bash Scripts (iOS only):**
```bash
./scripts/deploy/deploy_firebase.sh
./scripts/deploy/deploy_testflight.sh
./scripts/deploy/deploy_appstore.sh  # Double confirmation required
```

---

## Customization Points (for consumer apps)

When forking this template, your app is **offline-first by default** — `core-base/store`
decides every state transition (loading / no-network / captive-portal / empty / error /
content + freshness) so screens never have to. Your fork's only job is to brand the
visuals via `core/store/AppScreenStateDefaults.kt`.

`KptTheme` (`core/designsystem/.../theme/KptTheme.kt`) already wires
`LocalScreenStateDefaults provides appScreenStateDefaults()`, so every screen wrapped by the theme
picks up your branded defaults — zero per-screen wiring.

Customize in **`core/store`** (the single discoverable seam):

- **`AppScreenStateDefaults`** — brand visuals, copy, Lottie animations, telemetry hooks
- **`AppErrorMapper`** — domain-error → user-message mapping (extends `categorize()`)
- **`AppStoreRegistry`** — your named Store qualifiers
- **`appStoreModule`** — Koin DI module for Store factories

See `core/store/README.md` for the "what you get for free" list and full integration
pattern.

### User-facing surfaces (extend these in your fork)

The demo features ship two domain surfaces forks typically brand or extend:

- **Banking domain** (`core/model/banking/`, `core/data/banking/`,
  `core/database/banking/`) — `Loan` + `BillReminder` entities, repositories,
  Room DAOs. Add fields, new categories, or related entities (savings goals,
  budgets) here. The `feature/loans` and `feature/bills` UIs read straight from
  the repository contracts — extend the model + DAO and the UI follows.
- **Economic API integration** (`core/network/economic/`, `core/data/economic/`,
  `core/store/economic/`) — FRED + World Bank API clients, Store5-backed
  caches, repository surfaces. Add new FRED series by extending
  `feature/rates/.../RateSeriesCatalog.kt` (no client changes needed); add new
  World Bank indicators by extending `core/model/economic/MacroIndicator.kt`
  and the `MacroIndicatorsRepository` query set.

Both surfaces follow the same offline-first contract — see `core/store/README.md`.

**Do NOT modify `core-base/store` or `core-base/ui`** — they're framework-shared and
upgrade cleanly across template versions. Push fork pressure to `core/store` instead.

For paginated screens, use `PagingScreenContent { items(coins) { ... } }` —
core-base/ui owns the LazyColumn, load-more trigger, and footer wiring (loading /
error+retry / end-of-list). You declare only per-item content.

For detail pages, non-paginated lists, multi-source dashboards, and other patterns,
see the **screen-type taxonomy table** in `core/store/README.md` — it maps every
common screen type to the right framework API. (`PagingScreenContent` is for
infinite-scroll paginated lists only; detail pages use `ScreenContent`.)

For **input screens** (form, wizard, quick-action, confirm, gesture — anything where
the user submits a mutation), use `SubmitHandler` (simple) or `DraftSubmitHandler`
(offline-resilient, persists payload across restarts). Wire the screen with
`MutationScreenContent`. Control network vs. cache strategy per-request via `FetchPolicy`
(`CACHE_ONLY` / `NETWORK_ONLY` / `NETWORK_WITH_CACHE`).

> Screen-archetype vocabulary (used by `/kmp-feature` codegen via `ui.yaml.screens[].type`):
> `screen-content` (→ `ScreenContent`), `paging-list` (→ `PagingScreenContent`),
> `input` (→ `MutationScreenContent` + `SubmitHandler`/`DraftSubmitHandler`),
> `custom` (bring-your-own), `pure-ui` (no Store). Names align 1:1 with the Compose
> composable that wraps the screen body — see `core/store/README.md` taxonomy table.

On **logout**, call `storeCacheManager.clearAll()` to wipe all Store caches and draft rows.
On **app start**, call `storeCacheManager.pruneExpiredDrafts()` to remove SUBMITTED/FAILED
drafts older than 30 days (PENDING drafts are never pruned).

See [Store Implementation Guide](docs/claude/store-implementation.md) for full examples.

---

## Fork branding

Every brand-touching value — app id, display name, version, org details, endpoints,
credentials, icons — is single-sourced from **`app-profile/`** (`app.yaml` +
`platforms/**/*.yaml`), the fork-owned SoT. Edit it there, run `./gradlew syncForkConfig`,
and it propagates: syncForkConfig regenerates the derived build-bridge
`gradle/fork.properties` (headed *"GENERATED from app-profile — do not hand-edit"*) and
writes on to `gradle/libs.versions.toml`, `Config.xcconfig`, per-module `BuildKonfig`, and
per-flavor `BuildConfig`. `fork.properties` is a generated intermediate, not the SoT.

> **Source of truth for `appId`: `app-profile/app.yaml#identity.app_id`** — authored there; then
> `./gradlew syncForkConfig` regenerates `gradle/fork.properties#app.id` from it and writes it into
> `gradle/libs.versions.toml#appId` (the catalog the build reads via `libs.versions.appId`). Edit
> `app_id` in app-profile, never fork.properties or the catalog line;
> `scripts/product-health/checks/appid-consistency.sh` FAILs CI if the catalog + app.id drift. The other
> build-time keys (`appDisplayName`, `desktopAppName`, `projectName`) resolve the same way — app-profile
> first (`identity.app_name`, …), then the fork.properties bridge, then the `libs.versions.toml` fallback.
> `app.display.name` also flows to `cmp-ios/Configuration/Config.xcconfig#APP_NAME` and to per-module
> `BuildKonfig.APP_DISPLAY_NAME` (e.g. the `feature/settings` About footer). Endpoints / demo creds /
> log tag (`network.base.url.{demo,prod}` / `demo.username` / `demo.password` / `log.tag`) are read by
> `KMPFlavorsConventionPlugin` into per-flavor `BuildConfig` — set them in `fork.properties`.
>
> **Module namespaces are NOT a fork property.** Every module's Android `namespace` (R-class) derives
> from the framework-owned constant `org.convention.BASE_MODULE_NAMESPACE` (`kpt`) in `build-logic` —
> it matches the hardcoded `kpt.*` Kotlin package root, has no per-fork meaning, and is deliberately
> kept OUT of `libs.versions.toml` so it never causes a catalog-merge conflict during a template sync.

> Historical note: `gradle.properties` previously carried `APP_ID_BASE` / `APP_NAME` /
> `APP_VERSION_BASE` / `APP_BUNDLE_DISPLAY_NAME` / `APP_BRAND_PREFIX` "reference" placeholders for a
> one-shot rename script that was never built. They were never read by the build (and `APP_NAME` even
> disagreed with `fork.properties#app.display.name`); the `fork.properties` → `syncForkConfig` mechanism
> above superseded them, so they were removed (B3 dedup, epic pure-white-label-store5-network).
> `gradle.properties` now holds only build-tuning + the live `fork.project.name`
> (→ `settings.gradle.kts` `rootProject.name`, regenerated by `syncForkConfig`).

### Fork app icons

App icons follow the same source-of-truth → `syncForkConfig` propagation pattern
as the text fields above, just for binary files:

- **Drop fork-specific icons** into `app-profile/icons/` (one file per platform —
  see `app-profile/icons/README.md` for the exact name → destination mapping).
- **Run** `./gradlew syncForkConfig`. The task copies whichever files are
  present into the canonical platform locations
  (`cmp-ios/iosApp/Assets.xcassets/AppIcon.appiconset/AppIcon.png`,
  `cmp-web/src/{js,wasmJs}Main/resources/favicon.ico`,
  `cmp-desktop/icons/ic_launcher.{icns,ico,png}`).
- **Missing source = no-op.** An empty `app-profile/icons/` keeps the template
  defaults — every drop is opt-in.
- **Android adaptive icons** require Android Studio's Image Asset Studio (one
  time per fork, commit the result). Alternatively drop a pre-built res tree
  into `app-profile/icons/android/` to have `syncForkConfig` copy it across.

Implementation: `build-logic/convention/src/main/kotlin/SyncForkConfigPlugin.kt`.

---

## Canonical consumer walkthrough

The one authoritative loop for standing up a fork — every other doc links here rather than
re-deriving it:

> **TL;DR:** fork → **`syncForkConfig`** → declare **`store_archetype`** → codegen → build → **`/kmp-project-template-sync`**.

1. **fork** the template into your own repo.
2. Fill `app-profile/app.yaml` (+ `platforms/**`) and run **`./gradlew syncForkConfig`** — brand flows
   from `app-profile/` (the SoT) to every tokenized surface: `gradle/fork.properties`, bundle id +
   display name, `mac-app-store.entitlements`, `fastlane/Appfile`, Firebase config, icons.
3. Author a feature and **declare its `store_archetype`** (one of the 8) in its `feature_profile`.
4. Run codegen (`/kmp-implement` → `kmp-store-gen`) — it reads `store_archetype` and emits the matching
   `core/store` factory (`createStore` / `createMemoryStore` / `createOfflineStore` /
   `createMutableStore`) + `FetchPolicy`, registered into `AppStoreRegistry`.
5. **Build / run**, then periodically run **`/kmp-project-template-sync`** to pull future white-label
   improvements from the upstream template without losing your fork's work.

## Sync — pulling template updates

**`/kmp-project-template-sync`** (engine: `scripts/white-label/sync-dirs.sh`, `SYNC_DIRS` / `SYNC_FILES`)
pulls upstream template updates without clobbering fork work AND without missing updates. The
sync-reachable surface now covers `feature/{home,profile,settings}`, `customization-surface.yaml`,
root `build.gradle.kts`, and `kotlin-js-store`. Anti-clobber is preserved via `is_excluded` carve-outs
declared in `customization-surface.yaml` — fork-owned paths (e.g. `app-profile/**`) are never rewritten.

---

## Key Constraints

### Version Handling
- **Gradle generates:** `YYYY.M.D-{prerelease}.{commitCount}+{sha}` (e.g., `2026.1.1-beta.0.9+abc123`)
- **Firebase accepts:** Full semantic version (`2026.1.1-beta.0.9`)
- **App Store requires:** `YYYY.M.{commitCount}` format (`2026.1.9`)
- **Auto-sanitization:** Fastlane automatically converts Gradle version to App Store format

See [Version Handling Guide](docs/claude/version-handling.md) for details.

### Secret Management
- **`cmp-android/google-services.json` IS committed — it is PUBLIC, not a secret.** The Firebase **client**
  config (project/app IDs + a client API key that ships inside the APK) is protected by Firebase Security
  Rules + App Check, **not** by secrecy — it is not a private key. It lives at the module root
  `cmp-android/google-services.json` (the shared config Gradle's Google-Services plugin auto-applies to
  **every flavor** — no per-flavor copies needed) and is committed to the repo. Do **NOT** treat it as a
  vault secret, gitignore it, route it through `/secrets`, or ask about it. (The `google-services.json →
  GOOGLESERVICES` GitHub-secret row below is a legacy CI convenience, not a secrecy requirement.) The
  same holds for `GoogleService-Info.plist` (iOS Firebase client config).
- **NEVER commit:** `secrets/`, `keystores/`, `*.keystore`, `*.p8`, `*.p12`, `.env`
- **Use:** `scripts/white-label/keystore.sh` for all secret operations
- **GitHub Secrets:** 30+ secrets required for full deployment pipeline
- **File-to-Secret Mapping:**
  - `firebaseAppDistributionServiceCredentialsFile.json` → `FIREBASECREDS`
  - `google-services.json` → `GOOGLESERVICES`
  - `playStorePublishServiceCredentialsFile.json` → `PLAYSTORECREDS`
  - `Auth_key.p8` → `APPSTORE_AUTH_KEY`
  - `match_ci_key` → `MATCH_GIT_PRIVATE_KEY`

See [Secrets Management Guide](docs/claude/secrets-management.md) for complete reference.

### Production Deployments
⚠️ **CRITICAL:** App Store and Play Store **production** deployments require:
- Manual workflow dispatch
- Double confirmation
- No direct Fastlane commands (use GitHub Actions)

### Branch Protection
- **NEVER** commit directly to `master` or `dev`
- Always create feature branch → PR → merge
- Pre-commit hooks run automatically (Spotless, Detekt, Dependency Guard)

---

## Platform-Specific Notes

### Android
- **Package (applicationId):** authored in `app-profile/app.yaml#identity.app_id` (the single source of truth); `syncForkConfig` regenerates `fork.properties#app.id` from it and writes `gradle/libs.versions.toml#appId`, which the build reads. Edit it in app-profile — don't hand-edit fork.properties or the catalog.
- **Min SDK:** 24, **Target SDK:** 34
- **Flavors:** `prod`, `demo`
- **Build Types:** `debug`, `release`
- **Keystores:** ORIGINAL (for app signing) + UPLOAD (for Play Console)
- **Firebase:** 2 apps registered (prod + demo), 4 variants in google-services.json

### iOS
- **Bundle ID:** authored in `app-profile/app.yaml#identity.app_id` (the single source of truth) — same value as the Android applicationId; `syncForkConfig` regenerates `fork.properties#app.id` + writes `gradle/libs.versions.toml#appId`, which the build reads. Edit it in app-profile — don't hand-edit fork.properties or the catalog.
- **Min Version:** iOS 15.0, **Target:** iOS 17.0
- **Code Signing:** Fastlane Match (adhoc for Firebase, appstore for TestFlight/App Store)
- **Shared framework integration:** SwiftPM / XCFramework (`cmp-ios/Package.swift` binary target + the `[KMP] Embed and Sign ComposeApp XCFramework` Xcode Run-Script phase). No CocoaPods / Ruby pod toolchain.

### macOS
- **Code Signing:** Manual keychain setup with .p12 certificates
- **Provisioning:** Directly written from base64-encoded secrets

### Desktop
- **Matrix Builds:** Windows (EXE, MSI), macOS (DMG), Linux (DEB)
- **Gradle Task:** `packageReleaseDistributionForCurrentOS`

### Web
- **Output:** Kotlin/JS browser distribution
- **Deployment:** GitHub Pages via `gh-pages` branch

---

## Emergency Contacts

**Project Owner:** set in `app-profile/app.yaml` (fork-owned identity SoT)
**CI/CD Infrastructure:** the reusable workflows pinned in `.github/workflows/` (see `.github/CLAUDE.md`)
**Support:** set your fork's support contact in `app-profile/`

---

## Common Commands

```bash
# Run all checks
./gradlew check spotlessCheck detekt dependencyGuard

# Format code
./gradlew spotlessApply

# Build all platforms (debug)
./gradlew assembleDebug build

# Run tests
./gradlew test

# Build Android release
./gradlew :cmp-android:assembleRelease

# Build Desktop release
./gradlew packageReleaseDistributionForCurrentOS

# Build Web release
./gradlew jsBrowserDistribution

# Secrets management
scripts/white-label/keystore.sh view              # View current secrets
scripts/white-label/keystore.sh encode-secrets    # Encode secrets for GitHub Actions
scripts/white-label/keystore.sh add               # Add secrets to GitHub (requires gh CLI)
```

---

## Known Issues & Bugs

### 🔴 Critical
1. **Firebase `groups` parameter ignored** - Actions pass tester groups but Fastlane lanes don't use them
   - **Workaround:** Set `ENV['FIREBASE_GROUPS']` in GitHub Actions environment
2. **Signing parameter naming inconsistency** - Mixed snake_case/camelCase/UPPERCASE

### 🟡 Medium
3. **Hardcoded keystore filename** - `upload_keystore.keystore` in multiple places
4. **Version generation may fail silently** - `set +e` swallows errors
5. **Production promotion has no validation** - Doesn't verify beta release exists

See [BUGS_AND_ISSUES.md](docs/analysis/BUGS_AND_ISSUES.md) for complete analysis with fixes.

---

## Need Help?

1. **Start here:** [Onboarding Guide](docs/claude/onboarding.md)
2. **Stuck?** [Troubleshooting Guide](docs/claude/troubleshooting.md)
3. **Deploying?** [Deployment Playbook](docs/claude/deployment-playbook.md)
4. **GitHub Actions failing?** [GitHub Actions Deep Dive](docs/claude/github-actions-deep-dive.md)

---

**📝 Note:** This CLAUDE.md is the central hub. For platform-specific details, see the linked guides above.
