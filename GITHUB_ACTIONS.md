# GitHub Actions 自动构建说明

本项目已配置 GitHub Actions 工作流，可以自动构建 APK 文件。

## 📦 两个工作流

### 1. Build Workflow (`build.yml`)
**触发时机**：
- 推送代码到 main/master 分支
- 创建 Pull Request
- 手动触发

**功能**：
- 自动构建手机端 APK
- 自动构建投影仪端 APK
- 将 APK 作为 Artifacts 上传（保存 90 天）

**如何下载构建产物**：
1. 进入 GitHub 仓库
2. 点击 `Actions` 标签
3. 选择最近的工作流运行
4. 在页面底部找到 `Artifacts` 区域
5. 下载 `android-client-apk` 和 `android-tv-apk`

### 2. Release Workflow (`release.yml`)
**触发时机**：
- 推送带版本号的 tag（如 `v1.0.0`）
- 手动触发

**功能**：
- 构建两个 APK
- 自动创建 GitHub Release
- 自动上传 APK 到 Release 页面
- 生成版本说明

**如何创建发布版本**：
```bash
# 创建并推送 tag
git tag v1.0.0
git push origin v1.0.0

# 或者使用带注释的 tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

## 🚀 使用方法

### 首次设置
1. 将代码推送到 GitHub
2. GitHub Actions 会自动运行
3. 不需要任何额外配置

### 查看构建状态
在 GitHub 仓库页面可以看到构建状态徽章。

### 手动触发构建
1. 进入 `Actions` 标签
2. 选择工作流（Build 或 Release）
3. 点击 `Run workflow` 按钮
4. 选择分支并运行

## 📥 获取 APK

### 方式一：从 Artifacts 下载（开发版本）
1. GitHub → Actions → 选择工作流运行
2. 下载 Artifacts 中的 APK
3. 解压 zip 文件获得 APK

### 方式二：从 Releases 下载（正式版本）
1. GitHub → Releases 页面
2. 下载最新版本的 APK
3. 直接安装到设备

## 🔧 自定义配置

### 修改构建触发条件
编辑 `.github/workflows/build.yml`:
```yaml
on:
  push:
    branches: [ main ]  # 只在 main 分支触发
    paths-ignore:
      - '**.md'         # 忽略 Markdown 文件变更
```

### 修改 JDK 版本
如果需要不同的 Java 版本：
```yaml
- name: Set up JDK
  uses: actions/setup-java@v4
  with:
    java-version: '11'  # 改为 11 或其他版本
    distribution: 'temurin'
```

### 构建 Release 版本
如果想构建正式签名版本，需要：
1. 创建 keystore 文件
2. 将密钥信息添加到 GitHub Secrets
3. 修改工作流使用 `assembleRelease`

## 📊 构建状态徽章

可以在 README 中添加徽章：

```markdown
![Build Status](https://github.com/你的用户名/projection/workflows/Build%20Android%20APKs/badge.svg)
```

## ⚠️ 注意事项

1. **免费限额**：
   - GitHub Actions 对公开仓库免费
   - 私有仓库有分钟数限制

2. **Artifacts 保留期**：
   - 默认保存 90 天
   - 可以在工作流中配置

3. **构建时间**：
   - 首次构建较慢（需要下载依赖）
   - 后续构建会使用缓存

4. **签名问题**：
   - 工作流构建的是 debug 版本
   - 如需正式版本需要配置签名

## 🎯 常见问题

### 构建失败怎么办？
1. 查看 Actions 日志
2. 检查 Gradle 配置
3. 确认 JDK 版本兼容性

### 如何加速构建？
添加 Gradle 缓存：
```yaml
- name: Cache Gradle packages
  uses: actions/cache@v3
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
```

### 如何构建不同变体？
修改构建命令：
```bash
./gradlew assembleRelease  # Release 版本
./gradlew assembleDebug    # Debug 版本
```

## 📝 工作流文件位置

```
.github/
└── workflows/
    ├── build.yml      # 持续集成构建
    └── release.yml    # 发布版本构建
```

## 🔗 相关链接

- [GitHub Actions 文档](https://docs.github.com/cn/actions)
- [Android 构建最佳实践](https://developer.android.com/studio/build)
- [Gradle 构建指南](https://docs.gradle.org/current/userguide/userguide.html)
