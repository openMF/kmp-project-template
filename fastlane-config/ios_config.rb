module FastlaneConfig
  module IosConfig
    FIREBASE_CONFIG = {
      firebase_app_id: "1:728434912738:ios:1d81f8e53ca7a6f31a1dbb",
      firebase_service_creds_file: "secrets/firebaseAppDistributionServiceCredentialsFile.json",
      firebase_groups: "mifos-mobile-apps"
    }

    BUILD_CONFIG = {
      project_path: "cmp-ios/iosApp.xcodeproj",
      scheme: "iosApp",
      output_name: "iosApp.ipa",
      output_directory: "cmp-ios/build",
      match_git_private_key: "./secrets/match_ci_key",
      match_type: "adhoc",
      app_identifier: "org.mifos.kmp.template",
      provisioning_profile_name: "match AdHoc org.mifos.kmp.template",
      git_url: "git@github.com:openMF/ios-provisioning-profile.git",
      git_branch: "master",
      key_id: "HA469T6757",
      issuer_id: "8er9e361-9603-4c3e-b147-be3b1o816099",
      key_filepath: "./secrets/Auth_Key.p8",
      version_number: "1.0.0",
      metadata_path: "./fastlane/metadata",
      app_rating_config_path: "./fastlane/age_rating.json"
    }
  end
end