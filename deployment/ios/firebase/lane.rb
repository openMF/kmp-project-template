# deployment/ios/firebase/lane.rb
# Imported by fastlane/Fastfile via `import` directive (AC64).
# Extracted from legacy `deploy_on_firebase` lane (iOS platform block).
require_relative "../../_shared/lib/firebase_helpers"

platform :ios do
  desc "Upload iOS application to Firebase App Distribution"
  lane :deploy_on_firebase do |options|
    options = sanitize_options(options)
    firebase_config = FastlaneConfig.get_firebase_config(:ios)
    ios_config      = FastlaneConfig::IosConfig::BUILD_CONFIG

    # Inputs to Firebase = full semver (pre-release identifiers OK).
    increment_version(serviceCredsFile: firebase_config[:serviceCredsFile])

    build_signed_ios(
      options.merge(
        match_type: ios_config[:match_type],
        provisioning_profile_name: ios_config[:provisioning_profile_name],
      ),
    )

    releaseNotes = generateReleaseNote()

    firebase_app_distribution(
      app: options[:firebase_app_id] || firebase_config[:appId],
      service_credentials_file: FirebaseHelpers.service_credentials_path(firebase_config),
      release_notes: releaseNotes,
      groups: FirebaseHelpers.resolve_groups(options, firebase_config),
    )
  end
end
