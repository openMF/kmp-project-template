#!/usr/bin/env bash
# no-vacuous-assert.sh — a test that cannot fail is worse than no test.
#
# WHY THIS EXISTS
# `assertTrue(true, "MacroIndicatorStore is MEMORY_ONLY: …")` was written specifically to catch
# "someone adds a DAO to MacroIndicatorStore". When exactly that happened, it passed — because the
# assertion is a constant. It reported green, read as coverage in the suite count, and let an
# archetype showcase be deleted silently. An absent test would at least have been visible as absent.
#
# CONTRACT
#   VA-1 no `assertTrue(true …)` / `assertFalse(false …)` in *Test.kt
#
# ESCAPE HATCH: a genuine "we reached this line" completion sentinel is legitimate (e.g. proving a
# suspend function returned rather than hung). Mark it explicitly:
#     assertTrue(true) // vacuous-ok: completion sentinel — proves collect() returned
# The marker makes the intent reviewable instead of indistinguishable from a fake contract test.
#
# Exit 0 = PASS · 1 = FAIL (blocks) · 2 = WARN.
set -uo pipefail

ROOT="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}"
FAILED=0

# `git ls-files` keeps this to tracked sources — never build output or a fork's scratch files.
#
# The scan skips COMMENT lines (a KDoc quoting `assertTrue(true …)` while explaining this very rule
# is not a vacuous test), and honours a `vacuous-ok` marker on the assertion line OR in the three
# lines above it, so the justification can be a readable comment rather than a cramped trailing one.
while IFS= read -r f; do
    [ -f "$ROOT/$f" ] || continue
    hits=$(awk '
        {
            line[NR] = $0
        }
        /assert(True\([[:space:]]*true|False\([[:space:]]*false)/ {
            t = $0
            sub(/^[[:space:]]+/, "", t)
            # skip comment lines — prose about vacuous asserts is not a vacuous assert
            if (t ~ /^(\*|\/\/|\/\*)/) next
            # honour an explicit justification on this line or the 3 above
            for (i = NR; i >= NR - 3 && i > 0; i--) {
                if (line[i] ~ /vacuous-ok/) next
            }
            printf "%d\t%s\n", NR, t
        }
    ' "$ROOT/$f")
    [ -z "$hits" ] && continue
    while IFS=$'\t' read -r ln text; do
        [ -z "$ln" ] && continue
        printf '  ✗ VA-1 %s:%s vacuous assertion — this test cannot fail\n' "$f" "$ln"
        printf '        %s\n' "$text"
        FAILED=1
    done <<< "$hits"
done < <(cd "$ROOT" && git ls-files '*Test.kt' 2>/dev/null)

if [ "$FAILED" = "0" ]; then
    echo "  ✓ no vacuous assertions in tracked *Test.kt"
    exit 0
fi
echo "        → assert the real contract, or mark a deliberate sentinel with '// vacuous-ok: <why>'"
exit 1
