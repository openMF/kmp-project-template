# Fork Quickstart — Day-1 Customization Checklist

This is the long-form walkthrough for turning a fresh clone of
`kmp-project-template` into a branded, running app. The condensed five-step
version lives in [`core-base/README.md`](../core-base/README.md#day-1-fork-checklist) —
this file expands each step with rationale, gotchas, and pointers into the rest
of the codebase.

> **Estimated time:** 30–60 minutes for steps 1–5, plus whatever your auth /
> analytics / crash-reporter integrations take.

---

## Step 1 — Rebrand identifiers (`gradle.properties`)

The template centralises every brand-touching string into five properties at
the top of `gradle.properties`:

| Property                   | Default value     | Where it ends up                                                       |
| -------------------------- | ----------------- | ---------------------------------------------------------------------- |
| `APP_ID_BASE`              | `cmp.android.app` | Android `applicationId`; iOS bundle ID base.                           |
| `APP_NAME`                 | `Money Toolkit`   | App display name (`app_name` on Android; `CFBundleDisplayName` on iOS).|
| `APP_VERSION_BASE`         | `1.0.0`           | Base for Gradle-generated `YYYY.M.D-{prerelease}.{n}+{sha}` versions.  |
| `APP_BUNDLE_DISPLAY_NAME`  | `Money Toolkit`   | iOS Springboard label; macOS `CFBundleName`.                           |
| `APP_BRAND_PREFIX`         | `Kpt`             | Kotlin-namespace prefix (e.g. `KptTheme`, `KptProgress`).              |

Today the properties are reference values — the strings are still hard-coded
across `cmp-android/build.gradle.kts`, `cmp-ios/`, `cmp-desktop/build.gradle.kts`,
`cmp-web/build.gradle.kts`, `Info.plist`, `AndroidManifest.xml`, etc. The
one-shot rename script (`scripts/fork-rename.sh`) is planned but not yet
shipped; for now edit the consumer files by hand and keep the properties in
sync as the source of truth.

See the **Fork branding** section of the root `CLAUDE.md` for the full rename
surface.

## Step 2 — Override the 4 customization-point Koin bindings

The template ships working stubs for every fork-customizable dependency so the
demo build runs out of the box. Each binding lives behind a stable interface;
your fork swaps the implementation in its app Koin module and no callers
change.

### 2a. `AuthProvider`

- **Default:** `NoOpAuthProvider` (returns "not authenticated" forever — the
  demo app has no login).
- **Swap for:** Firebase Auth, OAuth, biometric-only, your own SSO.
- **Where:** consumer Koin module (typically `cmp-shared/`'s DI). Bind your
  impl as `single<AuthProvider> { ... }`.

### 2b. `CrashReporter`

- **Default:** `ConsoleCrashReporter` (prints to logcat / stderr).
- **Swap for:** `FirebaseCrashlyticsCrashReporter`, Sentry, Bugsnag.
- **Where:** consumer Koin module. Bind as `single<CrashReporter> { ... }`.
  The interface lives in `core-base/observability/`.

### 2c. `AnalyticsHelper`

- **Default:** `NoOpAnalyticsHelper` (drops events).
- **Swap for:** `FirebaseAnalyticsHelper` (already shipped — opt-in by
  changing the Koin binding), Amplitude, Mixpanel, PostHog.
- **Where:** consumer Koin module. The higher-level `KptAnalyticsTracker`
  (in `core/analytics/`) consumes whichever `AnalyticsHelper` you bind — no
  call-site changes.

### 2d. Network config bindings

- **`FredApiConfig`** — the FRED API key for B7 Interest Rates. Swap
  `apiKey` for your own key (free signup at
  https://fred.stlouisfed.org/docs/api/api_key.html) and optionally swap
  `baseUrl` to point at a proxy.
- **`FrankfurterApiConfig`** — the Frankfurter exchange-rates base URL.
  Override `baseUrl` to point at a self-hosted Frankfurter or your own FX
  service.
- **`WorldBankApiConfig`** — the World Bank Open Data base URL. No auth
  required; override `baseUrl` only if proxying.

All three configs are plain data classes — bind one of each in your fork's
Koin module with the values you want.

## Step 3 — Generate Android keystores

```bash
./keystore-manager.sh generate
```

Creates `keystores/release_keystore.keystore` (and the UPLOAD variant for Play
Console). The `keystores/` directory is `.gitignore`'d — values never reach
the public history. Use `./keystore-manager.sh encode-secrets` later to push
encoded keystores into GitHub Actions secrets for CI builds.

iOS keystore equivalents (Fastlane Match, `.p8` keys) — see
`scripts/setup_ios_complete.sh` and the [Secrets Management Guide](../docs/claude/secrets-management.md).

## Step 4 — Populate `.env.local`

```bash
cp .env.local.example .env.local
```

Edit `.env.local` and add at minimum:

```
FRED_API_KEY=<your-key-here>
```

Leave the key blank and the B7 Interest Rates screen renders an explicit
"FRED key not configured" empty state rather than crashing.

If your fork uses additional third-party APIs, add their keys to `.env.local`
alongside `FRED_API_KEY` and wire them through Koin (or BuildKonfig) the same
way `FredApiConfig` does.

## Step 5 — Smoke-test the demo build

```bash
./gradlew :cmp-android:installDemoDebug
```

Should install the demo Android variant on a connected device or emulator
and launch successfully. If the install fails:

1. Check Android Studio's `Build` panel — most failures are missing keystores
   (re-run Step 3) or stale Gradle caches (`./gradlew clean`).
2. Confirm `FRED_API_KEY` is set if the B7 screen is your smoke-test surface.
3. See [Troubleshooting Guide](../docs/claude/troubleshooting.md) for the
   common failure modes.

---

## Beyond Day 1

Once Steps 1–5 are done you have a renamed, signed, running demo build. The
following customizations are typically Day-2+ work — pick what your fork
needs:

- **Icons + splash** — `cmp-android/src/main/res/mipmap-*/` and
  `cmp-ios/iosApp/Assets.xcassets/AppIcon.appiconset/`.
- **Push notifications** — wire FCM (Android) and APNs (iOS); register a
  notification handler in the consumer Koin module.
- **Deep links** — declare intent filters / universal-link entitlements in
  `AndroidManifest.xml` and `Info.plist`.
- **Store listings** — Play Console + App Store Connect metadata, screenshots,
  release notes.
- **Replace shipped features** — every Money Toolkit feature is a working
  showcase of one framework pattern; remove what you don't want from
  `feature/` and `settings.gradle.kts`. See the
  [toolkit feature showcase table](../CLAUDE.md#toolkit-feature-showcase) for
  which pattern each feature demonstrates.
- **Brand visuals (Store-driven states)** — extend `AppScreenStateDefaults`
  in `core/store/` to swap loading / error / empty visuals across every
  screen at once. See [`core/store/README.md`](../core/store/README.md).
- **Domain model** — extend `core/model/banking/` (loans, bills) and
  `core/data/banking/` with your own entities; `feature/loans` + `feature/bills`
  read straight from the repository contracts.

---

## Pointers

- **`CLAUDE.md` (root)** — the central hub. Read this first if you're new.
- **`core-base/README.md`** — framework-shared layer overview; the `Kpt*`
  symbol catalogue.
- **`core/store/README.md`** — Store contract + screen-archetype taxonomy.
- **`docs/claude/onboarding.md`** — full onboarding walkthrough.
- **`docs/claude/deployment-playbook.md`** — pushing builds to Firebase,
  TestFlight, Play Store, App Store.
