# deployment/android/play-beta/lane.rb
# Extracted from legacy `promoteToBeta` lane in fastlane/Fastfile.
# Promotion-only lane (no build): moves the current internal track release to beta.

platform :android do
  desc "Promote internal track to beta on Google Play"
  lane :promoteToBeta do
    upload_to_play_store(
      track:                        "internal",
      track_promote_to:             "beta",
      # "completed" (was "draft") — a draft promote leaves the Open Testing track PAUSED so
      # testers never receive it. completed = the beta release goes live. (2026-06-22 fix)
      track_promote_release_status: "completed",
      json_key:                     File.join(DEPLOYMENT_REPO_ROOT, FastlaneConfig::SECRETS_DIR, "play", "service-account.json"),
      package_name:                 FastlaneConfig::ProjectConfig.android_package_name,
      skip_upload_changelogs:       true,
      skip_upload_metadata:         true,
      skip_upload_images:           true,
      skip_upload_screenshots:      true,
    )
  end
end
