#!/usr/bin/env bash
# gen-kotlin-filelist.sh — regenerate the Kotlin source .xcfilelist that gates the
# "[KMP] Embed and Sign ComposeApp XCFramework" Run Script phase.
#
# WHY THIS EXISTS. A Run Script phase with no declared inputs/outputs is ALWAYS out of date, so
# Xcode ran the Gradle phase on every build — ~8 min even when nothing changed. Declaring only
# OUTPUTS would be worse than useless: Xcode would then skip the phase whenever the staged
# framework exists, including after a Kotlin edit, and silently ship stale code. The inputs must
# therefore be the actual Kotlin sources, enumerated.
#
# LIMITATION, on purpose and in the open: a NEWLY ADDED .kt file is not in the list until this
# script is re-run, so the phase may not re-trigger for an add-only change. Re-run it after adding
# or deleting sources (it is idempotent and takes under a second).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
OUT="$ROOT/cmp-ios/kotlin-sources.xcfilelist"
# Emit $(SRCROOT)-RELATIVE paths, never absolute: an .xcfilelist is committed and inherited by
# every fork, and absolute paths would point at the machine that generated it.
( cd "$ROOT" && find cmp-shared/src core core-base feature \
     -name '*.kt' -not -path '*/build/*' 2>/dev/null | sort \
     | sed 's|^|$(SRCROOT)/../|' ) > "$OUT"
echo "note: [KMP] wrote $(wc -l < "$OUT" | tr -d ' ') Kotlin paths → ${OUT#"$ROOT"/}"
