# 安卓投屏项目

![Build Status](https://github.com/ZtfCoder/projection/workflows/Build%20Android%20APKs/badge.svg)
[![GitHub release](https://img.shields.io/github/v/release/ZtfCoder/projection)](https://github.com/ZtfCoder/projection/releases)

这是一个简单的安卓手机投屏到智能投影仪的应用，支持通过二维码快速连接和高清镜像投屏。

## 项目结构

```
projection/
├── android-client/    # 手机端应用
│   └── app/
│       ├── build.gradle
│       └── src/main/
│           ├── AndroidManifest.xml
│           ├── java/com/screencast/client/
│           │   ├── MainActivity.kt          # 主页面
│           │   ├── ScanActivity.kt         # 二维码扫描页面
│           │   ├── CastActivity.kt         # 投屏控制页面
│           │   └── service/
│           │       └── ScreenCastService.kt # 投屏服务
│           └── res/layout/
│
└── android-tv/       # 投影仪端应用（Android TV）
    └── app/
        ├── build.gradle
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/screencast/tv/
            │   ├── MainActivity.kt              # 主页面（显示二维码）
            │   ├── DisplayActivity.kt          # 全屏播放页面
            │   ├── service/
            │   │   └── VideoReceiverService.kt # 视频接收服务
            │   └── utils/
            │       └── NetworkUtils.kt         # 网络工具类
            └── res/layout/
```

## 功能特性

### 手机端（android-client）
- ✅ 二维码扫描连接
- ✅ 屏幕捕获（MediaProjection）
- ✅ H.264 视频编码
- ✅ UDP 视频流传输
- ✅ 前台服务保持连接

### 投影仪端（android-tv）
- ✅ 二维码生成和显示
- ✅ TCP/UDP 服务器
- ✅ H.264 视频解码
- ✅ 全屏播放
- ✅ Android TV 界面适配

## 技术栈

- **语言**: Kotlin
- **最低 SDK**: Android 5.0 (API 21)
- **目标 SDK**: Android 14 (API 34)
- **核心技术**:
  - MediaProjection API（屏幕捕获）
  - MediaCodec（H.264 编解码）
  - Socket（TCP/UDP 网络传输）
  - ZXing（二维码生成和扫描）
  - Coroutines（异步处理）

## 📥 下载安装

### 方式一：直接下载 APK（推荐）
前往 [Releases 页面](https://github.com/ZtfCoder/projection/releases) 下载最新版本：
- `screencast-client.apk` - 手机端
- `screencast-tv.apk` - 投影仪端

### 方式二：从 GitHub Actions 下载
1. 进入 [Actions 页面](https://github.com/ZtfCoder/projection/actions)
2. 选择最新的成功构建
3. 下载 Artifacts 中的 APK

## 🔨 自己构建

### 前置要求
- Android Studio (推荐最新版本)
- JDK 8 或更高版本
- Android SDK

### 构建步骤

#### 1. 构建手机端应用
```bash
cd android-client
./gradlew assembleDebug
```

生成的 APK 位于：`android-client/app/build/outputs/apk/debug/app-debug.apk`

#### 2. 构建投影仪端应用
```bash
cd android-tv
./gradlew assembleDebug
```

生成的 APK 位于：`android-tv/app/build/outputs/apk/debug/app-debug.apk`

### 安装方法

#### 方式一：通过 Android Studio
1. 打开项目
2. 连接设备
3. 点击 Run 按钮

#### 方式二：通过 ADB
```bash
# 安装手机端
adb install android-client/app/build/outputs/apk/debug/app-debug.apk

# 安装投影仪端
adb install android-tv/app/build/outputs/apk/debug/app-debug.apk
```

#### 方式三：直接安装（推荐）
1. 将 APK 文件传输到设备
2. 在设备上开启"允许安装未知来源应用"
3. 使用文件管理器找到 APK 并安装

## 使用说明

### 投影仪端操作
1. 在投影仪/电视盒子上安装并打开"投屏接收端"应用
2. 应用会自动显示二维码和 IP 地址
3. 确保设备已连接到 WiFi 网络

### 手机端操作
1. 确保手机和投影仪在同一 WiFi 网络下
2. 打开"投屏客户端"应用
3. 点击"扫描二维码连接"
4. 扫描投影仪上显示的二维码
5. 授予屏幕录制权限
6. 点击"开始投屏"

### 停止投屏
- 在手机端点击"停止投屏"按钮
- 或直接关闭应用

## 网络要求

- 手机和投影仪必须连接到同一 WiFi 网络
- 建议使用 5GHz WiFi 以获得更好的性能
- 确保路由器没有启用 AP 隔离功能

## 性能参数

- **分辨率**: 1280x720 (可根据设备调整)
- **帧率**: 30 FPS
- **比特率**: 2 Mbps
- **编码格式**: H.264
- **传输协议**: UDP (视频) + TCP (控制)
- **端口**: 8888 (TCP), 8889 (UDP)

## 权限说明

### 手机端权限
- `INTERNET`: 网络通信
- `CAMERA`: 扫描二维码
- `FOREGROUND_SERVICE`: 前台服务
- `FOREGROUND_SERVICE_MEDIA_PROJECTION`: 屏幕录制服务

### 投影仪端权限
- `INTERNET`: 网络通信
- `ACCESS_NETWORK_STATE`: 获取网络状态
- `ACCESS_WIFI_STATE`: 获取 WiFi 状态

## 故障排除

### 无法扫描二维码
- 检查相机权限是否已授予
- 确保光线充足，二维码清晰可见

### 无法连接
- 确保手机和投影仪在同一 WiFi 网络
- 检查防火墙设置
- 重启应用重试

### 投屏卡顿
- 检查 WiFi 信号强度
- 减少网络中其他设备的占用
- 尝试使用 5GHz WiFi

### 画面不清晰
- 可以在代码中调整 `bitRate` 参数提高比特率
- 调整 `screenWidth` 和 `screenHeight` 提高分辨率

## 自定义配置

### 修改视频质量
编辑 `ScreenCastService.kt`:
```kotlin
private var screenWidth = 1920  // 提高到 1080p
private var screenHeight = 1080
private val bitRate = 5000000   // 提高到 5 Mbps
private val frameRate = 60      // 提高到 60 FPS
```

### 修改端口
编辑 `MainActivity.kt` (TV端):
```kotlin
private val SERVER_PORT = 9999  // 修改为其他端口
```

## 注意事项

⚠️ **本项目仅供学习和个人使用**
- 未实现加密，不建议在公共网络使用
- 未实现身份认证，任何人都可以连接
- 开发阶段使用 debug 签名，生产环境需要正式签名

## 后续优化方向

- [ ] 添加音频投屏支持
- [ ] 实现连接加密
- [ ] 支持多设备同时连接
- [ ] 添加画质自适应调节
- [ ] 实现手势控制
- [ ] 优化延迟和流畅度

## 许可证

本项目采用 MIT 许可证，详见 LICENSE 文件。

## 🤖 自动构建

本项目配置了 GitHub Actions 自动构建：
- **每次推送**：自动构建并上传 APK 到 Artifacts
- **创建 Tag**：自动创建 Release 并发布 APK

详见 [GitHub Actions 说明](GITHUB_ACTIONS.md)

## 联系方式

如有问题或建议，请提交 Issue。
