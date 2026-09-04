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
#   VA-2 the same, when Spotless has WRAPPED the call so the constant sits on the next line:
#            assertTrue(
#                true,
#                "…",
#            )
#        The v1 scanner matched `assertTrue(` and `true` on ONE line only, so every wrapped
#        occurrence passed silently — a hole found by reading a file the gate had just declared
#        clean. Any line-oriented rule must account for the formatter's own line breaks.
#
# ESCAPE HATCH: a genuine "we reached this line" completion sentinel is legitimate (e.g. proving a
# suspend function returned rather than hung). Mark it explicitly:
#     assertTrue(true) // vacuous-ok: completion sentinel — proves collect() returned
# The marker makes the intent reviewable instead of indistinguishable from a fake contract test.
# For a wrapped call the marker may sit on any line of the call.
#
# Exit 0 = PASS · 1 = FAIL (blocks) · 2 = WARN.
set -uo pipefail

ROOT="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}"
FAILED=0

# `git ls-files` keeps this to tracked sources — never build output or a fork's scratch files.
#
# The scan skips COMMENT lines (a KDoc quoting `assertTrue(true …)` while explaining this very rule
# is not a vacuous test), and honours a `vacuous-ok` marker within the assertion's own lines or the
# three above it, so the justification can be a readable comment rather than a cramped trailing one.
while IFS= read -r f; do
    [ -f "$ROOT/$f" ] || continue
    hits=$(awk '
        { line[NR] = $0 }
        END {
            for (n = 1; n <= NR; n++) {
                t = line[n]
                sub(/^[[:space:]]+/, "", t)
                # skip comment lines — prose about vacuous asserts is not a vacuous assert
                if (t ~ /^(\*|\/\/|\/\*)/) continue

                joined = t
                # VA-2: the call is wrapped — pull in the next non-blank lines so the first
                # argument is visible to the same match the unwrapped form gets.
                if (t ~ /assert(True|False)\([[:space:]]*$/) {
                    k = n + 1; pulled = 0
                    while (k <= NR && pulled < 2) {
                        nxt = line[k]
                        sub(/^[[:space:]]+/, "", nxt)
                        if (nxt != "") { joined = joined nxt; pulled++ }
                        k++
                    }
                }

                if (joined ~ /assertTrue\([[:space:]]*true([^A-Za-z0-9_]|$)/ ||
                    joined ~ /assertFalse\([[:space:]]*false([^A-Za-z0-9_]|$)/) {
                    skip = 0
                    for (i = n - 3; i <= n + 3; i++)
                        if (i > 0 && i <= NR && line[i] ~ /vacuous-ok/) skip = 1
                    if (!skip) printf "%d\t%s\n", n, joined
                }
            }
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
