# deployment/android/play-production/lane.rb
# Extracted from legacy `promote_to_production` lane in fastlane/Fastfile.
# Promotion-only lane (no build): moves the current beta track release to production.
# Gated by `requires_confirm: true` in config.yaml (AC34).

platform :android do
  desc "Promote beta track to production on Google Play"
  lane :promote_to_production do
    upload_to_play_store(
      track: "beta",
      track_promote_to: "production",
      skip_upload_changelogs: true,
      skip_upload_metadata: true,
      skip_upload_images: true,
      skip_upload_screenshots: true,
    )
  end
end
