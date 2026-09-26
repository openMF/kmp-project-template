# `core/firebase`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_FIREBASE.md`
> **Measured:** 11 Kotlin files, 0 test files

## Principal types

`KptAnalyticsTracker`, `KptCrashKeys`, `KptEventTypes`, `KptParamKeys`, `KptParamValues`, `LoansAnalyticsTracker`, `LoansCrashKeys`, `LoansEventTypes`, `LoansParamKeys`, `LoansParamValues`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/firebase sha=c6c694a210f1b9fda03efaf54150bbee9a97003c -->
## API reference

_Generated from `core/firebase` at tree `c6c694a210f1` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsEvents.kt`

```kotlin
object KptEventTypes
```
CROSS-CUTTING analytics event keys — TEMPLATE-OWNED, full-copied by every sync. Everything here is true of ANY app built on this template: a session begins, a screen is shown, a request succeeds or fails, data syncs, a permission is granted.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsExtensions.kt:39</code></summary>

```kotlin
    logEvent(
        AnalyticsEvent(
            KptEventTypes.NAVIGATION,
            listOf(
                Param(KptParamKeys.FROM_SCREEN, from),
                Param(KptParamKeys.TO_SCREEN, to),
                Param(KptParamKeys.TRIGGER, trigger),
```

</details>

- `const val LOGIN_SUCCESS = "login_success"`
- `const val LOGIN_FAILURE = "login_failure"`
- `const val LOGOUT = "logout"`
- `const val SESSION_START = "session_start"`
- `const val SESSION_END = "session_end"`
- `const val SESSION_TIMEOUT = "session_timeout"`
- `const val BIOMETRIC_AUTH_SUCCESS = "biometric_auth_success"`
- `const val BIOMETRIC_AUTH_FAILURE = "biometric_auth_failure"`
- `const val SCREEN_VIEW = "screen_view"`
- `const val NAVIGATION = "navigation"`
- `const val DEEP_LINK_OPENED = "deep_link_opened"`
- `const val BACK_PRESSED = "back_pressed"`
- `const val API_CALL_SUCCESS = "api_call_success"`
- `const val API_CALL_FAILURE = "api_call_failure"`
  _…more members; read the file._

```kotlin
object KptParamKeys
```
CROSS-CUTTING parameter keys — TEMPLATE-OWNED, full-copied by every sync.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsExtensions.kt:41</code></summary>

```kotlin
            KptEventTypes.NAVIGATION,
            listOf(
                Param(KptParamKeys.FROM_SCREEN, from),
                Param(KptParamKeys.TO_SCREEN, to),
                Param(KptParamKeys.TRIGGER, trigger),
            ),
        ),
```

</details>

- `const val LOGIN_METHOD = "login_method"`
- `const val SESSION_DURATION_MS = "session_duration_ms"`
- `const val SCREEN_NAME = "screen_name"`
- `const val FROM_SCREEN = "from_screen"`
- `const val TO_SCREEN = "to_screen"`
- `const val TRIGGER = "trigger"`
- `const val ENDPOINT = "endpoint"`
- `const val HTTP_METHOD = "http_method"`
- `const val STATUS_CODE = "status_code"`
- `const val DURATION_MS = "duration_ms"`
- `const val SYNC_TYPE = "sync_type"`
- `const val RECORDS_SYNCED = "records_synced"`
- `const val CONFLICT_STRATEGY = "conflict_strategy"`
- `const val ERROR_TYPE = "error_type"`
  _…more members; read the file._

```kotlin
object KptParamValues
```
CROSS-CUTTING parameter values — TEMPLATE-OWNED, full-copied by every sync. Only values whose meaning is independent of any feature.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsExtensions.kt:35</code></summary>

```kotlin
    from: String,
    to: String,
    trigger: String = KptParamValues.TRIGGER_USER_ACTION,
) {
    logEvent(
        AnalyticsEvent(
            KptEventTypes.NAVIGATION,
```

</details>

- `const val TRIGGER_USER_ACTION = "user_action"`
- `const val TRIGGER_DEEP_LINK = "deep_link"`
- `const val TRIGGER_NOTIFICATION = "notification"`
- `const val TRIGGER_SYSTEM = "system"`
- `const val SYNC_FULL = "full"`
- `const val SYNC_INCREMENTAL = "incremental"`
- `const val SYNC_MANUAL = "manual"`
- `const val RESULT_SUCCESS = "success"`
- `const val RESULT_FAILURE = "failure"`
- `const val RESULT_CANCELLED = "cancelled"`

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsExtensions.kt`

```kotlin
fun AnalyticsHelper.trackNavigation(
```
A screen-to-screen transition. `trigger` is one of the `KptParamValues.TRIGGER_*` values.

```kotlin
fun AnalyticsHelper.trackScreenView(screenName: String)
```
A screen becoming visible. Separate from `trackNavigation` so first-render is countable alone.

```kotlin
fun AnalyticsHelper.trackApiCall(
```
One outbound API call. Intended for a single Ktor plugin rather than per-call sites — endpoint strings should already be templated (`/loans/{id}`), never interpolated with real ids.

```kotlin
fun AnalyticsHelper.trackValidationError(
```
A field-level validation failure. Never pass the rejected VALUE — only the field and the reason.

```kotlin
fun AnalyticsHelper.trackPreferenceChange(
```
A settings/preference change. `oldValue` and `newValue` must be non-PII.

```kotlin
fun AnalyticsHelper.trackPermission(permissionName: String, granted: Boolean)
```
A runtime permission decision.

```kotlin
fun AnalyticsHelper.trackTutorial(action: String, step: Int, tutorialName: String)
```
Onboarding progress. `action` is `started` / `step_completed` / `skipped` / `completed`.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsTracker.kt`

```kotlin
class KptAnalyticsTracker(
```
CROSS-CUTTING analytics tracker — TEMPLATE-OWNED, full-copied by every sync. Wraps the kmptoolkit `AnalyticsHelper` with the events every app on this template raises, whatever it ships: session, navigation, network, sync, performance.

```kotlin
fun rememberKptAnalyticsTracker(): KptAnalyticsTracker
```
Composition-scoped `KptAnalyticsTracker`, remembered against the ambient `AnalyticsHelper`. A feature tracker gets its own `remember…` in its own package; they share this helper instance.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/crashlytics/KptCrashExtensions.kt`

```kotlin
fun CrashReporter.setCurrentScreen(screenName: String, previousScreen: String? = null)
```
Record the screen the user is on, so a crash report opens with the right context. Call from the navigation observer that already raises `trackScreenView`.

```kotlin
fun CrashReporter.setNetworkState(online: Boolean, captivePortal: Boolean = false)
```
Connectivity at crash time. Offline-first apps behave differently offline, so a report without this is ambiguous between "broken" and "correctly degraded".

```kotlin
fun CrashReporter.setSyncState(inFlight: Boolean, pendingWrites: Int)
```
Whether a sync was in flight, and how many writes were still queued. A crash during replay of a 40-deep outbox is a different defect from a crash with an empty queue.

```kotlin
fun CrashReporter.setLastRequest(endpoint: String, statusCode: Int?)
```
The last request the app made. `endpoint` must be the TEMPLATED path (`/loans/{id}`) — an interpolated one would put a real id into the report.

```kotlin
fun CrashReporter.setAppearance(locale: String, themeMode: String)
```
Locale and theme — cheap to set, and they explain a surprising share of layout crashes.

```kotlin
fun CrashReporter.recordHandled(throwable: Throwable, context: String)
```
A non-fatal that the app handled but should not have hit — a `Result.failure` surfaced to the user, an unexpected empty state. Fatal crashes arrive on their own; these are the ones that stay invisible.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/crashlytics/KptCrashKeys.kt`

```kotlin
object KptCrashKeys
```
CROSS-CUTTING crash context — TEMPLATE-OWNED, full-copied by every sync. Mirrors its `config/analytics/` sibling: this file knows about NO feature.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/crashlytics/KptCrashExtensions.kt:32</code></summary>

```kotlin
 */
fun CrashReporter.setCurrentScreen(screenName: String, previousScreen: String? = null) {
    setCustomKey(KptCrashKeys.CURRENT_SCREEN, screenName)
    previousScreen?.let { setCustomKey(KptCrashKeys.PREVIOUS_SCREEN, it) }
    log("screen -> $screenName")
}
```

</details>

- `const val CURRENT_SCREEN = "current_screen"`
- `const val PREVIOUS_SCREEN = "previous_screen"`
- `const val SESSION_ID = "session_id"`
- `const val NETWORK_STATE = "network_state"`
- `const val SYNC_IN_FLIGHT = "sync_in_flight"`
- `const val LAST_ENDPOINT = "last_endpoint"`
- `const val LAST_STATUS_CODE = "last_status_code"`
- `const val APP_LOCALE = "app_locale"`
- `const val THEME_MODE = "theme_mode"`
- `const val PENDING_WRITES = "pending_writes"`

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/di/AnalyticsModule.kt`

```kotlin
val coreFirebaseModule: Module = module
```
Project-layer Koin module — the toolkit **default** analytics binding.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansAnalyticsEvents.kt`

```kotlin
object LoansEventTypes
```
`loans` feature analytics keys — DEMO-SHOWCASE, deleted by `--clean` with the loans feature. A fork's equivalent for its own feature is fork-owned and needs no declaration.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansAnalyticsTracker.kt:41</code></summary>

```kotlin
        analyticsHelper.logEvent(
            AnalyticsEvent(
                LoansEventTypes.LOANS_LIST_VIEWED,
                listOf(Param(LoansParamKeys.LOAN_COUNT, loanCount.toString())),
            ),
        )
    }
```

</details>

- `const val LOANS_LIST_VIEWED = "loans_list_viewed"`
- `const val LOAN_DETAIL_VIEWED = "loan_detail_viewed"`
- `const val LOAN_AMORTIZATION_VIEWED = "loan_amortization_viewed"`
- `const val LOAN_FORM_OPENED = "loan_form_opened"`
- `const val LOAN_FORM_ABANDONED = "loan_form_abandoned"`
- `const val LOAN_CREATED = "loan_created"`
- `const val LOAN_UPDATED = "loan_updated"`
- `const val LOAN_DELETED = "loan_deleted"`
- `const val LOAN_REMINDER_SCHEDULED = "loan_reminder_scheduled"`
- `const val LOAN_REMINDER_CANCELLED = "loan_reminder_cancelled"`
- `const val LOAN_REMINDER_FIRED = "loan_reminder_fired"`

```kotlin
object LoansParamKeys
```
`loans` parameter keys. NOTE none of these carry money or identity. `principal` is bucketed by `LoansParamValues`, and the loan id is deliberately absent: a Firebase event is not the place to reconstruct a user's debts.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansAnalyticsTracker.kt:42</code></summary>

```kotlin
            AnalyticsEvent(
                LoansEventTypes.LOANS_LIST_VIEWED,
                listOf(Param(LoansParamKeys.LOAN_COUNT, loanCount.toString())),
            ),
        )
    }
```

</details>

- `const val LOAN_KIND = "loan_kind"`
- `const val PRINCIPAL_BAND = "principal_band"`
- `const val TENURE_MONTHS = "tenure_months"`
- `const val RATE_BAND = "rate_band"`
- `const val FORM_STEP = "form_step"`
- `const val LOAN_COUNT = "loan_count"`
- `const val REMINDER_LEAD_DAYS = "reminder_lead_days"`

```kotlin
object LoansParamValues
```
`loans` parameter values. Bands rather than amounts. Analytics answers "do people track large loans?", which a band answers and an exact figure answers at the cost of shipping a financial profile to a third party.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansCrashExtensions.kt:61</code></summary>

```kotlin

private fun principalBand(principal: Double): String = when {
    principal < 1_000 -> LoansParamValues.PRINCIPAL_BAND_SMALL
    principal < 10_000 -> LoansParamValues.PRINCIPAL_BAND_MEDIUM
    principal < 100_000 -> LoansParamValues.PRINCIPAL_BAND_LARGE
    else -> LoansParamValues.PRINCIPAL_BAND_XLARGE
}
```

</details>

- `const val PRINCIPAL_BAND_SMALL = "lt_1k"`
- `const val PRINCIPAL_BAND_MEDIUM = "1k_10k"`
- `const val PRINCIPAL_BAND_LARGE = "10k_100k"`
- `const val PRINCIPAL_BAND_XLARGE = "gte_100k"`
- `const val RATE_BAND_LOW = "lt_5pct"`
- `const val RATE_BAND_MID = "5_15pct"`
- `const val RATE_BAND_HIGH = "gte_15pct"`
- `const val FORM_STEP_DETAILS = "details"`
- `const val FORM_STEP_TERMS = "terms"`
- `const val FORM_STEP_REVIEW = "review"`

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansAnalyticsExtensions.kt`

```kotlin
fun AnalyticsHelper.trackLoanFormStep(step: String, completed: Boolean)
```
One step of the add/edit funnel. Emitting a step event rather than only start/finish is what makes "where do people give up entering a loan?" answerable.

```kotlin
fun AnalyticsHelper.trackAmortizationViewed(kind: String, tenureMonths: Int)
```
The amortization schedule opened for a loan. Lives here rather than in the `amortization` feature because the schedule is a projection OF a loan — the funnel it belongs to is the loans funnel.

```kotlin
fun AnalyticsHelper.trackLoanReminderScheduled(leadDays: Int)
```
A reminder scheduled from `LoanReminderUseCase`, where no tracker is in scope.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansAnalyticsTracker.kt`

```kotlin
class LoansAnalyticsTracker(
```
`loans` feature tracker — DEMO-SHOWCASE. It is the template's worked example of the per-feature layout: shipped and synced like template code, and deleted by `remove-demo.sh --clean` along with the `feature/loans` module it tracks.

```kotlin
fun rememberLoansAnalyticsTracker(): LoansAnalyticsTracker
```
Composition-scoped `LoansAnalyticsTracker`, sharing the ambient `AnalyticsHelper`.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansCrashExtensions.kt`

```kotlin
fun CrashReporter.setLoanContext(
```
The loan being edited or viewed when a crash occurs. `principal` is banded on the way in — the caller passes the real figure and this function decides what reaches the report, exactly as `LoansAnalyticsTracker` does.

```kotlin
fun CrashReporter.setAmortizationContext(scheduleRows: Int)
```
Amortisation is the one place in this feature that builds an unbounded list — one row per month — so the row count is the first thing worth knowing about an OOM or a jank report here.

```kotlin
fun CrashReporter.setLoanFormStep(step: String)
```
Which step of the add/edit form was open. Pairs with the analytics funnel of the same name.

```kotlin
fun CrashReporter.setLoanCount(count: Int)
```
How many loans the user holds — list-rendering crashes scale with this.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansCrashKeys.kt`

```kotlin
object LoansCrashKeys
```
`loans` crash context — DEMO-SHOWCASE, deleted by `--clean` with the loans feature. The same file in a fork's own feature package is fork-owned.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansCrashExtensions.kt:36</code></summary>

```kotlin
    tenureMonths: Int,
) {
    setCustomKey(LoansCrashKeys.LOAN_KIND, kind)
    setCustomKey(LoansCrashKeys.PRINCIPAL_BAND, principalBand(principal))
    setCustomKey(LoansCrashKeys.TENURE_MONTHS, tenureMonths.toString())
    log("loans -> $kind / ${tenureMonths}mo")
}
```

</details>

- `const val LOAN_KIND = "loans_kind"`
- `const val PRINCIPAL_BAND = "loans_principal_band"`
- `const val TENURE_MONTHS = "loans_tenure_months"`
- `const val SCHEDULE_ROWS = "loans_schedule_rows"`
- `const val FORM_STEP = "loans_form_step"`
- `const val LOAN_COUNT = "loans_count"`

---

_10 type(s), 105 function(s)/property(ies); 33 carry KDoc at source; 0 authored example(s); 8 live call site(s)._
<!-- api-docs:end -->
