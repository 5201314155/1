# Termux 打包说明

## 使用目的
在 Termux（Android）环境下快速复用本机的 JDK 与 Android SDK，完成 APK 构建，无需重新配置容器环境。

## 运行步骤
1. 确认 Termux 已安装依赖：`pkg install openjdk-17 gradle git`，并通过官方渠道安装 Android SDK（默认路径 `$HOME/Android/Sdk`）。
2. 将仓库同步到 Termux：`git clone` 或直接复制本目录。
3. 在仓库根目录执行脚本（可指定任务，默认 `assembleDebug`）：
   ```bash
   bash termux_build.sh             # 默认打 Debug 包
   bash termux_build.sh bundleRelease  # 生成 AAB
   ```
   如需一键使用预设任务，可直接运行根目录的 `run.sh`，它会用 bash 调用 Termux 脚本并在成功后尝试拉起安装器：
   ```bash
   bash run.sh             # 等同 assembleDebug
   bash run.sh assembleRelease  # 生成正式签名前的 Release 包
   ```

> 兼容提示：旧的中文脚本名已移除，如需兼容历史调用，可使用仓库内保留的 `legacy_run_build.sh`，其内部会转发到新的 `run.sh`。
4. 构建成功后，APK/AAB 产物位于 `app/build/outputs/` 对应子目录。

## 行为细节
- 脚本会自动检测 `TERMUX_VERSION` 并打印系统信息。
- 自动探测 `JAVA_HOME`（基于 `java` 可执行所在路径）与 `ANDROID_HOME`（默认为 `$HOME/Android/Sdk`）。
- 优先使用项目内 `gradlew`；若仓库存放在共享存储导致无法 `chmod +x`，脚本会自动改用 `bash gradlew` 调用；如不存在 wrapper 则退回 Termux 全局 `gradle`。
- 执行命令附带 `--console=plain --no-daemon` 以便在移动端终端查看全量日志。

## 常见问题
- **提示找不到 AGP / 依赖**：请确保已执行 `sdkmanager --licenses` 并完成网络下载；必要时开启科学上网。
- **Gradle 版本不匹配**：如需强制指定版本，可在 Termux 全局 `~/.gradle/gradle.properties` 调整，或在仓库内升级 Gradle Wrapper 后重试。
- **权限问题**：首次执行会自动为 `gradlew` 赋权，若因共享目录限制无法修改，可直接依赖脚本的 `bash gradlew` 兜底或将仓库迁移至可执行挂载后再 `chmod +x gradlew`。
