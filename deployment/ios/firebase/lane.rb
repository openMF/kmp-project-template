# deployment/ios/firebase/lane.rb
# Imported by fastlane/Fastfile via `import` directive (AC64).
# Two Firebase App Distribution lanes mirroring Android prod/demo flavor pattern.
# Config is driven by gradle/fork.properties (local) or ENV vars (CI).
#
# Signing (three passwords):
#   KEYCHAIN_PASSWORD  — unlocks macOS login keychain (local) or creates CI keychain
#   MATCH_PASSWORD     — decrypts git-stored certs in the Match repo
#   CERTIFICATES_PASSWORD — alternative p12 import password (Match uses MATCH_PASSWORD
#                           for both encryption and import by default; set this only
#                           if your Match repo was initialised with a separate cert password)
require_relative "../../_shared/lib/firebase_helpers"

platform :ios do
  # ── flavor: :prod ─────────────────────────────────────────────────────────
  desc "Upload iOS production IPA to Firebase App Distribution"
  lane :deployProdIpaOnFirebase do |options|
    options        = sanitize_options(options)
    firebase_config = FastlaneConfig.get_firebase_config(:ios, :prod)
    ios_config      = FastlaneConfig::IosConfig::BUILD_CONFIG

    # Bump build number from Firebase before building.
    increment_version(
      serviceCredsFile: firebase_config[:serviceCredsFile],
      firebase_app_id:  firebase_config[:appId],
    )

    # SSH config + unlock keychain + ASC API key + Match certificate fetch.
    with_ios_preamble(options)
    setup_ios_keychain(keychain_password: options[:keychain_password] || ENV["KEYCHAIN_PASSWORD"])
    load_api_key(options)
    fetch_certificates_with_match(match_type: "adhoc")

    # The Xcode project uses CODE_SIGN_STYLE=Automatic by default. xcodebuild's
    # archive step reads signing settings directly from the project — it does NOT
    # honour gym's export_method. Switch to Manual + Distribution before archiving
    # so xcodebuild finds the AdHoc profile Match just installed instead of looking
    # for a Development profile and failing.
    update_code_signing_settings(
      use_automatic_signing: false,
      path:                  ios_config[:project_path],
      team_id:               ios_config[:team_id],
      code_sign_identity:    "Apple Distribution",
      targets:               [ios_config[:scheme]],
      bundle_identifier:     ios_config[:app_identifier],
      profile_name:          "match AdHoc #{ios_config[:app_identifier]}",
    )

    build_app(
      scheme:           ios_config[:scheme],
      workspace:        ios_config[:workspace],
      configuration:    "Release",
      output_name:      "iosApp-prod.ipa",
      output_directory: File.join(DEPLOYMENT_REPO_ROOT, "cmp-ios/build"),
      export_method:    "ad-hoc",
      include_bitcode:  false,
    )

    releaseNotes = generateReleaseNote()

    resolved_groups = FirebaseHelpers.resolve_groups(options, firebase_config)
    dist_params = {
      app:                      firebase_config[:appId],
      service_credentials_file: FirebaseHelpers.service_credentials_path(firebase_config),
      release_notes:            releaseNotes,
    }
    dist_params[:groups] = resolved_groups if resolved_groups && !resolved_groups.to_s.strip.empty?
    firebase_app_distribution(dist_params)
  end

  # ── flavor: :demo ─────────────────────────────────────────────────────────
  desc "Upload iOS demo IPA to Firebase App Distribution"
  lane :deployDemoIpaOnFirebase do |options|
    options        = sanitize_options(options)
    firebase_config = FastlaneConfig.get_firebase_config(:ios, :demo)
    ios_config      = FastlaneConfig::IosConfig::BUILD_CONFIG

    increment_version(
      serviceCredsFile: firebase_config[:serviceCredsFile],
      firebase_app_id:  firebase_config[:appId],
    )

    with_ios_preamble(options)
    setup_ios_keychain(keychain_password: options[:keychain_password] || ENV["KEYCHAIN_PASSWORD"])
    load_api_key(options)
    fetch_certificates_with_match(match_type: "adhoc")

    update_code_signing_settings(
      use_automatic_signing: false,
      path:                  ios_config[:project_path],
      team_id:               ios_config[:team_id],
      code_sign_identity:    "Apple Distribution",
      targets:               [ios_config[:scheme]],
      bundle_identifier:     ios_config[:app_identifier],
      profile_name:          "match AdHoc #{ios_config[:app_identifier]}",
    )

    build_app(
      scheme:           ios_config[:scheme],
      workspace:        ios_config[:workspace],
      configuration:    "Release",
      output_name:      "iosApp-demo.ipa",
      output_directory: File.join(DEPLOYMENT_REPO_ROOT, "cmp-ios/build"),
      export_method:    "ad-hoc",
      include_bitcode:  false,
    )

    releaseNotes = generateReleaseNote()

    resolved_groups = FirebaseHelpers.resolve_groups(options, firebase_config)
    dist_params = {
      app:                      firebase_config[:appId],
      service_credentials_file: FirebaseHelpers.service_credentials_path(firebase_config),
      release_notes:            releaseNotes,
    }
    dist_params[:groups] = resolved_groups if resolved_groups && !resolved_groups.to_s.strip.empty?
    firebase_app_distribution(dist_params)
  end

  # ── backward-compat alias ──────────────────────────────────────────────────
  desc "Alias for deployProdIpaOnFirebase (backward compatibility)"
  lane :deploy_on_firebase do |options|
    deployProdIpaOnFirebase(options)
  end
end
