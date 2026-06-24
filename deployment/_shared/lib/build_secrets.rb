# deployment/_shared/lib/build_secrets.rb
#
# The ONE implementation of the secrets resolver. Reads secrets/LAYOUT.yaml (the
# single source of truth for the secrets layout) and answers, for any secret key:
#   - path(key)          → where the file is / will be (live wins, else sample)
#   - value(key)         → the string value (env-first, file fallback)
#   - exists?(key)       → is it materialized?
#   - materialize!(key)  → decode the env/literal into its on-disk path
#   - application_id     → flavor-aware applicationId (libs.versions.toml#appId + suffix)
#
# Exposed two ways from this one file: a Ruby API (BuildSecrets.for(...)) used by
# config.rb + the Fastlane lanes, and a CLI (the `if __FILE__ == $0` block) used by
# the GitHub Actions pre_fastlane_script via deployment/scripts/build-secrets.
#
# Stdlib-only (yaml/json/fileutils/base64) so it runs on the runner's SYSTEM Ruby,
# BEFORE Fastlane's Bundler is set up (pre_fastlane_script timing).
require "yaml"
require "fileutils"
require "base64"

module BuildSecrets
  # LAYOUT lives at <repo>/secrets/LAYOUT.yaml. This file is at
  # <repo>/deployment/_shared/lib/build_secrets.rb → three dirs up to the repo root.
  REPO_ROOT = File.expand_path("../../..", __dir__)
  LAYOUT    = File.join(REPO_ROOT, "secrets", "LAYOUT.yaml")

  # Product-flavor → applicationId suffix. Mirrors org.convention.AppFlavor.
  FLAVOR_SUFFIX = { "prod" => "", "demo" => ".demo" }.freeze

  def self.for(flavor: :prod, variant: :release)
    Accessor.new(flavor.to_s, variant.to_s)
  end

  class Accessor
    def initialize(flavor, variant)
      @flavor  = flavor
      @variant = variant
      @doc     = YAML.load_file(LAYOUT)
    end

    # Spec for a secret, with any `by_flavor[<flavor>]` override merged on top.
    def spec(key)
      base = @doc.fetch("secrets").fetch(key.to_s) { raise "unknown secret '#{key}'" }
      ov   = base["by_flavor"] && base["by_flavor"][@flavor]
      ov ? base.merge(ov) : base
    end

    # Interpolate {flavor}/{variant} into a relative path.
    def rel(key)
      (spec(key)["rel"] || "").gsub("{flavor}", @flavor).gsub("{variant}", @variant)
    end

    # Candidate on-disk locations, in precedence order. `consume_at` (out-of-tree,
    # e.g. cmp-android/google-services.json) short-circuits the root search.
    def candidate_paths(key)
      s = spec(key)
      if s["consume_at"]
        [s["consume_at"].gsub("{flavor}", @flavor).gsub("{variant}", @variant)]
      else
        @doc.fetch("precedence").map { |root| File.join(@doc["roots"].fetch(root), rel(key)) }
      end
    end

    # Resolved read path: first existing candidate, else the highest-precedence one.
    def path(key)
      candidate_paths(key).find { |p| File.exist?(p) } || candidate_paths(key).last
    end

    def exists?(key)
      candidate_paths(key).any? { |p| File.exist?(p) }
    end

    # String value: env var first (CI), then the on-disk file (local fallback).
    def value(key)
      env = ENV[spec(key)["source_env"].to_s]
      return env unless env.to_s.empty?
      p = path(key)
      File.exist?(p) ? File.read(p).strip : nil
    end

    # Flavor-aware applicationId — feeds the FB-PRE-3 app-id↔package preflight.
    def application_id
      toml = File.join(REPO_ROOT, "gradle", "libs.versions.toml")
      base = File.read(toml)[/^appId\s*=\s*"([^"]+)"/, 1]
      raise "appId not found in #{toml}" if base.nil? || base.empty?
      "#{base}#{FLAVOR_SUFFIX.fetch(@flavor, '')}"
    end

    # Write a secret to its on-disk path. Returns the path, or nil if the source is empty.
    def materialize!(key, from_env: nil)
      s    = spec(key)
      kind = s.fetch("kind")
      dest = materialize_dest(key, s)
      body = materialize_body(key, s, kind, from_env)
      return nil if body.nil? || body.empty?

      FileUtils.mkdir_p(File.dirname(dest))
      # Files keep their bytes verbatim; line-oriented values get a trailing newline.
      File.write(dest, kind == "file" ? body : "#{body.chomp}\n")
      File.chmod(s["mode"].to_i(8), dest) if s["mode"]
      dest
    end

    # Materialize every secret whose source is present (used by `materialize-all`).
    def materialize_all!
      @doc.fetch("secrets").keys.filter_map { |k| materialize!(k) }
    end

    private

    def materialize_dest(key, s)
      live = @doc["roots"].fetch("live")
      return s["consume_at"].gsub("{flavor}", @flavor).gsub("{variant}", @variant) if s["consume_at"]
      return File.join(live, "_env", s.fetch("source_env")) if s["kind"] == "env_var"
      File.join(live, rel(key))
    end

    def materialize_body(_key, s, kind, from_env)
      case kind
      when "literal"
        s["value"]
      when "value", "env_var"
        ENV[s["source_env"].to_s]
      when "file"
        raw = ENV[(from_env || s["source_env"]).to_s]
        return nil if raw.to_s.empty?
        decode_file(raw, s["encoding"])
      else
        raise "unknown kind '#{kind}'"
      end
    end

    # base64 → decode; pem-or-base64 → raw PEM passthrough else decode; plain → verbatim.
    def decode_file(raw, encoding)
      case encoding
      when "pem-or-base64"
        raw.lstrip.start_with?("-----BEGIN") ? raw : Base64.decode64(raw)
      when "base64"
        Base64.decode64(raw)
      else
        raw
      end
    end
  end
end

# ── CLI ─────────────────────────────────────────────────────────────────────
if __FILE__ == $PROGRAM_NAME
  cmd, *rest = ARGV
  # keyless commands (application-id, materialize-all) have no positional <key>;
  # a leading "--flag" means the key was omitted.
  key = (rest.first && !rest.first.start_with?("--")) ? rest.shift : nil
  opts = {}
  rest.each_slice(2) { |flag, val| opts[flag.sub(/\A--/, "")] = val }
  acc = BuildSecrets.for(flavor: (opts["flavor"] || "prod"), variant: (opts["variant"] || "release"))

  case cmd
  when "path"            then puts acc.path(key)
  when "value"           then puts acc.value(key)
  when "application-id"  then puts acc.application_id
  when "exists"          then exit(acc.exists?(key) ? 0 : 1)
  when "materialize"
    dest = acc.materialize!(key, from_env: opts["from-env"])
    warn(dest ? "✓ materialized #{key} → #{dest}" : "· skip #{key} (source empty)")
  when "materialize-all"
    acc.materialize_all!.each { |d| warn "✓ #{d}" }
  else
    abort "usage: build-secrets {path|value|application-id|exists|materialize|materialize-all} " \
          "[key] [--flavor f] [--variant v] [--from-env VAR]"
  end
end
