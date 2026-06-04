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

    generateVersion(platform: "playstore")
    releaseNotes = generateReleaseNote()

    # Write release notes into Play Store metadata before upload.
    buildConfigPath = "metadata/android/en-US/changelogs/default.txt"
    FileUtils.mkdir_p(File.dirname(buildConfigPath))
    File.write(buildConfigPath, releaseNotes)

    # AC32 FIX (Play Store requires AAB, not APK) — gradle bundle, not assemble.
    buildAndSignApp(taskName: "bundleProd", buildType: "Release", **signing_config)

    upload_to_play_store(
      track: "internal",
      aab: build_paths[:prod_aab_path],
      skip_upload_metadata: true,
      skip_upload_images: true,
      skip_upload_screenshots: true,
    )
  end
end
