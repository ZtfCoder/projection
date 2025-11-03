# Gradle Wrapper 配置说明

本项目使用 Gradle Wrapper，无需预先安装 Gradle。

## 如果缺少 Gradle Wrapper 文件

在项目根目录运行：

```bash
# android-client 项目
cd android-client
gradle wrapper --gradle-version 8.0

# android-tv 项目
cd android-tv
gradle wrapper --gradle-version 8.0
```

## 构建命令

```bash
# Linux/macOS
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```
