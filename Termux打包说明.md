# Termux 打包说明

## 使用目的
在 Termux（Android）环境下快速复用本机的 JDK 与 Android SDK，完成 APK 构建，无需重新配置容器环境。

## 运行步骤
1. 确认 Termux 已安装依赖：`pkg install openjdk-17 gradle git`，并通过官方渠道安装 Android SDK（默认路径 `$HOME/Android/Sdk`）。
2. 将仓库同步到 Termux：`git clone` 或直接复制本目录。
3. 在仓库根目录执行统一入口 `run.sh`，命令均使用 `bash` 调用：
   ```bash
   bash run.sh 1          # 下载/刷新依赖
   bash run.sh 2          # 检查环境 (Termux/JDK/SDK/Gradle + aapt2 本地化)
   bash run.sh 3          # 完整构建 clean assembleDebug 并自动启动安装器（默认行为）
   bash run.sh 4          # 仅启动安装器，安装最新 APK
   bash run.sh 5          # 增量构建 assembleDebug 并自动启动安装器
   ```
   未传入参数时默认为选项 3。
4. 构建成功后，APK/AAB 产物位于 `app/build/outputs/` 对应子目录，选项 3/5 会自动查找最新 APK 并尝试调起系统安装器。

## 行为细节
- 脚本会自动检测 `TERMUX_VERSION` 并打印系统信息。
- 默认将 Gradle 用户目录隔离到仓库内的 `.gradle-mobile`，避免与桌面端缓存混用；也可通过设置 `GRADLE_USER_HOME` 指向其他路径复用已有缓存。
- 自动探测 `JAVA_HOME`（基于 `java` 可执行所在路径）与 `ANDROID_HOME`（默认为 `$HOME/Android/Sdk`）。
- 优先使用项目内 `gradlew`；若仓库存放在共享存储导致无法 `chmod +x`，脚本会自动改用 `bash gradlew` 调用；如不存在 wrapper 则退回 Termux 全局 `gradle`。
- 执行命令附带 `--console=plain --no-daemon` 以便在移动端终端查看全量日志。
- aapt2 默认优先使用本机版本：脚本会依次查找 Termux 包路径（含 `android-tools`/`aapt` 安装的 `build-tools/*/aapt2`）、PATH 下自带的 aapt2、以及 ANDROID_HOME 的 `build-tools/*/aapt2`，并通过 `-Dandroid.aapt2FromMaven=false -Dandroid.aapt2FromMavenOverride=<path>` 强制走本地二进制，避免下载 PC 架构的 aapt2 在手机端报错；若在隔离的 `.gradle-mobile` 缓存中发现 PC 架构的 aapt2，会整体删除对应 transform 目录后再构建；若仍未找到匹配架构，脚本会终止并提示先用 `pkg install aapt` 安装。

## 常见问题
- **提示找不到 AGP / 依赖**：请确保已执行 `sdkmanager --licenses` 并完成网络下载；必要时开启科学上网。
- **Gradle 版本不匹配**：如需强制指定版本，可在 Termux 全局 `~/.gradle/gradle.properties` 调整，或在仓库内升级 Gradle Wrapper 后重试。
- **权限问题**：首次执行会自动为 `gradlew` 赋权，若因共享目录限制无法修改，可直接依赖脚本的 `bash gradlew` 兜底或将仓库迁移至可执行挂载后再 `chmod +x gradlew`。
