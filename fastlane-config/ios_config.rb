module FastlaneConfig
  module IosConfig
    FIREBASE_CONFIG = {
      firebase_app_id: "1:728434912738:ios:1d81f8e53ca7a6f31a1dbb",
      firebase_service_creds_file: "secrets/firebaseAppDistributionServiceCredentialsFile.json",
      firebase_groups: "mifos-mobile-testers"
    }

    BUILD_CONFIG = {
      project_path: "cmp-ios/iosApp.xcodeproj",
      scheme: "iosApp",
      output_directory: "cmp-ios/build",
      git_basic_authorization: ENV["MATCH_GIT_BASIC_AUTHORIZATION"],
      keychain_name: "ci-signing.keychain",
      keychain_password: ENV["KEYCHAIN_PASSWORD"]
    }
  end
end