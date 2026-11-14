post_sync_fork_steps() {
    if [ -f app-profile/package-ledger.yaml ] && grep -q 'status: removed' app-profile/package-ledger.yaml \
       && [ -f scripts/remove-demo.sh ]; then
        print_step "Re-applying the fork's demo strip (app-profile/package-ledger.yaml records it)"
        bash scripts/remove-demo.sh --apply --no-format --no-regen >/dev/null \
            || handle_error "remove-demo.sh failed after the sync — the tree still carries template demo code"
        # The strip cuts demo blocks but leaves the imports they used; --no-format skips the slow Gradle
        # spotlessApply, so restore the formatting here with the fast conform tool. Its rc also carries
        # Detekt findings, which are the hooks' and CI's to report, not this step's.
        local stripped; stripped="$(git diff --name-only -- '*.kt' | tr '\n' ' ')"
        [ -n "$stripped" ] && [ -f scripts/ci/kotlin-conform.sh ] \
            && { bash scripts/ci/kotlin-conform.sh $stripped >/dev/null 2>&1 || true; }
    fi
    if [ -x ./gradlew ]; then
        print_step "Regenerating app-profile projections (./gradlew syncForkConfig)"
        ./gradlew syncForkConfig --console=plain -q >/dev/null \
            || handle_error "./gradlew syncForkConfig failed after the sync — generated files are the template's, not the fork's"
        # A merged catalog moves dependency versions; the dependency-guard baseline must move with it
        # or the fork's Dependency Guard check fails on the sync's own PR.
        local catalog_rc; git diff --quiet HEAD -- gradle/libs.versions.toml 2>/dev/null; catalog_rc=$?
        if [ "$catalog_rc" = 1 ]; then   # 1 = changed; anything else (no repo, no HEAD) is not a change
            print_step "Re-baselining dependency-guard (the catalog changed)"
            ./gradlew dependencyGuardBaseline --console=plain -q >/dev/null \
                || handle_error "./gradlew dependencyGuardBaseline failed after the sync"
        fi
    fi
    # docs/ is generated from the fork's own source, and the sync just changed that source: leave it
    # current, or the fork's "Docs are current with source" check goes red on the sync's own PR.
    if [ -f scripts/docs/refresh.sh ]; then
        print_step "Regenerating docs/ from the synced source (scripts/docs/refresh.sh)"
        bash scripts/docs/refresh.sh >/dev/null 2>&1 \
            || handle_error "scripts/docs/refresh.sh failed after the sync — docs/ no longer matches the source"
    fi
}
