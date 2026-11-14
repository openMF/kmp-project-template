#!/usr/bin/env bash
# Functional test for play_aware_version_code! (deployment/_shared/config.rb): runs the REAL method,
# extracted from config.rb, with the Play API, git and the key file stubbed.
#
#   commit count above the live Play maximum   → the commit count (Firebase and Play agree)
#   live Play maximum at or above the count    → live max + 1
#   the highest code sits on closed testing    → it still counts (alpha is read)
#   a track query fails                        → the other tracks still decide
#   no Play service account                    → the commit count
#   VERSION_CODE_OVERRIDE already set          → left as it is
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
# shellcheck source=/dev/null
. "$REPO/scripts/ruby-exec.sh"
TEST_RB="$(mktemp -t version-code-test.XXXXXX)"; trap 'rm -f "$TEST_RB"' EXIT
cat > "$TEST_RB" <<'RUBY'
src = File.read(ARGV[0])
method_src = src[/^def play_aware_version_code!.*?^end$/m] or abort("❌ play_aware_version_code! not found in config.rb")

module UI; def self.message(*) end; def self.important(*) end; end
DEPLOYMENT_REPO_ROOT = Dir.pwd
module FastlaneConfig; module ProjectConfig; def self.android_package_name = "org.example.app"; end; end
Variant = Struct.new(:secrets)
Secrets = Struct.new(:p) { def path(_) = p }

$commits = 0; $tracks = {}; $key = true
def sh(*) = "#{$commits}\n"
def google_play_track_version_codes(track:, **)
  v = $tracks.fetch(track, [])
  raise "boom" if v == :fail
  v
end
$key_body = '{"private_key": "x"}'
class File; class << self
  alias_method :__orig_exist?, :exist?; def exist?(p) = p.end_with?("play.json") ? $key : __orig_exist?(p)
  alias_method :__orig_read, :read;     def read(p, *a) = p.to_s.end_with?("play.json") ? $key_body : __orig_read(p, *a)
end; end
eval(method_src)

$fail = 0
def check(name, want, commits:, tracks: {}, key: true, preset: nil, key_body: '{"private_key": "x"}')
  $commits, $tracks, $key, $key_body = commits, tracks, key, key_body
  ENV["VERSION_CODE_OVERRIDE"] = preset.to_s
  play_aware_version_code!(variant: Variant.new(Secrets.new("play.json")))
  got = ENV["VERSION_CODE_OVERRIDE"].to_i
  if got == want then puts "  ✅ #{name} (#{got})" else puts "  ❌ #{name}: got #{got}, want #{want}"; $fail = 1 end
end

check "commit count above the live Play maximum", 298, commits: 298, tracks: { "production" => [213], "internal" => [213] }
check "live Play maximum at or above the commit count", 214, commits: 120, tracks: { "production" => [213], "beta" => [213] }
check "highest code on closed testing still counts", 507, commits: 10, tracks: { "production" => [213], "alpha" => [506] }
check "a failed track query does not stop the others", 214, commits: 1, tracks: { "beta" => :fail, "internal" => [213] }
check "no Play service account: commit count", 298, commits: 298, key: false
check "a pre-set VERSION_CODE_OVERRIDE wins", 999, commits: 298, tracks: { "production" => [213] }, preset: 999
# The Firebase job has no Play credentials: the resolver hands back the sample file, which has no
# private key. Querying Play with it fails on every track, so it must not be used at all.
check "sample key without a private key: commit count", 298, commits: 298, tracks: { "production" => [500] }, key_body: '{"type": "service_account"}'

# Each lane must still define every local its Play upload passes. Removing the inline version-code
# block once dropped `play_json_key`, and the lane died at upload ("Could not find ... play_json_key").
lane = File.read(ARGV[1])
used = lane.scan(/json_key:\s+(\w+)/).flatten.uniq
undefined = used.reject { |v| lane.match?(/^\s*#{v}\s*=/) }
if undefined.empty? then puts "  ✅ play-internal lane defines every json_key it passes (#{used.join(', ')})"
else puts "  ❌ play-internal lane passes undefined json_key: #{undefined.join(', ')}"; $fail = 1 end
puts($fail.zero? ? "version-code-test: PASS" : "version-code-test: FAIL")
exit $fail
RUBY
ruby_exec "$TEST_RB" "$REPO/deployment/_shared/config.rb" "$REPO/deployment/android/play-internal/lane.rb"
