#!/usr/bin/env bash
# Canary for NAP-15 (network-access-points.sh) — RED must FAIL on NAP-15, GREEN must PASS.
#
# RED is the pre-fix generation: app-profile declares `MY_CLIENT_KEY` with `secret_alias:` and
# `required_in_release: true`, but secrets/fork-layout.yaml has no row for it, the buildkonfig field reads
# env / local.properties only, and nothing stops a release build compiling it as "". GREEN is what
# syncForkConfig now generates: the fork-secrets row in the FORK-owned secrets/fork-layout.yaml
# (never the template-owned LAYOUT.yaml, which a sync replaces), the readSecretValue() read, the check.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
CHECK="$HERE/../../checks/network-access-points.sh"
rc=0
run_leg_out() {
  local leg="$1"
  HEALTH_ROOT="$HERE/$leg" \
  NAP_YAML="$HERE/$leg/app.yaml" \
  NAP_NET_DIR="$HERE/$leg/core/network/src/commonMain/kotlin/kpt/core/network" \
  NAP_BUILD_FILE="$HERE/$leg/core/network/build.gradle.kts" \
  NAP_LAYOUT="$HERE/$leg/secrets/LAYOUT.yaml" \
  NAP_FORK_LAYOUT="$HERE/$leg/secrets/fork-layout.yaml" \
    bash "$CHECK" 2>&1
}
echo "── RED (expect FAIL on NAP-15) ──"
red_out="$(run_leg_out red)"
if echo "$red_out" | grep -q '❌ NAP-15'; then
  echo "  ✓ RED failed on NAP-15 ($(echo "$red_out" | grep -c '❌ NAP-15') finding(s))"
else
  echo "  ✗ RED did not fail on NAP-15 — reported: $(echo "$red_out" | grep -oE '❌ NAP-[0-9]+' | sort -u | tr '\n' ' ')"; rc=1
fi
echo "── RED-TEMPLATE-OWNED (the generated row in LAYOUT.yaml, expect FAIL on NAP-15) ──"
misplaced="$(HEALTH_ROOT="$HERE/green" NAP_YAML="$HERE/green/app.yaml" \
  NAP_NET_DIR="$HERE/green/core/network/src/commonMain/kotlin/kpt/core/network" \
  NAP_BUILD_FILE="$HERE/green/core/network/build.gradle.kts" \
  NAP_LAYOUT="$HERE/green/secrets/fork-layout.yaml" NAP_FORK_LAYOUT=/dev/null bash "$CHECK" 2>&1)"
if echo "$misplaced" | grep -q '❌ NAP-15 secrets/LAYOUT.yaml carries'; then
  echo "  ✓ a fork row in the template-owned LAYOUT.yaml failed NAP-15"
else
  echo "  ✗ a fork row in LAYOUT.yaml was not flagged"; rc=1
fi
echo "── GREEN (expect PASS) ──"
if green_out="$(run_leg_out green)"; then echo "  ✓ GREEN passed"; else echo "  ✗ GREEN failed:"; echo "$green_out" | sed 's/^/    /'; rc=1; fi
echo ""
[ $rc = 0 ] && echo "CANARY: ✅ PASS" || echo "CANARY: ❌ FAIL"
exit $rc
