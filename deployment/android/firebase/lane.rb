# deployment/android/firebase/lane.rb
# Imported by fastlane/Fastfile via `import` directive (AC64).
# Consolidates the two Firebase-distribute Android flavors (prod, demo) that
# previously lived as `deployReleaseApkOnFirebase` + `deployDemoApkOnFirebase`
# in fastlane/Fastfile. Both flavors share helper module + workflow snippet.
require_relative "../../_shared/lib/firebase_helpers"

platform :android do
  # ── flavor: :prod ─────────────────────────────────────────────────────────
  desc "Publish Release Artifacts to Firebase App Distribution"
  lane :deployReleaseApkOnFirebase do |options|
    sh("bash #{Dir.pwd}/../deployment/_shared/scripts/keystore-manager.sh check") if File.exist?("#{Dir.pwd}/../deployment/_shared/scripts/keystore-manager.sh")
    options = sanitize_options(options)
    signing_config = FastlaneConfig.get_android_signing_config(options)
    firebase_config = FastlaneConfig.get_firebase_config(:android, :prod)
    build_paths = FastlaneConfig::AndroidConfig::BUILD_PATHS

    generateVersion(platform: "firebase", **firebase_config)
    releaseNotes = generateReleaseNote()

    buildAndSignApp(taskName: "assembleProd", buildType: "Release", **signing_config)

    resolved_groups = FirebaseHelpers.resolve_groups(options, firebase_config)
    dist_params = {
      app: firebase_config[:appId],
      android_artifact_type: "APK",
      android_artifact_path: build_paths[:prod_apk_path],
      service_credentials_file: FirebaseHelpers.service_credentials_path(firebase_config),
      release_notes: releaseNotes,
    }
    dist_params[:groups] = resolved_groups if resolved_groups && !resolved_groups.to_s.strip.empty?
    firebase_app_distribution(dist_params)
  end

  # ── flavor: :demo ─────────────────────────────────────────────────────────
  desc "Publish Demo Artifacts to Firebase App Distribution"
  lane :deployDemoApkOnFirebase do |options|
    sh("bash #{Dir.pwd}/../deployment/_shared/scripts/keystore-manager.sh check") if File.exist?("#{Dir.pwd}/../deployment/_shared/scripts/keystore-manager.sh")
    options = sanitize_options(options)
    signing_config = FastlaneConfig.get_android_signing_config(options)
    firebase_config = FastlaneConfig.get_firebase_config(:android, :demo)
    build_paths = FastlaneConfig::AndroidConfig::BUILD_PATHS

    generateVersion(platform: "firebase", **firebase_config)
    releaseNotes = generateReleaseNote()

    buildAndSignApp(taskName: "assembleDemo", buildType: "Release", **signing_config)

    resolved_groups = FirebaseHelpers.resolve_groups(options, firebase_config)
    dist_params = {
      app: firebase_config[:appId],
      android_artifact_type: "APK",
      android_artifact_path: build_paths[:demo_apk_path],
      service_credentials_file: FirebaseHelpers.service_credentials_path(firebase_config),
      release_notes: releaseNotes,
    }
    dist_params[:groups] = resolved_groups if resolved_groups && !resolved_groups.to_s.strip.empty?
    firebase_app_distribution(dist_params)
  end
end
