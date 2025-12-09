# Termux 打包说明

## 使用目的
在 Termux（Android）环境下快速复用本机的 JDK 与 Android SDK，完成 APK 构建，无需重新配置容器环境。

## 运行步骤
1. 确认 Termux 已安装依赖：`pkg install openjdk-17 gradle git`，并通过官方渠道安装 Android SDK（默认路径 `$HOME/Android/Sdk`）。
2. 将仓库同步到 Termux：`git clone` 或直接复制本目录。
3. 在仓库根目录执行脚本（可指定任务，默认 `assembleDebug`）：
   ```bash
   ./Termux打包脚本.sh            # 默认打 Debug 包
   ./Termux打包脚本.sh bundleRelease  # 生成 AAB
   ```
4. 构建成功后，APK/AAB 产物位于 `app/build/outputs/` 对应子目录。

## 行为细节
- 脚本会自动检测 `TERMUX_VERSION` 并打印系统信息。
- 自动探测 `JAVA_HOME`（基于 `java` 可执行所在路径）与 `ANDROID_HOME`（默认为 `$HOME/Android/Sdk`）。
- 优先使用项目内 `gradlew`，如不存在则退回 Termux 全局 `gradle`。
- 执行命令附带 `--console=plain --no-daemon` 以便在移动端终端查看全量日志。

## 常见问题
- **提示找不到 AGP / 依赖**：请确保已执行 `sdkmanager --licenses` 并完成网络下载；必要时开启科学上网。
- **Gradle 版本不匹配**：如需强制指定版本，可在 Termux 全局 `~/.gradle/gradle.properties` 调整，或在仓库内升级 Gradle Wrapper 后重试。
- **权限问题**：首次执行会自动为 `gradlew` 赋予可执行权限，若仍提示拒绝，请手动 `chmod +x gradlew`。
