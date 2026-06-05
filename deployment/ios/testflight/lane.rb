# deployment/ios/testflight/lane.rb
# Imported by fastlane/Fastfile via `import` directive (AC64).
# Extracted from legacy `beta` lane (iOS platform block).
require_relative "../../_shared/lib/appstore_helpers"
require_relative "../../_shared/lib/version_helpers"

platform :ios do
  desc "Upload beta build to TestFlight"
  lane :beta do |options|
    options = sanitize_options(options)
    ios_config        = FastlaneConfig::IosConfig::BUILD_CONFIG
    testflight_config = FastlaneConfig::IosConfig::TESTFLIGHT_CONFIG

    with_ios_preamble(options)
    setup_ci_if_needed
    load_api_key(options)
    fetch_certificates_with_match(options.merge(match_type: "appstore"))

    gradle_version = get_version_from_gradle(sanitize_for_appstore: true)

    latest_build_number = latest_testflight_build_number(
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

    build_ios_project(options.merge(provisioning_profile_name: ios_config[:provisioning_profile_appstore]))

    releaseNotes = generateReleaseNote()
    locale = ios_config[:primary_locale]
    localized_build_info = {
      "default" => { whats_new: releaseNotes },
      locale    => { whats_new: releaseNotes },
    }

    pilot(
      api_key: Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      beta_app_review_info: testflight_config[:beta_app_review_info],
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
end
