# Claude Code - KMP Project Template

**Last Updated:** 2026-02-13
**Project Type:** Kotlin Multiplatform (KMP)
**Platforms:** Android | iOS | macOS | Desktop (Windows/macOS/Linux) | Web

---

## Quick Links

📖 **Domain-Specific Guides:**
- [GitHub Actions & CI/CD](.github/CLAUDE.md) - Workflows, custom actions, secrets
- [Fastlane Deployment](fastlane/CLAUDE.md) - iOS & Android deployment lanes
- [Bash Scripts](scripts/CLAUDE.md) - Setup, deployment, and verification scripts

📚 **Deep-Dive Documentation:**
- [Troubleshooting Guide](docs/claude/troubleshooting.md)
- [Onboarding Guide](docs/claude/onboarding.md)
- [Deployment Playbook](docs/claude/deployment-playbook.md)
- [Patterns & Best Practices](docs/claude/patterns.md)
- [Store Implementation Guide](docs/claude/store-implementation.md) - Offline-first streams, mutations, FetchPolicy, cache lifecycle
- [GitHub Actions Deep Dive](docs/claude/github-actions-deep-dive.md)
- [Secrets Management](docs/claude/secrets-management.md)
- [Version Handling](docs/claude/version-handling.md)

🐛 **Known Issues:**
- [Infrastructure Bugs & Workarounds](docs/analysis/BUGS_AND_ISSUES.md)

---

## Project Overview

This is a **Kotlin Multiplatform (KMP) mobile/desktop/web application** with comprehensive CI/CD infrastructure spanning **5 platforms** and **9 deployment targets**.

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
(`CACHE_ONLY` / `NETWORK_ONLY` / `CACHE_THEN_NETWORK`).

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
