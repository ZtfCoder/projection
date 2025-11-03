# 安卓手机投屏到智能投影仪设计文档

## 项目概述

本项目旨在开发一个简单的安卓手机投屏软件，支持手机屏幕镜像到智能投影仪上。用户通过扫描二维码快速连接，实现清晰的全屏投屏。项目分为两个主要组件：安卓手机客户端（android-client）和安卓TV投影仪端（android-tv）。

### 主要功能
- **二维码连接**：投影仪端生成二维码，手机端扫描连接
- **屏幕镜像**：实时镜像手机屏幕到投影仪
- **清晰投屏**：支持高清分辨率，全屏播放
- **稳定连接**：基于WiFi的稳定网络连接

### 设计原则
- **简单性**：代码结构简单，易于维护
- **性能优先**：确保投屏流畅清晰
- **无安全性考虑**：专注于功能实现，不涉及加密或认证

## 系统架构

### 整体架构
```
手机端 (Android Client) <--- WiFi ---> 投影仪端 (Android TV)
     |                                       |
     | 屏幕捕获 (MediaProjection)            | 屏幕显示 (SurfaceView)
     |                                       |
     | 视频编码 (H.264)                     | 视频解码 (H.264)
     |                                       |
     | 网络传输 (TCP/UDP)                   | 网络接收 (TCP/UDP)
```

### 组件说明
1. **手机端 (android-client)**
   - 扫描二维码获取投影仪IP
   - 捕获屏幕内容
   - 编码并传输视频流

2. **投影仪端 (android-tv)**
   - 生成并显示二维码
   - 接收视频流
   - 解码并全屏显示

## 技术栈

### 核心技术
- **开发语言**：Java/Kotlin
- **平台**：Android SDK (API 21+)
- **屏幕捕获**：MediaProjection API
- **视频编解码**：MediaCodec (H.264)
- **网络通信**：Socket (TCP/UDP)
- **二维码**：ZXing库

### 第三方库
- ZXing：二维码生成和扫描
- OkHttp：网络请求（可选，用于初始连接）

## 功能模块设计

### 1. 连接模块
- **投影仪端**：
  - 获取本机IP地址
  - 生成包含IP和端口的二维码
  - 启动服务器监听连接

- **手机端**：
  - 相机权限申请
  - 扫描二维码解析连接信息
  - 建立Socket连接

### 2. 投屏模块
- **屏幕捕获**：
  - 使用MediaProjectionManager获取屏幕权限
  - 创建VirtualDisplay捕获屏幕
  - 实时获取屏幕图像数据

- **视频编码**：
  - 使用MediaCodec配置H.264编码器
  - 设置编码参数（分辨率、帧率、比特率）
  - 编码屏幕数据为视频流

- **网络传输**：
  - 建立TCP连接传输控制信息
  - 使用UDP传输视频数据（低延迟）
  - 处理网络异常和重连

- **视频解码与显示**：
  - 接收视频数据包
  - 使用MediaCodec解码H.264流
  - SurfaceView全屏显示解码后的视频

## 实现计划

### Phase 1: 项目搭建
- 创建Android项目结构
- 配置gradle依赖
- 实现基本UI界面

### Phase 2: 连接功能
- 实现二维码生成（TV端）
- 实现二维码扫描（手机端）
- 建立基础网络连接

### Phase 3: 投屏功能
- 实现屏幕捕获
- 实现视频编码传输
- 实现视频解码显示

### Phase 4: 优化和测试
- 优化视频质量和流畅度
- 处理各种分辨率适配
- 稳定性测试和bug修复

## 关键技术实现

### 屏幕捕获实现
```java
// 请求屏幕捕获权限
MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
Intent intent = projectionManager.createScreenCaptureIntent();
startActivityForResult(intent, REQUEST_CODE);

// 创建VirtualDisplay
VirtualDisplay virtualDisplay = projectionManager.getMediaProjection(resultCode, data)
    .createVirtualDisplay("ScreenCapture", width, height, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        surface, null, null);
```

### 视频编码实现
```java
// 配置MediaCodec编码器
MediaCodec codec = MediaCodec.createEncoderByType("video/avc");
MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
```

### 网络传输实现
```java
// TCP连接用于控制
Socket socket = new Socket(serverIP, port);
DataOutputStream out = new DataOutputStream(socket.getOutputStream());

// UDP用于视频数据传输
DatagramSocket udpSocket = new DatagramSocket();
byte[] buffer = new byte[bufferSize];
DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, port);
udpSocket.send(packet);
```

## 性能优化

### 视频质量优化
- 动态调整分辨率和帧率
- 自适应比特率控制
- 硬件加速编解码

### 网络稳定性
- 心跳包检测连接状态
- 自动重连机制
- 数据包重传和排序

### 内存管理
- 合理使用缓冲区
- 及时释放资源
- 避免内存泄漏

## 测试策略

### 单元测试
- 各模块功能测试
- 网络通信测试
- 编解码功能测试

### 集成测试
- 端到端投屏测试
- 不同设备兼容性测试
- 网络环境测试（WiFi稳定性）

### 性能测试
- 视频流畅度测试
- 内存和CPU使用率测试
- 网络带宽消耗测试

## 部署和分发

### 构建配置
- **APK签名说明**：安卓应用必须进行签名才能安装。开发阶段Android Studio会自动使用debug.keystore签名，无需手动操作。发布时需要创建正式签名文件，但过程相对简单。
- 优化APK大小
- 设置应用权限

### 分发方式
- **直接APK安装**（推荐）：
  - 通过USB连接电脑传输APK文件到手机
  - 或将APK上传到云端下载安装
  - 需要在手机设置中开启"允许安装未知来源应用"
  - 开发阶段使用debug签名，发布时使用正式签名
- 应用商店上架（可选，需要正式签名）

## 风险和注意事项

### 技术风险
- 不同Android版本的API兼容性
- 网络环境对投屏质量的影响
- 设备性能差异导致的体验不一致

### 用户体验
- 首次使用需要引导
- 网络连接失败的处理
- 投屏延迟的优化

### 后续扩展
- 支持音频投屏
- 添加更多连接方式
- 界面美化和功能增强

## 总结

本设计文档提供了一个简单高效的安卓投屏解决方案。通过二维码快速连接和MediaProjection API实现屏幕镜像，满足用户对清晰稳定投屏的需求。项目结构清晰，技术实现可行，便于后续维护和扩展。