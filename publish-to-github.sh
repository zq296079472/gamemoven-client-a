#!/bin/bash
# 发布SDK到GitHub Packages

set -e

echo "========================================="
echo "发布Client A SDK到GitHub Packages"
echo "========================================="

# 检查环境变量
if [ -z "$GITHUB_TOKEN" ]; then
    echo "❌ 错误: 请设置GITHUB_TOKEN环境变量"
    echo "   export GITHUB_TOKEN=ghp_xxxxx"
    exit 1
fi

echo "✅ GitHub Token已设置"

# 清理旧构建
echo ""
echo "🧹 清理旧构建..."
./gradlew clean

# 编译SDK
echo ""
echo "🔨 编译Client A SDK..."
./gradlew :client-a-sdk:assembleRelease

# 发布到GitHub Packages
echo ""
echo "📦 发布到GitHub Packages..."
./gradlew :client-a-sdk:publishReleasePublicationToGitHubPackagesRepository

echo ""
echo "========================================="
echo "✅ 发布成功!"
echo "========================================="
echo ""
echo "客户可以通过以下方式使用:"
echo ""
echo "repositories {"
echo "    maven {"
echo "        url \"https://maven.pkg.github.com/zq296079472/gamemoven-client-a\""
echo "        credentials {"
echo "        }"
echo "    }"
echo "}"
echo ""
echo "dependencies {"
echo "    implementation 'com.clienta:client-a-sdk:2.0.0'"
echo "}"

