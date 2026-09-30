#!/usr/bin/env bash
# scan-bounded: pure bash + find/shasum over one module tree (RULE-CI-001). Never idea-layer.
#
# scripts/docs/module-hash.sh — the ONE anchor for "has this template module's source changed?"
#
# WHY A SHARED HELPER
# ───────────────────
# `template-api-docs-gen.sh` stamps this value into each generated api-docs block and
# `framework-verify-template-api-docs.sh` compares against it. Two copies of the hashing rule would
# drift, and a drifted anchor fails in the worst direction: the gate reports the docs current because
# both sides computed the same wrong thing. Same reasoning as G-RECIPE-CHAIN RC-1 deferring to the
# gate's own dependency authority rather than re-deriving it.
#
# WHY NOT `git rev-parse HEAD:<module>`
# ────────────────────────────────────
# That reads the COMMITTED tree. Measured 2026-09-26: a new public `fun canaryProbeApi()` was appended
# to core-base/store's StoreFactory.kt and the gate reported PASS — because HEAD had not moved. The
# working tree is precisely the case that matters here: the PreToolUse hook fires when an agent EDITS
# template source, long before anything is committed, so an anchor blind to uncommitted work cannot
# enforce "docs update with the template".
#
# So: hash the CONTENT ON DISK of the module's Kotlin sources. `build/` is excluded — it holds
# generated output whose churn is not an API change, and including it would make the anchor move on
# every compile, training everyone to ignore the gate.
#
# Usage: scripts/docs/module-hash.sh <layer>/<module>          # e.g. core-base/store
# Env:   TEMPLATE_PATH  override the template root (canaries)
# Exit:  0 + 40-hex digest on stdout · 2 usage / module missing · 3 no Kotlin sources matched
set -uo pipefail

# Byte-deterministic text processing. Generated output must be a pure function of the tree, and the
# locale silently breaks that in two ways: `sort` collates differently (a UTF-8 locale folds case, so
# `di/SecurityModule.kt` sorts before `FailedAttemptTracker.kt` while C order puts it after), and `.`
# in a length-bounded regex counts CHARACTERS under UTF-8 but BYTES under C — so a 241-character KDoc
# summary containing an em dash (243 bytes) kept its second sentence on a Mac and lost it on the
# Linux CI runner. Pinning to C fixes the comparison basis everywhere; no script here runs Python, so
# this cannot force a Python stdout to ASCII.
export LC_ALL=C

# Repo-root resolved from this script's own location: this file SHIPS IN THE TEMPLATE, so the tree it
# measures is the repository it lives in. TEMPLATE_PATH still overrides for canaries and for the
# framework, which drives this same script against a checkout elsewhere.
TMPL="${TEMPLATE_PATH:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"

LM="${1:-}"
[ -n "$LM" ] || { echo "usage: scripts/docs/module-hash.sh <layer>/<module>" >&2; exit 2; }
[ -d "$TMPL/$LM" ] || { echo "module-hash: no such module $LM under $TMPL" >&2; exit 2; }

# Sorted for determinism; `-print0`/`-0` so a path with a space cannot split a filename and silently
# change the digest. Hash the per-file digests rather than concatenated bytes so a file RENAME moves
# the anchor too (a moved public API is an API change).
DIGEST="$(find "$TMPL/$LM" -type f -name '*.kt' -not -path '*/build/*' -print0 2>/dev/null \
  | LC_ALL=C sort -z \
  | xargs -0 shasum -a 256 2>/dev/null \
  | sed "s|$TMPL/||" \
  | shasum -a 256 | cut -c1-40)"

case "$DIGEST" in
  ""|*[!0-9a-f]*) echo "module-hash: no Kotlin sources under $LM (or hashing failed)" >&2; exit 3 ;;
esac
printf '%s\n' "$DIGEST"
