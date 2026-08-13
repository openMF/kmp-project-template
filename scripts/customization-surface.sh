#!/usr/bin/env bash
# customization-surface.sh — reader + validator for customization-surface.yaml
# ─────────────────────────────────────────────────────────────────────────────
# The single consumer-facing API for the fork-ownership contract. Pure bash + awk
# (no yq/python/jq dependency, so it runs on any fork out of the box).
#
# Library use (source it):
#     source scripts/customization-surface.sh
#     owner=$(cs_resolve_owner "cmp-android/src/main/AndroidManifest.xml")   # -> merge
#     strat=$(cs_resolve_strategy "cmp-android/src/main/AndroidManifest.xml") # -> manifest-union
#
# CLI use:
#     scripts/customization-surface.sh resolve <path>   # print owner (+ strategy)
#     scripts/customization-surface.sh report           # owner of every tracked path
#     scripts/customization-surface.sh verify            # CI: fail if any tracked
#                                                        # path only matches the default
#     scripts/customization-surface.sh --verify          # CI: fail if any ownership glob
#                                                        # matches ZERO real paths (dead-glob
#                                                        # / contract-rot guard; E0/T2)
# ─────────────────────────────────────────────────────────────────────────────
# NOTE: shell options are set only on direct execution (bottom), NOT on source —
# sourcing must not leak `set -u`/`pipefail` into a caller like scripts/white-label/sync-dirs.sh.

CS_SELF="${BASH_SOURCE[0]}"
CS_ROOT="$(cd "$(dirname "$CS_SELF")/.." && pwd)"
CS_CONTRACT="${CS_CONTRACT:-$CS_ROOT/customization-surface.yaml}"

# Parse the YAML into "glob<TAB>owner<TAB>strategy<TAB>default" rows, in file order.
# Handles both block form (- glob:\n  owner:) and inline form (- { glob: x, owner: y }).
cs_parse_rules() {
  awk '
    function emit() {
      if (glob != "") printf "%s|%s|%s|%s\n", glob, owner, strat, def
      glob=""; owner=""; strat=""; def="0"
    }
    # inline form:  - { glob: "x", owner: y, strategy: z }
    /^[[:space:]]*-[[:space:]]*\{/ {
      emit()               # flush any pending block-form rule first
      line=$0
      g=line; sub(/.*glob:[[:space:]]*"?/,"",g); sub(/"?[[:space:]]*,.*/,"",g); sub(/"?[[:space:]]*}.*/,"",g)
      o=line; sub(/.*owner:[[:space:]]*/,"",o); sub(/[[:space:]]*,.*/,"",o); sub(/[[:space:]]*}.*/,"",o)
      glob=g; owner=o; strat=""; def="0"
      if (line ~ /default:[[:space:]]*true/) def="1"
      emit(); next
    }
    # block form start — extract the QUOTED value (drops any trailing # comment)
    /^[[:space:]]*-[[:space:]]*glob:/ {
      emit()
      g=$0; sub(/.*glob:[[:space:]]*"/,"",g); sub(/".*/,"",g)
      glob=g; next
    }
    # owner/strategy are single tokens — take the first word (drops trailing comments)
    /^[[:space:]]*owner:/    { o=$0; sub(/.*owner:[[:space:]]*/,"",o);    sub(/[[:space:]].*/,"",o); owner=o; next }
    /^[[:space:]]*strategy:/ { s=$0; sub(/.*strategy:[[:space:]]*/,"",s); sub(/[[:space:]].*/,"",s); strat=s; next }
    /^[[:space:]]*default:[[:space:]]*true/ { def="1"; next }
    END { emit() }
  ' "$CS_CONTRACT"
}

# Convert a contract glob to an anchored ERE.
#   **/x -> (.*/)?x   |   ** -> .*   |   * -> [^/]*   |   . escaped
cs_glob_to_regex() {
  local g="$1"
  local s1=$'\x01' s2=$'\x02'   # sentinels — never occur in a path glob
  g="${g//./\\.}"
  # Placeholder the multi-segment tokens FIRST so the single-`*` pass below can't
  # re-clobber the `*` inside their `.*` expansions (globstar → sentinel → restore).
  g="${g//\*\*\//$s1}"   # **/  (match zero+ segments)
  g="${g//\*\*/$s2}"     # **   (match anything)
  g="${g//\*/[^/]*}"     # *    (match within one segment)
  g="${g//$s1/(.*/)?}"
  g="${g//$s2/.*}"
  printf '^%s$' "$g"
}

# Load + compile the rules ONCE into parallel arrays (parse is expensive; matching
# is hot). Idempotent — safe to call before every resolve.
CS_LOADED=0
cs_load_rules() {
  [ "$CS_LOADED" = 1 ] && return
  CS_RX=(); CS_OWNER=(); CS_STRAT=(); CS_DEF=(); CS_GLOB=()
  local glob owner strat def
  while IFS='|' read -r glob owner strat def; do
    [ -z "$glob" ] && continue
    CS_RX+=("$(cs_glob_to_regex "$glob")")
    CS_OWNER+=("$owner"); CS_STRAT+=("$strat"); CS_DEF+=("$def"); CS_GLOB+=("$glob")
  done < <(cs_parse_rules)
  CS_LOADED=1
}

# ── --verify zero-match-rule guard (pure-white-label-store5-network E0/T2) ────────
# Every NON-default ownership glob MUST match >=1 real path in the tree — a rule that
# resolves to zero paths silently mis-owns whatever the author MEANT to cover (exactly
# how the 4 dead globs `core/store/{economic,banking}/**`, `appStoreModule*.kt`,
# `core/**/src/**/local/**` survived). This turns contract rot into a hard failure.
#
# "Real path" universe = git-tracked files UNION files present on disk (so gitignored-
# but-materialized fork paths — local.properties, gradle/fork.properties, secrets/live/**,
# secrets/.sync-meta.json — count as matches, never false-flagged). Heavy build output
# (build/, .gradle/, .kotlin/, node_modules/, .git/) is pruned.
#
# EXEMPT: `owner: generated` rules describe REGENERABLE artifacts (store og-images /
# screenshots / metadata) that are legitimately absent from a freshly-synced / bare
# template tree — flagging them would be a false positive by design (a fork regenerates
# them from app-profile/). The catch-all default `**` rule is skipped too.
#
# Returns 0 iff every enforced rule matches >=1 path; 1 (listing the offenders) otherwise.
cs_build_path_universe() {
  {
    git -C "$CS_ROOT" ls-files 2>/dev/null
    ( cd "$CS_ROOT" 2>/dev/null && \
      find . \( -path './.git' -o -path '*/build' -o -path '*/.gradle' \
                -o -path '*/.kotlin' -o -path '*/node_modules' \) -prune \
             -o -type f -print 2>/dev/null | sed 's#^\./##' )
  } | sort -u
}

cs_verify_zero_match() {
  cs_load_rules
  local universe rc=0 i n
  universe="$(mktemp)"
  cs_build_path_universe > "$universe"
  if [ ! -s "$universe" ]; then
    rm -f "$universe"
    echo "❌ --verify: could not enumerate any real paths under $CS_ROOT" >&2
    return 1
  fi
  for i in "${!CS_RX[@]}"; do
    [ "${CS_DEF[$i]}" = "1" ] && continue            # catch-all default — matches everything by design
    [ "${CS_OWNER[$i]}" = "generated" ] && continue  # regenerable artifacts, legitimately absent
    n="$(grep -cE "${CS_RX[$i]}" "$universe" 2>/dev/null || true)"
    if [ "${n:-0}" -eq 0 ]; then
      echo "❌ ZERO-MATCH RULE (owner=${CS_OWNER[$i]}, matches no real path): ${CS_GLOB[$i]}" >&2
      rc=1
    fi
  done
  rm -f "$universe"
  if [ "$rc" -eq 0 ]; then
    echo "✅ --verify: every ownership rule matches >=1 real path (generated carve-outs exempt)"
  else
    echo "❌ --verify: one or more ownership rules match ZERO real paths — dead/mis-declared contract rot" >&2
  fi
  return "$rc"
}

# First matching rule → set globals CS_M_OWNER / CS_M_STRAT / CS_M_DEFAULT.
# No subshell / no print (hot path — called once per tracked file in `verify`).
cs_match_g() {
  cs_load_rules
  local path="$1" i
  for i in "${!CS_RX[@]}"; do
    if [[ "$path" =~ ${CS_RX[$i]} ]]; then
      CS_M_OWNER="${CS_OWNER[$i]}"; CS_M_STRAT="${CS_STRAT[$i]}"; CS_M_DEFAULT="${CS_DEF[$i]}"
      return 0
    fi
  done
  CS_M_OWNER="fork"; CS_M_STRAT=""; CS_M_DEFAULT="1"   # fallback = fork (never clobber unknown)
}

cs_resolve_owner()    { cs_match_g "$1"; printf '%s' "$CS_M_OWNER"; }
cs_resolve_strategy() { cs_match_g "$1"; printf '%s' "$CS_M_STRAT"; }
cs_is_default()       { cs_match_g "$1"; [ "$CS_M_DEFAULT" = "1" ]; }

# 3-way merge driver for a `merge`-owned file. This is the general merge strategy
# behind manifest-union / catalog-3way / include-union / kotlin-3way / strings-union:
# git merge-file cleanly unions non-overlapping additions (e.g. a fork's extra
# <uses-permission> lines) and only emits conflict markers on a true overlap.
#
#   cs_merge_3way <ours> <base> <theirs> [<out>]
#     ours   = the fork's current file        base = common ancestor
#     theirs = the upstream/template file      out  = result (default: ours, in place)
#   returns  0 clean · 1 merged WITH conflict markers (needs review) · 2 error
cs_merge_3way() {
  local ours="$1" base="$2" theirs="$3" out="${4:-$1}" rc tmp
  [ -f "$ours" ] && [ -f "$base" ] && [ -f "$theirs" ] || {
    echo "cs_merge_3way: need existing <ours> <base> <theirs>" >&2; return 2; }
  tmp="$(mktemp)"
  # -p prints the merged result (does not mutate inputs); exit = conflict count.
  git merge-file -p "$ours" "$base" "$theirs" > "$tmp"; rc=$?
  if [ "$rc" -ge 128 ]; then rm -f "$tmp"; echo "cs_merge_3way: git merge-file error ($rc)" >&2; return 2; fi
  mv "$tmp" "$out"
  [ "$rc" -eq 0 ] && return 0 || return 1
}

# Semantic AndroidManifest union — the `manifest-union` strategy. A textual 3-way
# spurious-conflicts when fork + template both insert permission lines at the same
# spot, so union by android:name instead: take THEIRS (the template's structurally
# updated manifest) and inject every <uses-permission>/<uses-feature> line present
# in OURS whose android:name theirs lacks. Always clean (returns 0).
#
#   cs_merge_manifest <ours> <theirs> [<out>]
cs_merge_manifest() {
  local ours="$1" theirs="$2" out="${3:-$1}" tmp add
  [ -f "$ours" ] && [ -f "$theirs" ] || { echo "cs_merge_manifest: need <ours> <theirs>" >&2; return 2; }
  local theirs_names; theirs_names="$(grep -oE 'android:name="[^"]*"' "$theirs" 2>/dev/null | sort -u)"
  # Extract each uses-permission/uses-feature ELEMENT (robust to one-per-line AND
  # multiple-per-line manifests); add the ones whose android:name theirs lacks.
  add="$(grep -oE '<uses-(permission|feature)[^>]*/?>' "$ours" 2>/dev/null | while IFS= read -r el; do
        nm="$(printf '%s' "$el" | grep -oE 'android:name="[^"]*"' | head -1)"
        [ -n "$nm" ] || continue
        printf '%s\n' "$theirs_names" | grep -qxF "$nm" || printf '    %s\n' "$el"
      done)"
  if [ -z "$add" ]; then cp "$theirs" "$out"; return 0; fi
  local addf; addf="$(mktemp)"; printf '%s\n' "$add" > "$addf"
  tmp="$(mktemp)"
  # Read the injected lines from a file (awk -v cannot carry newlines).
  awk -v addf="$addf" '
    function dumpadd(   l) { while ((getline l < addf) > 0) print l; close(addf) }
    /<application/ && !ins { dumpadd(); ins=1 }
    /<\/manifest>/ && !ins { dumpadd(); ins=1 }
    { print }
  ' "$theirs" > "$tmp"
  mv "$tmp" "$out"; rm -f "$addf"
  return 0
}

# Strategy dispatcher used by scripts/white-label/sync-dirs.sh for a `merge`-owned file.
#   cs_merge <strategy> <ours> <base> <theirs> [<out>]
cs_merge() {
  local strat="$1" ours="$2" base="$3" theirs="$4" out="${5:-$2}"
  case "$strat" in
    manifest-union) cs_merge_manifest "$ours" "$theirs" "$out" ;;
    *)              cs_merge_3way "$ours" "$base" "$theirs" "$out" ;;
  esac
}

# ── CLI ──
# white-label-template-completion E0/T1 — the closed set of ownership rows the boundary flip depends on.
# Asserts each via cs_resolve_owner (no brittle count literal). Returns 0 iff all hold.
cs_require_flip_preconditions() {
  local bad=0
  _cs_expect() { # <path> <expected-owner>
    local got; got="$(cs_resolve_owner "$1")"
    [ "$got" = "$2" ] || { echo "❌ flip-precondition: $1 owner=$got, expected $2"; bad=1; }
  }
  # og-images are DERIVED store media (glob `deployment/**/og-images/** → generated`): regenerated from
  # app-profile, never hand-edited, so sync IGNORES them. Assert `generated` — this both reflects the
  # reclassification AND proves the generated-class row is present in the contract.
  _cs_expect "deployment/web/og-images/01_home.png"                generated
  _cs_expect "gradle.properties"                                   fork
  _cs_expect "secrets-manifest.yaml"                               fork
  _cs_expect "secrets/live/keystore.jks"                           fork
  _cs_expect "tests/anything.sh"                                   template
  _cs_expect "core/store/AppStoreRegistry.kt"                      fork
  _cs_expect "core/store/economic/ExchangeRatesStore.kt"          template
  _cs_expect "core/store/banking/InterestRateSeriesStore.kt"      template
  [ "$bad" -eq 0 ] && echo "✅ T1 flip preconditions hold (og-images generated · secrets-manifest/gradle.properties/keystore + core/store seam fork · tests/core-store-impl template)"
  return "$bad"
}

cs_main() {
  local cmd="${1:-}"; shift || true
  case "$cmd" in
    --verify|verify-zero-match)
      # E0/T2: zero-match-rule guard — exit 1 if ANY enforced glob matches zero real paths.
      cs_verify_zero_match
      ;;
    resolve)
      local p="${1:?usage: resolve <path>}"
      cs_match_g "$p"
      printf '%s\t%s%s\n' "$p" "$CS_M_OWNER" "${CS_M_STRAT:+  (strategy: $CS_M_STRAT)}"
      ;;
    report)
      local f; declare -A cnt=()
      while IFS= read -r f; do
        cs_match_g "$f"; cnt[$CS_M_OWNER]=$(( ${cnt[$CS_M_OWNER]:-0} + 1 ))
        printf '%s\t%s\n' "$CS_M_OWNER" "$f"
      done < <(git -C "$CS_ROOT" ls-files) | sort
      local k; for k in "${!cnt[@]}"; do printf '# %-9s %d files\n' "$k" "${cnt[$k]}" >&2; done
      ;;
    verify)
      local unclassified=0 total=0 f
      while IFS= read -r f; do
        total=$((total+1))
        cs_match_g "$f"
        if [ "$CS_M_DEFAULT" = "1" ]; then
          echo "UNCLASSIFIED (matches only the default rule): $f"
          unclassified=$((unclassified+1))
        fi
      done < <(git -C "$CS_ROOT" ls-files)
      echo "── customization-surface coverage: $((total-unclassified))/$total classified ──"
      if [ "$unclassified" -gt 0 ]; then
        echo "❌ $unclassified path(s) unclassified — add an explicit rule to customization-surface.yaml"
        return 1
      fi
      # white-label-template-completion E0/T1 flip-precondition assertions (closed-set, not a count literal)
      if ! cs_require_flip_preconditions; then return 1; fi
      echo "✅ every tracked path has an explicit owner + T1 flip preconditions hold"
      ;;
    require-flip-preconditions)
      # E0/T1 (FIX-01-R2-ATOMIC): exit 0 iff every T1 ownership row is present + correct — the atomic
      # self-guard T3's scripts/white-label/sync-dirs.sh flip calls before preserving, so a consumer that pulled the
      # mechanism flip WITHOUT the ownership fix HALTs (no clobber).
      cs_require_flip_preconditions
      ;;
    merge)
      cs_merge "$@"   # <strategy> <ours> <base> <theirs> [<out>]
      ;;
    *)
      echo "usage: customization-surface.sh {resolve <path>|report|verify|--verify|merge <strategy> <ours> <base> <theirs> [out]}" >&2
      return 2
      ;;
  esac
}

# Run CLI only when executed directly (not when sourced as a library).
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
  set -uo pipefail
  cs_main "$@"
fi
