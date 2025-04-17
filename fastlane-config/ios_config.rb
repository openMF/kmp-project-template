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
      output_name: "iosApp.ipa",
      output_directory: "cmp-ios/build",
      match_git_basic_authorization: "someBase64Code",
      match_password: "somePassphrasePassword",
      keychain_name: "ci-signing.keychain",
      keychain_password: "someCustomKeychainPassword",
      match_type: "adhoc",
      app_identifier: "org.mifos.kmp.template",
      export_method: "ad-hoc",
      provisioning_profile_name: "match AdHoc org.mifos.kmp.template",
      git_url: "https://github.com/openMF/ios-provisioning-profile",
      git_branch: "master",
      username: "hekmatullah.amin@icloud.com",
      storage_mode: "git",
    }
  end
end