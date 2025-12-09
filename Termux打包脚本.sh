#!/usr/bin/env bash
set -euo pipefail

log() { printf '[打包] %s\n' "$*"; }
warn() { printf '[警告] %s\n' "$*" >&2; }

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

log "工程目录：$PROJECT_DIR"
log "系统信息：$(uname -a)"

if [[ -n "${TERMUX_VERSION-}" ]]; then
  log "检测到 Termux 环境，版本：$TERMUX_VERSION"
else
  warn "未检测到 TERMUX_VERSION，将按通用 Linux 环境执行。"
fi

JAVA_BIN="$(command -v java || true)"
if [[ -n "$JAVA_BIN" ]]; then
  export JAVA_HOME="${JAVA_HOME:-$(dirname "$(dirname "$JAVA_BIN")")}" 
  log "JAVA_HOME = $JAVA_HOME"
else
  warn "未找到 java，请在 Termux 中安装 openjdk-17：pkg install openjdk-17"
fi

export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
if [[ ! -d "$ANDROID_HOME" ]]; then
  warn "未找到 ANDROID_HOME=$ANDROID_HOME，请确认已安装 Android SDK 或手动设置环境变量。"
else
  log "ANDROID_HOME = $ANDROID_HOME"
fi

GRADLE_CMD="./gradlew"
if [[ -x "$GRADLE_CMD" ]]; then
  log "使用项目内 gradlew。"
else
  if [[ -f "$GRADLE_CMD" ]]; then
    chmod +x "$GRADLE_CMD"
    log "已赋予 gradlew 可执行权限。"
  else
    GRADLE_CMD="$(command -v gradle || true)"
    if [[ -z "$GRADLE_CMD" ]]; then
      warn "未找到 gradle/gradlew，请在 Termux 中安装 gradle：pkg install gradle"
      exit 1
    else
      log "使用全局 gradle：$GRADLE_CMD"
    fi
  fi
fi

TASK="${1:-assembleDebug}"
log "准备执行：$GRADLE_CMD $TASK --console=plain --no-daemon"

set +e
$GRADLE_CMD "$TASK" --console=plain --no-daemon
RESULT=$?
set -e

if [[ $RESULT -eq 0 ]]; then
  log "构建成功，APK 位置可在 app/build/outputs/ 中查找。"
else
  warn "构建失败，退出码：$RESULT。请检查日志，或在网络可用的环境下重试。"
fi

exit $RESULT
