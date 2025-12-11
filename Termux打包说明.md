# Termux 打包说明

## 使用目的
在 Termux（Android）环境下快速复用本机的 JDK 与 Android SDK，完成 APK 构建，无需重新配置容器环境。

## 运行步骤
1. 确认 Termux 已安装依赖：`pkg install openjdk-17 git wget unzip which file libandroid-posix-semaphore`，如需本地 aapt2 可额外 `pkg install aapt android-tools`；若缺少 sdkmanager，可先跳过。
2. 将仓库同步到 Termux：`git clone` 或直接复制本目录，建议放在 `/sdcard` 或 `~/storage/shared`。
3. 在仓库根目录执行前，可按需设置（可放入 `~/.profile` 便于复用）：
   ```bash
   export REPO_OS_OVERRIDE=linux
   export REPO_ARCH_OVERRIDE=arm64
   export ANDROID_SDK_ROOT=/data/data/com.termux/files/home/android-sdk
   export ANDROID_HOME=$ANDROID_SDK_ROOT
   export GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx1536m -Dfile.encoding=UTF-8"
   unset JAVA_TOOL_OPTIONS
   unset _JAVA_OPTIONS
   ```
   确保 `local.properties` 中的 `sdk.dir` 与以上路径一致。
4. 在仓库根目录执行统一入口 `run.sh`，命令均使用 `bash` 调用：
   ```bash
   bash run.sh 1          # 下载/刷新依赖
   bash run.sh 2          # 检查环境 (Termux/JDK/SDK/Gradle + aapt2 本地化)
   bash run.sh 3          # 完整构建 clean assembleDebug 并自动启动安装器（默认行为）
   bash run.sh 4          # 仅启动安装器，安装最新 APK
   bash run.sh 5          # 增量构建 assembleDebug 并自动启动安装器
   ```
   未传入参数时默认为选项 3。
5. 构建成功后，APK/AAB 产物位于 `app/build/outputs/` 对应子目录，选项 3/5 会自动查找最新 APK 并尝试调起系统安装器。

## 行为细节
- 脚本会自动检测 `TERMUX_VERSION` 并打印系统信息。
- 默认将 Gradle 用户目录隔离到仓库内的 `.gradle-mobile`，避免与桌面端缓存混用；如需复用缓存可自定义 `GRADLE_USER_HOME`。每次构建前脚本会清理 `caches/daemon/native`、`~/.android` 与 `~/.cache/gradle`，规避“无法反序列化”“immutable workspace”类报错。
- 自动探测 `JAVA_HOME`（基于 `java` 可执行所在路径）与 `ANDROID_HOME`（默认 `/data/data/com.termux/files/home/android-sdk`）。
- **永远使用项目 Gradle Wrapper**：脚本强制走 `bash gradlew`，即便无法 `chmod`，不再退回系统 Gradle，避免 AGP/Gradle 版本不匹配。
- 执行命令附带 `--console=plain --no-daemon`，并锁定 JVM 堆 `-Xmx1536m`，手机端更稳。
- aapt2 必须是 ARM64：脚本优先检测预置路径 `/data/data/com.termux/files/home/android-tools-arm64/aapt2`，再查找 Termux/SDK 本地 aapt2，自动清理缓存里的 PC 架构 aapt2，并强制 `-Dandroid.aapt2FromMaven=false -Dandroid.aapt2FromMavenOverride=<path>`；如缺失会尝试 `pkg install aapt android-tools`，仍未找到则终止并提示补齐。

## 常见问题
- **提示找不到 AGP / 依赖**：请确保已执行 `sdkmanager --licenses` 并完成网络下载；必要时开启科学上网。
- **Gradle 版本不匹配**：请使用项目自带 Wrapper（AGP 8.x 对应 Gradle 8.x），不要使用系统 Gradle 9.x；缺少 Wrapper 时需先在联网环境生成。
- **权限问题**：共享存储 noexec 时，直接 `bash gradlew` 即可，勿用 `./gradlew`；脚本已自动兼容。
- **aapt2 架构错误 / Syntax error**：检查 `file <aapt2>` 输出必须包含 `ARM` 或 `aarch64`，必要时 `pkg install aapt android-tools`，并确认 `gradle.properties` 的 override 指向手机端 aapt2。
