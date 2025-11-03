# 快速开始指南

## 1. 使用 Android Studio 打开项目（推荐）

### 打开投影仪端项目
1. 启动 Android Studio
2. 选择 `File` -> `Open`
3. 选择 `android-tv` 文件夹
4. 等待 Gradle 同步完成
5. 连接 Android TV 设备或启动模拟器
6. 点击 Run 按钮（绿色三角形）

### 打开手机端项目
1. 启动 Android Studio（或新开一个窗口）
2. 选择 `File` -> `Open`
3. 选择 `android-client` 文件夹
4. 等待 Gradle 同步完成
5. 连接 Android 手机或启动模拟器
6. 点击 Run 按钮

## 2. 使用命令行构建

```bash
# 进入项目目录
cd /Users/zhangtengfei/Desktop/code/projection

# 构建 TV 端
cd android-tv
chmod +x gradlew  # macOS/Linux 需要添加执行权限
./gradlew assembleDebug

# 构建手机端
cd ../android-client
chmod +x gradlew
./gradlew assembleDebug
```

## 3. 安装到设备

```bash
# 确保设备已通过 USB 连接并开启 USB 调试
adb devices

# 安装 TV 端（到电视盒子/投影仪）
adb install android-tv/app/build/outputs/apk/debug/app-debug.apk

# 安装手机端
adb install android-client/app/build/outputs/apk/debug/app-debug.apk
```

## 4. 开始使用

1. 在投影仪上打开"投屏接收端"
2. 记下显示的二维码
3. 在手机上打开"投屏客户端"
4. 扫描二维码
5. 授予权限
6. 开始投屏

## 常见问题

### Gradle 同步失败
- 检查网络连接
- 尝试使用国内镜像源
- 确保安装了正确版本的 JDK

### 无法运行到设备
- 确保 USB 调试已开启
- 检查驱动程序
- 尝试更换 USB 数据线

### 编译错误
- 清理项目：`./gradlew clean`
- 删除 `.gradle` 和 `build` 文件夹
- 重新同步项目
