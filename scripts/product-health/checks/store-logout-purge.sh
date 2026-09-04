#!/usr/bin/env bash
# store-logout-purge.sh — every read store is purged on logout.
#
# WHY THIS EXISTS
# `StoreCacheManager.clearAll()` is a PRIVACY boundary: on a shared device it is what stops user A's
# cached rows from surfacing in user B's session. Registration is manual — a fork adds
# `single(AppStoreRegistry.Foo) { … }` and must ALSO add `mgr.register(get(AppStoreRegistry.Foo))` to
# the logout list. Nothing enforced the second half, so a new store could ship fully wired, fully
# tested, and silently survive logout.
#
# CONTRACT
#   LP-1 every read store registered as a `single(AppStoreRegistry.X)` also appears in the
#        logout-clear list
#
# EXCLUDED BY TYPE, NOT BY CHOICE: the `*Mutable` qualifiers. In Store5 5.1 `MutableStore` is not a
# `Store` subtype (separate read hierarchy) and `StoreCacheManagerImpl.register` takes `Store<*, *>`,
# so they cannot be registered. Their DATA is still purged — each write store shares its Room table
# with the matching read store, whose `deleteAll` wipes it. What is not purged is the MutableStore's
# own in-memory cache; if Store5 ever unifies the hierarchies, register them and drop this carve-out.
#
# Exit 0 = PASS · 1 = FAIL (blocks) · 2 = WARN.
set -uo pipefail

ROOT="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}"
MODULE="$ROOT/core/store/src/commonMain/kotlin/kpt/core/store/di/StoreModule.kt"

if [ ! -f "$MODULE" ]; then
    echo "  ✗ missing $MODULE"
    exit 1
fi

singles=$(grep -oE 'single\(AppStoreRegistry\.[A-Za-z]+\)' "$MODULE" \
    | sed 's/single(AppStoreRegistry\.//; s/)//' | grep -v 'Mutable$' | sort -u)
cleared=$(grep -oE 'register\(get\(AppStoreRegistry\.[A-Za-z]+' "$MODULE" \
    | sed 's/.*AppStoreRegistry\.//' | sort -u)

missing=$(comm -23 <(printf '%s\n' "$singles") <(printf '%s\n' "$cleared"))

if [ -n "$missing" ]; then
    for m in $missing; do
        printf '  ✗ LP-1 AppStoreRegistry.%s is registered but never cleared on logout\n' "$m"
    done
    echo "        → add: mgr.register(get(AppStoreRegistry.<name>)) to the logout-clear block in StoreModule.kt"
    echo "        → a store missing here keeps one user's cached rows visible to the next user on a shared device"
    exit 1
fi

n=$(printf '%s\n' "$singles" | grep -c .)
printf '  ✓ all %s read stores are purged on logout (*Mutable excluded — not a Store subtype)\n' "$n"
exit 0
