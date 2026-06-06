# deployment/android/play-production/lane.rb
# Extracted from legacy `promote_to_production` lane in fastlane/Fastfile.
# Promotion-only lane (no build): moves the current beta track release to production.
# Gated by `requires_confirm: true` in config.yaml (AC34).

platform :android do
  desc "Promote beta track to production on Google Play"
  lane :promote_to_production do
    upload_to_play_store(
      track:                   "beta",
      track_promote_to:        "production",
      json_key:                File.join(DEPLOYMENT_REPO_ROOT, FastlaneConfig::SECRETS_DIR, "play", "service-account.json"),
      package_name:            FastlaneConfig::ProjectConfig.android_package_name,
      skip_upload_changelogs:  true,
      skip_upload_metadata:    true,
      skip_upload_images:      true,
      skip_upload_screenshots: true,
    )
  end

  desc "Sync Play Store store listing — title, description, screenshots, feature graphic. No APK/AAB uploaded."
  lane :syncListing do |options|
    options = sanitize_options(options)

    metadata_path = FastlaneConfig::AndroidConfig::METADATA_PATH
    json_key      = FastlaneConfig::ProjectConfig::ANDROID[:play_store_json_key]
    pkg           = FastlaneConfig::ProjectConfig.android_package_name

    UI.message("Syncing Play Store listing for #{pkg}")
    UI.message("Metadata path: #{metadata_path}")
    UI.message("Service account: #{json_key}")

    upload_to_play_store(
      package_name:           pkg,
      json_key:               json_key,
      metadata_path:          metadata_path,
      # Metadata-only: no binary upload
      skip_upload_apk:        true,
      skip_upload_aab:        true,
      # Changelogs are version-specific — uploaded alongside each release build
      skip_upload_changelogs: true,
      # Upload screenshots + feature graphic from metadata/images/
      sync_image_upload:      true,
    )

    UI.success("Play Store listing updated successfully!")
  end
end
