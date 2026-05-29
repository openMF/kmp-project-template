# Claude Code - Money Toolkit (KMP)

**Last Updated:** 2026-05-24
**Project Type:** Kotlin Multiplatform (KMP) — generic financial utility toolkit
**Platforms:** Android | iOS | macOS | Desktop (Windows/macOS/Linux) | Web

---

## Quick Links

🚀 **New fork? Start here:**
- [Fork Quickstart](templates/FORK_QUICKSTART.md) - Day-1 customization checklist for new forks

📖 **Domain-Specific Guides:**
- [GitHub Actions & CI/CD](.github/CLAUDE.md) - Workflows, custom actions, secrets
- [Fastlane Deployment](fastlane/CLAUDE.md) - iOS & Android deployment lanes
- [Bash Scripts](scripts/CLAUDE.md) - Setup, deployment, and verification scripts

📚 **Deep-Dive Documentation:**
- [Troubleshooting Guide](docs/claude/troubleshooting.md)
- [Onboarding Guide](docs/claude/onboarding.md)
- [Deployment Playbook](docs/claude/deployment-playbook.md)
- [Patterns & Best Practices](docs/claude/patterns.md)
- [Independent Cards Pattern](docs/claude/PATTERN-independent-cards.md) - Multi-card dashboards where each card has its own ScreenState (loading / error / empty / content) — `IndependentCardLayout` + `DashboardProgressBar` + `aggregateDashboardProgress`
- [Store Implementation Guide](docs/claude/store-implementation.md) - Offline-first streams, mutations, FetchPolicy, cache lifecycle
- [Motion + Transitions](core-base/ui/MOTION.md) - Symmetric durations, M3 patterns, debug Transition Gallery
- [GitHub Actions Deep Dive](docs/claude/github-actions-deep-dive.md)
- [Secrets Management](docs/claude/secrets-management.md)
- [Version Handling](docs/claude/version-handling.md)

🐛 **Known Issues:**
- [Infrastructure Bugs & Workarounds](docs/analysis/BUGS_AND_ISSUES.md)

---

## Project Overview

This is the **Money Toolkit** — a generic, open-source financial utility template
built on Kotlin Multiplatform. It ships working personal-finance tools out of
the box (loan tracking, bill reminders, interest-rate watching, calculators,
country-level macro indicators) wired through the same offline-first store
contract every framework feature uses. No login. No backend. Fork to brand and
extend.

The project doubles as a **reference implementation** for every architectural
pattern in `core-base/store` and `core-base/ui` — each shipped feature is the
canonical showcase for one or more framework archetypes (see "Toolkit feature
showcase" below).

CI/CD infrastructure spans **5 platforms** and **9 deployment targets**.

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
├── fastlane/            # Deployment automation (iOS & Android)
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

## Store Archetype Showcases (kmp-project-template)

| Archetype | Store | ViewModel/Feature | Test |
|---|---|---|---|
| OFFLINE_LOCAL_ONLY | `AlertsStore`, `LoansStore`, `BillRemindersStore` | `AmortizationScheduleViewModel` | `AlertsStoreTest`, `LoansStoreTest`, `AmortizationScheduleViewModelTest` |
| NETWORK_WITH_CACHE | `ExchangeRatesStore`, `InterestRateSeriesStore` | `ExchangeRatesViewModel` | `EconomicMemoryOnlyTest` |
| NETWORK_ONLY | `SpotRateLookupStore` | `CurrencyConverterViewModel` (online) | `SpotRateLookupStoreTest` |
| CACHE_ONLY | `SpotRateLookupStore` | `CurrencyConverterViewModel` (offline) | `CurrencyConverterViewModelTest` |
| PERIODIC | `ExchangeRatesStore` | `HomeDashboardViewModel` tile | `HomeDashboardViewModelTest` |
| MEMORY_ONLY | `MacroIndicatorStore` | `MacroIndicatorsViewModel` | `EconomicMemoryOnlyTest` |
| LOAD_ONCE | `LoansStore` | `LoanDetailViewModel` | `LoanDetailViewModelTest` |
| MUTABLE | (DraftSubmitHandler) | `BillReminderCreateViewModel` | (existing) |

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
- GitHub Actions with **reusable workflows** (`openMF/mifos-x-actionhub@v1.0.8`)
- **13 custom actions** (4 Android, 4 iOS, 2 macOS, 1 Desktop, 1 Web, 1 Static Analysis)
- **Fastlane** (12 lanes: 7 Android + 5 iOS)
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

After cloning, before running the toolkit's economic-data screens (B7 Interest
Rate Tracker, B8 Country Macro Snapshot), copy `.env.local.example` to
`.env.local` and fill in fork-specific values:

```bash
cp .env.local.example .env.local
# Edit .env.local — add your FRED API key
```

**FRED (Federal Reserve Economic Data)** — free developer key required:

1. Sign up: https://fred.stlouisfed.org/docs/api/api_key.html (30 seconds)
2. Paste the key into `.env.local` as `FRED_API_KEY=...`
3. Wire it into Koin in your fork's app module:
   ```kotlin
   single { FredApiConfig(apiKey = System.getenv("FRED_API_KEY")) }
   ```
   (Or load via BuildKonfig / Gradle property — whichever your fork prefers.)

Leave the key blank and the FRED-backed screens render an explicit "FRED key
not configured" empty state rather than crashing.

**World Bank Open Data** — no setup. Fully open API.

---

## Development Workflow

### 1. Initial Setup

```bash
# For new contributors:
./setup-project.sh  # Master setup script

# OR follow detailed setup:
./keystore-manager.sh generate  # Generate Android keystores
./firebase-setup.sh             # Configure Firebase projects
./scripts/setup_ios_complete.sh # iOS code signing setup
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
./scripts/verify_ios_deployment.sh

# Check version sanitization (iOS only)
./scripts/check_ios_version.sh
```

### 4. Deployment

**Via GitHub Actions (Recommended):**
1. Push to `dev` branch
2. Trigger `multi-platform-build-and-publish` workflow
3. Select deployment targets via workflow inputs

**Via Fastlane (Local/Manual):**
```bash
# Android
bundle exec fastlane android deployReleaseApkOnFirebase
bundle exec fastlane android deployInternal

# iOS
bundle exec fastlane ios deploy_on_firebase
bundle exec fastlane ios beta
bundle exec fastlane ios release
```

**Via Bash Scripts (iOS only):**
```bash
./scripts/deploy_firebase.sh
./scripts/deploy_testflight.sh
./scripts/deploy_appstore.sh  # Double confirmation required
```

---

## Customization Points (for consumer apps)

When forking this template, your app is **offline-first by default** — `core-base/store`
decides every state transition (loading / no-network / captive-portal / empty / error /
content + freshness) so screens never have to. Your fork's only job is to brand the
visuals via `core/store/AppScreenStateDefaults.kt`.

`MifosTheme` already wires `LocalScreenStateDefaults provides appScreenStateDefaults()`,
so every screen wrapped by the theme picks up your branded defaults — zero per-screen
wiring.

Customize in **`core/store`** (the single discoverable seam):

- **`AppScreenStateDefaults`** — brand visuals, copy, Lottie animations, telemetry hooks
- **`AppErrorMapper`** — domain-error → user-message mapping (extends `categorize()`)
- **`AppStoreRegistry`** — your named Store qualifiers
- **`appStoreModule`** — Koin DI module for Store factories

See `core/store/README.md` for the "what you get for free" list and full integration
pattern.

### User-facing surfaces (extend these in your fork)

The Money Toolkit ships two domain surfaces forks typically brand or extend:

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

The toolkit centralises every brand-touching string into **five properties** in
`gradle.properties`. Today they're reference values (consumers still have the
strings hardcoded across `cmp-android/build.gradle.kts`, `cmp-ios/`,
`cmp-desktop/build.gradle.kts`, `cmp-web/build.gradle.kts`, `Info.plist`,
`AndroidManifest.xml`, etc.). The intent: a future one-shot rename script reads
these five properties + does substitutions across the consumer build files in a
single pass.

| Property                   | Default value    | Consumer (planned)                                                  |
| -------------------------- | ---------------- | ------------------------------------------------------------------- |
| `APP_ID_BASE`              | `cmp.android.app`| Android `applicationId`; iOS bundle ID base                         |
| `APP_NAME`                 | `Money Toolkit`  | App display name (Android `app_name`, iOS `CFBundleDisplayName`)    |
| `APP_VERSION_BASE`         | `1.0.0`          | Base for Gradle-generated `YYYY.M.D-{prerelease}.{n}+{sha}` versions|
| `APP_BUNDLE_DISPLAY_NAME`  | `Money Toolkit`  | iOS Springboard label; macOS `CFBundleName`                         |
| `APP_BRAND_PREFIX`         | `Kpt`            | Kotlin-namespace prefix (e.g. `KptTheme`, `KptProgress`)            |

**Today**: forks edit these properties **and** every consumer file by hand.
**Roadmap**: a `scripts/fork-rename.sh` (TBD) will accept new values and write
them through to every consumer file in one pass — eliminating the rename-drift
class of fork failure. The properties exist today so:

1. Forks can grep `APP_NAME` / `APP_ID_BASE` and confirm the rename surface.
2. The rename-script PR has a stable target — no schema renegotiation.
3. Consumer build files can incrementally migrate to reading these properties
   via `project.findProperty("APP_NAME") as? String ?: "Money Toolkit"` patterns
   without breaking forks mid-flight.

See `gradle.properties` for the current values; see Phase 10 of the
core-base-store-coverage epic for the seam rationale.

---

## Key Constraints

### Version Handling
- **Gradle generates:** `YYYY.M.D-{prerelease}.{commitCount}+{sha}` (e.g., `2026.1.1-beta.0.9+abc123`)
- **Firebase accepts:** Full semantic version (`2026.1.1-beta.0.9`)
- **App Store requires:** `YYYY.M.{commitCount}` format (`2026.1.9`)
- **Auto-sanitization:** Fastlane automatically converts Gradle version to App Store format

See [Version Handling Guide](docs/claude/version-handling.md) for details.

### Secret Management
- **NEVER commit:** `secrets/`, `keystores/`, `*.keystore`, `*.p8`, `*.p12`, `.env`
- **Use:** `keystore-manager.sh` for all secret operations
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
- **Package:** `cmp.android.app`
- **Min SDK:** 24, **Target SDK:** 34
- **Flavors:** `prod`, `demo`
- **Build Types:** `debug`, `release`
- **Keystores:** ORIGINAL (for app signing) + UPLOAD (for Play Console)
- **Firebase:** 2 apps registered (prod + demo), 4 variants in google-services.json

### iOS
- **Bundle ID:** `org.mifos.kmp.template`
- **Min Version:** iOS 15.0, **Target:** iOS 17.0
- **Code Signing:** Fastlane Match (adhoc for Firebase, appstore for TestFlight/App Store)
- **CocoaPods:** Required for iOS dependencies

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

**Project Owner:** Mifos Initiative
**CI/CD Infrastructure:** mifos-x-actionhub (openMF/mifos-x-actionhub)
**Support:** team@mifos.org

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
./keystore-manager.sh view              # View current secrets
./keystore-manager.sh encode-secrets    # Encode secrets for GitHub Actions
./keystore-manager.sh add               # Add secrets to GitHub (requires gh CLI)
```

---

## Known Issues & Bugs

### 🔴 Critical
1. **Firebase `groups` parameter ignored** - Actions pass tester groups but Fastlane lanes don't use them
   - **Workaround:** Set `ENV['FIREBASE_GROUPS']` in GitHub Actions environment
2. **Signing parameter naming inconsistency** - Mixed snake_case/camelCase/UPPERCASE

### 🟡 Medium
3. **Hardcoded keystore filename** - `release_keystore.keystore` in multiple places
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
