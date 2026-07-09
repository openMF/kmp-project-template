# deployment/_shared/config.rb
#
# Single source of truth for all deployment configuration.
# Imported by deployment/Fastfile BEFORE all lane.rb imports.
#
# Fork identity source of truth: gradle/libs.versions.toml (6 keys).
# Secrets: read from secrets/ filesystem first, ENV fallback for CI.
#
# Usage:
#   Local/manual  → cp secrets/sample/* secrets/live/, fill values, run fastlane
#   GitHub Actions → secrets injected as ENV vars by workflow-snippet.yml
#   Framework     → /secrets pull materializes vault to secrets/, run fastlane

require_relative "lib/appstore_helpers"
require_relative "lib/firebase_helpers"
require_relative "lib/version_helpers"
require_relative "lib/build_secrets"   # secrets path/value resolver (secrets/LAYOUT.yaml is SoT)
require_relative "variant_resolver"    # (flavor, buildType) → gradle_task / artifact / ios_scheme
                                       # by convention (Phase 2 of deploy-gha-product-flavors epic;
                                       # delegates all secret paths to BuildSecrets.for(flavor:))

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
      # DRIFT FIX: was "secrets/play/service-account.json" (wrong — lanes/script use
      # secrets/android/play/…). Resolver returns the canonical path from LAYOUT.yaml.
      play_store_json_key: BuildSecrets.for.path(:play_service_account),
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
      key_id:                        BuildSecrets.for.value(:appstore_key_id),
      issuer_id:                     BuildSecrets.for.value(:appstore_issuer_id),
      key_filepath:                  File.join(DEPLOYMENT_REPO_ROOT, BuildSecrets.for.path(:appstore_auth_key)),
      # Match certificate repository — ENV (CI) → secrets/ file → fork.properties → default
      match_git_url:    _c._secret("MATCH_GIT_URL",    "#{_s}/apple/match/git_url")    || _c._fork_prop("apple.match.git.url"),
      match_git_branch: _c._secret("MATCH_GIT_BRANCH", "#{_s}/apple/match/git_branch") || _c._fork_prop("apple.match.git.branch") || "master",
      match_type:                    "adhoc",
      match_ssh_key_path:            BuildSecrets.for.path(:match_ssh_key),
      match_password:                BuildSecrets.for.value(:match_password),
      certificates_password:         _c._secret("CERTIFICATES_PASSWORD",   "#{_s}/apple/match/certificates_password"),
      keychain_password:             _c._secret("KEYCHAIN_PASSWORD",       "#{_s}/apple/match/keychain_password"),
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
        contact_email:         _c._secret("TESTFLIGHT_CONTACT_EMAIL") || _c._fork_prop("org.email")      || "team@mifos.org",
        contact_first_name:    _c._secret("TESTFLIGHT_FIRST_NAME")    || _c._fork_prop("org.first.name") || "Mifos",
        contact_last_name:     _c._secret("TESTFLIGHT_LAST_NAME")     || _c._fork_prop("org.last.name")  || "Team",
        contact_phone:         _c._secret("TESTFLIGHT_PHONE")         || _c._fork_prop("org.phone")      || "+1234567890",
        demo_account_required: false,
      }.freeze,
      beta_app_feedback_email:           _c._secret("BETA_FEEDBACK_EMAIL") || _c._fork_prop("org.email") || "team@mifos.org",
      beta_app_description:              "#{ForkIdentity::APP_DISPLAY_NAME} beta build",
      demo_account_required:             false,
      distribute_external:               true,
      notify_external_testers:           true,
      groups:                            (_c._secret("TESTFLIGHT_GROUPS") || _c._fork_prop("apple.tf.groups") || "internal-testers").split(",").map(&:strip),
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
        first_name: _c._secret("APPSTORE_REVIEW_FIRST_NAME") || _c._fork_prop("org.first.name") || "Mifos",
        last_name:  _c._secret("APPSTORE_REVIEW_LAST_NAME")  || _c._fork_prop("org.last.name")  || "Team",
        phone:      _c._secret("APPSTORE_REVIEW_PHONE")      || _c._fork_prop("org.phone")      || "+1234567890",
        email:      _c._secret("APPSTORE_REVIEW_EMAIL")      || _c._fork_prop("org.email")      || "review@mifos.org",
      }.freeze,
    }.freeze
  end

  # --------------------------------------------------------------------------
  # AndroidConfig — Play Store metadata path + per-flavor Play track policy.
  #
  # Artifact paths are NO LONGER stored as per-flavor constants — every lane
  # derives its APK/AAB path by convention through
  # `VariantResolver.resolve(flavor:, build_type:).apk_path` / `.aab_path`
  # (single derivation point, D9/AC-3). The former `BUILD_PATHS` symbolic
  # constant has been removed entirely.
  # --------------------------------------------------------------------------
  module AndroidConfig
    METADATA_PATH = "deployment/android/metadata".freeze

    # Per-flavor Play track destination — the mechanism that makes demo's Play
    # publication config, not a Ruby conditional (AC-12). Each flavor maps to
    # a Play track string (`"internal"` / `"beta"` / `"production"`) that
    # `deployInternal` passes to `upload_to_play_store`, or to `nil` when the
    # flavor is Firebase-only and MUST NOT be uploaded to Play. `deployInternal`
    # errors loudly on `nil` — a silent skip would hide a mis-configuration.
    #
    # Override order (highest → lowest):
    #   1. `PLAY_TRACKS_BY_FLAVOR` env var — CSV of `flavor=track` pairs
    #      (blank value ⇒ Firebase-only). Example:
    #        PLAY_TRACKS_BY_FLAVOR="prod=internal,demo="
    #      threads clean through workflow-dispatch env inputs.
    #   2. `android.play.tracks.by_flavor` `fork.properties` key — same CSV
    #      shape as the env var. Local override for a colleague fork.
    #   3. Default hash below: prod → "internal", demo → "internal".
    #
    # Any flavor not in the resolved hash is undefined ⇒ `deployInternal`
    # errors ("no Play track configured"). Add a flavor to `kmpFlavors {}` DSL
    # AND to this map (via env var / fork.properties / editing the default) —
    # the DSL alone is not enough because Play publication is a policy choice.
    DEFAULT_PLAY_TRACKS_BY_FLAVOR = {
      "prod" => "internal",
      "demo" => "internal",
    }.freeze

    def self.play_tracks_by_flavor
      csv = FastlaneConfig._secret("PLAY_TRACKS_BY_FLAVOR") ||
            FastlaneConfig._fork_prop("android.play.tracks.by_flavor")
      return DEFAULT_PLAY_TRACKS_BY_FLAVOR if csv.nil? || csv.strip.empty?
      csv.split(",").each_with_object({}) do |pair, acc|
        k, v = pair.split("=", 2).map { |s| s.to_s.strip }
        next if k.nil? || k.empty?
        acc[k] = (v.nil? || v.empty?) ? nil : v
      end
    end
  end

  # --------------------------------------------------------------------------
  # get_firebase_config — returns config hash for firebase_app_distribution
  # --------------------------------------------------------------------------
  def self.get_firebase_config(platform, flavor = :prod)
    _s     = SECRETS_DIR         # relative — for _secret_file (which prepends DEPLOYMENT_REPO_ROOT)
    _s_abs = SECRETS_DIR_ABS     # absolute — for paths passed directly to fastlane actions
    base = {
      serviceCredsFile: ENV["FIREBASE_SERVICE_ACCOUNT_PATH"] ||
                        File.join(DEPLOYMENT_REPO_ROOT, BuildSecrets.for(flavor: flavor).path(:firebase_service_account)),
      groups: ENV["FIREBASE_GROUPS"] || nil,
    }
    case platform
    when :ios
      # Priority: ENV (CI) → fork.properties key (local) → secrets/ file (vault) → ""
      app_id = if flavor == :demo
                 ENV["FIREBASE_IOS_DEMO_APP_ID"] ||
                   _fork_prop("firebase.ios.demo.app.id") ||
                   _secret_file("#{_s}/apple/firebase/ios_demo_app_id") || ""
               else
                 ENV["FIREBASE_IOS_PROD_APP_ID"] ||
                   _fork_prop("firebase.ios.prod.app.id") ||
                   ENV["FIREBASE_IOS_APP_ID"] ||
                   _fork_prop("firebase.ios.app.id") ||
                   _secret_file("#{_s}/apple/firebase/ios_prod_app_id") ||
                   _secret_file("#{_s}/apple/firebase/ios_app_id") || ""
               end
      base.merge(appId: app_id)
    when :android
      # by_flavor in LAYOUT.yaml maps prod→FIREBASE_ANDROID_APP_ID, demo→FIREBASE_ANDROID_DEMO_APP_ID
      # (env-first, file fallback) — collapses the old per-flavor if/else.
      app_id = BuildSecrets.for(flavor: flavor).value(:firebase_android_app_id) || ""
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
    props_path = "#{_s}/android/keystores/upload_keystore.properties"
    props = {}
    if File.exist?(File.join(DEPLOYMENT_REPO_ROOT, props_path))
      File.readlines(File.join(DEPLOYMENT_REPO_ROOT, props_path)).each do |line|
        k, v = line.strip.split("=", 2)
        props[k&.strip] = v&.strip if k && v
      end
    end

    # Read storeFile dynamically so forks can rename the keystore without
    # touching config.rb (storeFile key in upload_keystore.properties is canonical).
    default_jks = "#{_s}/android/keystores/#{props.fetch("storeFile", "upload_keystore.keystore")}"

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

  # Install CocoaPods when the iOS project uses them — the Xcode archive needs the generated
  # Pods/ + .xcconfig/.xcfilelist. This config.rb preamble overrides deploy_helpers' (which ran
  # cocoapods), so without this the archive fails "Unable to open Pods-iosApp.release.xcconfig".
  # KMP + CocoaPods: cmp_shared.podspec is generated by the Kotlin cocoapods plugin, so pod
  # install fails "should run :generateDummyFramework first" unless these Gradle tasks run first
  # (matches .github/workflows/pr-check.yml line 61). (2026-06-22)
  podfile = File.join(DEPLOYMENT_REPO_ROOT, "cmp-ios", "Podfile")
  if File.exist?(podfile)
    sh("#{DEPLOYMENT_REPO_ROOT}/gradlew -p #{DEPLOYMENT_REPO_ROOT} :cmp-shared:podspec :cmp-shared:generateDummyFramework")
    cocoapods(podfile: podfile, try_repo_update_on_error: true)
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
  # Priority: call option → ENV → BUILD_CONFIG (file-backed via secrets/apple/match/).
  match_pass = options[:match_password] || ENV["MATCH_PASSWORD"] || cfg[:match_password]
  ENV["MATCH_PASSWORD"] = match_pass.to_s if match_pass && ENV["MATCH_PASSWORD"].to_s.empty?

  # CERTIFICATES_PASSWORD — p12 import password (same as MATCH_PASSWORD in standard Match setups).
  certs_pass = options[:certificates_password] || ENV["CERTIFICATES_PASSWORD"] || cfg[:certificates_password]
  ENV["CERTIFICATES_PASSWORD"] = certs_pass.to_s if certs_pass && ENV["CERTIFICATES_PASSWORD"].to_s.empty?

  base = {
    type:                     options[:match_type]       || cfg[:match_type],
    app_identifier:           options[:app_identifier]   || cfg[:app_identifier],
    git_url:                  options[:match_git_url]    || cfg[:match_git_url],
    git_branch:               options[:match_git_branch] || cfg[:match_git_branch],
    git_private_key:          File.exist?(ssh_key) ? ssh_key : nil,
    include_all_certificates: true,
  }

  # Explicit override — a caller can pin readonly (e.g. bootstrap passes readonly:false to mint).
  if options.key?(:readonly)
    ro = options[:readonly]
    match(**base, readonly: ro, force: options.fetch(:force, !ro))
    return
  end

  # FULLY-AUTOMATIC Match (auto readonly/force + auto-renew):
  #   Phase 1 — readonly fetch. Installs whatever valid cert/profile already lives in the Match
  #     repo. NO Dev Portal call → works even when the Apple PLA is pending. If it raises (repo
  #     empty / cert revoked), fall through to renewal.
  phase1_ok = begin
    match(**base, readonly: true, force: false)
    true
  rescue StandardError => e
    UI.important("readonly match could not install a cert/profile (#{e.message.to_s.lines.first&.strip}); will renew.")
    false
  end

  #   Phase 2 — decide force LOCALLY (no portal): renew only when the AppStore cert/profile is
  #     missing or expiring soon. readonly:false + force:true regenerates on the Dev Portal AND
  #     commits the fresh assets back to the Match repo (auto-renew). Renewal is the ONLY path
  #     that needs the portal/PLA — the valid-cert steady state never touches it. (2026-06-22)
  if !phase1_ok || match_assets_expired?(cfg, options)
    UI.important("⚠️ AppStore cert/profile expired or missing → AUTO-RENEWING on the Dev Portal " \
                 "(requires the Apple Program License Agreement to be current for the account holder).")
    match(**base, readonly: false, force: true)
  else
    UI.message("✅ Valid AppStore cert/profile already in the Match repo — readonly, no Dev Portal / PLA needed.")
  end
end

# Drives the auto-renew decision WITHOUT a Dev Portal call: returns true ONLY when there is no
# valid Apple Distribution signing identity available AND no unexpired AppStore profile on disk.
# `security find-identity -v` lists ONLY non-expired codesigning identities, so a hit means the
# cert is valid → stay readonly (no portal/PLA). Checks BOTH the legacy and the Xcode-16+/26
# provisioning-profile directories. Defaults to NOT-expired on any uncertainty, so a valid cert
# is never needlessly renewed (which would hit the PLA). (2026-06-22)
def match_assets_expired?(cfg, options = {}, min_days = 7)
  # 1) Valid (non-expired) Distribution cert in the keychain → certs are good.
  ids = `security find-identity -v -p codesigning 2>/dev/null`
  return false if ids =~ /Apple Distribution|iPhone Distribution|3rd Party Mac/

  # 2) No cert hit → fall back to an unexpired AppStore profile on disk (either profile dir).
  require "time"
  app_id = options[:app_identifier] || cfg[:app_identifier]
  dirs = [
    File.expand_path("~/Library/Developer/Xcode/UserData/Provisioning Profiles"),
    File.expand_path("~/Library/MobileDevice/Provisioning Profiles"),
  ]
  dirs.each do |dir|
    Dir.glob(File.join(dir, "*.mobileprovision")).each do |p|
      xml = `security cms -D -i "#{p}" 2>/dev/null`
      next unless xml.include?(app_id)
      next if xml =~ %r{<key>get-task-allow</key>\s*<true}  # skip Development profiles
      if xml =~ %r{<key>ExpirationDate</key>\s*<date>([^<]+)</date>}
        return false if Time.parse(Regexp.last_match(1)) > Time.now + (min_days * 86_400)
      end
    end
  end
  UI.important("No valid Apple Distribution identity or unexpired AppStore profile found — renewal needed.")
  true
rescue StandardError => e
  UI.important("match validity check failed (#{e.message}); assuming VALID (readonly) to avoid an unneeded Dev Portal/PLA call.")
  false
end

# Apple's App Store Connect API intermittently throws "A required agreement is missing or
# has expired" on TestFlight reads (latest_testflight_build_number / pilot) even when ALL
# agreements are current — a transient Apple-side bug seen across CI platforms and Xcode
# versions (fastlane#29860, surfaced ~2026-06). It clears on retry. Wrap the read so a flake
# RETRIES instead of failing the release. Versioning is unchanged: on success the real build
# number (and the LATEST_TESTFLIGHT_VERSION side-effect) is returned exactly as before; only
# after max_attempts genuine failures does it re-raise. NEVER falls back to a fabricated
# build number (that could be LOWER than the live TF build → Apple rejects the upload). (2026-06-23)
def latest_tf_build_number_resilient(max_attempts: 3, sleep_seconds: 20, **opts)
  attempts = 0
  begin
    latest_testflight_build_number(**opts)
  rescue StandardError => e
    attempts += 1
    raise if attempts >= max_attempts
    UI.important("⚠️ latest_testflight_build_number flaked (#{e.message.to_s.lines.first&.strip}); " \
                 "retry #{attempts}/#{max_attempts - 1} in #{sleep_seconds}s (Apple ASC transient, fastlane#29860)…")
    sleep(sleep_seconds)
    retry
  end
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
#   The clone lives at secrets/apple/match/ios-provisioning-profile/ (gitignored).
#   • Local / colleagues: clone is reused across runs (git fetch + reset — fast).
#   • GitHub Actions: starts clean each run, falls back to git clone --depth 1.
#
#   Colleague first-time setup:
#     GIT_SSH_COMMAND="ssh -i secrets/apple/match/match_ci_key -o StrictHostKeyChecking=no" \
#       git clone --depth 1 git@github.com:openMF/ios-provisioning-profile.git \
#       secrets/apple/match/ios-provisioning-profile
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
  match_local = File.join(DEPLOYMENT_REPO_ROOT, "secrets", "apple", "match", "ios-provisioning-profile")

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
# `configuration:` was hardcoded pre-Phase-2 — parameterized so lanes can pass
# the resolved build-type (e.g. "Staging" → the Staging xcconfig) that
# `VariantResolver.resolve(...).ios_scheme` was built against; defaults to
# "Release" to preserve pre-Phase-2 caller behavior.
def build_ios_project(options = {})
  cfg = FastlaneConfig::IosConfig::BUILD_CONFIG
  build_app(
    scheme:           options[:scheme]        || cfg[:scheme],
    workspace:        options[:workspace]     || cfg[:workspace],
    configuration:    options[:configuration] || "Release",
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
  # Give reckon the full tag history. The CI checkout is shallow, so without this reckon can't
  # see the release tags and versionFile falls back to 0.0.1 / 1.0.0 inconsistently. Best-effort:
  # a fresh clone may already be complete (--unshallow then errors → tolerated). (2026-06-22 fix)
  sh("cd #{DEPLOYMENT_REPO_ROOT} && git fetch --tags --force --quiet 2>/dev/null; git fetch --unshallow --quiet 2>/dev/null || true") rescue nil
  # Run versionFile and SURFACE failures (was `2>/dev/null || true`, which hid reckon errors and
  # silently produced a 1.0.0 build). version.txt is written to the repo root by the task.
  begin
    # --no-configuration-cache: config-cache is ON for this project, so a plain `versionFile`
    # gets served a stale cached project.version (1.0.0) and overwrites the committed version.txt.
    # Disabling it forces reckon to re-evaluate fresh against the just-fetched tags. (2026-06-22)
    sh("cd #{DEPLOYMENT_REPO_ROOT} && ./gradlew versionFile --no-configuration-cache --console=plain")
  rescue => e
    UI.important("⚠️ ./gradlew versionFile failed: #{e.message.to_s.lines.first&.strip}")
  end
  version_name = VersionHelpers.gradle_version
  UI.important("⚠️ versionName fell back to '#{version_name}' — reckon/versionFile did not produce version.txt; check git tag history in CI.") if %w[1.0.0 0.0.0].include?(version_name)
  # VERSION_CODE_OVERRIDE: pre-set ENV (e.g. by act --env-file) wins over
  # git-rev-list. Lets iteration runs bump past the last successful upload
  # without a real git commit on the source repo each time.
  override = ENV["VERSION_CODE_OVERRIDE"].to_s.strip
  version_code = if !override.empty?
                   override.to_i
                 else
                   sh("git rev-list --count HEAD 2>/dev/null || echo 1").strip.to_i rescue 1
                 end

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
  # DRIFT FIX: was hardcoded "secrets/android/keystores/upload_keystore.keystore"
  # (flat pre-restructure path — broke after secrets/{live,sample} split). CI passes
  # keystore_path from `build-secrets path upload_keystore`; the local-run fallback
  # must resolve through the SAME LAYOUT resolver (live-wins-else-sample), never hardcode.
  keystore = signing_config[:keystore_path] || BuildSecrets.for.path(:upload_keystore)
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
