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

# Read a key from gradle/fork.properties (local, gitignored).
# Returns nil when the file is absent (CI uses ENV vars directly).
def _fork_prop(key)
  props = File.join(DEPLOYMENT_REPO_ROOT, "gradle", "fork.properties")
  return nil unless File.exist?(props)
  File.readlines(props).each do |line|
    next if line.strip.start_with?("#") || !line.include?("=")
    k, v = line.strip.split("=", 2)
    return v&.strip if k&.strip == key
  end
  nil
end

# ── Fork identity (single read at load time) ──────────────────────────────────
# Uses module_function so _toml_value is callable with self=ForkIdentity
# (plain `def` at eval-binding scope is not accessible inside module bodies).

module ForkIdentity
  module_function

  def _read_toml(key)
    toml = File.join(DEPLOYMENT_REPO_ROOT, "gradle", "libs.versions.toml")
    File.readlines(toml).each do |line|
      m = line.match(/^\s*#{Regexp.escape(key)}\s*=\s*"([^"]+)"/)
      return m[1] if m
    end
    nil
  rescue Errno::ENOENT
    nil
  end

  public

  APP_ID           = _read_toml("appId")          || "org.example.app"
  APP_DISPLAY_NAME = _read_toml("appDisplayName") || "My App"
  BASE_NAMESPACE   = _read_toml("baseNamespace")  || "app"
  DESKTOP_APP_NAME = _read_toml("desktopAppName") || "My App"
  PROJECT_NAME     = _read_toml("projectName")    || "kmp-project-template"
  IOS_TEAM_ID      = _read_toml("iosTeamId")      || ""
end

# ── FastlaneConfig module ─────────────────────────────────────────────────────

module FastlaneConfig
  SECRETS_DIR = "secrets".freeze
  SECRETS_DIR_ABS = File.join(DEPLOYMENT_REPO_ROOT, "secrets").freeze

  # ── Module-level helpers (module_function so callable from nested modules) ──
  # Top-level `def _secret/_secret_file` is accessible only in FastFile's eval
  # binding scope (i.e. inside lane bodies), NOT from nested module bodies at
  # parse time. These module_function copies fix that gap.
  module_function

  def _secret(env_var, file_path = nil)
    ENV[env_var] || (file_path ? _secret_file(file_path) : nil)
  end

  def _secret_file(rel_path)
    full = File.join(DEPLOYMENT_REPO_ROOT, rel_path)
    File.exist?(full) ? File.read(full).strip : nil
  end

  def _fork_prop(key)
    props = File.join(DEPLOYMENT_REPO_ROOT, "gradle", "fork.properties")
    return nil unless File.exist?(props)
    File.readlines(props).each do |line|
      next if line.strip.start_with?("#") || !line.include?("=")
      k, v = line.strip.split("=", 2)
      return v&.strip if k&.strip == key
    end
    nil
  end

  public

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
    _c = FastlaneConfig  # alias for brevity — calls FastlaneConfig._secret(...)

    BUILD_CONFIG = {
      scheme:                        "iosApp",
      workspace:                     File.join(DEPLOYMENT_REPO_ROOT, "cmp-ios/iosApp.xcworkspace"),
      project_path:                  File.join(DEPLOYMENT_REPO_ROOT, "cmp-ios/iosApp.xcodeproj"),
      app_identifier:                ForkIdentity::APP_ID,
      team_id:                       ForkIdentity::IOS_TEAM_ID,
      # ASC API key — ENV preferred (CI); file fallback (local/vault)
      key_id:                        _c._secret("APPSTORE_KEY_ID",    "#{_s}/appstore/key_id"),
      issuer_id:                     _c._secret("APPSTORE_ISSUER_ID", "#{_s}/appstore/issuer_id"),
      key_filepath:                  File.join(DEPLOYMENT_REPO_ROOT, "#{_s}/appstore/AuthKey.p8"),
      # Match certificate repository — ENV (CI) → secrets/ file → fork.properties → default
      match_git_url:    _c._secret("MATCH_GIT_URL",    "#{_s}/match/git_url")    || _c._fork_prop("apple.match.git.url"),
      match_git_branch: _c._secret("MATCH_GIT_BRANCH", "#{_s}/match/git_branch") || _c._fork_prop("apple.match.git.branch") || "master",
      match_type:                    "adhoc",
      match_ssh_key_path:            "#{_s}/match/match_ci_key",
      match_password:                _c._secret("MATCH_PASSWORD",          "#{_s}/match/.match_password"),
      certificates_password:         _c._secret("CERTIFICATES_PASSWORD",   "#{_s}/match/certificates_password"),
      keychain_password:             _c._secret("KEYCHAIN_PASSWORD",       "#{_s}/match/keychain_password"),
      # Provisioning profiles
      provisioning_profile_adhoc:    "#{ForkIdentity::APP_ID} AdHoc",
      provisioning_profile_appstore: "#{ForkIdentity::APP_ID} AppStore",
      # Metadata paths
      plist_path:                    File.join(DEPLOYMENT_REPO_ROOT, "cmp-ios/iosApp/Info.plist"),
      metadata_path:                 File.join(DEPLOYMENT_REPO_ROOT, "deployment/ios/appstore/metadata"),
      screenshots_path:              File.join(DEPLOYMENT_REPO_ROOT, "deployment/ios/appstore/metadata/screenshots"),
      app_rating_config_path:        File.join(DEPLOYMENT_REPO_ROOT, "deployment/ios/appstore/metadata/app_store_rating_config.json"),
      primary_locale:                "en-US",
    }.freeze

    TESTFLIGHT_CONFIG = {
      beta_app_review_info: {
        contact_email:         _c._secret("TESTFLIGHT_CONTACT_EMAIL") || "team@mifos.org",
        contact_first_name:    _c._secret("TESTFLIGHT_FIRST_NAME")    || "Mifos",
        contact_last_name:     _c._secret("TESTFLIGHT_LAST_NAME")     || "Team",
        contact_phone:         _c._secret("TESTFLIGHT_PHONE")         || "+1234567890",
        demo_account_required: false,
      }.freeze,
      beta_app_feedback_email:           _c._secret("BETA_FEEDBACK_EMAIL") || "team@mifos.org",
      beta_app_description:              "#{ForkIdentity::APP_DISPLAY_NAME} beta build",
      demo_account_required:             false,
      distribute_external:               true,
      notify_external_testers:           true,
      groups:                            (_c._secret("TESTFLIGHT_GROUPS") || "internal-testers").split(",").map(&:strip),
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
      automatic_release:                  true,
      phased_release:                     false,
      skip_app_version_update:            false,
      reject_if_possible:                 true,
      force:                              true,
      precheck_include_in_app_purchases:  false,
      run_precheck_before_submit:         true,
      submission_information:             { add_id_info_uses_idfa: false }.freeze,
      app_review_information: {
        first_name: _c._secret("APPSTORE_REVIEW_FIRST_NAME") || "Mifos",
        last_name:  _c._secret("APPSTORE_REVIEW_LAST_NAME")  || "Team",
        phone:      _c._secret("APPSTORE_REVIEW_PHONE")      || "+1234567890",
        email:      _c._secret("APPSTORE_REVIEW_EMAIL")      || "review@mifos.org",
      }.freeze,
    }.freeze
  end

  # --------------------------------------------------------------------------
  # AndroidConfig — build artifact paths + Play Store metadata path
  # --------------------------------------------------------------------------
  module AndroidConfig
    METADATA_PATH = "deployment/android/metadata".freeze

    BUILD_PATHS = {
      prod_apk_path: File.join(DEPLOYMENT_REPO_ROOT, "cmp-android/build/outputs/apk/prod/release/cmp-android-prod-release.apk"),
      demo_apk_path: File.join(DEPLOYMENT_REPO_ROOT, "cmp-android/build/outputs/apk/demo/release/cmp-android-demo-release.apk"),
      prod_aab_path: File.join(DEPLOYMENT_REPO_ROOT, "cmp-android/build/outputs/bundle/prodRelease/cmp-android-prod-release.aab"),
    }.freeze
  end

  # --------------------------------------------------------------------------
  # get_firebase_config — returns config hash for firebase_app_distribution
  # --------------------------------------------------------------------------
  def self.get_firebase_config(platform, flavor = :prod)
    _s     = SECRETS_DIR         # relative — for _secret_file (which prepends DEPLOYMENT_REPO_ROOT)
    _s_abs = SECRETS_DIR_ABS     # absolute — for paths passed directly to fastlane actions
    base = {
      serviceCredsFile: ENV["FIREBASE_SERVICE_ACCOUNT_PATH"] ||
                        "#{_s_abs}/firebase/service-account.json",
      groups: ENV["FIREBASE_GROUPS"] || nil,
    }
    case platform
    when :ios
      # Priority: ENV (CI) → fork.properties key (local) → secrets/ file (vault) → ""
      app_id = if flavor == :demo
                 ENV["FIREBASE_IOS_DEMO_APP_ID"] ||
                   _fork_prop("firebase.ios.demo.app.id") ||
                   _secret_file("#{_s}/firebase/ios_demo_app_id") || ""
               else
                 ENV["FIREBASE_IOS_PROD_APP_ID"] ||
                   _fork_prop("firebase.ios.prod.app.id") ||
                   ENV["FIREBASE_IOS_APP_ID"] ||
                   _fork_prop("firebase.ios.app.id") ||
                   _secret_file("#{_s}/firebase/ios_prod_app_id") ||
                   _secret_file("#{_s}/firebase/ios_app_id") || ""
               end
      base.merge(appId: app_id)
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
  # Reads UPLOAD keystore credentials (Play App Signing model — Play Console
  # verifies upload signature, then re-signs with the Google-held app signing key).
  def self.get_android_signing_config(options = {})
    _s = SECRETS_DIR
    props_path = "#{_s}/keystores/upload_keystore.properties"
    props = {}
    if File.exist?(File.join(DEPLOYMENT_REPO_ROOT, props_path))
      File.readlines(File.join(DEPLOYMENT_REPO_ROOT, props_path)).each do |line|
        k, v = line.strip.split("=", 2)
        props[k&.strip] = v&.strip if k && v
      end
    end

    # Read storeFile dynamically so forks can rename the keystore without
    # touching config.rb (storeFile key in upload_keystore.properties is canonical).
    default_jks = "#{_s}/keystores/#{props.fetch("storeFile", "upload_keystore.keystore")}"

    {
      keystore_path:     options[:keystore_path]     ||
                         ENV["KEYSTORE_PATH"]        ||
                         default_jks,
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

# Unlock (local) or create (CI) the keychain before Match imports certificates.
# Call this before fetch_certificates_with_match.
# options:
#   :keychain_password — macOS login keychain password (unlocks so Match can import)
#   ENV["KEYCHAIN_PASSWORD"] — CI / local env override
def setup_ios_keychain(options = {})
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  keychain_pass = options[:keychain_password] ||
                  ENV["KEYCHAIN_PASSWORD"] ||
                  cfg[:keychain_password]
  return unless keychain_pass

  if ENV["CI"].to_s != ""
    create_keychain(
      name:             "build.keychain-db",
      password:         keychain_pass,
      default_keychain: true,
      unlock:           true,
      timeout:          false,
      lock_when_sleeps: false,
    )
  else
    unlock_keychain(
      path:        File.expand_path("~/Library/Keychains/login.keychain-db"),
      password:    keychain_pass,
      set_default: true,
    )
  end
end

# Run Fastlane Match to fetch/refresh certificates and provisioning profiles.
def fetch_certificates_with_match(options = {})
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  ssh_key = File.join(DEPLOYMENT_REPO_ROOT, cfg[:match_ssh_key_path])

  # Match reads MATCH_PASSWORD automatically to decrypt the git-stored certs.
  # Priority: call option → ENV → BUILD_CONFIG (file-backed via secrets/match/).
  match_pass = options[:match_password] || ENV["MATCH_PASSWORD"] || cfg[:match_password]
  ENV["MATCH_PASSWORD"] = match_pass.to_s if match_pass && ENV["MATCH_PASSWORD"].to_s.empty?

  # CERTIFICATES_PASSWORD — p12 import password (same as MATCH_PASSWORD in standard Match setups).
  certs_pass = options[:certificates_password] || ENV["CERTIFICATES_PASSWORD"] || cfg[:certificates_password]
  ENV["CERTIFICATES_PASSWORD"] = certs_pass.to_s if certs_pass && ENV["CERTIFICATES_PASSWORD"].to_s.empty?

  # readonly: false + force: true when the stored cert is expired so Match revokes
  # it, creates a fresh Distribution cert, saves it to the repo, and imports it.
  # Set readonly: true (via options) once a valid cert exists in the Match repo.
  readonly = options.key?(:readonly) ? options[:readonly] : false
  force    = options.key?(:force)    ? options[:force]    : !readonly

  match(
    type:                     options[:match_type]       || cfg[:match_type],
    app_identifier:           options[:app_identifier]   || cfg[:app_identifier],
    git_url:                  options[:match_git_url]    || cfg[:match_git_url],
    git_branch:               options[:match_git_branch] || cfg[:match_git_branch],
    git_private_key:          File.exist?(ssh_key) ? ssh_key : nil,
    include_all_certificates: true,
    readonly:                 readonly,
    force:                    force,
  )
end

# Revoke iOS Distribution certificates from Apple Developer Portal to free slots for
# a fresh cert. Strategy (in order):
#   1. Revoke all truly expired certs first (safe, they're already unusable).
#   2. Ensure at least `min_free_slots` (default 2) slots are available. If we have
#      fewer free slots than required, revoke the soonest-expiring cert within
#      `revoke_within_months` months (default 12). This handles two edge cases:
#       a. At the 3-cert maximum (0 free slots) — always triggers.
#       b. At 2/3 (1 free slot) — triggers when min_free_slots >= 2. Needed because
#          the legacy `cert` action can disagree with ConnectAPI on the cert count
#          (different Apple backends) and fail with "maximum" even at 2/3.
#
# Requires load_api_key to have run first (populates Spaceship::ConnectAPI.token).
def revoke_expired_distribution_certs(revoke_within_months: 12, min_free_slots: 1)
  require "spaceship"
  UI.user_error!("No ASC API token — call load_api_key before revoke_expired_distribution_certs") unless Spaceship::ConnectAPI.token

  max_dist_certs = 3

  dist_certs = Spaceship::ConnectAPI::Certificate.all.select do |c|
    c.certificate_type == "DISTRIBUTION" || c.certificate_type == "IOS_DISTRIBUTION"
  end

  # Step 1 — revoke already-expired certs.
  now = Time.now
  expired, valid = dist_certs.partition do |c|
    c.expiration_date.to_s.empty? ? false : Time.parse(c.expiration_date) < now
  end

  expired.each do |cert|
    UI.important("Revoking expired Distribution cert #{cert.id} (expired #{cert.expiration_date})")
    cert.delete!
    UI.success("Revoked #{cert.id}")
  end

  remaining = valid.size
  free_slots = max_dist_certs - remaining
  UI.message("Distribution certs after expired-revoke: #{remaining}/#{max_dist_certs} (#{free_slots} free)")
  return if free_slots >= min_free_slots

  # Step 2 — need more free slots; revoke the soonest-expiring cert within the window.
  cutoff = now + (revoke_within_months * 30 * 24 * 3600)
  candidate = valid.min_by { |c| Time.parse(c.expiration_date) }

  if candidate && Time.parse(candidate.expiration_date) < cutoff
    UI.important("Need #{min_free_slots} free slot(s), have #{free_slots} — revoking soonest-expiring cert " \
                 "#{candidate.id} (expires #{candidate.expiration_date}) to make room")
    candidate.delete!
    UI.success("Revoked #{candidate.id}")
  else
    UI.user_error!(
      "Need #{min_free_slots} free Distribution cert slot(s) but all #{remaining} remaining certs " \
      "expire beyond #{revoke_within_months} months. Revoke one manually at " \
      "https://developer.apple.com/account/resources/certificates/list then re-run renewCerts."
    )
  end
end

# Clone (or update) the Match git repo, delete all distribution cert/key files and any
# provisioning profiles tied to our app identifier, then push. After this,
# fetch_certificates_with_match sees an empty cert directory and creates a fresh
# Distribution certificate via the ASC API.
#
# PERSISTENT CLONE STRATEGY
#   The clone lives at secrets/match/ios-provisioning-profile/ (gitignored).
#   • Local / colleagues: clone is reused across runs (git fetch + reset — fast).
#   • GitHub Actions: starts clean each run, falls back to git clone --depth 1.
#
#   Colleague first-time setup:
#     GIT_SSH_COMMAND="ssh -i secrets/match/match_ci_key -o StrictHostKeyChecking=no" \
#       git clone --depth 1 git@github.com:openMF/ios-provisioning-profile.git \
#       secrets/match/ios-provisioning-profile
#   Subsequent runs: the lane updates the clone automatically.
#
# Safe: does NOT revoke from Apple Developer Portal (expired certs are already unusable).
# Idempotent: no-op if the cert directory is already empty.
def purge_match_distribution_certs
  cfg        = FastlaneConfig::IosConfig::BUILD_CONFIG
  ssh_key    = File.join(DEPLOYMENT_REPO_ROOT, cfg[:match_ssh_key_path])
  git_url    = cfg[:match_git_url]    || ""
  git_branch = cfg[:match_git_branch] || "master"
  bundle_id  = cfg[:app_identifier]  || ""

  git_env = {"GIT_SSH_COMMAND" => "ssh -i #{ssh_key} -o StrictHostKeyChecking=no"}

  # Persistent clone — reused locally, cloned fresh on CI (secrets/ is gitignored).
  match_local = File.join(DEPLOYMENT_REPO_ROOT, "secrets", "match", "ios-provisioning-profile")

  if Dir.exist?(File.join(match_local, ".git"))
    UI.message("Updating existing Match repo clone at #{match_local}...")
    sh(git_env, "git -C #{match_local} fetch --depth 1 origin #{git_branch}")
    sh(git_env, "git -C #{match_local} reset --hard origin/#{git_branch}")
  else
    UI.message("Cloning Match repo (shallow) into #{match_local}...")
    FileUtils.mkdir_p(File.dirname(match_local))
    sh(git_env, "git clone --depth 1 --single-branch --branch #{git_branch} #{git_url} #{match_local}")
  end

  sh("git -C #{match_local} config user.email 'match@fastlane.tools'")
  sh("git -C #{match_local} config user.name 'fastlane match renewal'")

  deleted = []

  # Remove every distribution cert + private key (they're tied to the revoked cert).
  Dir[File.join(match_local, "certs", "distribution", "*")].sort.each do |abs|
    rel = abs.delete_prefix("#{match_local}/")
    sh("git -C #{match_local} rm -f -- #{rel.shellescape}")
    deleted << File.basename(abs)
  end

  # Remove provisioning profiles for our app (they reference the revoked cert).
  Dir[File.join(match_local, "profiles", "**", "*")].select { |f| File.file?(f) }.each do |abs|
    next unless File.basename(abs).include?(bundle_id.tr(".", "_").tr("-", "_")) ||
                File.basename(abs).downcase.include?(bundle_id.downcase)
    rel = abs.delete_prefix("#{match_local}/")
    sh("git -C #{match_local} rm -f -- #{rel.shellescape}")
    deleted << File.basename(abs)
  end

  if deleted.any?
    sh("git -C #{match_local} commit -m 'chore(certs): purge expired Distribution cert + stale profiles'")
    sh(git_env, "git -C #{match_local} push origin #{git_branch}")
    UI.success("Purged from Match repo: #{deleted.join(', ')}")
  else
    UI.important("Match repo already clean — nothing to purge")
  end
end

# Delete ALL provisioning profiles for our bundle ID from Apple's Developer Portal.
# Required when previous Match runs leave duplicate "match AdHoc …" / "match AppStore …"
# profiles that trigger a 409 ENTITY_ERROR on the next profile-create call.
def purge_apple_portal_profiles
  require "spaceship"
  UI.user_error!("No ASC API token — call load_api_key before purge_apple_portal_profiles") unless Spaceship::ConnectAPI.token

  bundle_id = FastlaneConfig::IosConfig::BUILD_CONFIG[:app_identifier] || ""

  %w[adhoc appstore].each do |type|
    profile_name = "match #{type == 'adhoc' ? 'AdHoc' : 'AppStore'} #{bundle_id}"
    profiles = Spaceship::ConnectAPI::Profile.all.select { |p| p.name == profile_name }
    if profiles.empty?
      UI.message("No portal profiles found with name '#{profile_name}'")
      next
    end
    UI.important("Found #{profiles.size} portal profile(s) named '#{profile_name}' — deleting for clean recreation")
    profiles.each do |profile|
      profile.delete!
      UI.success("Deleted portal profile #{profile.id} (#{profile.name})")
    end
  end
end

# Get app version from gradle-generated version.txt, optionally sanitized for App Store.
def get_version_from_gradle(sanitize_for_appstore: false)
  sh("./gradlew versionFile 2>/dev/null || true") rescue nil
  version = VersionHelpers.gradle_version
  sanitize_for_appstore ? VersionHelpers.appstore_sanitize(version) : version
end

# Generate release notes from recent git merge commits.
# Play Store enforces a 500-char cap per locale; truncate with ellipsis if needed.
def generateReleaseNote
  notes = changelog_from_git_commits(
    commits_count:          15,
    pretty:                 "- %s",
    merge_commit_filtering: "only_include_merges",
    quiet:                  true,
  )
  notes.length <= 500 ? notes : notes[0, 497] + "..."
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
    output_directory: File.join(DEPLOYMENT_REPO_ROOT, "cmp-ios/build"),
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

# Increment iOS build number by fetching the latest release from Firebase App Distribution.
# firebase_app_id: pass the flavor-specific app ID; falls back to env vars.
def increment_version(serviceCredsFile:, firebase_app_id: nil)
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  app_id = firebase_app_id ||
           ENV["FIREBASE_IOS_PROD_APP_ID"] ||
           ENV["FIREBASE_IOS_APP_ID"] || ""
  if app_id.empty?
    UI.important("⚠️ Firebase iOS app ID not set — skipping build number increment from Firebase")
    return
  end
  latest = firebase_app_distribution_get_latest_release(
    app:                      app_id,
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
  sh("#{DEPLOYMENT_REPO_ROOT}/gradlew versionFile 2>/dev/null || true") rescue nil
  version_name = VersionHelpers.gradle_version
  version_code = sh("git rev-list --count HEAD 2>/dev/null || echo 1").strip.to_i rescue 1

  ENV["VERSION_NAME"] = version_name
  ENV["VERSION_CODE"] = version_code.to_s

  UI.message("📦 Version: #{version_name} (#{version_code})")
  { version_name: version_name, version_code: version_code }
end

# Build and sign an Android APK/AAB via Gradle with signing config injected.
# Uses sh() + absolute gradlew path because the multi-module repo layout has
# gradlew at root, not inside cmp-android/ — incompatible with gradle() action's
# project_dir expectation.
def buildAndSignApp(taskName:, buildType: "Release", **signing_config)
  keystore = signing_config[:keystore_path] || "secrets/keystores/upload_keystore.keystore"
  keystore_abs = File.expand_path(File.join(DEPLOYMENT_REPO_ROOT, keystore))
  gradlew    = File.join(DEPLOYMENT_REPO_ROOT, "gradlew")
  full_task  = ":cmp-android:#{taskName}#{buildType}"

  # -p tells Gradle to use repo root as project dir, overriding whatever cwd
  # Fastlane sets (deployment/fastlane/) when running the lane.
  sh(
    gradlew, "-p", DEPLOYMENT_REPO_ROOT, full_task,
    "-Pandroid.injected.signing.store.file=#{keystore_abs}",
    "-Pandroid.injected.signing.store.password=#{signing_config[:keystore_password] || ''}",
    "-Pandroid.injected.signing.key.alias=#{signing_config[:key_alias] || 'release'}",
    "-Pandroid.injected.signing.key.password=#{signing_config[:key_password] || ''}",
    "-PVERSION_NAME=#{ENV['VERSION_NAME'] || '1.0.0'}",
    "-PVERSION_CODE=#{ENV['VERSION_CODE'] || '1'}",
  )
end
