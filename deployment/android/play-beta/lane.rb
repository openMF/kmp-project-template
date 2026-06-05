# deployment/android/play-beta/lane.rb
# Extracted from legacy `promoteToBeta` lane in fastlane/Fastfile.
# Promotion-only lane (no build): moves the current internal track release to beta.

platform :android do
  desc "Promote internal track to beta on Google Play"
  lane :promoteToBeta do
    upload_to_play_store(
      track: "internal",
      track_promote_to: "beta",
      skip_upload_changelogs: true,
      skip_upload_metadata: true,
      skip_upload_images: true,
      skip_upload_screenshots: true,
    )
  end
end
