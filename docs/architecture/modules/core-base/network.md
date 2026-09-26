# `core-base/network`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_NETWORK.md`
> **Measured:** 21 Kotlin files, 0 test files

**Defines annotations:** `@ApiBinding`

## Principal types

`AccessPoint`, `AccessPointKind`, `AccessPointRegistry`, `ApiBinding`, `AuthHeaderBridge`, `AuthProviders`, `AuthScheme`, `AuthTokenSource`, `DefaultHeaderProvider`, `DynamicBaseUrlConfig`, `DynamicBaseUrlPlugin`, `DynamicLoggableHosts`, `DynamicUrlConfigProvider`, `HeaderSpec`  …and 11 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/network sha=a9d5261dcebfab423de4c411f931317292a6f16a -->
## API reference

_Generated from `core-base/network` at tree `a9d5261dcebf` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/AccessPointRegistry.kt`

```kotlin
enum class AccessPointKind
```
Transport kind of a declared network access point.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt:36</code></summary>

```kotlin
        AccessPoint(
            id = "main",
            kind = AccessPointKind.REST,
            baseUrl = "https://api.example.com/",
            loggableHost = "api.example.com",
        ),
        AccessPoint(
```

</details>

```kotlin
data class AccessPoint(
```
One declared network access point — a named endpoint the app talks to.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt:33</code></summary>

```kotlin
    // Edit access points THERE (the SoT) and run `./gradlew syncForkConfig`; do not hand-edit this block.
    // `type` defaults to UrlType(id.uppercase()) — value-class-equal to the AppUrlTypes.* constants.
    val points: List<AccessPoint> = listOf(
        AccessPoint(
            id = "main",
            kind = AccessPointKind.REST,
            baseUrl = "https://api.example.com/",
```

</details>

```kotlin
class AccessPointRegistry(val points: List<AccessPoint>)
```
Template registry MECHANISM over a fork-provided list of `points`.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:69</code></summary>

```kotlin
    // The fork's generated access points, wrapped by the framework registry mechanism (core-base/network).
    // The restApi("<id>") DSL and AppMultiUrlConfigProvider both resolve transports/base-URLs from this.
    single { AccessPointRegistry(AppAccessPoints.points) }

    // Unified access-point provider — resolves every named UrlType to its REST base URL from the
    // AccessPointRegistry. Clients thread it via
    // setupDefaultHttpClient(multiUrlProvider = get(), urlType = AppUrlTypes.<NAME>).
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/annotation/ApiBinding.kt`

```kotlin
annotation class ApiBinding(val accessPoint: String)
```
Binds this API type to a declared access point, so its Koin binding is derived rather than written.

<details><summary>Example</summary>

```kotlin
@ApiBinding("coingecko")
interface CoinGeckoApi { ... }
```

</details>

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appconfig/api/impl/AppConfigApiImpl.kt:32</code></summary>

```kotlin
 * so Koin can build the binding on an unconfigured fork safely.
 */
@ApiBinding("lwmswhoxvvoagzkqxiyd")
class AppConfigApiImpl(
    private val supabase: SupabaseConfigClient,
) : AppConfigApi {
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/AuthScheme.kt`

```kotlin
enum class AuthScheme
```
How an access point authenticates, declared as `auth:` in `app-profile/app.yaml#network.access_points[]`.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt:83</code></summary>

```kotlin
            basePath = "fineract-provider/api/v1/",
            loggableHost = "sandbox.mifos.community",
            auth = AuthScheme.BASIC,
            headers = listOf(
                HeaderSpec(name = "Fineract-Platform-TenantId", value = "default"),
                HeaderSpec(name = "Authorization", runtimeKey = "fineract.auth"),
            ),
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/AuthTokenSource.kt`

```kotlin
fun interface AuthTokenSource
```
The stored credential, as a stream. A PORT, implemented where the credential actually lives (`core/data`, over `core/datastore`'s encrypted store).

```kotlin
class AuthHeaderBridge(
```
Keeps `RuntimeHeaderStore` in step with the stored credential, so `Authorization` is automatic.

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/config/AuthSchemeTest.kt:60</code></summary>

```kotlin
        val key = AuthScheme.runtimeKeyFor("fineract")

        AuthHeaderBridge(
            points = listOf(point("fineract", AuthScheme.BASIC)),
            tokenSource = AuthTokenSource { token },
            headers = headers,
        ).start(TestScope(testScheduler))
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/DefaultHeaderProvider.kt`

```kotlin
interface DefaultHeaderProvider
```
Headers this app sends on EVERY request, resolved per access point.

<details><summary>Example</summary>

```kotlin
object ProjectNetworkHeaders : DefaultHeaderProvider {
    override fun headersFor(accessPointId: String): Map<String, String> = when (accessPointId) {
        "main" -> mapOf("X-Client-Version" to BuildKonfig.VERSION_NAME)
        else -> emptyMap()
    }
}
```

</details>

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:57</code></summary>

```kotlin
    // built by `restApi(...)` resolves this, so a fork adds an app-wide header without hand-building
    // a Ktorfit and giving up the generated @ApiBinding wiring. Neutral on the template.
    single<DefaultHeaderProvider> { ProjectNetworkHeaders }

    // Every declared endpoint's Koin binding, GENERATED from app-profile#network.access_points into
    // the sibling [GeneratedApiBindings] (same package — no import, so this file keeps its zero-demo
    // -imports property and stays blind-copyable on a template sync). It lives HERE rather than in the
```

</details>

- `fun headersFor(accessPointId: String): Map<String, String> = emptyMap()` — Headers to append to every request for `accessPointId` — the `id:` from `app-profile/app.yaml#network.access_points`. Returns empty by default: the neutral template sends nothing extra.
- `val None: DefaultHeaderProvider = object : DefaultHeaderProvider {}` — The no-op default. Bound by `NetworkModule` unless a fork binds its own.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/DynamicBaseUrlPlugin.kt`

```kotlin
class DynamicBaseUrlPlugin private constructor(
```
Ktor plugin that dynamically sets the base URL for each request based on a `DynamicUrlConfigProvider` or `MultiUrlConfigProvider`.

<details><summary>Example</summary>

```kotlin
val client = httpClient {
    install(DynamicBaseUrlPlugin) {
        configProvider = myConfigProvider
    }
}
// one client per named endpoint
val server1Client = httpClient {
    install(DynamicBaseUrlPlugin) {
        multiConfigProvider = myMultiConfigProvider
        urlType = AppUrlTypes.SERVER1   // == UrlType("SERVER1")
    }
}

val stagingClient = httpClient {
    install(DynamicBaseUrlPlugin) {
        multiConfigProvider = myMultiConfigProvider
        urlType = AppUrlTypes.STAGING
    }
}
```

</details>

```kotlin
class DynamicBaseUrlConfig
```
Configuration class for `DynamicBaseUrlPlugin`.

```kotlin
class DynamicLoggableHosts(
```
A dynamic list implementation that provides loggable hosts from a `DynamicUrlConfigProvider`. This list is evaluated each time it's iterated, so it reflects the currently configured loggable hosts.

<details><summary>Example</summary>

```kotlin
val client = httpClient(
    setupDefaultHttpClient(
        baseUrl = "https://placeholder.local/",
        loggableHosts = DynamicLoggableHosts(myConfigProvider),
    )
) {
    install(DynamicBaseUrlPlugin) {
        configProvider = myConfigProvider
    }
}
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/DynamicUrlConfigProvider.kt`

```kotlin
interface DynamicUrlConfigProvider
```
Interface for providing dynamic URL configuration at runtime. Implementations of this interface allow the HTTP client to dynamically switch between different server endpoints without recreating the client.

<details><summary>Example</summary>

```kotlin
class MyConfigProvider(
    private val preferencesRepository: UserPreferencesRepository
) : DynamicUrlConfigProvider {
    override fun getBaseUrl(): String = preferencesRepository.selectedServer.value?.url
        ?: "https://default.api.com"

    override fun getLoggableHosts(): List<String> = listOf(
        preferencesRepository.selectedServer.value?.host ?: "default.api.com"
    )
}
```

</details>

- `fun getBaseUrl(): String` — Returns the current base URL to use for API requests. This is called for each request, allowing runtime URL switching.
- `fun getLoggableHosts(): List<String>` — Returns the list of hostnames that should have HTTP logging enabled. This is called dynamically, allowing the logging filter to adapt to the currently selected server.

```kotlin
value class UrlType(val key: String)
```
Open, extensible identifier for a named API endpoint.

<details><summary>Example</summary>

```kotlin
// in core/ (your project) — name them however you like, add as many as you need
object AppUrlTypes {
    val MAIN    = UrlType.MAIN
    val SERVER1 = UrlType("SERVER1")
    val STAGING = UrlType("STAGING")
}
```

</details>

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/AppMultiUrlConfigProvider.kt:31</code></summary>

```kotlin
) : MultiUrlConfigProvider {

    override fun getBaseUrl(type: UrlType): String =
        registry.restBaseUrl(type)
            ?: registry.restBaseUrl(UrlType.MAIN)
            ?: error("No REST access point registered for $type or MAIN")
```

</details>

```kotlin
interface MultiUrlConfigProvider : DynamicUrlConfigProvider
```
Extension of `DynamicUrlConfigProvider` for applications that expose more than one endpoint (identified by an open `UrlType` — the project names them in `core/`).

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:74</code></summary>

```kotlin
    // AccessPointRegistry. Clients thread it via
    // setupDefaultHttpClient(multiUrlProvider = get(), urlType = AppUrlTypes.<NAME>).
    single<MultiUrlConfigProvider> { AppMultiUrlConfigProvider(get()) }

    // Per-point Supabase client factory — URL from AccessPointRegistry, anon key by id.
    single {
        SupabaseClientFactory(
```

</details>

- `fun getBaseUrl(type: UrlType): String` — Returns the base URL for the specified `type`.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/factory/ResultSuspendConverterFactory.kt`

```kotlin
class ResultSuspendConverterFactory : Converter.Factory
```
A custom `Converter.Factory` for Ktorfit that provides a suspend response converter which wraps successful or error HTTP responses into a sealed `NetworkResult` type.

<details><summary>Example</summary>

```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): Result<List<User>, RemoteError>
}
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/HeaderSpec.kt`

```kotlin
data class HeaderSpec(
```
A header an access point sends on every request, declared in `app-profile/app.yaml#network.access_points[].headers[]`. Two kinds, because they have different lifetimes: - **static** (`value` set) — known at build time.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt:85</code></summary>

```kotlin
            auth = AuthScheme.BASIC,
            headers = listOf(
                HeaderSpec(name = "Fineract-Platform-TenantId", value = "default"),
                HeaderSpec(name = "Authorization", runtimeKey = "fineract.auth"),
            ),
        ),
        AccessPoint(
```

</details>

```kotlin
class RuntimeHeaderStore
```
Values for the `runtimeKey` headers, written when they become known and read on every request.

<details><summary>Example</summary>

```kotlin
// after a successful sign-in
runtimeHeaders[FineractHeaders.AUTH] = "Basic ${base64("$user:$password")}"

// on sign-out — clearing is as important as setting
runtimeHeaders.clear(FineractHeaders.AUTH)
```

</details>

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:52</code></summary>

```kotlin
    // Runtime header values — written at login (Basic / OAuth), read on EVERY request. A singleton,
    // because the whole point is that a value set after the clients were built still reaches them.
    single { RuntimeHeaderStore() }

    // Default request headers, from the fork-owned ProjectNetworkHeaders seam. Every REST client
    // built by `restApi(...)` resolves this, so a fork adds an app-wide header without hand-building
    // a Ktorfit and giving up the generated @ApiBinding wiring. Neutral on the template.
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/KtorHttpClient.kt`

```kotlin
expect fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient
```
Creates a Ktor `HttpClient` on the target's own engine (OkHttp, Darwin, JS fetch, CIO).

```kotlin
data class AuthProviders(
```
The auth-credential providers for `setupDefaultHttpClient`, grouped so the client builder stays under the parameter-count limit. All null (the default) installs no `Auth` plugin.

```kotlin
fun setupDefaultHttpClient(
```
Provides a default `HttpClientConfig` setup for use with a Ktor-based HTTP client.

<details><summary>Example</summary>

```kotlin
val client = httpClient(setupDefaultHttpClient(baseUrl = "https://api.example.com"))
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/NetworkDsl.kt`

```kotlin
fun AccessPointRegistry.ktorfitFor(
```
Build a Ktorfit for the REST access point declared under `accessPointId`. Base URL + loggable host + proxied host come from this `AccessPointRegistry`; throws if the id isn't declared.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/NetworkError.kt`

```kotlin
enum class NetworkError
```
Represents standardized error types for remote or network operations. This enum is typically used with the `NetworkResult.Error` variant to describe what kind of failure occurred.

<details><summary>Used in the template — <code>feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/ui/CurrencyRatesScreenPreview.kt:64</code></summary>

```kotlin
            amount = "100",
            targetCode = "EUR",
            spotState = ScreenState.Error(IllegalStateException("offline"), isNetworkError = true),
            onAmountChange = {},
            onTargetChange = {},
            onRetry = {},
        )
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/NetworkResult.kt`

```kotlin
sealed interface NetworkResult<out D, out E : NetworkError>
```
Represents the result of a network or remote operation, encapsulating either a success or an error.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/SupabaseClientFactory.kt`

```kotlin
class SupabaseClientFactory(
```
Per-point Supabase client factory. Builds one `SupabaseConfigClient` per declared Supabase `AccessPoint`.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:78</code></summary>

```kotlin
    // Per-point Supabase client factory — URL from AccessPointRegistry, anon key by id.
    single {
        SupabaseClientFactory(
            registry = get(),
            anonKeyFor = AppSupabaseAnonKeys::forId,
            // THE LINK THAT MAKES THE SEAM REAL. SupabaseConfigClient and SupabaseClientFactory both
            // accept the extras hook, but if nothing passes it here the default `{ {} }` wins and a
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/SupabaseConfigClient.kt`

```kotlin
class SupabaseConfigClient(
```
Generic Supabase client wrapper for KMP projects.

<details><summary>Example</summary>

```kotlin
// Consumers do NOT construct this directly — SupabaseClientFactory builds one per declared
// SUPABASE access point, and `supabaseApi("<id>")` injects it into the fork's API type.
val configClient = supabaseClientFactory.requireClientFor("project")

// Use the client for queries
if (configClient.isConfigured) {
    val result = configClient.postgrest
        .from("app_config")
        .select()
        .decodeSingle<MyConfig>()
}
```

</details>

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appconfig/api/impl/AppConfigApiImpl.kt:34</code></summary>

```kotlin
@ApiBinding("lwmswhoxvvoagzkqxiyd")
class AppConfigApiImpl(
    private val supabase: SupabaseConfigClient,
) : AppConfigApi {

    override val isConfigured: Boolean get() = supabase.isConfigured
```

</details>

```kotlin
interface SupabaseCredentials
```
Interface for Supabase credentials. Implement this interface to provide Supabase URL and anon key from your project's build configuration or secrets.

<details><summary>Example</summary>

```kotlin
object MySupabaseCredentials : SupabaseCredentials {
    override val url: String = BuildConfig.SUPABASE_URL
    override val anonKey: String = BuildConfig.SUPABASE_ANON_KEY
}
```

</details>

- `val url: String` — The Supabase project URL.
- `val anonKey: String` — The Supabase anon (public) key. This key is safe to use in client-side code.
- `val isConfigured: Boolean` — Checks if the credentials are properly configured. Returns false if URL or key contain placeholder values.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/SupabaseExtrasProvider.kt`

```kotlin
fun interface SupabaseExtrasProvider
```
Fork seam for the supabase-kt modules the default exposer does not install. `SupabaseConfigClient` installs Postgrest and nothing else — Auth / ComposeAuth / Realtime / Storage are opt-in.

<details><summary>Example</summary>

```kotlin
single<SupabaseExtrasProvider> {
    SupabaseExtrasProvider { id ->
        when (id) {
            AUTH_PROJECT -> {
                {
                    install(Auth) { … }
                    install(ComposeAuth) { … }
                }
            }
            ANALYTICS_PROJECT -> {
                { install(Realtime) }
            }
            // Shared across several projects — a `when` branch takes a comma-separated list, which
            // an if/else chain cannot express without repeating the block.
            PROJECT_A, PROJECT_B -> {
                { install(Storage) }
            }
            else -> {
                {}
            }
        }
    }
}
```

</details>

---

_22 type(s), 13 function(s)/property(ies); 35 carry KDoc at source; 12 authored example(s); 14 live call site(s)._
<!-- api-docs:end -->
