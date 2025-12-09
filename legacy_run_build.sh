#!/usr/bin/env bash
set -euo pipefail

# Legacy wrapper kept for backward compatibility; please use run.sh instead.
log() { printf '[run-legacy] %s\n' "$*"; }
warn() { printf '[warn] %s\n' "$*" >&2; }

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

RUN_SCRIPT="$PROJECT_DIR/run.sh"
if [[ ! -f "$RUN_SCRIPT" ]]; then
  warn "Wrapper target not found: $RUN_SCRIPT"
  exit 1
fi

log "Forwarding arguments to run.sh"
bash "$RUN_SCRIPT" "$@"
