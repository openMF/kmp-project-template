#!/usr/bin/env bash
# checks/store-listing.sh — DIRECTIONAL, placeholder-aware store-copy guard.
#
# fork-identity.sh covers the SIGNING/org fields (Apple rejects a wrong team). This is the softer
# twin: the store *content* (title, subtitle, description, promo, copyright) that ships to the App
# Store / Play listing. It loads the single-source placeholder vocabulary
# (core/registries/WHITE_LABEL_PLACEHOLDERS.yaml, resolved by lib.sh) — replacing the old hardcoded
# TEMPLATE_MARKER regex and the blanket TEMPLATE_SELF_BUILD=1 skip (pure-white-label-100 WS6).
#
#   template (TEMPLATE_SELF_BUILD=1) → every store-copy key MUST match a placeholder marker; an
#                                      authored value is a real-brand leak → FAIL (exit 1).
#   fork     (TEMPLATE_SELF_BUILD unset) → NO key may still match a placeholder marker; WARN (exit 2),
#                                      since prose is human-authored and must not ship-block a build.
#
# exit 0 PASS / 1 FAIL (template leak) / 2 WARN (fork unfilled).
set -uo pipefail
# shellcheck source=scripts/product-health/lib.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"
: "${FORK_PROPERTIES:?store-listing: FORK_PROPERTIES not set (run via product-health.sh)}"

is_template="${TEMPLATE_SELF_BUILD:-0}"
# Union placeholder regex across the three categories that describe "template store copy".
MARKER_RE="$(
  {
    wl_placeholders_load app_display_name
    wl_placeholders_load org_name
    wl_placeholders_load store_copy_marker
  } | paste -sd'|' -
)"
# The Mifos REFERENCE store copy the template ships (example_identity.store_copy_reference).
REF_RE="$(wl_example_load store_copy_reference | paste -sd'|' -)"
KEYS=(store.title store.subtitle store.description store.promotional.text app.description store.copyright)

leaked=(); reference=(); authored=()
for key in "${KEYS[@]}"; do
  val="$(fp_get "$key")"
  # printf (NOT echo): store copy carries literal '\n' paragraph breaks — echo would interpret them
  # into real newlines, and the empty-value marker '^$' would then false-match a blank paragraph line.
  if [ -n "$MARKER_RE" ] && printf '%s' "$val" | grep -qiE "$MARKER_RE"; then
    leaked+=("$key")
  elif [ -n "$REF_RE" ] && printf '%s' "$val" | grep -qiE "$REF_RE"; then
    reference+=("$key")
  else
    authored+=("$key")
  fi
done

if [ "$is_template" = "1" ]; then
  # template: the store copy IS the committed Mifos "Money Toolkit" example (authored prose — some keys
  # carry the brand, others are brand-free descriptive text like the subtitle). It must be COMPLETE:
  # the only failure is an UNFILLED placeholder marker. Authored example copy is the intended state.
  if [ "${#leaked[@]}" -ne 0 ]; then
    echo "❌ store copy on the TEMPLATE still carries UNFILLED placeholder marker(s) — the Mifos example listing must be complete (WHITE_LABEL_PLACEHOLDERS.yaml#store_copy_marker):"
    for k in "${leaked[@]}"; do echo "     · $k"; done
    exit 1
  fi
  echo "template store copy is the authored Mifos reference listing (no unfilled placeholders — directional PASS)"
  exit 0
fi

# fork: NO key may still be a placeholder marker OR the Mifos reference copy; WARN (exit 2), since prose is human-authored.
if [ "${#leaked[@]}" -ne 0 ] || [ "${#reference[@]}" -ne 0 ]; then
  echo "⚠️  store copy still carries template placeholder(s) or the Mifos reference copy — author for your app in gradle/fork.properties (WHITE_LABEL_PLACEHOLDERS.yaml):"
  for k in "${leaked[@]}" "${reference[@]}"; do echo "     · $k"; done
  echo "   (non-blocking: the listing ships to App Store / Play — write your own before you release.)"
  exit 2
fi
echo "store listing is authored for this fork"
exit 0
