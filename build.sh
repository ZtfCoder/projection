#!/bin/bash

# 输出带颜色的信息
print_info() {
    echo -e "\033[1;34m[INFO] $1\033[0m"
}

print_success() {
    echo -e "\033[1;32m[SUCCESS] $1\033[0m"
}

print_error() {
    echo -e "\033[1;31m[ERROR] $1\033[0m"
}

# 确保 gradlew 有执行权限
print_info "Setting execute permission for gradlew files..."
chmod +x android-client/gradlew android-tv/gradlew

# 创建输出目录
print_info "Creating output directory..."
mkdir -p release

# 编译客户端 APK
print_info "Building Android client APK..."
cd android-client
./gradlew clean assembleDebug
if [ $? -ne 0 ]; then
    print_error "Failed to build Android client APK"
    exit 1
fi
cd ..

# 编译电视端 APK
print_info "Building Android TV APK..."
cd android-tv
./gradlew clean assembleDebug
if [ $? -ne 0 ]; then
    print_error "Failed to build Android TV APK"
    exit 1
fi
cd ..

# 复制 APK 文件到 release 目录
print_info "Copying APK files to release directory..."
cp android-client/app/build/outputs/apk/debug/app-debug.apk release/screencast-client.apk
cp android-tv/app/build/outputs/apk/debug/app-debug.apk release/screencast-tv.apk

# 输出最终的位置信息
print_success "Build completed successfully!"
print_success "APK files location:"
print_success "  Client APK: release/screencast-client.apk"
print_success "  TV APK: release/screencast-tv.apk"