module FastlaneConfig
  module AndroidConfig
    STORE_CONFIG = {
      default_store_file: "release_keystore.keystore",
      default_store_password: "Wizard@123",
      default_key_alias: "kmp-project-template",
      default_key_password: "Wizard@123"
    }

    FIREBASE_CONFIG = {
      firebase_prod_app_id: "1:728434912738:android:3902eb3363b0938f1a1dbb",
      firebase_demo_app_id: "1:728434912738:android:4e72c77e967965ce1a1dbb",
      firebase_service_creds_file: "secrets/firebaseAppDistributionServiceCredentialsFile.json",
      firebase_groups: "mifos-mobile-apps"
    }

    BUILD_PATHS = {
      prod_apk_path: "cmp-android/build/outputs/apk/prod/release/cmp-android-prod-release.apk",
      demo_apk_path: "cmp-android/build/outputs/apk/demo/release/cmp-android-demo-release.apk",
      prod_aab_path: "cmp-android/build/outputs/bundle/prodRelease/cmp-android-prod-release.aab"
    }
  end
end
