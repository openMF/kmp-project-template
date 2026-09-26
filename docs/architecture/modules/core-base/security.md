# `core-base/security`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_SECURITY.md`
> **Measured:** 43 Kotlin files, 7 test files

## Principal types

`BiometricAuthenticator`, `BiometricResult`, `CertificatePinConfig`, `DeepLinkValidator`, `FailedAttemptTracker`, `FailureAction`, `SecureAuthManager`, `SecureNavHandler`, `SecureWiper`, `SecurityConfig`, `SecurityPolicy`, `SecurityState`, `SensitiveString`, `SessionManager`  …and 1 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/security sha=4bf52cd496293127072915d5d0470bc25b568a98 -->
## API reference

_Generated from `core-base/security` at tree `4bf52cd49629` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/BiometricAuthenticator.kt`

```kotlin
expect class BiometricAuthenticator()
```
Platform-agnostic biometric authentication interface.

```kotlin
sealed class BiometricResult
```
The outcome of one biometric prompt.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/BuildInfo.kt`

```kotlin
expect fun isReleaseBuild(): Boolean
```
Platform-specific build type detection. Each platform provides its own heuristic to determine whether the app is running in a release configuration. Used by `SecurityConfig` to auto-configure security policies without consumer input.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/CertificatePinConfig.kt`

```kotlin
data class CertificatePinConfig(
```
Configuration for TLS certificate pinning per hostname. Consumer apps must configure pins for their API domains.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/DeepLinkValidator.kt`

```kotlin
class DeepLinkValidator(
```
Validates deep link URIs against a whitelist of allowed schemes and hosts to prevent open-redirect and injection attacks. Consumer apps register their allowed patterns during initialization.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/di/SecurityModule.kt`

```kotlin
val SecurityModule = module
```
Zero-config security Koin module. Auto-detects build type via platform-specific `kpt.core.base.security.isReleaseBuild` and registers all security components with sensible defaults.

```kotlin
expect val platformSecurityModule: Module
```
Per-target security bindings supplied by each `actual` — the keystore/keychain backing and the platform biometric prompt.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/FailedAttemptTracker.kt`

```kotlin
class FailedAttemptTracker(
```
Tracks failed authentication attempts and triggers lockout or data wipe when configured thresholds are exceeded.

```kotlin
enum class FailureAction
```
What the tracker did in response to a failed attempt — the caller's cue for what to show next. Escalates in order: the attempt was counted, the account locked, or the local data was wiped after the final permitted attempt.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/SecureAuthManager.kt`

```kotlin
class SecureAuthManager(
```
Unified authentication manager that coordinates failure tracking, session lifecycle, and biometric authentication.

<details><summary>Example</summary>

```kotlin
val authManager: SecureAuthManager = koinInject()
// on wrong password:
val action = authManager.onAuthFailure()
// on correct password:
authManager.onAuthSuccess()
```

</details>

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/SecureNavHandler.kt`

```kotlin
class SecureNavHandler(
```
Deep link security handler wrapping `DeepLinkValidator`. Provides a clean API for navigation code to validate incoming deep links before processing them.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/SecureWiper.kt`

```kotlin
expect class SecureWiper()
```
Securely wipes sensitive data from storage and memory. Used by `FailedAttemptTracker` when the wipe threshold is exceeded and by session management on logout.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/SecurityConfig.kt`

```kotlin
data class SecurityConfig(
```
Central configuration for all security behavior. Controls debug/release gates and configurable policy thresholds. Created per-platform in each app module and passed to `securityModule` as a function parameter to avoid Koin init-order issues.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/SecurityGate.kt`

```kotlin
fun SecurityGate(
```
Root security composable that auto-wires all runtime security behavior.

<details><summary>Example</summary>

```kotlin
@Composable
fun App() {
    SecurityGate {
        AppTheme { NavHost(...) }
    }
}
```

</details>

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/SecurityPolicy.kt`

```kotlin
data class SecurityPolicy(
```
Configurable security policy. Consumer apps can adjust thresholds based on their risk profile.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/SecurityState.kt`

```kotlin
class SecurityState
```
Observable security state exposed to the UI layer via `LocalSecurityState`. Updated automatically by `SecurityGate`.

```kotlin
val LocalSecurityState = staticCompositionLocalOf<SecurityState>
```
CompositionLocal providing `SecurityState` to the composable tree. Provided by `SecurityGate`. Throws if accessed outside of a SecurityGate.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/SensitiveString.kt`

```kotlin
class SensitiveString(private val chars: CharArray) : AutoCloseable
```
Zeroable credential wrapper backed by `CharArray` instead of `String`. JVM String is immutable and can linger in memory. `SensitiveString` allows explicit zeroing after use to minimize exposure window.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/SessionManager.kt`

```kotlin
class SessionManager(
```
Manages user session lifecycle with inactivity timeout. Call `touch` on every user interaction to reset the inactivity timer. Call `checkTimeout` periodically (e.g., on app foreground) to verify the session hasn't expired.

### `core-base/security/src/commonMain/kotlin/kpt/core/base/security/TamperDetector.kt`

```kotlin
expect class TamperDetector()
```
Detects runtime environment tampering such as root/jailbreak, debugger attachment, and signature mismatch. Each platform provides its own detection heuristics via `expect/actual`.

---

_15 type(s), 5 function(s)/property(ies); 20 carry KDoc at source; 2 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->
