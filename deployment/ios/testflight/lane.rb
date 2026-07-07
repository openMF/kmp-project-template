# deployment/ios/testflight/lane.rb
# Imported by fastlane/Fastfile via `import` directive (AC64).
# Extracted from legacy `beta` lane (iOS platform block).
#
# Phase 2 of `deploy-gha-product-flavors` epic (D5/D9): TestFlight lanes now
# accept `flavor:` + `build_type:` options and derive the Xcode scheme from
# `VariantResolver.resolve(...).ios_scheme` — the pre-Phase-2 hardcoded
# fallback to `ios_config[:scheme]` ("iosApp") is replaced by the
# convention-derived per-variant scheme (`prodRelease`, `demoStaging`, …).
# All aliases + `uploadTestFlight` / `promoteToExternalBeta` are preserved.
require_relative "../../_shared/lib/appstore_helpers"
require_relative "../../_shared/lib/version_helpers"

platform :ios do
  desc "Upload an already-built IPA to TestFlight (skips build; use after beta build succeeded but pilot upload failed)"
  lane :uploadTestFlight do |options|
    options         = sanitize_options(options)
    ios_config      = FastlaneConfig::IosConfig::BUILD_CONFIG
    testflight_config = FastlaneConfig::IosConfig::TESTFLIGHT_CONFIG

    load_api_key(options)

    ipa_path = options[:ipa] || File.join(DEPLOYMENT_REPO_ROOT, "cmp-ios/build/iosApp.ipa")
    UI.user_error!("IPA not found at #{ipa_path}") unless File.exist?(ipa_path)
    UI.important("📦 Uploading existing IPA: #{ipa_path} (#{File.size(ipa_path) / 1_048_576} MB)")

    releaseNotes = generateReleaseNote()
    locale = ios_config[:primary_locale]
    localized_build_info = {
      "default" => { whats_new: releaseNotes },
      locale    => { whats_new: releaseNotes },
    }

    pilot(
      api_key:                              Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      apple_id:                             ios_config[:apple_id] || "6744892773",
      ipa:                                  ipa_path,
      beta_app_review_info:                 testflight_config[:beta_app_review_info].dup,
      beta_app_feedback_email:              testflight_config[:beta_app_feedback_email],
      beta_app_description:                 testflight_config[:beta_app_description],
      demo_account_required:                testflight_config[:demo_account_required],
      distribute_external:                  testflight_config[:distribute_external],
      notify_external_testers:              testflight_config[:notify_external_testers],
      groups:                               testflight_config[:groups],
      skip_submission:                      testflight_config[:skip_submission],
      skip_waiting_for_build_processing:    testflight_config[:skip_waiting_for_build_processing],
      submit_beta_review:                   testflight_config[:submit_beta_review],
      expire_previous_builds:               testflight_config[:expire_previous_builds],
      reject_build_waiting_for_review:      testflight_config[:reject_build_waiting_for_review],
      wait_processing_interval:             testflight_config[:wait_processing_interval],
      wait_processing_timeout_duration:     testflight_config[:wait_processing_timeout_duration],
      uses_non_exempt_encryption:           testflight_config[:uses_non_exempt_encryption],
      changelog:                            releaseNotes,
      localized_app_info:                   testflight_config[:localized_app_info],
      localized_build_info:                 localized_build_info,
    )

    UI.success("✅ Successfully uploaded to TestFlight!")
  end

  desc "Upload beta build to TestFlight (parameterized on flavor + build_type; scheme from resolver)"
  lane :beta do |options|
    options = sanitize_options(options)
    flavor    = (options[:flavor]     || :prod).to_sym
    build_ty  = (options[:build_type] || :release).to_sym

    # Xcode scheme is derived by CONVENTION from the manifest — no more
    # hardcoded "iosApp" default. The resolved scheme name matches the
    # `.xcscheme` filenames shipped under
    # `cmp-ios/iosApp.xcodeproj/xcshareddata/xcschemes/` (six today:
    # {prod,demo}{Debug,Staging,Release}).
    variant           = VariantResolver.resolve(flavor: flavor.to_s, build_type: build_ty.to_s)
    ios_config        = FastlaneConfig::IosConfig::BUILD_CONFIG
    testflight_config = FastlaneConfig::IosConfig::TESTFLIGHT_CONFIG

    with_ios_preamble(options)
    setup_ci_if_needed
    load_api_key(options)
    fetch_certificates_with_match(options.merge(match_type: "appstore"))

    # Switch project from whatever signing state the previous lane left it in
    # (e.g. Manual+AdHoc from a Firebase deploy) to Manual+AppStore so xcodebuild
    # archives with the AppStore profile Match just installed. Target is the
    # resolved per-variant scheme, not a generic "iosApp".
    update_code_signing_settings(
      use_automatic_signing: false,
      path:                  ios_config[:project_path],
      team_id:               ios_config[:team_id],
      code_sign_identity:    "Apple Distribution",
      targets:               [variant.ios_scheme],
      bundle_identifier:     ios_config[:app_identifier],
      profile_name:          "match AppStore #{ios_config[:app_identifier]}",
    )

    gradle_version = get_version_from_gradle(sanitize_for_appstore: true)

    latest_build_number = latest_tf_build_number_resilient(
      app_identifier: options[:app_identifier] || ios_config[:app_identifier],
      api_key: Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
    )
    latest_version = Actions.lane_context[SharedValues::LATEST_TESTFLIGHT_VERSION]

    version = AppStoreHelpers.bumped_version(
      options[:version_number] || gradle_version,
      latest_version,
    )
    UI.important("📱 Final App Store version: #{version}")

    increment_version_number(xcodeproj: ios_config[:project_path], version_number: version)
    increment_build_number(xcodeproj: ios_config[:project_path], build_number: latest_build_number + 1)

    build_ios_project(
      options.merge(
        scheme:                    variant.ios_scheme,
        configuration:             build_ty.to_s.capitalize,
        provisioning_profile_name: ios_config[:provisioning_profile_appstore],
      ),
    )

    releaseNotes = generateReleaseNote()
    locale = ios_config[:primary_locale]
    localized_build_info = {
      "default" => { whats_new: releaseNotes },
      locale    => { whats_new: releaseNotes },
    }

    pilot(
      api_key: Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      beta_app_feedback_email: testflight_config[:beta_app_feedback_email],
      beta_app_description: testflight_config[:beta_app_description],
      demo_account_required: testflight_config[:demo_account_required],
      distribute_external: testflight_config[:distribute_external],
      notify_external_testers: testflight_config[:notify_external_testers],
      groups: testflight_config[:groups],
      skip_submission: testflight_config[:skip_submission],
      skip_waiting_for_build_processing: testflight_config[:skip_waiting_for_build_processing],
      submit_beta_review: testflight_config[:submit_beta_review],
      expire_previous_builds: testflight_config[:expire_previous_builds],
      reject_build_waiting_for_review: testflight_config[:reject_build_waiting_for_review],
      wait_processing_interval: testflight_config[:wait_processing_interval],
      wait_processing_timeout_duration: testflight_config[:wait_processing_timeout_duration],
      uses_non_exempt_encryption: testflight_config[:uses_non_exempt_encryption],
      changelog: releaseNotes,
      localized_app_info: testflight_config[:localized_app_info],
      localized_build_info: localized_build_info,
    )

    UI.success("✅ Successfully uploaded to TestFlight!")
  end

  desc "Stage 1 → Stage 2 promotion: distribute an already-uploaded TF build to external testers (no rebuild, no re-upload). Triggers Apple's beta review (~24h)."
  lane :promoteToExternalBeta do |options|
    options           = sanitize_options(options)
    ios_config        = FastlaneConfig::IosConfig::BUILD_CONFIG
    testflight_config = FastlaneConfig::IosConfig::TESTFLIGHT_CONFIG

    load_api_key(options)

    # Resolve build: explicit option > latest TF build for this app.
    build_number = options[:build_number]&.to_s || latest_tf_build_number_resilient(
      app_identifier: ios_config[:app_identifier],
      api_key:        Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
    ).to_s

    external_groups = options[:groups] ||
                      testflight_config[:external_groups] ||
                      ["External Beta"]

    UI.important("📦 Promoting TF build #{build_number} → external testers (#{external_groups.join(', ')})")

    # pilot in distribute-only mode — Spaceship updates the build's group
    # assignment + submits for beta review. No IPA upload.
    pilot(
      api_key:                              Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      app_identifier:                       ios_config[:app_identifier],
      build_number:                         build_number,
      distribute_only:                      true,
      distribute_external:                  true,
      notify_external_testers:              true,
      groups:                               external_groups,
      submit_beta_review:                   true,
      reject_build_waiting_for_review:      true,
      changelog:                            options[:changelog] || generateReleaseNote(),
      beta_app_review_info:                 testflight_config[:beta_app_review_info]&.dup,
      uses_non_exempt_encryption:           testflight_config[:uses_non_exempt_encryption],
    )

    UI.success("✅ Build #{build_number} submitted for Apple's beta review — external testers will receive it on approval (~24h).")
  end
end
