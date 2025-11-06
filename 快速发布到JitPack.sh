#!/bin/bash

# ==================== JitPack快速发布脚本 ====================
# 用途：一键推送代码并创建tag，触发JitPack构建

set -e  # 遇到错误立即退出

VERSION="1.1.0"
GITHUB_REPO="zq296079472/gamemoven-client-a"

echo "========================================"
echo "🚀 JitPack SDK发布脚本"
echo "========================================"
echo "版本: $VERSION"
echo "仓库: https://github.com/$GITHUB_REPO"
echo "========================================"
echo ""

# 步骤1：检查git状态
echo "📝 检查git状态..."
git status

# 步骤2：添加并提交代码
echo ""
echo "📦 提交代码..."
git add .
git commit -m "release: v$VERSION - 修复ProGuard规则，支持JitPack" || echo "没有需要提交的改动"

# 步骤3：推送到GitHub
echo ""
echo "⬆️  推送到GitHub..."
git push origin main

# 步骤4：创建tag
echo ""
echo "🏷️  创建tag..."
git tag -a $VERSION -m "Release v$VERSION"

# 步骤5：推送tag
echo ""
echo "⬆️  推送tag到GitHub..."
git push origin $VERSION

echo ""
echo "========================================"
echo "✅ 发布完成！"
echo "========================================"
echo ""
echo "🔗 JitPack构建页面:"
echo "   https://jitpack.io/#$GITHUB_REPO/$VERSION"
echo ""
echo "📦 Maven坐标:"
echo "   implementation 'com.github.$GITHUB_REPO:$VERSION'"
echo ""
echo "🎯 下一步:"
echo "   1. 访问上面的JitPack链接"
echo "   2. 点击'Get it'触发构建"
echo "   3. 等待2-5分钟构建完成"
echo "   4. 客户可以无需认证直接使用！"
echo ""
echo "========================================"

