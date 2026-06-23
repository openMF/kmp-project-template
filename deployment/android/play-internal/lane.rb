# deployment/android/play-internal/lane.rb
# Imported by fastlane/Fastfile via `import` directive (AC64).
# Extracted from legacy `deployInternal` lane. The historical AC32 bug
# (`gradle assemble` produced an APK where Play Store required an AAB) was
# fixed in SP-03 — this lane invokes `bundleProd` to produce the .aab.

platform :android do
  desc "Deploy AAB to Google Play Store internal track"
  lane :deployInternal do |options|
    sh("bash #{Dir.pwd}/../deployment/_shared/scripts/keystore-manager.sh check") if File.exist?("#{Dir.pwd}/../deployment/_shared/scripts/keystore-manager.sh")
    options = sanitize_options(options)
    signing_config = FastlaneConfig.get_android_signing_config(options)
    build_paths = FastlaneConfig::AndroidConfig::BUILD_PATHS

    # --- Track-aware versionCode (Option B, epic multi-platform-release-completion D15, 2026-06-22) ---
    # Query the LIVE Play tracks and set VERSION_CODE_OVERRIDE = global_max + 1 so the upload
    # always clears the live ceiling, regardless of which repo builds. A fork's commit count is
    # below the real app's accumulated live codes, which triggers Play's
    #   "cannot rollout: existing users can't upgrade to the newly added APKs".
    # We query production + beta + internal and take the GLOBAL max (a higher track can carry a
    # higher code than internal). Reuses generateVersion's existing VERSION_CODE_OVERRIDE hook,
    # so generateVersion stays generic. deployInternal is the ONLY lane that builds a fresh AAB;
    # promoteToBeta / promote_to_production reuse the uploaded code and need no change.
    play_json_key = File.join(DEPLOYMENT_REPO_ROOT, FastlaneConfig::SECRETS_DIR, "android", "play", "service-account.json")
    play_pkg      = FastlaneConfig::ProjectConfig.android_package_name
    live_max = %w[production beta internal].flat_map do |track|
      begin
        google_play_track_version_codes(package_name: play_pkg, track: track, json_key: play_json_key)
      rescue => e
        UI.important("⚠️ Play track '#{track}' version-code query failed (#{e.message}); skipping it.")
        []
      end
    end.map(&:to_i).max || 0
    if live_max > 0
      ENV["VERSION_CODE_OVERRIDE"] = (live_max + 1).to_s
      UI.message("📈 Track-aware versionCode: live global max=#{live_max} → override=#{ENV['VERSION_CODE_OVERRIDE']}")
    else
      # New app (empty tracks) or API unreachable — fall back to commit_count × 10 (the legacy
      # headroom scheme from multi-platform-build-and-publish.yaml). Logged loudly, never silent.
      fallback = (sh("git rev-list --count HEAD", log: false).strip.to_i rescue 0) * 10
      ENV["VERSION_CODE_OVERRIDE"] = fallback.to_s if fallback > 0
      UI.important("⚠️ No live Play track codes (new app / API unreachable); fallback versionCode=commit_count×10=#{fallback}.")
    end
    # --- end track-aware versionCode ---

    generateVersion(platform: "playstore")
    releaseNotes = generateReleaseNote()

    # Write release notes into Play Store metadata before upload.
    buildConfigPath = "metadata/android/en-US/changelogs/default.txt"
    FileUtils.mkdir_p(File.dirname(buildConfigPath))
    File.write(buildConfigPath, releaseNotes)

    # AC32 FIX (Play Store requires AAB, not APK) — gradle bundle, not assemble.
    buildAndSignApp(taskName: "bundleProd", buildType: "Release", **signing_config)

    # Full store-listing sync — EXPLICIT (was relying on supply's defaults). Pushes the complete
    # listing AND all media alongside the AAB: title/short/full description, changelogs, the
    # feature graphic, and phone / 7" / 10" screenshots from metadata_path. (2026-06-22)
    upload_to_play_store(
      track: "internal",
      aab: build_paths[:prod_aab_path],
      json_key: File.join(DEPLOYMENT_REPO_ROOT, FastlaneConfig::SECRETS_DIR, "android", "play", "service-account.json"),
      package_name: FastlaneConfig::ProjectConfig.android_package_name,
      metadata_path: File.join(DEPLOYMENT_REPO_ROOT, "deployment/android/metadata"),
      skip_upload_metadata:    false,  # title / short_description / full_description
      skip_upload_changelogs:  false,  # changelogs/<locale>/default.txt
      skip_upload_images:      false,  # featureGraphic / icon / promo graphics
      skip_upload_screenshots: false,  # phone / sevenInch / tenInch screenshots
    )
  end
end
