# `core-base/observability`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_OBSERVABILITY.md`
> **Measured:** 4 Kotlin files, 1 test files

## Principal types

`ConsoleCrashReporter`, `CrashReporter`, `CrashSeverity`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/observability sha=586c421b447a4e8525906a7c89b687e93b26b38d -->
## API reference

_Generated from `core-base/observability` at tree `586c421b447a` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/observability/src/commonMain/kotlin/kpt/core/base/observability/ConsoleCrashReporter.kt`

```kotlin
class ConsoleCrashReporter : CrashReporter
```
Default `CrashReporter` binding that writes events to stdout. Suitable for local development and CI runs where you want to see crash-relevant events in the build log without wiring a real crash-reporter SDK.

<details><summary>Example</summary>

```kotlin
[CrashReporter] Exception: java.io.IOException: connect timed out — message="retryAll: gave up"
[CrashReporter] Info: user signed in
[CrashReporter] User: u-12345 (or null on sign-out)
```

</details>

### `core-base/observability/src/commonMain/kotlin/kpt/core/base/observability/CrashReporter.kt`

```kotlin
interface CrashReporter
```
Fork-customization seam for crash + non-fatal-error reporting. The toolkit ships `ConsoleCrashReporter` which writes everything to stdout — sufficient for local development but not for shipped builds.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansCrashExtensions.kt:31</code></summary>

```kotlin
 * what reaches the report, exactly as `LoansAnalyticsTracker` does.
 */
fun CrashReporter.setLoanContext(
    kind: String,
    principal: Double,
    tenureMonths: Int,
) {
```

</details>

- `fun recordException(throwable: Throwable, message: String? = null)` — Record a caught exception with optional context message. Use for: - Retry-loop exhaustion (e.g.
- `fun recordMessage(message: String, level: CrashSeverity = CrashSeverity.Info)` — Record a free-form message at a severity level.
- `fun setUser(userId: String?)` — Set (or clear) the current user identifier for subsequent crash reports. Pass `null` on sign-out so crashes after sign-out are attributed to "anonymous".
- `val isConfigured: Boolean` — `true` when the fork has wired a real crash reporter. `false` for the default `ConsoleCrashReporter` (signals to startup checks that crash reporting is local-only).

```kotlin
enum class CrashSeverity
```
Severity vocabulary for `CrashReporter.recordMessage`.

### `core-base/observability/src/commonMain/kotlin/kpt/core/base/observability/di/ObservabilityModule.kt`

```kotlin
val observabilityModule = module
```
Koin module exposing the default `CrashReporter` binding (stdout console).

<details><summary>Example</summary>

```kotlin
// In fork's app module
single<CrashReporter> { FirebaseCrashlyticsReporter() }
```

</details>

---

_3 type(s), 5 function(s)/property(ies); 8 carry KDoc at source; 2 authored example(s); 1 live call site(s)._
<!-- api-docs:end -->
