#!/bin/bash
# 发布SDK到GitHub Packages

set -e

REPO_URL="https://maven.pkg.github.com/zq296079472/gamemoven-client-a"
PACKAGE_COORD="com.clienta:game-sdk:1.0.0"

cat <<INFO
=========================================
发布 Client A SDK 到 GitHub Packages
=========================================
INFO

if [ -z "$GITHUB_TOKEN" ]; then
    cat <<ERR
❌ 错误: 未检测到 GITHUB_TOKEN 环境变量。
   请先执行: export GITHUB_TOKEN=<你的GitHub Token>
ERR
    exit 1
fi

cat <<MSG
✅ GitHub Token 已设置

🧹 清理旧构建...
MSG
./gradlew clean

cat <<MSG
🔨 编译 Client A SDK...
MSG
./gradlew :client-a-sdk:assembleRelease

cat <<MSG
📦 发布到 GitHub Packages...
MSG
./gradlew :client-a-sdk:publishReleasePublicationToGitHubPackagesRepository -PGITHUB_TOKEN="$GITHUB_TOKEN"

cat <<'INFO'
=========================================
✅ 发布成功!
=========================================

客户集成示例:

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/zq296079472/gamemoven-client-a")
        credentials {
            username = "zq296079472"
            password = "<YOUR_GITHUB_TOKEN>"
        }
    }
}

dependencies {
    implementation("com.clienta:game-sdk:1.0.0")
}
INFO
