# deployment/desktop/mac-app-store/lane.rb
#
# macOS PKG → TestFlight / App Store via Fastlane + Gradle.
#
# Signing approach — bypass Compose Desktop's internal signing entirely:
#
#   Compose Desktop 1.11.0's ExternalToolRunner calls /usr/bin/security find-certificate
#   via execOperations.exec() using an ABSOLUTE path. On macOS 15 Sequoia this subprocess
#   returns 0 bytes (stdout + stderr), causing "Could not find certificate" regardless of
#   which keychain is targeted (login.keychain-db, custom, default search list — all fail).
#   ProcessBuilder from Gradle doLast{} and Fastlane sh() both work fine (1770 bytes).
#   The absolute /usr/bin/security path also makes a PATH-shim impossible.
#
#   Solution: run createReleaseDistributable (unsigned app bundle, ProGuard applied),
#   then sign manually in Fastlane sh() context where security works:
#     1. createReleaseDistributable  — unsigned .app, MAC_SIGNING_IDENTITY unset so
#                                      Gradle sees macSigningId = null → sign.set(false)
#     2. embed provisioning profile  — copy .provisionprofile → app/Contents/
#     3. codesign                    — sign .app with Apple Distribution cert
#     4. productbuild --sign         — wrap .app into signed .pkg with installer cert
#
#   Local:  Match (readonly) installs Apple Distribution cert into login.keychain-db.
#           MAC_KEYCHAIN_PATH = login.keychain-db is passed to codesign --keychain.
#   CI:     Composite action (mifos-x-actionhub-publish-macos-on-appstore-testflight-kmp)
#           creates build.keychain, imports both signing + installer certs, runs
#           set-key-partition-list, then calls this lane.  Lane reads the keychain path
#           from ~/Library/Keychains/build.keychain-db and resolves identities from it.
#
# Secrets (all read via config.rb _secret helper — ENV first, secrets/ fallback):
#   secrets/apple/appstore/AuthKey.p8          — ASC API key
#   secrets/apple/appstore/key_id              — ASC key ID
#   secrets/apple/appstore/issuer_id           — ASC issuer ID
#   secrets/apple/match/match_ci_key           — SSH key for Match git repo (local only)
#   secrets/apple/match/.match_password        — Match encryption password (local only)
#
# Env vars (optional overrides):
#   MAC_APP_IDENTIFIER             — macOS bundle ID (defaults to ForkIdentity::APP_ID)
#   MAC_SIGNING_IDENTITY           — Apple Distribution identity for codesign
#   MAC_KEYCHAIN_PATH              — keychain file path for codesign --keychain
#   MAC_PROVISIONING_PROFILE_PATH  — .provisionprofile path embedded into app bundle
#   MAC_APP_STORE_CONNECT_APP_ID   — Numeric ASC app ID for the macOS app

MAC_APP_STORE_METADATA_PATH    = File.join(DEPLOYMENT_REPO_ROOT, "deployment/desktop/mac-app-store/metadata").freeze
MAC_APP_STORE_SCREENSHOTS_PATH = File.join(DEPLOYMENT_REPO_ROOT, "deployment/desktop/mac-app-store/metadata/screenshots").freeze
MAC_APP_STORE_PRIMARY_LOCALE   = "en-GB".freeze

platform :mac do
  desc "Build and upload macOS desktop build to TestFlight (Mac App Store track)"
  lane :desktop_testflight do |options|
    options       = sanitize_options(options)
    mac_bundle_id = ENV["MAC_APP_IDENTIFIER"] || ENV["MAC_BUNDLE_ID"] || ForkIdentity::APP_ID

    load_api_key(options)
    with_ios_preamble(options)
    setup_mac_signing_keychain(options, mac_bundle_id)

    pkg_path = build_mac_pkg(mac_bundle_id, options)

    upload_to_testflight(
      pkg:                               pkg_path,
      api_key:                           Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      skip_waiting_for_build_processing: true,
      apple_id:                          ENV["MAC_APP_STORE_CONNECT_APP_ID"] || options[:apple_id],
    )

    UI.success("✅ macOS PKG uploaded to TestFlight — processing in progress.")
  ensure
    # Local: we use login.keychain-db directly (no custom keychain created), so no cleanup needed.
    # CI:    composite action owns its own keychain lifecycle.
  end

  desc "One-shot: create proper Mac Installer Distribution cert in Apple Developer Portal + push to Match repo."
  lane :create_mac_installer_cert do |options|
    options = sanitize_options(options)
    mac_bundle_id = ENV["MAC_APP_IDENTIFIER"] || ENV["MAC_BUNDLE_ID"] || ForkIdentity::APP_ID
    load_api_key(options)
    cfg     = FastlaneConfig::IosConfig::BUILD_CONFIG
    ssh_key = File.join(DEPLOYMENT_REPO_ROOT, cfg[:match_ssh_key_path])
    match_pass = options[:match_password] || ENV["MATCH_PASSWORD"] || cfg[:match_password]
    ENV["MATCH_PASSWORD"] = match_pass.to_s if match_pass && ENV["MATCH_PASSWORD"].to_s.empty?
    match(
      type:            "mac_installer_distribution",
      platform:        "macos",
      app_identifier:  mac_bundle_id,
      git_url:         options[:match_git_url]    || cfg[:match_git_url],
      git_branch:      options[:match_git_branch] || cfg[:match_git_branch],
      git_private_key: File.exist?(ssh_key) ? ssh_key : nil,
      api_key:         Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      readonly:        false,
    )
    UI.success("✅ Mac Installer Distribution cert created and pushed to Match repo.")
  end

  desc "Stage 1 → Stage 2 promotion: distribute an already-uploaded Mac TF build to external testers (no rebuild). Triggers Apple's beta review (~24h)."
  lane :promoteMacToExternalBeta do |options|
    options       = sanitize_options(options)
    mac_bundle_id = ENV["MAC_APP_IDENTIFIER"] || ENV["MAC_BUNDLE_ID"] || ForkIdentity::APP_ID

    load_api_key(options)

    build_number = options[:build_number]&.to_s || latest_testflight_build_number(
      app_identifier: mac_bundle_id,
      api_key:        Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      platform:       "osx",
    ).to_s

    external_groups = options[:groups] || ["External Beta"]

    UI.important("📦 Promoting Mac TF build #{build_number} → external testers (#{external_groups.join(', ')})")

    pilot(
      api_key:                              Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      app_platform:                         "osx",
      app_identifier:                       mac_bundle_id,
      build_number:                         build_number,
      distribute_only:                      true,
      distribute_external:                  true,
      notify_external_testers:              true,
      groups:                               external_groups,
      submit_beta_review:                   true,
      reject_build_waiting_for_review:      true,
      changelog:                            options[:changelog] || generateReleaseNote(),
    )

    UI.success("✅ Mac build #{build_number} submitted for beta review — external testers receive it on approval (~24h).")
  end

  desc "Promote an existing Mac TestFlight build to Mac App Store review — no rebuild, no re-upload."
  lane :promoteMacToAppStore do |options|
    options       = sanitize_options(options)
    mac_bundle_id = ENV["MAC_APP_IDENTIFIER"] || ENV["MAC_BUNDLE_ID"] || ForkIdentity::APP_ID

    load_api_key(options)

    if options[:build_number]
      build_number = options[:build_number].to_s
      app_version  = options[:app_version]
      UI.important("📦 Using provided build #{build_number}")
    else
      build_number = latest_testflight_build_number(
        app_identifier: mac_bundle_id,
        api_key:        Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
        platform:       "osx",
      ).to_s
      app_version = Actions.lane_context[SharedValues::LATEST_TESTFLIGHT_VERSION]
      UI.important("📦 Latest Mac TestFlight build: #{build_number}  (#{app_version})")
    end

    UI.message("🚀 Submitting Mac build #{build_number} for App Store review...")
    UI.message("   automatic_release: true  (goes live on approval — no manual step)")

    deliver(
      platform:                             "osx",
      api_key:                              Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      app_identifier:                       mac_bundle_id,
      app_version:                          app_version,
      build_number:                         build_number,
      skip_binary_upload:                   true,
      metadata_path:                        MAC_APP_STORE_METADATA_PATH,
      screenshots_path:                     MAC_APP_STORE_SCREENSHOTS_PATH,
      overwrite_screenshots:                true,
      ignore_language_directory_validation: true,
      skip_app_version_update:              true,
      submit_for_review:                    true,
      automatic_release:                    true,
      phased_release:                       false,
      reject_if_possible:                   true,
      run_precheck_before_submit:           false,
      force:                                true,
    )

    UI.success("✅ Mac build #{build_number} submitted for App Store review — will auto-release on approval.")
  end

  desc "Full Mac App Store release — build PKG from source + deliver (use promoteMacToAppStore when a TestFlight build already exists)"
  lane :desktop_release do |options|
    options       = sanitize_options(options)
    mac_bundle_id = ENV["MAC_APP_IDENTIFIER"] || ENV["MAC_BUNDLE_ID"] || ForkIdentity::APP_ID

    load_api_key(options)
    with_ios_preamble(options)
    setup_mac_signing_keychain(options, mac_bundle_id)

    pkg_path = build_mac_pkg(mac_bundle_id, options)

    deliver(
      pkg:               pkg_path,
      api_key:           Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      submit_for_review: options.fetch(:submit_for_review, false),
      automatic_release: options.fetch(:automatic_release, false),
      force:             true,
      skip_screenshots:  true,
      skip_metadata:     true,
    )
  ensure
    # Local: login.keychain-db used directly — no custom keychain to delete.
    # CI:    composite action owns cleanup.
  end

  # ── Helpers ────────────────────────────────────────────────────────────────

  # Set up signing context and populate:
  #   ENV["MAC_SIGNING_IDENTITY"]          — Apple Distribution identity for codesign
  #   ENV["MAC_KEYCHAIN_PATH"]             — keychain path passed to codesign --keychain
  #   ENV["MAC_PROVISIONING_PROFILE_PATH"] — .provisionprofile embedded into the app bundle
  #
  # Local: Match installs cert into login.keychain-db; MAC_KEYCHAIN_PATH points there.
  # CI:    composite action owns the keychain; lane reads build.keychain path + identities.
  def setup_mac_signing_keychain(options, mac_bundle_id)
    if ENV["CI"].to_s != ""
      _setup_mac_signing_keychain_ci(options, mac_bundle_id)
    else
      _setup_mac_signing_keychain_local(options, mac_bundle_id)
    end
  end

  def _setup_mac_signing_keychain_ci(options, mac_bundle_id)
    # Composite action (mifos-x-actionhub-publish-macos-on-appstore-testflight-kmp) runs
    # before this lane: creates build.keychain, imports both the App Distribution cert
    # (-T /usr/bin/codesign) and the Installer Distribution cert (-T /usr/bin/productsign),
    # runs set-key-partition-list, and places the provisioning profile at
    # ~/Library/MobileDevice/Provisioning Profiles/macos.provisionprofile.
    # The lane resolves the keychain path and identities from there.

    # security create-keychain build.keychain → file is build.keychain-db on macOS 12+.
    keychain_path = ["build.keychain-db", "build.keychain"].map { |name|
      File.expand_path("~/Library/Keychains/#{name}")
    }.find { |p| File.exist?(p) }

    unless keychain_path
      # Fallback: query default-keychain (composite action called default-keychain -s).
      keychain_path = sh(
        "security default-keychain 2>/dev/null | tr -d '\" \t\n'",
        log: false,
      ).strip.then { |p| p.empty? ? nil : p }
    end

    if keychain_path
      ENV["MAC_KEYCHAIN_PATH"] = keychain_path
      UI.message("🔐 CI keychain: #{keychain_path}")
    else
      UI.error("CI: could not locate build.keychain — codesign may fail")
    end

    identity = ENV["MATCH_CODESIGNING_IDENTITY"].to_s.strip
    if identity.empty?
      find_args = keychain_path ? keychain_path.shellescape : ""
      identity = sh(
        "security find-identity -v -p codesigning #{find_args} 2>/dev/null" \
        " | grep 'Apple Distribution\\|3rd Party Mac Developer' | head -1" \
        " | sed 's/.*\"\\(.*\\)\".*/\\1/'",
        log: false,
      ).strip
    end
    UI.user_error!("No Apple Distribution identity found on CI") if identity.empty?
    ENV["MAC_SIGNING_IDENTITY"] = identity
    UI.message("🔏 Signing identity: #{identity}")

    # Resolve provisioning profile placed by the composite action.
    _resolve_mac_provisioning_profile(mac_bundle_id)
  end

  def _setup_mac_signing_keychain_local(options, mac_bundle_id)
    cfg        = FastlaneConfig::IosConfig::BUILD_CONFIG
    ssh_key    = File.join(DEPLOYMENT_REPO_ROOT, cfg[:match_ssh_key_path])
    match_pass = options[:match_password] || ENV["MATCH_PASSWORD"] || cfg[:match_password]

    ENV["MATCH_PASSWORD"] = match_pass.to_s if match_pass && ENV["MATCH_PASSWORD"].to_s.empty?

    login_kc = File.expand_path("~/Library/Keychains/login.keychain-db")
    UI.user_error!("login.keychain-db not found at #{login_kc}") unless File.exist?(login_kc)

    # ── Step 0: Remove duplicate Apple Distribution certs ──────────────────────────
    # Match re-installs its cert on every run, causing "ambiguous" errors when a
    # stale copy of the same-named cert remains. Wipe all copies before Match so
    # exactly one cert exists after the install.
    _remove_duplicate_distribution_certs(login_kc)

    match_base = {
      app_identifier:  mac_bundle_id,
      git_url:         options[:match_git_url]    || cfg[:match_git_url],
      git_branch:      options[:match_git_branch] || cfg[:match_git_branch],
      git_private_key: File.exist?(ssh_key) ? ssh_key : nil,
      api_key:         Actions.lane_context[SharedValues::APP_STORE_CONNECT_API_KEY],
      readonly:        true,
    }

    # ── Step 1a: Match appstore — installs Apple Distribution cert + provisioning profile ─
    match(**match_base.merge(type: "appstore", platform: "macos"))

    # ── Step 2: Set MAC_KEYCHAIN_PATH immediately after appstore Match ────────────
    ENV["MAC_KEYCHAIN_PATH"] = login_kc
    UI.message("🔐 Keychain: #{login_kc}")

    # Resolve provisioning profile now (while MATCH_PROVISIONING_PROFILE_MAPPING
    # reflects only the appstore Match result, not the installer cert Match below).
    _resolve_mac_provisioning_profile(mac_bundle_id)

    # ── Step 3: Capture app-signing SHA1 before installer Match muddies keychain ──
    # Use SHA1 over display name to avoid "ambiguous" codesign errors.
    app_sha1 = sh(
      "security find-certificate -c 'Apple Distribution' -Z #{login_kc.shellescape} 2>/dev/null" \
      " | grep 'SHA-1 hash:' | tail -1 | awk '{print $3}'",
      log: false,
    ).strip
    UI.user_error!("No Apple Distribution cert SHA1 found in login.keychain-db") if app_sha1.empty?
    ENV["MAC_SIGNING_IDENTITY"] = app_sha1
    UI.message("🔏 App signing SHA1: #{app_sha1}")

    # ── Step 1b: Match mac_installer_distribution — installs installer cert ────────
    # Snapshot SHA1s before so we can diff to find the exact installer cert SHA1,
    # regardless of whether it is named "3rd Party Mac Developer Installer" or
    # "Apple Distribution" in the keychain.
    sha1s_before_installer = _list_identity_sha1s(login_kc)
    match(**match_base.merge(type: "mac_installer_distribution", platform: "macos"))
    sha1s_after_installer  = _list_identity_sha1s(login_kc)
    new_installer_sha1s    = sha1s_after_installer - sha1s_before_installer

    # ── Step 4: Store installer identity ──────────────────────────────────────────
    if new_installer_sha1s.any?
      ENV["MAC_INSTALLER_IDENTITY"] = new_installer_sha1s.first
      UI.message("📦 Installer SHA1: #{new_installer_sha1s.first}")
    else
      # Fallback: name-based search for known installer cert patterns
      installer = sh(
        "security find-identity -v -p basic #{login_kc.shellescape} 2>/dev/null" \
        " | grep -iE 'installer|3rd party mac' | head -1 | sed 's/.*\"\\(.*\\)\".*/\\1/'",
        log: false,
      ).strip
      unless installer.empty?
        ENV["MAC_INSTALLER_IDENTITY"] = installer
        UI.message("📦 Installer (name): #{installer}")
      else
        UI.important("⚠️  No installer cert found — PKG will be unsigned (local dev only).")
      end
    end
  end

  def _list_identity_sha1s(keychain_path)
    sh(
      "security find-identity -v -p basic #{keychain_path.shellescape} 2>/dev/null" \
      " | grep -oE '[0-9A-F]{40}'",
      log: false,
    ).strip.split
  end

  # Remove all copies of "Apple Distribution" certs from the given keychain.
  # Prevents "ambiguous" codesign errors when Match re-installs the cert on
  # subsequent runs. Safe: Match will immediately reinstall from the repo.
  def _remove_duplicate_distribution_certs(keychain_path)
    sha1s = sh(
      "security find-certificate -c 'Apple Distribution' -a -Z #{keychain_path.shellescape} 2>/dev/null" \
      " | grep 'SHA-1 hash:' | awk '{print $3}'",
      log: false,
    ).strip.split
    return if sha1s.empty?

    UI.message("🧹 Removing #{sha1s.size} Apple Distribution cert(s) before Match install...")
    sha1s.each do |sha1|
      sh(
        "security delete-certificate -Z #{sha1} #{keychain_path.shellescape} 2>/dev/null || true",
        log: false,
      )
    end
  end

  # Resolve the provisioning profile that Match installed and set MAC_PROVISIONING_PROFILE_PATH.
  # macOS .provisionprofile files land in ~/Library/Developer/Xcode/UserData/Provisioning Profiles/
  # (not ~/Library/MobileDevice/Provisioning Profiles/ which is iOS-only).
  def _resolve_mac_provisioning_profile(mac_bundle_id)
    return unless ENV["MAC_PROVISIONING_PROFILE_PATH"].to_s.strip.empty?

    xcode_profiles_dir  = File.expand_path("~/Library/Developer/Xcode/UserData/Provisioning Profiles")
    mobile_profiles_dir = File.expand_path("~/Library/MobileDevice/Provisioning Profiles")

    # 1. From Match's lane context (most reliable — UUID direct lookup).
    profile_mapping = Actions.lane_context[SharedValues::MATCH_PROVISIONING_PROFILE_MAPPING]
    if profile_mapping&.any?
      profile_uuid = profile_mapping.values.first
      [xcode_profiles_dir, mobile_profiles_dir].each do |dir|
        candidate = File.join(dir, "#{profile_uuid}.provisionprofile")
        if File.exist?(candidate)
          ENV["MAC_PROVISIONING_PROFILE_PATH"] = candidate
          UI.message("🗂  Profile (Match context): #{candidate}")
          return
        end
      end
    end

    # 2. Newest .provisionprofile across both macOS profile directories.
    all_profiles = Dir[File.join(xcode_profiles_dir, "*.provisionprofile")] +
                   Dir[File.join(mobile_profiles_dir, "*.provisionprofile")]
    if all_profiles.any?
      latest = all_profiles.max_by { |f| File.mtime(f) }
      ENV["MAC_PROVISIONING_PROFILE_PATH"] = latest
      UI.message("🗂  Profile (newest): #{latest}")
      return
    end

    UI.important("⚠️  No .provisionprofile found — Mac App Store upload will fail without one.")
  end

  def build_mac_pkg(mac_bundle_id, options = {})
    repo_root    = DEPLOYMENT_REPO_ROOT
    gradlew      = File.join(repo_root, "gradlew")
    identity     = ENV["MAC_SIGNING_IDENTITY"].to_s
    keychain     = ENV["MAC_KEYCHAIN_PATH"].to_s
    profile_path = ENV["MAC_PROVISIONING_PROFILE_PATH"].to_s
    entitlements = File.join(repo_root, "cmp-desktop/mac-app-store.entitlements")

    UI.user_error!("MAC_SIGNING_IDENTITY is not set") if identity.strip.empty?
    UI.message("🔏 Identity:  #{identity}")
    UI.message("🔐 Keychain:  #{keychain.empty? ? '(default search list)' : keychain}")
    UI.message("🗂  Profile:   #{profile_path.empty? ? '(none)' : profile_path}")

    # ── Step 1: Build unsigned .app bundle ────────────────────────────────────────
    # Bypass Compose Desktop's internal signing: ExternalToolRunner calls
    # /usr/bin/security find-certificate via execOperations.exec() with an absolute
    # path — returns 0 bytes on macOS 15 Sequoia regardless of keychain target.
    # Fastlane sh() works fine; we sign manually below.
    #
    # Temporarily unset signing env vars so Gradle sees macSigningId = null
    # → sign.set(false) in build.gradle.kts — no certificate lookup attempted.
    saved_env = {}
    %w[MAC_SIGNING_IDENTITY MAC_KEYCHAIN_PATH MAC_PROVISIONING_PROFILE_PATH].each { |k| saved_env[k] = ENV.delete(k) }

    begin
      UI.message("Building unsigned .app bundle (createReleaseDistributable)...")
      sh(gradlew, "-p", repo_root, ":cmp-desktop:createReleaseDistributable",
         "--no-daemon", "--no-configuration-cache")
    ensure
      saved_env.each { |k, v| ENV[k] = v if v }
    end

    # ── Step 2: Locate .app bundle ────────────────────────────────────────────────
    app_dir  = File.join(repo_root, "cmp-desktop/build/compose/binaries/main-release/app")
    app_path = Dir[File.join(app_dir, "*.app")].first
    UI.user_error!("No .app bundle found in #{app_dir}") unless app_path && File.exist?(app_path)
    UI.message("App bundle: #{app_path}")

    # ── Step 2.5: Patch LSMinimumSystemVersion for arm64-only bundles ─────────────
    # Apple rejects arm64-only apps whose deployment target is < 12.0.
    # The bundled JVM runtime sets this to 10.13 by default; override to 12.0.
    info_plist = File.join(app_path, "Contents", "Info.plist")
    if File.exist?(info_plist)
      sh("/usr/libexec/PlistBuddy -c 'Set :LSMinimumSystemVersion 12.0' #{info_plist.shellescape}",
         log: false)
      UI.message("📋 Set LSMinimumSystemVersion → 12.0")
    end

    # ── Step 3: Embed provisioning profile ────────────────────────────────────────
    unless profile_path.strip.empty?
      embedded = File.join(app_path, "Contents", "embedded.provisionprofile")
      FileUtils.cp(profile_path, embedded)
      UI.message("Embedded provisioning profile → #{embedded}")
    else
      UI.important("⚠️  No provisioning profile — Mac App Store upload will fail.")
    end

    # ── Step 4: Sign .app with codesign (--deep signs all nested content) ────────
    keychain_arg = keychain.strip.empty? ? "" : "--keychain #{keychain.shellescape}"
    sh(
      "codesign --force --deep --sign #{identity.shellescape} #{keychain_arg} " \
      "--entitlements #{entitlements.shellescape} #{app_path.shellescape}",
    )

    # ── Step 4.5: Re-sign JVM runtime helpers with sandbox + inherit ──────────────
    # After --deep, sign jspawnhelper (and any other plain executables in the JVM
    # runtime) with the sandbox+inherit entitlements plist, then re-sign the top-level
    # bundle (without --deep) to rebuild the sealed-resource hash over the updated
    # helpers. Order matters: deep → sign helpers → re-sign bundle → verify.
    jvm_helper_entitlements = File.join(repo_root, "cmp-desktop/jvm-helper.entitlements")
    jvm_runtime = File.join(app_path, "Contents", "runtime")
    if File.exist?(jvm_runtime) && File.exist?(jvm_helper_entitlements)
      helpers = Dir.glob("#{jvm_runtime}/**/*").select { |f| File.file?(f) && File.executable?(f) }
      unless helpers.empty?
        helpers.each do |helper|
          sh(
            "codesign --force --sign #{identity.shellescape} #{keychain_arg} " \
            "--entitlements #{jvm_helper_entitlements.shellescape} #{helper.shellescape}",
            log: false,
          )
        end
        UI.message("🔏 Signed #{helpers.size} JVM runtime helper(s) with sandbox entitlement")
        # Rebuild the bundle seal to include the updated helper signatures.
        sh(
          "codesign --force --sign #{identity.shellescape} #{keychain_arg} " \
          "--entitlements #{entitlements.shellescape} #{app_path.shellescape}",
        )
      end
    end

    sh("codesign --verify --deep --strict #{app_path.shellescape}")
    UI.success("✅ App bundle signed")

    # ── Step 5: Create PKG with productbuild ─────────────────────────────────────
    # productbuild uses the system keychain search list (no --keychain option).
    # Look for "Mac Installer Distribution" / "3rd Party Mac Developer Installer" cert.
    # If none found locally (Match only installs the App Distribution cert; the
    # installer cert comes from secrets on CI), build an unsigned PKG with a warning.
    # On CI the composite action imports both signing + installer certs so this always
    # finds an installer identity there.
    keychain_find_arg = keychain.strip.empty? ? "" : keychain.shellescape
    # Prefer MAC_INSTALLER_IDENTITY set by _setup_mac_signing_keychain_local (SHA1 diff)
    installer_identity = ENV["MAC_INSTALLER_IDENTITY"].to_s.strip
    if installer_identity.empty?
      installer_identity = sh(
        "security find-identity -v -p basic #{keychain_find_arg} 2>/dev/null" \
        " | grep -iE 'installer|3rd party mac' | head -1 | sed 's/.*\"\\(.*\\)\".*/\\1/'",
        log: false,
      ).strip
    end

    pkg_dir  = File.join(repo_root, "cmp-desktop/build/compose/binaries/main-release/pkg")
    FileUtils.mkdir_p(pkg_dir)
    app_name = File.basename(app_path, ".app")
    pkg_path = File.join(pkg_dir, "#{app_name}.pkg")

    if installer_identity.empty?
      UI.important(
        "⚠️  No Mac Installer Distribution cert found in keychain.\n" \
        "     Creating UNSIGNED PKG (local dev only — cannot be uploaded to TestFlight).\n" \
        "     On CI, the composite action provides the installer cert via secrets.",
      )
      sh(
        "productbuild --component #{app_path.shellescape} /Applications #{pkg_path.shellescape}",
      )
    else
      UI.message("🔐 Installer identity: #{installer_identity}")
      sh(
        "productbuild --sign #{installer_identity.shellescape} " \
        "--component #{app_path.shellescape} /Applications #{pkg_path.shellescape}",
      )
    end

    UI.user_error!("PKG not found at #{pkg_path} after productbuild") unless File.exist?(pkg_path)
    UI.success("Built PKG: #{pkg_path}")
    pkg_path
  end
end
