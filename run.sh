#!/usr/bin/env bash
set -euo pipefail

log() { printf '[run] %s\n' "$*"; }
warn() { printf '[warn] %s\n' "$*" >&2; }

GRADLE_ARGS=()

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
  $GRADLE_CMD "${GRADLE_ARGS[@]}" "$@" --console=plain --no-daemon
  local result=$?
  set -e
  return $result
}

prepare_aapt2() {
  local arch="$(uname -m 2>/dev/null || echo unknown)"
  local candidate=""
  local cache_purged="false"

  purge_incompatible_aapt2() {
    local cache_root="$HOME/.gradle/caches"
    [[ -d "$cache_root" ]] || return
    local stale=()
    while IFS= read -r path; do
      if [[ -n "$path" ]]; then
        stale+=("$path")
      fi
    done < <(find "$cache_root" -type f -name aapt2 2>/dev/null | while read -r bin; do
      local info
      info=$(file "$bin" 2>/dev/null || true)
      if [[ -n "$info" && "$info" != *"$arch"* ]]; then
        printf '%s\n' "$bin"
      fi
    done)

    if [[ ${#stale[@]} -gt 0 ]]; then
      log "发现与当前 $arch 不匹配的 aapt2 缓存，清理以避免调用 PC 版本"
      for s in "${stale[@]}"; do
        rm -f "$s" || true
      done
      cache_purged="true"
    fi
  }

  purge_incompatible_aapt2

  # 1) 优先使用 Termux 包中的 aapt2（如安装了 termux/apt 包 android-tools 或 aapt2）
  if [[ -z "$candidate" && -n "${PREFIX-}" ]]; then
    local termux_aapt2
    termux_aapt2=$(find "$PREFIX" -path "*/build-tools/*/aapt2" -type f 2>/dev/null | sort -Vr | head -n1)
    if [[ -n "$termux_aapt2" ]]; then
      candidate="$termux_aapt2"
      log "检测到 Termux 包含的 aapt2: $candidate"
    fi
  fi

  # 2) 其次使用 PATH 中的 aapt2（若 termux-openjdk 或用户手动编译提供）
  if [[ -z "$candidate" ]] && command -v aapt2 >/dev/null 2>&1; then
    candidate="$(command -v aapt2)"
    log "检测到 Termux/系统 aapt2: $candidate"
  fi

  # 3) 再尝试 ANDROID_HOME 下的 build-tools（用户自行同步到手机端的 aarch64 构建）
  if [[ -z "$candidate" && -d "$ANDROID_HOME/build-tools" ]]; then
    candidate=$(find "$ANDROID_HOME/build-tools" -maxdepth 2 -type f -name aapt2 | sort -Vr | head -n1)
    [[ -n "$candidate" ]] && log "使用 SDK build-tools 中的 aapt2: $candidate"
  fi

  if [[ -z "$candidate" ]]; then
    warn "未找到本地 aapt2，可通过 pkg install aapt/android-tools 或同步 aarch64 build-tools 后重试"
    warn "为避免继续拉取 PC 版 aapt2，将终止构建"
    exit 1
  fi

  if [[ ! -x "$candidate" ]]; then
    if chmod +x "$candidate" 2>/dev/null; then
      log "已为 aapt2 赋权"
    else
      warn "aapt2 无法赋权，可能受共享存储限制"
    fi
  fi

  local file_info
  file_info=$(file "$candidate" 2>/dev/null || true)
  if [[ -n "$file_info" && "$file_info" != *"$arch"* ]]; then
    warn "aapt2 架构与当前 $arch 可能不匹配: $file_info"
  fi

  if [[ "$cache_purged" == "true" ]]; then
    warn "已清理 PC 架构 aapt2 缓存，若仍失败请在 Termux 安装 aapt2: pkg install aapt"
  fi

  GRADLE_ARGS+=("-Dandroid.aapt2FromMaven=false" "-Dandroid.aapt2FromMavenOverride=$candidate")
  log "已启用本地 aapt2 覆盖，避免下载 PC 版本 aapt2"
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
  prepare_aapt2
  log "Gradle 版本信息:"
  run_gradle --version || warn "获取 Gradle 版本信息失败"
}

full_build() {
  ensure_gradle
  prepare_aapt2
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
  prepare_aapt2
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
