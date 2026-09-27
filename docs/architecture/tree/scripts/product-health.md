# `scripts/product-health/`

> Part of [`scripts/`](../scripts.md)  
> **Measured:** 370 tracked files — 140× `.kt`, 75× `.sh`, 52× `.md`, 29× `.yaml`, 12× `.gitkeep`

### Scripts

| Script | Purpose |
|---|---|
| `appid-consistency.sh` | gradle/fork.properties#app.id is the AUTHORED single source of truth |
| `cache-key-call-sites.sh` | no production call site spells a cacheKey string literal. |
| `core-module-deps.sh` | every core/<mod> build file full-copies, and its fork deps live in a seam. |
| `core-package-ownership.sh` | declared-package modules stay consistent with the contract. |
| `demo-strip-coherence.sh` | the POST-`--clean` tree must be coherent, not just the template's. |
| `deployment-whitelabel.sh` | the white-label boundary between the fork-owned deployment SoT |
| `fork-identity.sh` | DIRECTIONAL, placeholder-aware signing + org identity guard. |
| `fork-props-manifest-parity.sh` | the TWO writers of gradle/fork.properties must agree on |
| `fork-props-single-reader.sh` | gradle/fork.properties has ONE reader per language. |
| `generated-file-ownership.sh` | every WHOLE-FILE generated source is owner:fork. |
| `ios-pbxproj-identity.sh` | the iOS Xcode project MUST derive its bundle id + provisioning |
| `ios-swiftpm-linked-package.sh` | cmp-ios/KotlinMultiplatformLinkedPackage MUST stay |
| `kdoc-comment-balance.sh` | a glob written in a KDoc must not open or close a comment. |
| `keychain-cleanup.sh` | setup_ci's throwaway keychain must be GUARANTEED-cleaned so it never |
| `listing-sync.sh` | the store listing must sync to the store on EVERY store deploy state, |
| `migration-ledger.sh` | the Room migration ledger is append-only and internally coherent. |
| `network-access-points.sh` | app-profile is the ONLY place a network endpoint is declared, |
| `no-vacuous-assert.sh` | a test that cannot fail is worse than no test. |
| `pbxproj-merge.sh` | the Xcode project survives a template sync intact. |
| `ruby-toolchain-coherence.sh` | ONE Ruby version, declared once, reachable everywhere. |
| `secrets-alias-namespace.sh` | DIRECTIONAL guard on the vault-facing secrets files |
| `secrets-no-clobber-tracked.sh` | `/secrets pull` (BuildSecrets#materialize!) must NEVER |
| `self-test-canaries.sh` | runs every product-health canary (tests/*/run.sh) so the RED/GREEN |
| `shell-portability.sh` | tracked shell scripts must run on the RUNNERS, not just on a Mac. |
| `store-archetype-coverage.sh` | the 8 Store5 archetypes each keep a real showcase. |
| `store-fork-seam-wiring.sh` | the template actually CONSULTS its fork seams. |
| `store-listing.sh` | DIRECTIONAL, placeholder-aware store-copy guard. |
| `store-logout-purge.sh` | every store's rows are reachable by the logout purge. |
| `sync-merge-base.sh` | an `owner: merge` row must actually 3-way, not silently full-copy. |
| `test-fixture-honesty.sh` | a test fixture must PRODUCE the failure mode it claims. |

_…and 45 more._

<!-- tree-scaffold:end -->
## Significance

Thirty-three checks that fail the build on **architectural drift**, not on style. Spotless and Detekt
already police formatting; this suite polices the things a linter cannot see — that `appId` in the
version catalog still matches `app-profile`, that every Store5 archetype still has a showcase, that the
fork seams are wired, that the migration ledger was appended to rather than renumbered.

**Its 370 files are mostly RED FIXTURES,** and that is the point: a check that cannot be demonstrated
to FAIL on broken input has not been tested. `tests/*-canary/red*/` directories are deliberately
malformed, which is why the whole tree is excluded from detekt — asking a red fixture to satisfy the
linter deletes the evidence.

Run `TEMPLATE_SELF_BUILD=1` on the upstream template itself, where fork-identity checks would otherwise
misfire on placeholder values.
