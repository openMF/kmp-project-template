#!/usr/bin/env bash
#
# verify_libs_resolved.sh — Catalog-resolution gate for build-logic.
#
# Every `libs.<name>` accessor used inside `build-logic/` must resolve to an alias
# declared in `gradle/libs.versions.toml`. When `build-logic/` is auto-synced into
# a consumer fork via the `sync-dirs.yaml` workflow but the matching version-catalog
# entries are NOT (because `gradle/libs.versions.toml` is intentionally project-local
# — it carries the consumer's package namespace, version overrides, etc.), the
# synced build-logic ends up referencing aliases that do not exist in the consumer's
# catalog. Result: silent breakage that only surfaces on the first build attempt
# against the synced state, hours-to-weeks after the bad sync ran.
#
# This script greps every `libs.<name>` reference inside `build-logic/`, normalises
# each to its expected TOML alias key per Gradle's version-catalog accessor
# convention, and verifies the key is declared in `gradle/libs.versions.toml`.
# Unresolved references fail the gate.
#
# Namespace handling:
#   libs.<a>                  → look up `<a>` in [libraries] (dots → hyphens)
#   libs.plugins.<a>          → look up `<a>` in [plugins]
#   libs.versions.<a>         → look up `<a>` in [versions]
#   libs.findLibrary("…")     → SKIPPED (method call, not an alias accessor)
#   libs.findVersion("…")     → SKIPPED
#   libs.findBundle("…")      → SKIPPED
#   libs.findPlugin("…")      → SKIPPED
#   "libs.versions.toml"      → SKIPPED (catalog file name in string literals)
#
# Modes:
#   default          → standalone check, exits 0 = green / 1 = unresolved refs found
#   --list-missing   → list only the missing aliases, exits 0 if none / 1 if any
#
# Usage:
#   bash scripts/verify_libs_resolved.sh
#   bash scripts/verify_libs_resolved.sh --list-missing
#
# Companion: invoked by `.github/workflows/verify-libs-resolved.yml` on every PR
# that touches `build-logic/**` or `gradle/libs.versions.toml`, and by
# `scripts/ci-equivalent-check.sh` for local CI parity.

set -uo pipefail

# Resolve repo root: git top-level is the truth when available (handles CI checkout
# layouts + the case where this script is sync'd into a consumer fork and runs
# against the CONSUMER's catalog, not the template's). Falls back to CWD for
# environments without git (very rare).
if command -v git >/dev/null 2>&1 && REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null)"; then
    :
else
    REPO_ROOT="$(pwd)"
fi
LIBS_TOML="$REPO_ROOT/gradle/libs.versions.toml"
BUILD_LOGIC="$REPO_ROOT/build-logic"

LIST_MISSING_ONLY=false
if [[ "${1:-}" == "--list-missing" ]]; then
    LIST_MISSING_ONLY=true
fi

if [[ ! -f "$LIBS_TOML" ]]; then
    echo "❌ gradle/libs.versions.toml not found at $LIBS_TOML" >&2
    exit 1
fi
if [[ ! -d "$BUILD_LOGIC" ]]; then
    echo "❌ build-logic/ not found at $BUILD_LOGIC" >&2
    exit 1
fi

# Method-call accessor names that are NOT alias references — `libs.findLibrary(...)`
# etc. take a string parameter rather than dispatching on a property path.
METHOD_ACCESSORS_RE='^(findLibrary|findVersion|findBundle|findPlugin)([(.]|$)'

# Section-aware alias-resolution. Returns 0 if the alias resolves in `libs.versions.toml`,
# 1 otherwise. Echoes nothing (verification is by exit code only).
alias_resolves_in_toml() {
    local ref="$1"
    local section="libraries"
    local key

    # Detect explicit namespaces
    case "$ref" in
        plugins.*)
            section="plugins"
            ref="${ref#plugins.}"
            ;;
        versions.*)
            section="versions"
            ref="${ref#versions.}"
            # Whitelist the catalog filename — `libs.versions.toml` is a string in source.
            [[ "$ref" == "toml" ]] && return 0
            ;;
    esac

    key=$(echo "$ref" | sed 's/\./-/g')

    # All three sections use `<key> = ...` form at column 0 — match any of them.
    grep -qE "^${key}[[:space:]]*=" "$LIBS_TOML"
}

# Extract every libs.<ref> usage inside build-logic Kotlin / KTS source.
# Strip the "libs." prefix to get the dotted accessor path.
raw_refs=$(grep -rhoE '\blibs\.[a-zA-Z][a-zA-Z0-9._]*' \
        --include='*.kt' --include='*.kts' \
        "$BUILD_LOGIC" 2>/dev/null \
    | sed -E 's/^libs\.//' \
    | sort -u)

if [[ -z "$raw_refs" ]]; then
    if [[ "$LIST_MISSING_ONLY" != "true" ]]; then
        echo "ℹ️  No libs.* references found in build-logic/ — nothing to verify."
    fi
    exit 0
fi

missing=()
resolved=0
skipped=0
total=0

while IFS= read -r ref; do
    [[ -z "$ref" ]] && continue
    total=$((total + 1))

    # Skip method-call accessors — they take string args, not property paths.
    if [[ "$ref" =~ $METHOD_ACCESSORS_RE ]]; then
        skipped=$((skipped + 1))
        continue
    fi

    if alias_resolves_in_toml "$ref"; then
        resolved=$((resolved + 1))
    else
        # Show what key shape we were looking for, for friendly debugging.
        looked_up_key=$(echo "$ref" | sed -E 's/^(plugins|versions)\.//' | sed 's/\./-/g')
        section_hint=""
        case "$ref" in
            plugins.*)  section_hint=" (in [plugins] section)" ;;
            versions.*) section_hint=" (in [versions] section)" ;;
            *)          section_hint=" (in [libraries] section)" ;;
        esac
        missing+=("libs.${ref}  →  key '${looked_up_key}'${section_hint}")
    fi
done <<< "$raw_refs"

if [[ "$LIST_MISSING_ONLY" == "true" ]]; then
    for m in "${missing[@]}"; do
        echo "$m"
    done
    [[ ${#missing[@]} -eq 0 ]] && exit 0 || exit 1
fi

echo "═══ build-logic libs.* resolution check ═══"
echo "  References found:     $total"
echo "  Resolved in catalog:  $resolved"
echo "  Method calls skipped: $skipped"
echo "  Unresolved:           ${#missing[@]}"
echo

if [[ ${#missing[@]} -gt 0 ]]; then
    echo "❌ The following libs.* references in build-logic/ do NOT resolve to an"
    echo "   alias in gradle/libs.versions.toml:"
    echo
    for m in "${missing[@]}"; do
        echo "   - $m"
    done
    echo
    echo "Why this matters:"
    echo "  Consumer forks pull build-logic/ via sync-dirs.yaml but keep their own"
    echo "  gradle/libs.versions.toml (project-specific package names, version pins)."
    echo "  When this gate fires on template, the corresponding consumer-side fix is"
    echo "  to add the missing alias entries to their libs.versions.toml — same"
    echo "  artifact coordinates, just declared in the consumer catalog."
    echo
    echo "  Fix in template: add the missing alias(es) to gradle/libs.versions.toml"
    echo "  under [versions] / [libraries] / [plugins] as appropriate."
    exit 1
fi

echo "✅ All $resolved libs.* alias references in build-logic/ resolve to declared catalog entries."
exit 0
