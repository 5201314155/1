#!/usr/bin/env bash
set -euo pipefail

log() { printf '[run-build] %s\n' "$*"; }
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

if [[ $RESULT -eq 0 ]]; then
  log "Done. Check app/build/outputs/ for APK/Bundle artifacts."
else
  warn "Task failed (exit code: $RESULT). Review logs or retry with network access."
fi

exit $RESULT
