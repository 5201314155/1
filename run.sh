#!/usr/bin/env bash
set -euo pipefail

log() { printf '[run] %s\n' "$*"; }
warn() { printf '[warn] %s\n' "$*" >&2; }

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

log "脚本所在目录: $PROJECT_DIR"
log "系统信息: $(uname -a)"

detect_env() {
  if [[ -n "${TERMUX_VERSION-}" ]]; then
    log "Termux 版本: $TERMUX_VERSION"
  else
    warn "未检测到 TERMUX_VERSION，按通用 Linux 处理"
  fi

  local java_bin
  java_bin="$(command -v java || true)"
  if [[ -n "$java_bin" ]]; then
    export JAVA_HOME="${JAVA_HOME:-$(dirname "$(dirname "$java_bin")")}" 
    log "JAVA_HOME = $JAVA_HOME"
  else
    warn "未找到 java，可在 Termux 安装 openjdk-17: pkg install openjdk-17"
  fi

  export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
  if [[ -d "$ANDROID_HOME" ]]; then
    log "ANDROID_HOME = $ANDROID_HOME"
  else
    warn "未找到 ANDROID_HOME=$ANDROID_HOME，请先安装/配置 Android SDK"
  fi
}

ensure_gradle() {
  local gradle_cmd="./gradlew"
  if [[ -x "$gradle_cmd" ]]; then
    GRADLE_CMD="$gradle_cmd"
    log "使用项目 gradlew"
  elif [[ -f "$gradle_cmd" ]]; then
    if chmod +x "$gradle_cmd" 2>/dev/null; then
      GRADLE_CMD="$gradle_cmd"
      log "已为 gradlew 赋权"
    else
      GRADLE_CMD="bash gradlew"
      warn "共享存储不可 chmod，改用 bash gradlew 兼容调用"
    fi
  else
    GRADLE_CMD="$(command -v gradle || true)"
    if [[ -z "$GRADLE_CMD" ]]; then
      warn "未找到 gradle/gradlew，请安装 gradle 或拉取 wrapper"
      exit 1
    fi
    log "使用系统 gradle: $GRADLE_CMD"
  fi
}

run_gradle() {
  set +e
  $GRADLE_CMD "$@" --console=plain --no-daemon
  local result=$?
  set -e
  return $result
}

find_latest_apk() {
  local apk_dir="$PROJECT_DIR/app/build/outputs"
  [[ -d "$apk_dir" ]] || return 1

  local latest
  latest=$(find "$apk_dir" -type f -name "*.apk" -print0 2>/dev/null | xargs -0r ls -t | head -n1)
  [[ -n "${latest:-}" ]] || return 1
  printf '%s\n' "$latest"
}

launch_installer() {
  local apk_path="$1"
  [[ -n "$apk_path" ]] || { warn "未提供 APK 路径，无法触发安装器"; return 1; }

  if command -v termux-open >/dev/null 2>&1; then
    log "使用 termux-open 调起安装器: $apk_path"
    termux-open --view "$apk_path" || warn "termux-open 调起失败"
  elif command -v am >/dev/null 2>&1; then
    log "使用 Activity Manager 调起安装器: $apk_path"
    am start -a android.intent.action.VIEW \
      -d "file://$apk_path" \
      -t "application/vnd.android.package-archive" >/dev/null 2>&1 || warn "Activity Manager 打开失败"
  else
    warn "未找到 termux-open 或 am，无法自动打开安装器"
    return 1
  fi
}

auto_install_latest() {
  local apk
  apk="$(find_latest_apk || true)"
  if [[ -n "$apk" ]]; then
    log "检测到最新 APK: $apk"
    launch_installer "$apk" || warn "自动启动安装器失败"
  else
    warn "未找到 app/build/outputs 下的 APK，跳过自动安装"
  fi
}

download_deps() {
  log "开始刷新并下载依赖 ( :app:dependencies )"
  run_gradle --refresh-dependencies :app:dependencies
  local result=$?
  if [[ $result -eq 0 ]]; then
    log "依赖下载完成"
  else
    warn "依赖下载失败 (退出码 $result)"
  fi
  return $result
}

check_environment() {
  detect_env
  ensure_gradle
  log "Gradle 版本信息:"
  run_gradle --version || warn "获取 Gradle 版本信息失败"
}

full_build() {
  ensure_gradle
  log "执行完整构建: clean assembleDebug"
  run_gradle clean assembleDebug
  local result=$?
  if [[ $result -eq 0 ]]; then
    log "完整构建成功，尝试自动安装"
    auto_install_latest
  else
    warn "完整构建失败 (退出码 $result)"
  fi
  return $result
}

incremental_build() {
  ensure_gradle
  log "执行增量构建: assembleDebug"
  run_gradle assembleDebug
  local result=$?
  if [[ $result -eq 0 ]]; then
    log "增量构建成功，尝试自动安装"
    auto_install_latest
  else
    warn "增量构建失败 (退出码 $result)"
  fi
  return $result
}

launch_only() {
  auto_install_latest
}

usage() {
  cat <<'EOF'
用法: bash run.sh [选项]
  1 | deps         下载/刷新依赖
  2 | check        检查环境 (Termux/JDK/SDK/Gradle)
  3 | build        完整构建 (clean assembleDebug) 并自动安装
  4 | install      仅启动安装器，安装最新 APK
  5 | incremental  增量构建 (assembleDebug) 并自动安装

未传入参数时默认执行选项 3。
EOF
}

ACTION="${1:-3}"

case "$ACTION" in
  1|deps)
    detect_env
    ensure_gradle
    download_deps
    ;;
  2|check)
    check_environment
    ;;
  3|build)
    detect_env
    full_build
    ;;
  4|install)
    detect_env
    launch_only
    ;;
  5|incremental)
    detect_env
    incremental_build
    ;;
  -h|--help)
    usage
    ;;
  *)
    usage
    exit 1
    ;;
esac
