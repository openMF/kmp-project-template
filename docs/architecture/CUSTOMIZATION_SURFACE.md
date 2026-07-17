# Customization Surface — the fork-ownership contract

`customization-surface.yaml` (repo root) is the **single declared source of truth
for who owns each path** when a fork syncs template updates. It replaces ownership
knowledge that was previously implicit and scattered across `sync-dirs.sh`
(`SYNC_DIRS` + `EXCLUSIONS`), `customizer.sh`, and the `syncForkConfig` copy map.

## Why it exists

A fork runs `sync-dirs.sh` to pull template updates while keeping its own branding
+ features. The hard question is *"which files may the sync overwrite?"* When that
answer lives only in an ad-hoc `EXCLUSIONS` list, a path with no exclusion gets
**blind-overwritten** — and that silently drops fork edits. The motivating case:

> `cmp-android/src/main/AndroidManifest.xml` is template-shipped **and**
> fork-extended (a fork adds `RECORD_AUDIO`, `FOREGROUND_SERVICE_MICROPHONE`, …).
> The `EXCLUSIONS` map preserved only `src/main/res`, so a full sync overwrote the
> manifest and **dropped the fork's permissions** — breaking the app.

The contract makes ownership **explicit, single-source, and machine-checkable**, so
that whole class of silent loss becomes impossible.

## The three ownership modes

| owner | meaning | sync behaviour |
|---|---|---|
| `template` | template is the sole author | **overwrite** the fork's copy. A fork edit here is drift — a genuine fix belongs **upstream** as a PR to `openMF/kmp-project-template`. |
| `fork` | the fork is the sole author | **never touch** it (branding, demo, the `core/store` seam, generated `Config.xcconfig`, local flavors, icons, store listings, fork identity). |
| `merge` | both author | **3-way merge**, never a blind copy (`AndroidManifest.xml`, `strings.xml`, `libs.versions.toml`, `settings.gradle.kts`, nav host). |

## Precedence

Rules are ordered **most-specific → most-general**, and the **first matching rule
wins**. So the narrow `merge`/`fork` carve-outs are declared *before* the broad
`template` module globs and correctly win over them. A trailing catch-all `**`
rule (`owner: fork`, `default: true`) means the runtime never clobbers an unknown
path — while `--verify` flags anything that reaches it so a human classifies it.

Glob syntax: `*` matches within one path segment; `**` matches across segments;
`**/x` matches `x` at any depth (including the repo root).

## The reader / validator — `scripts/customization-surface.sh`

Pure bash + awk (no `yq`/`jq`/`python` dependency — runs on any fork as-is).

```bash
# what owns this path?
scripts/customization-surface.sh resolve cmp-android/src/main/AndroidManifest.xml
#   → cmp-android/src/main/AndroidManifest.xml   merge  (strategy: manifest-union)

# owner of every tracked path
scripts/customization-surface.sh report

# CI canary — fail if any tracked path is unclassified (only matches the default)
scripts/customization-surface.sh verify
#   → ── customization-surface coverage: 1587/1587 classified ──
#   → ✅ every tracked path has an explicit owner
```

Source it as a library too:

```bash
source scripts/customization-surface.sh
cs_resolve_owner    "gradle/libs.versions.toml"   # → merge
cs_resolve_strategy "gradle/libs.versions.toml"   # → catalog-3way
```

## How `sync-dirs.sh` uses it (today)

`sync-dirs.sh` sources the reader and, before the mechanical sync, prints the
**`merge`-owned paths in the sync surface** as an advisory — so a maintainer can
confirm the `EXCLUSIONS` map preserves fork edits. This step is fully guarded and
**cannot abort a sync**. The mechanical `SYNC_DIRS` / `EXCLUSIONS` behaviour is
**unchanged** in this step; the contract is the declared source of truth the merge
engine adopts next.

## Recommended CI wiring

Add a coverage gate so a new template file can never land unclassified (and thus
never silently clobber a fork later):

```yaml
- name: Verify customization-surface coverage
  run: bash scripts/customization-surface.sh verify
```

## Roadmap (follow-ups)

1. **Merge engine adoption** — `sync-dirs.sh` consults the contract per path
   (template → copy, fork → skip, merge → 3-way) and retires the ad-hoc
   `EXCLUSIONS` map. `merge` strategies (`manifest-union`, `strings-union`,
   `catalog-3way`, `include-union`, `kotlin-3way`) become real merge drivers.
2. **`syncForkConfig` alignment** — the plugin reads the same contract to know
   which generated files it owns vs must not clobber.

## Editing the contract

- A new **template-owned** module → add a `template` rule (and, if forks extend a
  specific file inside it, a narrower `merge`/`fork` rule *above* it).
- A new **fork-branded** path → add a `fork` rule.
- A file both sides edit → add a `merge` rule with a `strategy:`.
- Always run `scripts/customization-surface.sh verify` after editing.
