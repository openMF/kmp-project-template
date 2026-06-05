# deployment/_shared/config.rb
#
# Single source of truth for all deployment configuration.
# Imported by deployment/Fastfile BEFORE all lane.rb imports.
#
# Fork identity source of truth: gradle/libs.versions.toml (6 keys).
# Secrets: read from secrets/ filesystem first, ENV fallback for CI.
#
# Usage:
#   Local/manual  → cp secrets_demo/* secrets/, fill values, run fastlane
#   GitHub Actions → secrets injected as ENV vars by workflow-snippet.yml
#   Framework     → /secrets pull materializes vault to secrets/, run fastlane

require_relative "lib/appstore_helpers"
require_relative "lib/firebase_helpers"
require_relative "lib/version_helpers"

# ── Helpers: read libs.versions.toml + secrets/ ──────────────────────────────

DEPLOYMENT_REPO_ROOT = File.expand_path("../..", __dir__).freeze

def _toml_value(key)
  toml = File.join(DEPLOYMENT_REPO_ROOT, "gradle", "libs.versions.toml")
  File.readlines(toml).each do |line|
    m = line.match(/^\s*#{Regexp.escape(key)}\s*=\s*"([^"]+)"/)
    return m[1] if m
  end
  nil
rescue Errno::ENOENT
  UI.error("libs.versions.toml not found at #{toml}") if defined?(UI)
  nil
end

def _secret_file(rel_path)
  full = File.join(DEPLOYMENT_REPO_ROOT, rel_path)
  File.exist?(full) ? File.read(full).strip : nil
end

# ENV takes precedence (CI mode); falls back to file content (local/vault mode).
def _secret(env_var, file_path = nil)
  ENV[env_var] || (file_path ? _secret_file(file_path) : nil)
end

# ── Fork identity (single read at load time) ──────────────────────────────────

module ForkIdentity
  APP_ID           = _toml_value("appId")          || "org.example.app"
  APP_DISPLAY_NAME = _toml_value("appDisplayName") || "My App"
  BASE_NAMESPACE   = _toml_value("baseNamespace")  || "app"
  DESKTOP_APP_NAME = _toml_value("desktopAppName") || "My App"
  PROJECT_NAME     = _toml_value("projectName")    || "kmp-project-template"
  IOS_TEAM_ID      = _toml_value("iosTeamId")      || ""
end

# ── FastlaneConfig module ─────────────────────────────────────────────────────

module FastlaneConfig
  SECRETS_DIR = "secrets".freeze

  # --------------------------------------------------------------------------
  # ProjectConfig — identity + service-account paths consumed by Appfile
  # --------------------------------------------------------------------------
  module ProjectConfig
    ORGANIZATION_NAME = "Mifos Initiative".freeze

    ANDROID = {
      package_name:        ForkIdentity::APP_ID,
      play_store_json_key: "#{FastlaneConfig::SECRETS_DIR}/play/service-account.json",
    }.freeze

    IOS = {
      app_identifier: ForkIdentity::APP_ID,
      team_id:        ForkIdentity::IOS_TEAM_ID,
    }.freeze

    def self.android_package_name
      ANDROID[:package_name]
    end
  end

  # --------------------------------------------------------------------------
  # IosConfig — build paths, ASC API, Match, TestFlight, App Store settings
  # --------------------------------------------------------------------------
  module IosConfig
    _s = FastlaneConfig::SECRETS_DIR

    BUILD_CONFIG = {
      scheme:                        "iosApp",
      workspace:                     "cmp-ios/iosApp.xcworkspace",
      project_path:                  "cmp-ios/iosApp.xcodeproj",
      app_identifier:                ForkIdentity::APP_ID,
      team_id:                       ForkIdentity::IOS_TEAM_ID,
      # ASC API key — ENV preferred (CI); file fallback (local/vault)
      key_id:                        _secret("APPSTORE_KEY_ID",    "#{_s}/appstore/key_id"),
      issuer_id:                     _secret("APPSTORE_ISSUER_ID", "#{_s}/appstore/issuer_id"),
      key_filepath:                  "#{_s}/appstore/AuthKey.p8",
      # Match certificate repository
      match_git_url:                 _secret("MATCH_GIT_URL"),
      match_git_branch:              "main",
      match_type:                    "adhoc",
      match_ssh_key_path:            "#{_s}/match/match_ci_key",
      match_password:                _secret("MATCH_PASSWORD", "#{_s}/match/.match_password"),
      # Provisioning profiles
      provisioning_profile_adhoc:    "#{ForkIdentity::APP_ID} AdHoc",
      provisioning_profile_appstore: "#{ForkIdentity::APP_ID} AppStore",
      # Metadata paths
      plist_path:                    "cmp-ios/iosApp/iosApp/Info.plist",
      metadata_path:                 "deployment/ios/appstore/metadata",
      app_rating_config_path:        "deployment/ios/appstore/metadata/ratings_config.json",
      primary_locale:                "en-US",
    }.freeze

    TESTFLIGHT_CONFIG = {
      beta_app_review_info: {
        contact_email:         _secret("TESTFLIGHT_CONTACT_EMAIL") || "team@mifos.org",
        contact_first_name:    _secret("TESTFLIGHT_FIRST_NAME")    || "Mifos",
        contact_last_name:     _secret("TESTFLIGHT_LAST_NAME")     || "Team",
        contact_phone:         _secret("TESTFLIGHT_PHONE")         || "+1234567890",
        demo_account_required: false,
      }.freeze,
      beta_app_feedback_email:           _secret("BETA_FEEDBACK_EMAIL") || "team@mifos.org",
      beta_app_description:              "#{ForkIdentity::APP_DISPLAY_NAME} beta build",
      demo_account_required:             false,
      distribute_external:               true,
      notify_external_testers:           true,
      groups:                            (_secret("TESTFLIGHT_GROUPS") || "internal-testers").split(",").map(&:strip),
      skip_submission:                   false,
      skip_waiting_for_build_processing: true,
      submit_beta_review:                true,
      expire_previous_builds:            true,
      reject_build_waiting_for_review:   true,
      wait_processing_interval:          30,
      wait_processing_timeout_duration:  900,
      uses_non_exempt_encryption:        false,
      localized_app_info:                {},
    }.freeze

    APPSTORE_CONFIG = {
      submit_for_review:                  true,
      automatic_release:                  false,
      phased_release:                     true,
      skip_app_version_update:            false,
      reject_if_possible:                 true,
      force:                              true,
      precheck_include_in_app_purchases:  false,
      run_precheck_before_submit:         true,
      submission_information:             { add_id_info_uses_idfa: false }.freeze,
      app_review_information: {
        first_name: _secret("APPSTORE_REVIEW_FIRST_NAME") || "Mifos",
        last_name:  _secret("APPSTORE_REVIEW_LAST_NAME")  || "Team",
        phone:      _secret("APPSTORE_REVIEW_PHONE")      || "+1234567890",
        email:      _secret("APPSTORE_REVIEW_EMAIL")      || "review@mifos.org",
      }.freeze,
    }.freeze
  end

  # --------------------------------------------------------------------------
  # AndroidConfig — build artifact paths
  # --------------------------------------------------------------------------
  module AndroidConfig
    BUILD_PATHS = {
      prod_apk_path: "cmp-android/build/outputs/apk/prod/release/cmp-android-prod-release.apk",
      demo_apk_path: "cmp-android/build/outputs/apk/demo/release/cmp-android-demo-release.apk",
      prod_aab_path: "cmp-android/build/outputs/bundle/prodRelease/cmp-android-prod-release.aab",
    }.freeze
  end

  # --------------------------------------------------------------------------
  # get_firebase_config — returns config hash for firebase_app_distribution
  # --------------------------------------------------------------------------
  def self.get_firebase_config(platform, flavor = :prod)
    _s = SECRETS_DIR
    base = {
      serviceCredsFile: ENV["FIREBASE_SERVICE_ACCOUNT_PATH"] ||
                        "#{_s}/firebase/service-account.json",
      groups: ENV["FIREBASE_GROUPS"] || "internal-testers",
    }
    case platform
    when :ios
      base.merge(
        appId: ENV["FIREBASE_IOS_APP_ID"] ||
               _secret_file("#{_s}/firebase/ios_app_id") || "",
      )
    when :android
      app_id = if flavor == :demo
                 ENV["FIREBASE_ANDROID_DEMO_APP_ID"] ||
                   _secret_file("#{_s}/firebase/android_demo_app_id") || ""
               else
                 ENV["FIREBASE_ANDROID_APP_ID"] ||
                   _secret_file("#{_s}/firebase/android_app_id") || ""
               end
      base.merge(appId: app_id)
    else
      base
    end
  end

  # --------------------------------------------------------------------------
  # get_android_signing_config — returns signing config hash for buildAndSignApp
  # --------------------------------------------------------------------------
  def self.get_android_signing_config(options = {})
    _s = SECRETS_DIR
    props_path = "#{_s}/keystores/release.properties"
    props = {}
    if File.exist?(File.join(DEPLOYMENT_REPO_ROOT, props_path))
      File.readlines(File.join(DEPLOYMENT_REPO_ROOT, props_path)).each do |line|
        k, v = line.strip.split("=", 2)
        props[k&.strip] = v&.strip if k && v
      end
    end

    {
      keystore_path:     options[:keystore_path]     ||
                         "#{_s}/keystores/release.jks",
      keystore_password: options[:keystore_password] ||
                         ENV["KEYSTORE_PASSWORD"]    ||
                         props["storePassword"]      || "",
      key_alias:         options[:key_alias]         ||
                         ENV["KEY_ALIAS"]            ||
                         props["keyAlias"]           || "release",
      key_password:      options[:key_password]      ||
                         ENV["KEY_PASSWORD"]         ||
                         props["keyPassword"]        || "",
    }
  end
end

# ─────────────────────────────────────────────────────────────────────────────
# Global Fastlane helpers — available in all platform lanes after import.
# ─────────────────────────────────────────────────────────────────────────────

# Normalize options hash: ensure symbol keys, return empty hash on nil.
def sanitize_options(options)
  return {} if options.nil?
  options.transform_keys(&:to_sym)
rescue
  options || {}
end

# Run Fastlane's setup_ci action when running on CI.
def setup_ci_if_needed
  setup_ci(force: true) if ENV["CI"]
end

# Load App Store Connect API key into lane context.
def load_api_key(options = {})
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  app_store_connect_api_key(
    key_id:       options[:appstore_key_id]    || cfg[:key_id],
    issuer_id:    options[:appstore_issuer_id] || cfg[:issuer_id],
    key_filepath: options[:key_filepath]       || cfg[:key_filepath],
    duration:     1200,
    in_house:     false,
  )
end

# Setup SSH agent + write match_ci_key to expected path for Match.
def with_ios_preamble(options = {})
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  ssh_key_path = options[:match_ssh_key_path] || cfg[:match_ssh_key_path]
  full_key_path = File.join(DEPLOYMENT_REPO_ROOT, ssh_key_path)

  if File.exist?(full_key_path)
    sh("chmod 600 '#{full_key_path}'") rescue nil
    # Write ~/.ssh/config entry so git can reach the Match repo
    ssh_config = File.expand_path("~/.ssh/config")
    entry = "\nHost github.com\n  IdentityFile #{full_key_path}\n  StrictHostKeyChecking no\n"
    unless File.exist?(ssh_config) && File.read(ssh_config).include?(full_key_path)
      FileUtils.mkdir_p(File.dirname(ssh_config))
      File.open(ssh_config, "a") { |f| f.write(entry) }
      File.chmod(0600, ssh_config) rescue nil
    end
  end
end

# Run Fastlane Match to fetch/refresh certificates and provisioning profiles.
def fetch_certificates_with_match(options = {})
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  match(
    type:           options[:match_type]       || cfg[:match_type],
    app_identifier: options[:app_identifier]   || cfg[:app_identifier],
    git_url:        options[:match_git_url]    || cfg[:match_git_url],
    git_branch:     options[:match_git_branch] || cfg[:match_git_branch],
    readonly:       ENV["CI"] ? true : false,
  )
end

# Get app version from gradle-generated version.txt, optionally sanitized for App Store.
def get_version_from_gradle(sanitize_for_appstore: false)
  sh("./gradlew versionFile 2>/dev/null || true") rescue nil
  version = VersionHelpers.gradle_version
  sanitize_for_appstore ? VersionHelpers.appstore_sanitize(version) : version
end

# Generate release notes from recent git merge commits.
def generateReleaseNote
  changelog_from_git_commits(
    commits_count:          15,
    pretty:                 "- %s",
    merge_commit_filtering: "only_include_merges",
    quiet:                  true,
  )
rescue
  "#{ForkIdentity::APP_DISPLAY_NAME} update"
end

# Build iOS project using build_app (gym). Supports ad-hoc and app-store exports.
def build_ios_project(options = {})
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  build_app(
    scheme:           options[:scheme]    || cfg[:scheme],
    workspace:        options[:workspace] || cfg[:workspace],
    configuration:    "Release",
    output_name:      "iosApp.ipa",
    output_directory: "cmp-ios/build",
    export_method:    options[:export_method] || "app-store",
    include_bitcode:  false,
    include_symbols:  true,
  )
end

# Build signed iOS IPA — wrapper around build_ios_project with export method resolved.
def build_signed_ios(options = {})
  export = options[:match_type] == "adhoc" ? "ad-hoc" : "app-store"
  build_ios_project(options.merge(export_method: export))
end

# Increment iOS build number by fetching latest build from Firebase App Distribution.
def increment_version(serviceCredsFile:)
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  firebase_app_id = ENV["FIREBASE_IOS_APP_ID"] || ""
  if firebase_app_id.empty?
    UI.important("⚠️ FIREBASE_IOS_APP_ID not set — skipping build number increment from Firebase")
    return
  end
  latest = firebase_app_distribution_get_latest_release(
    app:                      firebase_app_id,
    service_credentials_file: serviceCredsFile,
  )
  build_num = (latest&.dig(:buildVersion) || 0).to_i + 1
  increment_build_number(xcodeproj: cfg[:project_path], build_number: build_num)
rescue => e
  UI.important("⚠️ Firebase latest release fetch failed: #{e.message}. Using current build number.")
end

# Compute and export VERSION_NAME / VERSION_CODE for Android builds.
# Must be called before buildAndSignApp so Gradle picks up the injected values.
def generateVersion(platform: "firebase", **config)
  sh("./gradlew versionFile 2>/dev/null || true") rescue nil
  version_name = VersionHelpers.gradle_version
  version_code = sh("git rev-list --count HEAD 2>/dev/null || echo 1").strip.to_i rescue 1

  ENV["VERSION_NAME"] = version_name
  ENV["VERSION_CODE"] = version_code.to_s

  UI.message("📦 Version: #{version_name} (#{version_code})")
  { version_name: version_name, version_code: version_code }
end

# Build and sign an Android APK/AAB via Gradle with signing config injected.
def buildAndSignApp(taskName:, buildType: "Release", **signing_config)
  keystore = signing_config[:keystore_path] || "secrets/keystores/release.jks"
  keystore_abs = File.expand_path(File.join(DEPLOYMENT_REPO_ROOT, keystore))

  gradle(
    task:        taskName.to_s,
    build_type:  buildType,
    project_dir: "cmp-android",
    properties: {
      "android.injected.signing.store.file"     => keystore_abs,
      "android.injected.signing.store.password" => signing_config[:keystore_password] || "",
      "android.injected.signing.key.alias"      => signing_config[:key_alias]         || "release",
      "android.injected.signing.key.password"   => signing_config[:key_password]      || "",
      "VERSION_NAME"                            => ENV["VERSION_NAME"] || "1.0.0",
      "VERSION_CODE"                            => ENV["VERSION_CODE"] || "1",
    },
  )
end
