#!/usr/bin/env bash
#
# framework-verify-idea-source-drift.sh — runtime-independent idea<->source drift gate.
#
# whole-source-idea-layer-reimport Phase 5 (AC-14 DRIFT-GATE-BLOCKING). This is the
# COMMITTED, standalone evaluator called by .github/workflows/quality-gate.yml. It runs on a
# stock ubuntu-latest runner with NO framework runtime — a bare `/idea verify --source-drift`
# slash command resolves to "command not found" on CI (AC-5), so the D4/D7 drift verdict is
# reconstructed here from artifacts committed alongside the source:
#
#   scripts/idea-source-drift-baseline.manifest   <relative-path>:<sha256> rows
#
# The manifest captures the expected content hash of the fork-owned data-contract (DTO/model)
# surface the idea-layer mirrors. If a listed source file's current content hash differs from
# the committed baseline (D4 DTO-field drift / D7 source-hash drift) OR a listed file is
# missing, that is a BLOCKER -> exit 2, which reds the CI job (shell -e default; no
# continue-on-error, no `|| true` on the job). This keeps the idea-layer locked to source:
# a source change without a matching idea-layer update + re-baseline fails the gate.
#
# Pure bash + find + a sha256 helper (sha256sum on Linux CI, shasum -a 256 on macOS) — mirrors
# the existing scripts/verify-demo-convention.sh pattern (no Gradle, no network, sub-second).
#
# Exit codes:  0 = clean (no drift)    2 = drift blocker(s) detected / manifest error
#
# Usage:
#   bash scripts/framework-verify-idea-source-drift.sh                 # verify real tree vs committed manifest
#   bash scripts/framework-verify-idea-source-drift.sh --manifest P --root D   # verify a custom tree (canary/fixture)
#   bash scripts/framework-verify-idea-source-drift.sh --fixture green # self-check: real tree, expect exit 0
#   bash scripts/framework-verify-idea-source-drift.sh --fixture red   # self-seed D4 drift, expect exit 2
#   bash scripts/framework-verify-idea-source-drift.sh --rebaseline    # maintainer: rewrite manifest from current source

set -uo pipefail

# Resolve repo root (evaluator lives in <repo>/scripts/) so paths are stable regardless of caller cwd.
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

MANIFEST_DEFAULT="$SCRIPT_DIR/idea-source-drift-baseline.manifest"
DTO_GLOB_ROOT="core/model/src/commonMain/kotlin/kpt/core/model/demo"

MANIFEST="$MANIFEST_DEFAULT"
ROOT="$REPO_ROOT"
FIXTURE=""
REBASELINE=0

while [ $# -gt 0 ]; do
  case "$1" in
    --manifest)   MANIFEST="$2"; shift 2 ;;
    --root)       ROOT="$2";     shift 2 ;;
    --fixture)    FIXTURE="$2";  shift 2 ;;
    --rebaseline) REBASELINE=1;  shift ;;
    *)            shift ;;
  esac
done

# ── portable sha256 ───────────────────────────────────────────────────────────────────
sha() {
  if command -v sha256sum >/dev/null 2>&1; then sha256sum "$1" | awk '{print $1}'
  else shasum -a 256 "$1" | awk '{print $1}'; fi
}

# ── --rebaseline (maintainer, after an intentional idea-layer-approved change) ──────────
if [ "$REBASELINE" -eq 1 ]; then
  cd "$REPO_ROOT"
  {
    echo "# idea-source-drift-baseline.manifest"
    echo "# Committed D4/D7 drift baseline consumed by scripts/framework-verify-idea-source-drift.sh."
    echo "# Rows: <relative-source-path>:<sha256>  — the fork-owned data-contract (DTO/model) surface"
    echo "# the idea-layer mirrors. Any content change to a listed file WITHOUT a corresponding"
    echo "# idea-layer update + re-baseline is idea<->source drift (D4 DTO-field / D7 hash) => exit 2."
    echo "# Re-baseline after an intentional, idea-layer-approved change:"
    echo "#   bash scripts/framework-verify-idea-source-drift.sh --rebaseline"
    while IFS= read -r f; do
      echo "${f}:$(sha "$f")"
    done < <(find "$DTO_GLOB_ROOT" -name '*.kt' 2>/dev/null | grep -v '/build/' | LC_ALL=C sort)
  } > "$MANIFEST_DEFAULT"
  echo "rebaselined: $MANIFEST_DEFAULT ($(grep -vc '^#' "$MANIFEST_DEFAULT") rows)"
  exit 0
fi

# ── --fixture red: self-seed a D4 DTO-field drift into a temp tree, expect detection ────
if [ "$FIXTURE" = "red" ]; then
  TMP="$(mktemp -d)"
  trap 'rm -rf "$TMP"' EXIT
  # Pick the first real DTO file + its baseline hash from the committed manifest.
  first_row="$(grep -vE '^#' "$MANIFEST_DEFAULT" | head -1)"
  rel="${first_row%:*}"
  base_hash="${first_row##*:}"
  mkdir -p "$TMP/$(dirname "$rel")"
  # Seed drift: copy the real file then APPEND a fake new DTO field (D4 drift).
  cp "$REPO_ROOT/$rel" "$TMP/$rel"
  printf '\n// SEEDED D4 DRIFT: val driftField: String = ""\n' >> "$TMP/$rel"
  # Fixture manifest keeps the ORIGINAL (pre-drift) hash -> current content mismatches.
  printf '%s:%s\n' "$rel" "$base_hash" > "$TMP/baseline.manifest"
  MANIFEST="$TMP/baseline.manifest"
  ROOT="$TMP"
elif [ "$FIXTURE" = "green" ]; then
  MANIFEST="$MANIFEST_DEFAULT"
  ROOT="$REPO_ROOT"
fi

# ── verify ─────────────────────────────────────────────────────────────────────────────
if [ ! -f "$MANIFEST" ]; then
  echo "BLOCKER: drift baseline manifest not found: $MANIFEST" >&2
  echo "  Generate it with: bash scripts/framework-verify-idea-source-drift.sh --rebaseline" >&2
  exit 2
fi

blockers=0
rows=0
while IFS= read -r line; do
  case "$line" in ''|\#*) continue ;; esac
  rows=$((rows + 1))
  rel="${line%:*}"
  expected="${line##*:}"
  target="$ROOT/$rel"
  if [ ! -f "$target" ]; then
    echo "DRIFT [D4/D7 missing]: $rel — in baseline manifest but absent from source (removed without idea-layer update)" >&2
    blockers=$((blockers + 1))
    continue
  fi
  actual="$(sha "$target")"
  if [ "$actual" != "$expected" ]; then
    echo "DRIFT [D4/D7 hash]: $rel — content changed vs baseline (expected ${expected:0:12}…, got ${actual:0:12}…)" >&2
    blockers=$((blockers + 1))
  fi
done < "$MANIFEST"

if [ "$rows" -eq 0 ]; then
  echo "BLOCKER: drift baseline manifest has zero data-contract rows: $MANIFEST" >&2
  exit 2
fi

if [ "$blockers" -gt 0 ]; then
  echo "idea-source-drift: ${blockers} blocker(s) across ${rows} baselined data-contract file(s) — idea<->source drift detected." >&2
  echo "  If the source change is intentional AND the idea-layer was updated to match, re-baseline:" >&2
  echo "    bash scripts/framework-verify-idea-source-drift.sh --rebaseline" >&2
  exit 2
fi

echo "idea-source-drift: clean — ${rows} baselined data-contract file(s) match the idea-layer-locked source snapshot."
exit 0
