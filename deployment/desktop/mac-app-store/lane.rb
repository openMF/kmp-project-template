# deployment/desktop/mac-app-store/lane.rb
# Lifted from the interim `platform :mac do` block in fastlane/Fastfile
# (introduced by SP-03). After this lift, the legacy interim block is removed
# from fastlane/Fastfile per SP-08 spec.

platform :mac do
  desc "Upload macOS desktop build to TestFlight (Mac App Store track)"
  lane :desktop_testflight do |options|
    options = sanitize_options(options)
    upload_to_testflight(
      pkg:                                pkg_path_for_macos(options),
      username:                           ENV["FASTLANE_APPLE_ID"] || options[:apple_id_email],
      team_id:                            ENV["APPLE_TEAM_ID"],
      skip_waiting_for_build_processing:  true,
      apple_id:                           ENV["APP_STORE_CONNECT_APP_ID"],
    )
  end

  desc "Promote macOS desktop build to Mac App Store production"
  lane :desktop_release do |options|
    options = sanitize_options(options)
    deliver(
      pkg:                pkg_path_for_macos(options),
      username:           ENV["FASTLANE_APPLE_ID"] || options[:apple_id_email],
      team_id:            ENV["APPLE_TEAM_ID"],
      submit_for_review:  options.fetch(:submit_for_review, false),
      automatic_release:  options.fetch(:automatic_release, false),
      force:              true,
      skip_screenshots:   true,
      skip_metadata:      true,
    )
  end

  # Helper: resolve a .pkg / .dmg path from lane options.
  def pkg_path_for_macos(options)
    options[:pkg_path] || options[:dmg_path]
  end
end
