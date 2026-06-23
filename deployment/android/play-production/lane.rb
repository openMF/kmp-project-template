# deployment/android/play-production/lane.rb
# Extracted from legacy `promote_to_production` lane in fastlane/Fastfile.
# Promotion-only lane (no build): moves the current beta track release to production.
# Gated by `requires_confirm: true` in config.yaml (AC34).

platform :android do
  desc "Promote to production on Google Play (internal → production for first-time apps; beta → production otherwise)"
  lane :promote_to_production do
    json_key = File.join(DEPLOYMENT_REPO_ROOT, FastlaneConfig::SECRETS_DIR, "android", "play", "service-account.json")
    pkg      = FastlaneConfig::ProjectConfig.android_package_name

    # Honor the caller's production_rollout (ENV ROLLOUT forwarded by the composite). Was
    # hardcoded "1.0" (100%), ignoring the staged fraction the dispatcher passed. A fraction
    # < 1.0 yields a staged (inProgress) production rollout; 1.0 = full release. (2026-06-22 fix)
    rollout_pct = ENV.fetch("ROLLOUT", "1.0").to_s.strip
    rollout_pct = "1.0" if rollout_pct.empty?

    # For first-time apps the beta release may be in draft with no country targeting,
    # making beta→production promotion fail with "Release in track targeting no countries".
    # Promote from internal directly to production in that case.
    begin
      upload_to_play_store(
        track:                  "beta",
        track_promote_to:       "production",
        rollout:                rollout_pct,
        json_key:               json_key,
        package_name:           pkg,
        skip_upload_changelogs: true,
        skip_upload_metadata:   true,
        skip_upload_images:     true,
        skip_upload_screenshots: true,
      )
    rescue => e
      if e.message.include?("targeting no countries") || e.message.include?("draft app")
        UI.important("Beta track unavailable (#{e.message.split(".").first}). Promoting from internal → production.")
        upload_to_play_store(
          track:                  "internal",
          track_promote_to:       "production",
          rollout:                rollout_pct,
          json_key:               json_key,
          package_name:           pkg,
          skip_upload_changelogs: true,
          skip_upload_metadata:   true,
          skip_upload_images:     true,
          skip_upload_screenshots: true,
        )
      else
        raise e
      end
    end
  end

end
