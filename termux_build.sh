#!/usr/bin/env bash
set -euo pipefail

log() { printf '[build] %s\n' "$*"; }
warn() { printf '[warn] %s\n' "$*" >&2; }

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

log "Project dir: $PROJECT_DIR"
log "System info: $(uname -a)"

if [[ -n "${TERMUX_VERSION-}" ]]; then
  log "Termux detected: $TERMUX_VERSION"
else
  warn "TERMUX_VERSION not found; running in generic Linux mode."
fi

JAVA_BIN="$(command -v java || true)"
if [[ -n "$JAVA_BIN" ]]; then
  export JAVA_HOME="${JAVA_HOME:-$(dirname "$(dirname "$JAVA_BIN")")}" 
  log "JAVA_HOME = $JAVA_HOME"
else
  warn "java not found. Install openjdk-17 in Termux: pkg install openjdk-17"
fi

export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
if [[ ! -d "$ANDROID_HOME" ]]; then
  warn "ANDROID_HOME=$ANDROID_HOME not found; install Android SDK or set the variable."
else
  log "ANDROID_HOME = $ANDROID_HOME"
fi

gradle_cmd="./gradlew"
if [[ -x "$gradle_cmd" ]]; then
  log "Using project gradlew."
elif [[ -f "$gradle_cmd" ]]; then
  if chmod +x "$gradle_cmd" 2>/dev/null; then
    log "Granted execute permission to gradlew."
  else
    warn "Unable to change gradlew permission (likely shared storage). Falling back to bash gradlew."
    gradle_cmd="bash gradlew"
  fi
else
  gradle_cmd="$(command -v gradle || true)"
  if [[ -z "$gradle_cmd" ]]; then
    warn "gradle/gradlew not found. Install gradle in Termux: pkg install gradle"
    exit 1
  else
    log "Using global gradle: $gradle_cmd"
  fi
fi

TASK="${1:-assembleDebug}"
log "Running: $gradle_cmd $TASK --console=plain --no-daemon"

set +e
$gradle_cmd "$TASK" --console=plain --no-daemon
RESULT=$?
set -e

if [[ $RESULT -eq 0 ]]; then
  log "Build succeeded. Find outputs under app/build/outputs/."
else
  warn "Build failed with exit code $RESULT. Check logs or retry with network access."
fi

exit $RESULT
