# Kotlin 2.4.0 dependency modernization — investigation + verification record

Epic: `kotlin-2-4-0-upgrade`. Kotlin toolchain was already at **2.4.0** (from PR #230),
so this branch is pure ecosystem modernization. KSP2 is on (`ksp.useKSP2=true`), KSP `2.3.9`,
ktorfit `2.7.5` (the compiler-plugin ABI blocker is already resolved).

## Investigations

### Q2 — `androidxComposeCompiler = "1.5.15"` — vestigial or retained?
**Verdict: VESTIGIAL (retained, no bump).** The pin feeds only the `androidx-compose-compiler`
*library* alias (`androidx.compose.compiler:compiler`), which is **not referenced by any
`build.gradle.kts`**. Every module compiles Compose via the bundled `libs.plugins.compose.compiler`
(`org.jetbrains.kotlin.plugin.compose`, `version.ref = kotlin`). Left in place (removal is optional
cleanup, out of scope for a dependency bump); the version is inert under Kotlin 2.4.0.

### Q4 — `kotlin-inject 0.9.0` — KSP2/2.4.0-clean or bump?
**Verdict: retained at 0.9.0; KSP2 codegen clean pending CI compile.** `kotlin-inject` is a KSP
processor (`me.tatarka.inject:kotlin-inject-compiler-ksp`). Under `ksp.useKSP2=true` its generated
code must compile; the authoritative check is CI's `compileKotlin*` across targets (this branch's
PR). No newer 0.9.x was required by the audit; no bump applied.

## Version verification (Maven Central / Google Maven, 2026-07-08)

All bump targets were confirmed published before pinning:

| Bucket | Notable confirmations |
|---|---|
| A (minors) | kotlinx-serialization 1.11.0, coil 3.5.0, koin 4.2.2, okhttp 5.4.0, jackson 2.22.0, ktor 3.5.1, kermit 2.1.0, roborazzi 1.67.0, spotless 8.8.0, junit-jupiter **5.14.4** (stays 5.x — D7, 6.x deliberately skipped), kmptoolkit 3.5.3, kmpflavors 2.8.1, github-api 1.330 |
| B (alpha→stable) | **androidx.room3:room3-runtime 3.0.0** (stable — confirmed release; the 2.x `androidx.room:room-runtime` line is a different artifact), androidx.sqlite 2.7.0 |
| C (majors) | aboutLibraries 15.0.3, ktlint-gradle 14.2.0, koin-annotations 4.2.2, **constraintlayout-compose-multiplatform 0.8.0** (plain coordinate — the `-shaded-core` is a separate variant) |
| Tier-3 | compose-bom 2026.06.01, core-ktx 1.19.0, browser 1.10.0, firebase-bom 34.15.0, android-tools 32.2.1 |

## Deliberately held (NOT bumped)

- `store = "5.1.0-alpha08"` — deliberately ahead of stable (D3).
- `composeLifecycle = "2.11.0-beta01"` — deliberately ahead of stable (D3).
- `androidxComposeRuntimeTracing = "1.11.0"` — plan's 1.11.4 was unconfirmed (latest is
  1.12.0-beta02); low-value tracing lib held to avoid an unverified pin.

## Verification strategy

Per-target main+test compile is delegated to **CI** (`pr-check.yml` + `quality-gate.yml`) rather than
slow local Gradle. `scripts/verify-2-4-0.sh` mirrors the CI matrix for local reproduction of a
specific target failure. Reformat (ktlint 14) + `yarn.lock` regen + dependency-guard baseline are a
separate finalization step once CI is green.
