#!/usr/bin/env bash
# scripts/health/lib.sh — shared helpers for the project-health harness.
#
# gradle/fork.properties is the SINGLE SOURCE OF TRUTH for project-level data (identity,
# signing, testers, firebase, store listing, legal). EVERY health check reads it through
# fp_get() so there is exactly one parser and one SoT — a check never re-implements the
# read. Sourced by project-health.sh and each checks/*.sh; never executed directly.

# health_resolve_sot <repo_root> — echo the path to the project SoT, or return 1.
# Honors a caller-provided $FORK_PROPERTIES (lets a test point at a fixture).
health_resolve_sot() {
  local root="$1"
  if [ -n "${FORK_PROPERTIES:-}" ] && [ -f "$FORK_PROPERTIES" ]; then echo "$FORK_PROPERTIES"; return 0; fi
  if [ -f "$root/gradle/fork.properties" ]; then echo "$root/gradle/fork.properties"; return 0; fi
  if [ -f "$root/gradle/fork.properties.template" ]; then echo "$root/gradle/fork.properties.template"; return 0; fi
  return 1
}

# fp_get <key> — read one key from $FORK_PROPERTIES, trimming inline `# comment` + whitespace.
fp_get() {
  [ -f "${FORK_PROPERTIES:-}" ] || return 1
  grep -E "^$1=" "$FORK_PROPERTIES" 2>/dev/null | head -1 | cut -d= -f2- | sed 's/[[:space:]]*#.*$//; s/[[:space:]]*$//'
}

# Colors — only when stdout is a tty.
if [ -t 1 ]; then
  C_RED=$'\033[0;31m'; C_GRN=$'\033[0;32m'; C_YEL=$'\033[0;33m'; C_DIM=$'\033[2m'; C_BLD=$'\033[1m'; C_RST=$'\033[0m'
else
  C_RED=; C_GRN=; C_YEL=; C_DIM=; C_BLD=; C_RST=
fi

# Guard against direct execution — this is a library.
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
  echo "lib.sh is a library — source it, don't run it." >&2; exit 64
fi
