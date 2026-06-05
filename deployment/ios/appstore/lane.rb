# deployment/ios/appstore/lane.rb
# Imported by fastlane/Fastfile via `import` directive (AC64).
# Extracted from legacy `release` lane (iOS platform block).
# Implements the AC10/AC39 Info.plist write-then-restore pattern via
# AppStoreHelpers.with_plist_backup() — the working tree stays clean after
# the lane runs (success OR failure).
require_relative "../../_shared/lib/appstore_helpers"
require_relative "../../_shared/lib/version_helpers"

platform :ios do
  desc "Upload iOS application to App Store"
  lane :release do |options|
    options          = sanitize_options(options)
    ios_config       = FastlaneConfig::IosConfig::BUILD_CONFIG
    appstore_config  = FastlaneConfig::IosConfig::APPSTORE_CONFIG

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
    version = AppStoreHelpers.bumped_version(options[:version_number] || gradle_version, latest_version)
    UI.important("📱 Final App Store version: #{version}")

    increment_version_number(xcodeproj: ios_config[:project_path], version_number: version)
    increment_build_number(xcodeproj: ios_config[:project_path], build_number: latest_build_number + 1)

    plist_path = ios_config[:plist_path]
    # AC10/AC39 — write-then-restore so the working tree stays clean.
    AppStoreHelpers.with_plist_backup(plist_path) do
      update_plist(
        plist_path: plist_path,
        block: proc do |plist|
          plist["NSContactsUsageDescription"] = "This app does not access your contacts. This message is required for compliance only."
          plist["NSLocationWhenInUseUsageDescription"] = "This app does not access your location. This message is required for compliance only."
          plist["NSBluetoothAlwaysUsageDescription"] = "This app does not use Bluetooth. This message is required for compliance only."
        end,
      )

      build_ios_project(options.merge(provisioning_profile_name: ios_config[:provisioning_profile_appstore]))

      releaseNotes = generateReleaseNote()
      locale = ios_config[:primary_locale]
      release_notes_path = File.join(ios_config[:metadata_path], locale, "release_notes.txt")
      FileUtils.mkdir_p(File.dirname(release_notes_path))
      File.write(release_notes_path, releaseNotes)

      deliver(
        api_key: Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
        copyright: "#{Time.now.year} #{FastlaneConfig::ProjectConfig::ORGANIZATION_NAME}",
        metadata_path: ios_config[:metadata_path],
        skip_metadata: false,
        skip_screenshots: true,
        skip_binary_upload: options[:skip_binary_upload] || false,
        overwrite_screenshots: false,
        app_review_information: appstore_config[:app_review_information],
        submit_for_review: options[:submit_for_review] || appstore_config[:submit_for_review],
        automatic_release: options[:automatic_release] || appstore_config[:automatic_release],
        phased_release: options[:phased_release] || appstore_config[:phased_release],
        skip_app_version_update: options[:skip_app_version_update] || appstore_config[:skip_app_version_update],
        reject_if_possible: appstore_config[:reject_if_possible],
        force: appstore_config[:force],
        precheck_include_in_app_purchases: appstore_config[:precheck_include_in_app_purchases],
        run_precheck_before_submit: appstore_config[:run_precheck_before_submit],
        submission_information: appstore_config[:submission_information],
        app_rating_config_path: ios_config[:app_rating_config_path],
      )

      UI.success("✅ Successfully deployed to App Store!")
    end
  end
end
