# Billing — integrating PayCraft

> **Library:** `io.github.mobilebytelabs:cmp-paycraft` · **SDK docs:** the `paycraft-instructions/`
> corpus in the PayCraft repo (`PUBLIC_API.md`, `WIRING_CONTRACTS.md`, `PAYWALL_CUSTOMIZATION.md`,
> `BILLING_STATE_SEMANTICS.md`, `FAILURE_MODES.md`)
> **Status:** OPTIONAL. PayCraft is not a template dependency — a fork that does not monetise adds
> nothing. This page is how a fork that does monetise wires it, and what NOT to build.

---

## Overview

PayCraft is a self-hosted, multi-provider subscription library for KMP apps. The shape that matters
for a template fork:

```
app → PayCraft SDK → Supabase (source of truth)
                 ↑
   payment provider (Play / StoreKit / Stripe / Razorpay) → webhook → Supabase
```

**The app never talks to the payment provider.** It queries Supabase through the SDK. Webhooks keep
Supabase in sync. Products, pricing, providers, paywall layout and all user-facing copy come from the
PayCraft dashboard, so changing them is a dashboard edit rather than an app release.

### The one thing to take away

A fork writes **no** billing repository, store, API wrapper, paywall UI, settings section, or restore
flow. All of it ships in the SDK. Measured on two consumer apps before they adopted the facades:

| What was hand-written | Cost |
|---|---|
| `PayCraftApiImpl` + 4 sibling modules wrapping the SDK | 670 LOC over 12 SDK call sites |
| Subscription logic in a settings ViewModel | 67 lines, plus the section UI, plus its own tier→String map |

Both were deleted by injecting one interface. If you find yourself writing either, stop and read
`PUBLIC_API.md` — the thing you need is almost certainly already there.

---

## Wiring

### 1. Version catalog

Not in the template's `gradle/libs.versions.toml` by default. Add it in your fork:

```toml
[versions]
cmp-paycraft = "2.4.6"

[libraries]
cmp-paycraft = { module = "io.github.mobilebytelabs:cmp-paycraft", version.ref = "cmp-paycraft" }
```

Then in the modules that need it — `core/network`, `core/data`, `core/store`, and any
`feature/<f>` that renders a paywall or a subscription row:

```kotlin
commonMain.dependencies { implementation(libs.cmp.paycraft) }
```

### 2. Initialization order — this order, not another

```
1. PayCraft.initialize(apiKey = "pk_live_…")      // synchronous; captures apiKey + backend
2. startKoin { modules(PayCraftModule, /* your modules */) }
3. (Android, optional) loadKoinModules(paycraftPlayBillingModule(context, activityProvider))
4. app UI composes; PayCraftRepository resolves lazily
```

`PayCraftModule`'s `SupabaseClient` reads `PayCraft.backend` and its `PayCraftService` reads
`PayCraft.apiKey`, both set synchronously inside `initialize`. So `initialize` before `startKoin`,
and **never gate Koin startup on the `/config` fetch arriving** — config lands asynchronously, well
after Koin materialises its lazy singletons.

Getting it wrong is no longer fatal (`PayCraftServiceImpl.apiKey` is nullable and the RPCs resolve no
tenant, so the honest answer is `Free`), but it will silently behave as unconfigured.

### 3. Publishable keys are public

`pk_test_…` / `pk_live_…` are **publishable** keys, safe to commit. They are not secrets and do not
belong in the vault. Anything with a `sk_` prefix or a webhook secret is server-side only and never
reaches the app.

---

## What to inject, per module

Three facades, one per layer, so a module injects what it needs rather than the whole engine:

| Facade | Template layer | Use for |
|---|---|---|
| `PayCraftApi` | `core/network` | raw SDK network calls |
| `PayCraftRepository` | `core/data` | **the usual answer** — entitlement state + every action |
| `PayCraftStore` | `core/store` | the cached email / subscription / device token |

```kotlin
class SubscriptionViewModel(
    private val paycraft: PayCraftRepository,   // single<PayCraftRepository> in PayCraftModule
) : ViewModel() {
    val isPremium = paycraft.isPremium          // StateFlow<Boolean>
}
```

**Gate UI on `entitlement`, not on `billingState`.** `EntitlementSummary` preserves the tier across
`Loading` / `PaymentPending` / `Error`; pattern-matching `billingState is Premium` is a second
derivation of the same truth and locks out a buyer whose payment has not cleared, who is in offline
grace, or who is mid device-conflict.

For tests and `@Preview`s inject `FakePayCraftRepository` — it ships in the main artifact, so it
reaches `commonTest` with no extra wiring.

### Internal, deliberately

`BillingManager` and `PayCraftPaywallViewModel` are `internal`. If a snippet you find online passes a
`billingManager` to a composable, it predates SDK 2.4.6. Everything a consumer legitimately needs is
on the three facades — including the device-conflict actions `confirmDeviceTransfer()` and
`revokeCurrentDevice()`, which were the last gap.

---

## UI — drop-ins, not a toolkit

Every one of these takes presentation arguments only. None takes a callback, a copy parameter or a
repository: the SDK owns the data, the dashboard owns the words, and each surface presents its own
paywall and restore sheet.

```kotlin
// Settings screen — the subscription section (status, Manage, Restore)
PayCraftSubscriptionSettings(
    style = SubscriptionSettingsStyle.Rows,     // Minimal · Rows · Card · Hero · Compact
    header = { Text("Subscription") },
)

// Settings screen — the banner
PayCraftBanner()                                       // Adaptive: subscribers see their plan
PayCraftBanner(style = PayCraftBannerStyle.Upsell)     // renders nothing for subscribers

// Gate a feature
PayCraftPremiumGate { DeepInsightsCard() }
PayCraftSoftGate { ChartRow() }                        // content stays readable, the TAP is shielded
PayCraftRewardedGate(featureKey = "export") { ExportButton() }
```

### Theming adopts your design system

`PayCraftSettingsTheme.defaults()` reads the ambient `MaterialTheme`, so a fork that themes itself —
which every template fork does — gets its own palette, shape and type with no argument. Pass values
only where your design system exposes something Material does not:

```kotlin
PayCraftSubscriptionSettings(
    theme = PayCraftSettingsTheme.defaults().copy(
        iconTileBackground = MaterialTheme.colorScheme.tertiaryContainer,
        rowSpacing = MaterialTheme.spacing.md,          // template's own spacing scale
    ),
)
```

Keep `rowMinHeight` at its 48dp default. That is the row a user taps in order to **cancel**, and a
fiddly target there is the worst place to save space.

### Platform sign-in

The SDK cannot run native Google/Apple sign-in from `commonMain`. Provide the handler once around
your app and every surface that needs it — the restore sheet, the device-conflict screen — gains its
sign-in buttons:

```kotlin
ProvidePayCraftOAuthHandler(handler = { provider -> startPlatformSignIn(provider) }) {
    App()
}
```

---

## Failure modes a fork actually hits

Full catalogue in the SDK's `FAILURE_MODES.md` (55 entries). The ones that bite template forks:

| Symptom | Cause | Fix |
|---|---|---|
| Paying user sees a paywall on cold start | Gated on `billingState is Premium`; `Loading` fell into the not-entitled branch | Gate on `entitlement` / `isPremium`; treat `Loading` as its own state |
| An Upgrade or Restore tap does nothing | The surface signalled presentation instead of owning it — `presentPaywall()` only flips a flow a host must observe | Use the SDK's drop-ins; they present their own sheet |
| Banner copy disagrees with the paywall | Copy pinned in app code | Dashboard is the only copy source |
| Checkout opens a browser for a digital plan on Android/iOS | Hand-rolled checkout bypassed lane resolution | Call `PayCraftRepository.checkout(plan)`; it resolves the lane from the server-set `storeBinding` |
| Store review rejects for a missing restore path | No restore affordance | Add `PayCraftBanner` or `PayCraftSubscriptionSettings` — both present restore themselves |
| Device registers but is never premium | `register_device` wrote `is_active` false | Server-side; all three read RPCs filter `is_active = true` |

---

## Template-specific notes

- **`core-base/` is read-only to features.** PayCraft wiring belongs in `core/network`, `core/data`,
  `core/store` and `feature/<f>` — never in `core-base/`, which `scripts/white-label/sync-dirs.sh`
  overwrites wholesale on sync.
- **Fork-level feature modules go in `settings.local.gradle.kts`.** A `feature/paycraft-paywall`
  include placed in `settings.gradle.kts` is one sync from silent deletion: that file is
  template-derived and full-copied on sync, and no fork-owned file would show a diff. The seam is
  declared in `docs/architecture/CONTRACT.yaml` and documented in the header of
  `settings.local.gradle.kts` itself.
- **The iOS deployment lane already knows about PayCraft.** `deployment/_shared/scripts/
  asc-attach-subscriptions.rb` expects the product catalogue to exist before it attaches
  subscriptions; for a PayCraft-backed app that catalogue is the dashboard's product sync, so run
  that first.

---

## Where the authoritative answer lives

This page is an orientation, not the contract. The SDK corpus is regenerated from PayCraft source by
`/paycraft-corpus-fold` and each document carries an `example-provenance` commit, so it cannot drift
from the code unnoticed:

| Question | Document |
|---|---|
| What may I call? | `PUBLIC_API.md` |
| How do I wire DI? | `WIRING_CONTRACTS.md` |
| How do I restyle the paywall / settings copy? | `PAYWALL_CUSTOMIZATION.md` |
| What does each `BillingState` mean? | `BILLING_STATE_SEMANTICS.md` |
| Which provider on which platform? | `PROVIDERS_AND_STORES.md` |
| Why is my entitlement wrong? | `FAILURE_MODES.md` |

If this page disagrees with the corpus, the corpus is right.
