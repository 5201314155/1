#!/usr/bin/env bash
set -euo pipefail

log() { printf '[运行打包] %s\n' "$*"; }
warn() { printf '[警告] %s\n' "$*" >&2; }

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

TERMUX_SCRIPT="$PROJECT_DIR/Termux打包脚本.sh"
if [[ ! -f "$TERMUX_SCRIPT" ]]; then
  warn "未找到 Termux 打包脚本：$TERMUX_SCRIPT"
  warn "请确认仓库完整或从最新分支同步后重试。"
  exit 1
fi

TASK="${1:-assembleDebug}"
log "当前目录：$PROJECT_DIR"
log "准备通过 Termux 脚本执行任务：$TASK"

set +e
bash "$TERMUX_SCRIPT" "$TASK"
RESULT=$?
set -e

if [[ $RESULT -eq 0 ]]; then
  log "任务完成。可在 app/build/outputs/ 中查看 APK/Bundle 输出。"
else
  warn "任务失败（退出码：$RESULT）。请检查日志或在可联网环境重试。"
fi

exit $RESULT
