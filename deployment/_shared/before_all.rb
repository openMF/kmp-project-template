# deployment/_shared/before_all.rb — regenerate the derived store listing before ANY lane.
#
# app-profile/ (app.yaml + platforms/<p>/*.yaml + platforms/<p>/media) is the SINGLE source of truth
# for all store-listing text + media. deployment/**/metadata is DERIVED (customization-surface
# owner:generated, gitignored) — fastlane's `metadata_path` reads that dir, so it MUST be materialized
# from app-profile via `./gradlew syncForkConfig` BEFORE deliver/supply run, or a fresh clone / CI
# deploy would upload EMPTY listings.
#
# Imported by BOTH deployment/Fastfile (CI entry) and deployment/fastlane/Fastfile (local) so it runs
# whichever Fastfile fastlane loads. Runs once per invocation (SYNC_FORK_DONE latch — /idea-deploy
# STEP 1.7.0 already sets it when it pre-syncs). Fail-soft: a gradle hiccup never blocks a deploy.
before_all do
  # Locate repo root (dir holding app-profile/app.yaml) by walking up from cwd — robust to whatever
  # cwd fastlane sets for the loaded Fastfile (deployment/ for CI, repo root for local).
  repo_root = Dir.pwd
  20.times do
    break if File.exist?(File.join(repo_root, "app-profile", "app.yaml"))
    parent = File.dirname(repo_root)
    break if parent == repo_root
    repo_root = parent
  end

  if ENV["SYNC_FORK_DONE"].nil? && File.exist?(File.join(repo_root, "app-profile", "app.yaml"))
    UI.message("app-profile → deployment/**/metadata: materializing store listing via ./gradlew syncForkConfig…")
    begin
      Dir.chdir(repo_root) { sh("./gradlew", "-q", "syncForkConfig") }
    rescue => e
      UI.important("syncForkConfig failed (#{e.message}) — deployment/metadata may be stale; using existing files")
    end
    ENV["SYNC_FORK_DONE"] = "1"
  end
end
