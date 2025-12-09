#!/usr/bin/env bash
set -euo pipefail

log() { printf '[run] %s\n' "$*"; }
warn() { printf '[warn] %s\n' "$*" >&2; }

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

TERMUX_SCRIPT="$PROJECT_DIR/termux_build.sh"
if [[ ! -f "$TERMUX_SCRIPT" ]]; then
  warn "Termux build script not found: $TERMUX_SCRIPT"
  warn "Please sync the repository and retry."
  exit 1
fi

TASK="${1:-assembleDebug}"
log "Working directory: $PROJECT_DIR"
log "Executing task via Termux script: $TASK"

set +e
bash "$TERMUX_SCRIPT" "$TASK"
RESULT=$?
set -e

find_latest_apk() {
  local apk_dir="$PROJECT_DIR/app/build/outputs"
  if [[ ! -d "$apk_dir" ]]; then
    return 1
  fi

  local latest
  latest=$(find "$apk_dir" -type f -name "*.apk" -print0 2>/dev/null | xargs -0r ls -t | head -n1)
  if [[ -z "${latest:-}" ]]; then
    return 1
  fi

  printf '%s\n' "$latest"
}

launch_installer() {
  local apk_path="$1"
  if [[ -z "$apk_path" ]]; then
    warn "No APK path provided to installer trigger."
    return 1
  fi

  if command -v termux-open >/dev/null 2>&1; then
    log "Launching installer via termux-open: $apk_path"
    termux-open --view "$apk_path" || warn "termux-open failed to trigger installer."
  elif command -v am >/dev/null 2>&1; then
    log "Launching installer via Activity Manager: $apk_path"
    am start -a android.intent.action.VIEW \
      -d "file://$apk_path" \
      -t "application/vnd.android.package-archive" >/dev/null 2>&1 || warn "Activity Manager failed to open installer."
  else
    warn "Installer trigger not available (termux-open/am missing)."
    return 1
  fi
}

if [[ $RESULT -eq 0 ]]; then
  log "Build succeeded. Searching for APK to install..."
  APK_PATH="$(find_latest_apk || true)"
  if [[ -n "$APK_PATH" ]]; then
    log "Latest APK: $APK_PATH"
    launch_installer "$APK_PATH" || warn "Unable to auto-launch installer."
  else
    warn "No APK artifact found under app/build/outputs/. Skipping installer launch."
  fi
else
  warn "Task failed (exit code: $RESULT). Review logs or retry with network access."
fi

exit $RESULT
