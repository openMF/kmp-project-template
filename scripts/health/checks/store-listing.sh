#!/usr/bin/env bash
# checks/store-listing.sh — a fork's app-store listing must be its OWN words, not the template's.
#
# fork-identity.sh covers the SIGNING/org fields (Apple rejects a wrong team). This is the softer
# twin: the store *content* (description, title, promo, app.description) that ships to the App Store /
# Play listing. A fork that never authored these ships the template's Mifos / Money-Toolkit copy —
# not a build failure, but a real reviewer-facing leak (exactly the awaaz-flow case). WARN, not FAIL:
# it needs human authoring, so it must not block a build — but it must be visible on every health run.
# exit 0 PASS / 2 WARN. Self-skips (PASS) on the upstream template (TEMPLATE_SELF_BUILD=1).
set -uo pipefail
# shellcheck source=scripts/health/lib.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib.sh"

if [ "${TEMPLATE_SELF_BUILD:-}" = "1" ]; then
  echo "TEMPLATE_SELF_BUILD=1 — this IS the upstream template; store copy N/A"; exit 0
fi
: "${FORK_PROPERTIES:?store-listing: FORK_PROPERTIES not set (run via project-health.sh)}"

# Store-copy keys and the template markers that mean "still un-authored".
TEMPLATE_MARKER='Mifos|Money-Toolkit|Money Toolkit|Your Organization|^$'
KEYS=(store.title store.subtitle store.description store.promotional.text app.description store.copyright)

leaked=()
for key in "${KEYS[@]}"; do
  val="$(fp_get "$key")"
  if echo "$val" | grep -qiE "$TEMPLATE_MARKER"; then
    leaked+=("$key")
  fi
done

[ "${#leaked[@]}" = 0 ] && { echo "store listing is authored for this fork"; exit 0; }
echo "⚠️  store copy still carries template/empty text — author for your app in gradle/fork.properties:"
for k in "${leaked[@]}"; do echo "     · $k"; done
echo "   (non-blocking: the listing ships to App Store / Play — write your own before you release.)"
exit 2
