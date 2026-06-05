# deployment/_shared/lib/version_helpers.rb
# Shared version-generation helpers extracted from legacy fastlane/Fastfile.
# Consumed by every lane.rb that needs to compute VERSION / VERSION_CODE.

module VersionHelpers
  module_function

  # Read the gradle-generated version.txt (produced by `./gradlew versionFile`).
  # Falls back to "1.0.0" when the file is missing.
  def gradle_version
    File.read("../version.txt").strip
  rescue StandardError
    "1.0.0"
  end

  # Sanitize a gradle semver (e.g. "2026.1.1-beta.0.9+abc123") to App-Store
  # compatible MAJOR.MINOR.PATCH form (e.g. "2026.1.9"). The final integer is
  # the commit-count extracted from the pre-release suffix.
  def appstore_sanitize(full_version)
    base = full_version.split("-")[0].split("+")[0]
    parts = base.split(".")
    commit_count = "0"
    if full_version.include?("-")
      pre = full_version.split("-")[1].split("+")[0]
      commit_count = pre.split(".").last || "0"
    end
    return full_version unless parts.length >= 2
    "#{parts[0]}.#{parts[1]}.#{commit_count}"
  end
end
