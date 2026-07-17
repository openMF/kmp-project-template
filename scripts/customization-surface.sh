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
# ─────────────────────────────────────────────────────────────────────────────
# NOTE: shell options are set only on direct execution (bottom), NOT on source —
# sourcing must not leak `set -u`/`pipefail` into a caller like sync-dirs.sh.

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
  CS_RX=(); CS_OWNER=(); CS_STRAT=(); CS_DEF=()
  local glob owner strat def
  while IFS='|' read -r glob owner strat def; do
    [ -z "$glob" ] && continue
    CS_RX+=("$(cs_glob_to_regex "$glob")")
    CS_OWNER+=("$owner"); CS_STRAT+=("$strat"); CS_DEF+=("$def")
  done < <(cs_parse_rules)
  CS_LOADED=1
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

# ── CLI ──
cs_main() {
  local cmd="${1:-}"; shift || true
  case "$cmd" in
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
      echo "✅ every tracked path has an explicit owner"
      ;;
    *)
      echo "usage: customization-surface.sh {resolve <path>|report|verify}" >&2
      return 2
      ;;
  esac
}

# Run CLI only when executed directly (not when sourced as a library).
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
  set -uo pipefail
  cs_main "$@"
fi
