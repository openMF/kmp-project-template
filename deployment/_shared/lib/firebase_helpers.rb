# deployment/_shared/lib/firebase_helpers.rb
# Shared Firebase App Distribution helpers extracted from legacy fastlane/Fastfile.
# Consumed by:
#   - deployment/android/firebase/lane.rb (prod + demo flavors)
#   - deployment/ios/firebase/lane.rb
#
# Each consumer require_relative-s this file; the module functions are pure
# helpers so they can be mixed into either :android or :ios platform scope.
require "json"

module FirebaseHelpers
  module_function

  # Resolve canonical tester groups.
  # Precedence:
  #   1. options[:tester_groups] (workflow input)
  #   2. ENV["FIREBASE_GROUPS"] (CI override — historical workaround)
  #   3. firebase_config[:groups] (fastlane-config default)
  def resolve_groups(options, firebase_config)
    options[:tester_groups] ||
      ENV["FIREBASE_GROUPS"] ||
      firebase_config[:groups]
  end

  # Path to the Firebase service-account JSON. Materialized by /secrets pull
  # OR by manual base64-decode step in workflow-snippet.yml.
  def service_credentials_path(firebase_config)
    firebase_config[:serviceCredsFile] ||
      ENV["FIREBASE_SERVICE_ACCOUNT_PATH"] ||
      "secrets/android/firebaseAppDistributionServiceCredentialsFile.json"
  end

  # ── Pre-build validation ────────────────────────────────────────────────────
  # Runs BEFORE the (~6-min) Gradle build so config drift fails in seconds — the
  # Firebase analogue of the live preflight the Play lane does
  # (google_play_track_version_codes) and the iOS lane does
  # (latest_testflight_build_number). Deterministic + offline by design (no flaky
  # network call in the gate).
  #
  # Fail-fast checks (all UI.user_error!):
  #   FB-PRE-1  service-account JSON exists, parses, carries client_email + project_id
  #   FB-PRE-2  app id present + well-formed  (1:<project-number>:android:<hex>)
  #   FB-PRE-3  app id matches the google-services.json entry for the build's
  #             applicationId — catches "APK package X does not match your Firebase
  #             app's package Y" BEFORE building (the 2026-06-24 incident: a stale
  #             FIREBASE_ANDROID_APP_ID pointed at the old cmp.android.app app).
  def preflight!(firebase_config, expected_application_id:, google_services_path: nil)
    app_id  = firebase_config[:appId].to_s.strip
    sa_path = service_credentials_path(firebase_config)

    # FB-PRE-1 — service account is real
    unless File.exist?(sa_path)
      UI.user_error!("🔥 Firebase preflight (FB-PRE-1): service-account not found at #{sa_path}. " \
        "Materialize it via `/secrets pull` or the workflow's FIREBASE_CREDS decode step.")
    end
    sa = begin
      JSON.parse(File.read(sa_path))
    rescue => e
      UI.user_error!("🔥 Firebase preflight (FB-PRE-1): service-account at #{sa_path} is not valid JSON (#{e.message}).")
    end
    missing = %w[client_email project_id].select { |k| sa[k].to_s.strip.empty? }
    unless missing.empty?
      UI.user_error!("🔥 Firebase preflight (FB-PRE-1): service-account is missing #{missing.join(', ')} — wrong file or truncated secret.")
    end

    # FB-PRE-2 — app id is well-formed
    UI.user_error!("🔥 Firebase preflight (FB-PRE-2): Firebase app id is empty (set FIREBASE_ANDROID_APP_ID / *_DEMO_*).") if app_id.empty?
    unless app_id =~ /\A1:\d+:android:[0-9a-f]+\z/
      UI.user_error!("🔥 Firebase preflight (FB-PRE-2): app id '#{app_id}' is malformed — expected 1:<project-number>:android:<hex>.")
    end

    # FB-PRE-3 — app id ↔ package coherence (the 2026-06-24 mismatch class)
    gs_path = google_services_path || default_google_services_path
    if gs_path && File.exist?(gs_path)
      gs      = (JSON.parse(File.read(gs_path)) rescue nil)
      clients = gs.is_a?(Hash) ? gs["client"] : nil
      if clients.is_a?(Array)
        entry = clients.find { |c| c.dig("client_info", "android_client_info", "package_name") == expected_application_id }
        if entry.nil?
          UI.important("⚠️ Firebase preflight (FB-PRE-3): #{File.basename(gs_path)} has no entry for applicationId " \
            "'#{expected_application_id}' — skipping app-id↔package cross-check (regenerate google-services.json from Firebase).")
        else
          registered = entry.dig("client_info", "mobilesdk_app_id").to_s
          if registered != app_id
            UI.user_error!(
              "🔥 Firebase preflight (FB-PRE-3): app-id MISMATCH for package '#{expected_application_id}'.\n" \
              "     FIREBASE app id (used for upload) : #{app_id}\n" \
              "     google-services.json registered   : #{registered}\n" \
              "   The APK package would not match the Firebase app and the upload would be rejected\n" \
              "   ('package name does not match your Firebase app'). Point FIREBASE_ANDROID_APP_ID\n" \
              "   (vault alias: mifos-x-kmp-project-template-firebase-android-app-id) at #{registered}, or\n" \
              "   refresh google-services.json from the Firebase console.")
          end
        end
      end
    else
      UI.important("⚠️ Firebase preflight (FB-PRE-3): google-services.json not found — skipping app-id↔package cross-check.")
    end

    UI.success("✅ Firebase preflight OK — app #{app_id} ↔ #{expected_application_id} (SA: #{sa['client_email']}).")
  end

  # Best-effort location of the google-services.json the build embeds.
  def default_google_services_path
    roots = []
    roots << ::DEPLOYMENT_REPO_ROOT if defined?(::DEPLOYMENT_REPO_ROOT)
    roots << File.expand_path("..", Dir.pwd)   # fastlane/ → repo root
    roots << Dir.pwd
    roots.compact.map(&:to_s).uniq.each do |r|
      ["cmp-android/google-services.json", "secrets/android/firebase/google-services.json"].each do |rel|
        candidate = File.join(r, rel)
        return candidate if File.exist?(candidate)
      end
    end
    nil
  end
end
