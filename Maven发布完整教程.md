# 📦 Maven 仓库发布完整教程

## 📋 目录
1. [从零开始构建 AAR](#从零开始构建-aar)
2. [前置准备](#前置准备)
3. [选择 Maven 仓库](#选择-maven-仓库)
4. [配置认证信息](#配置认证信息)
5. [配置发布地址](#配置发布地址)
6. [执行发布](#执行发布)
7. [验证发布](#验证发布)
8. [客户端集成](#客户端集成)
9. [常见问题](#常见问题)

---

## 从零开始构建 AAR

### 📱 什么是 AAR？

AAR（Android Archive）是 Android 库的打包格式，包含：
- ✅ 编译后的代码（classes.jar）
- ✅ 资源文件（res/）
- ✅ AndroidManifest.xml
- ✅ ProGuard 混淆规则

### 🎯 第一步：检查项目结构

```bash
# 1. 进入项目目录
cd /Users/xiongshi/Desktop/prodect/GameMoven

# 2. 查看项目结构
ls -la
```

你应该看到：
```
├── game-sdk/              ← SDK 模块
├── libservice/            ← Cocos 依赖
├── settings.gradle.kts    ← 项目配置
├── gradle.properties      ← 全局属性
└── gradlew               ← Gradle 包装器
```

### 🎯 第二步：验证 Gradle 环境

```bash
# 1. 检查 Gradle 版本
./gradlew --version

# 2. 列出所有可用任务
./gradlew :game-sdk:tasks --all

# 3. 查看项目信息
./gradlew projects
```

**预期输出**：
```
Root project 'GameMoven'
+--- Project ':app'
+--- Project ':game-sdk'      ← 你的 SDK 模块
\--- Project ':libservice'
```

### 🎯 第三步：清理旧的构建产物

```bash
# 清理 game-sdk 模块
./gradlew :game-sdk:clean
```

**这个命令会删除**：
- `game-sdk/build/` 目录下的所有内容
- 临时编译文件
- 旧的 AAR 文件

### 🎯 第四步：构建 Debug AAR（快速测试）

```bash
# 构建 Debug 版本（不混淆，编译快）
./gradlew :game-sdk:assembleDebug
```

**构建过程**：
```
> Task :game-sdk:preBuild
> Task :game-sdk:compileDebugKotlin
> Task :game-sdk:bundleDebugAar
> Task :game-sdk:assembleDebug

BUILD SUCCESSFUL in 10s
```

**产物位置**：
```bash
# 查看生成的 AAR
ls game-sdk/build/outputs/aar/

# 应该看到：
game-sdk-debug.aar  (大小约 150KB)
```

### 🎯 第五步：构建 Release AAR（正式版本）

```bash
# 构建 Release 版本（混淆、优化）
./gradlew :game-sdk:assembleRelease
```

**构建过程**：
```
> Configure project :game-sdk
[game-sdk] 跳过可选的广告网络依赖，如需包含请设置 -PGAME_SDK_INCLUDE_FULL_MEDIATION=true

> Task :game-sdk:compileReleaseKotlin
> Task :game-sdk:bundleReleaseAar
> Task :game-sdk:assembleRelease

BUILD SUCCESSFUL in 15s
```

**产物位置**：
```bash
# 查看生成的 Release AAR
ls -lh game-sdk/build/outputs/aar/

# 应该看到：
game-sdk-release.aar  (约 150KB)
```

### 🎯 第六步：验证 AAR 内容

```bash
# 1. 查看 AAR 文件结构
unzip -l game-sdk/build/outputs/aar/game-sdk-release.aar

# 应该看到：
#   R.txt                  (资源ID)
#   AndroidManifest.xml    (清单文件)
#   classes.jar           (代码，约150KB)
#   proguard.txt          (混淆规则)
```

```bash
# 2. 查看 classes.jar 中的类
cd game-sdk/build/outputs/aar
unzip -p game-sdk-release.aar classes.jar > /tmp/classes.jar
jar tf /tmp/classes.jar | head -20

# 应该看到你的 SDK 类：
#   com/twist/screw/sdk/GameSDK.class
#   com/twist/screw/sdk/ads/max/MaxAdManager.class
#   com/twist/screw/sdk/analytics/AnalyticsManager.class
#   ...
```

### 🎯 第七步：构建并发布到本地 Maven

```bash
# 回到项目根目录
cd /Users/xiongshi/Desktop/prodect/GameMoven

# 构建 + 发布到本地 Maven
./gradlew :game-sdk:publishToMavenLocal
```

**或者使用自定义目录**：
```bash
# 发布到 game-sdk/build/maven/
./gradlew :game-sdk:publish
```

**产物位置**：
```bash
# 查看发布结果（使用 ls 命令）
ls -lh game-sdk/build/maven/com/sdk/gamemoven/game-sdk/1.0.0/
```

**应该看到**：
```
game-sdk-1.0.0.aar              (AAR 文件)
game-sdk-1.0.0.pom              (Maven 依赖描述)
game-sdk-1.0.0.module           (Gradle 元数据)
game-sdk-1.0.0-sources.jar      (源码包)
game-sdk-1.0.0.aar.md5          (校验文件)
game-sdk-1.0.0.aar.sha1
game-sdk-1.0.0.aar.sha256
game-sdk-1.0.0.aar.sha512
...
```

### 🎯 第八步：验证发布成功

```bash
# 1. 查看 POM 文件
cat game-sdk/build/maven/com/sdk/gamemoven/game-sdk/1.0.0/game-sdk-1.0.0.pom
```

**应该看到**：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.sdk.gamemoven</groupId>
  <artifactId>game-sdk</artifactId>
  <version>1.0.0</version>
  <packaging>aar</packaging>
  ...
</project>
```

```bash
# 2. 验证 sources.jar
jar tf game-sdk/build/maven/com/sdk/gamemoven/game-sdk/1.0.0/game-sdk-1.0.0-sources.jar | head

# 应该看到源码文件：
#   main/com/twist/screw/sdk/GameSDK.kt
#   main/com/twist/screw/sdk/ads/max/MaxAdManager.kt
#   ...
```

### ✅ 完成！你已经成功构建了 AAR

现在你可以：
1. ✅ 在本地测试集成
2. ✅ 发布到远程 Maven 仓库
3. ✅ 分享给其他开发者

---

## 🐛 常见构建问题

### 问题1：找不到 libservice

**错误信息**：
```
Could not resolve project :libservice
```

**解决方法**：
```bash
# 1. 检查 settings.gradle.kts
cat settings.gradle.kts | grep libservice

# 应该有这一行：
# include(":libservice")

# 2. 如果没有，添加它
echo 'include(":libservice")' >> settings.gradle.kts
```

### 问题2：编译版本不匹配

**错误信息**：
```
Dependency requires compileSdk 36 but project uses 34
```

**解决方法**：
```bash
# 编辑 gradle.properties
nano gradle.properties

# 修改版本：
PROP_COMPILE_SDK_VERSION=36
PROP_TARGET_SDK_VERSION=36
```

### 问题3：Kotlin 版本冲突

**错误信息**：
```
Kotlin version mismatch
```

**解决方法**：
```bash
# 查看当前 Kotlin 版本
grep kotlin_version gradle.properties

# 确保版本一致：
kotlin_version=2.0.21
```

### 问题4：内存不足

**错误信息**：
```
Out of memory error
```

**解决方法**：
```bash
# 编辑 gradle.properties
nano gradle.properties

# 增加内存：
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

### 问题5：网络依赖下载失败

**错误信息**：
```
Could not resolve com.applovin:applovin-sdk:13.5.0
```

**解决方法**：
```bash
# 清理缓存重试
./gradlew --stop
./gradlew :game-sdk:clean
./gradlew :game-sdk:assembleRelease --refresh-dependencies
```

---

## 🎓 Gradle 命令速查表

### 常用构建命令

```bash
# 清理构建
./gradlew :game-sdk:clean

# 构建 Debug AAR
./gradlew :game-sdk:assembleDebug

# 构建 Release AAR
./gradlew :game-sdk:assembleRelease

# 构建所有变体
./gradlew :game-sdk:assemble

# 运行测试
./gradlew :game-sdk:test

# 查看依赖树
./gradlew :game-sdk:dependencies

# 查看可用任务
./gradlew :game-sdk:tasks
```

### Maven 发布命令

```bash
# 发布到本地 Maven (~/.m2/repository/)
./gradlew :game-sdk:publishToMavenLocal

# 发布到自定义目录 (game-sdk/build/maven/)
./gradlew :game-sdk:publish

# 发布到特定仓库（如果配置了多个）
./gradlew :game-sdk:publishReleasePublicationToGitHubPackagesRepository
./gradlew :game-sdk:publishReleasePublicationToNexusRepository
```

### 调试命令

```bash
# 详细日志
./gradlew :game-sdk:assembleRelease --info

# 堆栈跟踪
./gradlew :game-sdk:assembleRelease --stacktrace

# 性能分析
./gradlew :game-sdk:assembleRelease --profile

# 离线模式（使用缓存）
./gradlew :game-sdk:assembleRelease --offline
```

---

## 前置准备

### ✅ 检查当前状态

经过上述步骤，你现在已经：
- ✅ 成功构建了 game-sdk 的 AAR
- ✅ 了解了 AAR 的内容和结构
- ✅ 可以发布到本地 Maven
- ✅ 准备好发布到远程仓库

### 📦 当前发布配置

```gradle
// game-sdk/build.gradle (已配置)
afterEvaluate {
    publishing {
        publications {
            create("release", MavenPublication) {
                from components.release
                groupId = 'com.sdk.gamemoven'
                artifactId = 'game-sdk'
                version = '1.0.0'
            }
        }
        repositories {
            maven {
                url = uri(findProperty('GAME_SDK_PUBLISH_URL') ?: 'file://...')
            }
        }
    }
}
```

---

## 选择 Maven 仓库

### 方案对比

| 方案 | 适用场景 | 费用 | 难度 |
|------|---------|------|------|
| **Maven Central** | 公开开源项目 | 免费 | ⭐⭐⭐⭐⭐ 复杂 |
| **GitHub Packages** | GitHub项目，团队内部 | 免费(有限额) | ⭐⭐⭐ 中等 |
| **JitPack** | GitHub/GitLab项目 | 免费 | ⭐ 简单 |
| **Nexus私服** | 企业内部 | 需自建服务器 | ⭐⭐⭐⭐ 复杂 |
| **阿里云效** | 国内企业 | 付费 | ⭐⭐ 简单 |

### 💡 推荐方案

**第一次使用，我推荐以下顺序**：

1. **JitPack（最简单）** - 如果你的代码在 GitHub 上
2. **GitHub Packages（中等）** - 如果你有 GitHub 仓库
3. **本地文件仓库** - 用于测试和内网分享

---

## 🎯 方案一：JitPack（推荐新手）

### 优点
- ✅ 不需要任何配置
- ✅ 不需要注册账号
- ✅ 不需要上传，自动从 GitHub 构建
- ✅ 5分钟即可完成

### 步骤

#### 1. 确保代码在 GitHub 上

```bash
# 1.1 初始化 Git（如果还没有）
cd /Users/xiongshi/Desktop/prodect/GameMoven
git init

# 1.2 添加所有文件
git add game-sdk/ gradle/ build.gradle.kts settings.gradle.kts gradle.properties

# 1.3 提交
git commit -m "Initial commit: game-sdk ready for Maven"

# 1.4 关联远程仓库（替换成你的 GitHub 仓库地址）
git remote add origin https://github.com/你的用户名/GameMoven.git

# 1.5 推送
git push -u origin main
```

#### 2. 创建 Release Tag

```bash
# 2.1 打标签（版本号）
git tag -a v1.0.0 -m "Release version 1.0.0"

# 2.2 推送标签
git push origin v1.0.0
```

#### 3. 等待 JitPack 构建

访问：`https://jitpack.io/#你的用户名/GameMoven`

点击 "Get it" 按钮，JitPack 会自动构建你的项目。

#### 4. 客户端集成

```gradle
// 客户端项目的 settings.gradle.kts 或 build.gradle
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.你的用户名:GameMoven:1.0.0")
}
```

✅ **完成！不需要任何认证和配置！**

---

## 🎯 方案二：GitHub Packages（团队协作）

### 优点
- ✅ 与 GitHub 集成
- ✅ 支持私有仓库
- ✅ 免费（每月 500MB 流量）

### 步骤

#### 1. 生成 GitHub Token

1. 登录 GitHub
2. 点击右上角头像 → Settings
3. 左侧菜单：Developer settings → Personal access tokens → Tokens (classic)
4. 点击 "Generate new token (classic)"
5. 勾选权限：
   - ✅ `write:packages` (上传包)
   - ✅ `read:packages` (下载包)
6. 点击 "Generate token"
7. **复制 Token（只显示一次！）**

示例 Token: `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

#### 2. 配置本地认证

**方法A：使用 gradle.properties（推荐）**

编辑 `~/.gradle/gradle.properties`：

```properties
# GitHub Packages 认证
github.username=你的GitHub用户名
github.token=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

**方法B：使用环境变量**

```bash
# 编辑 ~/.zshrc 或 ~/.bash_profile
export GITHUB_USERNAME="你的GitHub用户名"
export GITHUB_TOKEN="ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

# 重新加载
source ~/.zshrc
```

#### 3. 修改 game-sdk/build.gradle

在 `publishing` 块中添加 GitHub 仓库：

```gradle
afterEvaluate {
    publishing {
        publications {
            create("release", MavenPublication) {
                from components.release
                groupId = 'com.sdk.gamemoven'
                artifactId = 'game-sdk'
                version = '1.0.0'
            }
        }
        
        repositories {
            // 本地仓库（测试用）
            maven {
                name = "Local"
                url = uri("${rootProject.buildDir}/maven")
            }
            
            // GitHub Packages
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/你的用户名/GameMoven")
                credentials {
                    username = project.findProperty("github.username") ?: System.getenv("GITHUB_USERNAME")
                    password = project.findProperty("github.token") ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}
```

#### 4. 发布到 GitHub Packages

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven

# 发布到 GitHub Packages
./gradlew :game-sdk:publishReleasePublicationToGitHubPackagesRepository

# 或者发布到所有配置的仓库
./gradlew :game-sdk:publish
```

#### 5. 客户端集成

```gradle
// 客户端的 settings.gradle.kts
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/你的用户名/GameMoven")
        credentials {
            username = project.findProperty("github.username") as String?
            password = project.findProperty("github.token") as String?
        }
    }
}

dependencies {
    implementation("com.sdk.gamemoven:game-sdk:1.0.0")
}
```

---

## 🎯 方案三：本地文件仓库（最简单测试）

### 适用场景
- ✅ 本地测试
- ✅ 内网共享（通过网络文件夹）
- ✅ 快速验证

### 步骤

#### 1. 发布到本地目录

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven

# 发布到默认位置：game-sdk/build/maven/
./gradlew :game-sdk:publish
```

#### 2. 查看发布结果

```bash
ls game-sdk/build/maven/com/sdk/gamemoven/game-sdk/1.0.0/
```

应该看到：
```
game-sdk-1.0.0.aar
game-sdk-1.0.0.pom
game-sdk-1.0.0-sources.jar
...
```

#### 3. 客户端集成（本地测试）

```gradle
// 客户端的 settings.gradle.kts
repositories {
    maven {
        url = uri("file:///Users/xiongshi/Desktop/prodect/GameMoven/game-sdk/build/maven")
    }
}

dependencies {
    implementation("com.sdk.gamemoven:game-sdk:1.0.0")
}
```

---

## 🎯 方案四：Nexus 私服（企业级）

### 前置条件
- 需要有 Nexus 服务器（自建或购买服务）
- 需要管理员提供仓库地址和账号密码

### 步骤

#### 1. 获取 Nexus 信息

向管理员获取：
- 仓库 URL：例如 `https://nexus.yourcompany.com/repository/maven-releases/`
- 用户名：例如 `deploy-user`
- 密码：例如 `your-password`

#### 2. 配置认证

编辑 `~/.gradle/gradle.properties`：

```properties
# Nexus 认证
nexus.username=deploy-user
nexus.password=your-password
```

#### 3. 修改 game-sdk/build.gradle

```gradle
afterEvaluate {
    publishing {
        publications {
            create("release", MavenPublication) {
                from components.release
                groupId = 'com.sdk.gamemoven'
                artifactId = 'game-sdk'
                version = '1.0.0'
            }
        }
        
        repositories {
            maven {
                name = "Nexus"
                url = uri("https://nexus.yourcompany.com/repository/maven-releases/")
                credentials {
                    username = project.findProperty("nexus.username") as String?
                    password = project.findProperty("nexus.password") as String?
                }
            }
        }
    }
}
```

#### 4. 发布

```bash
./gradlew :game-sdk:publishReleasePublicationToNexusRepository
```

---

## 📝 实战演练：从零开始完整流程

### 场景：你想发布到 GitHub Packages

#### Step 1: 准备 GitHub 仓库

```bash
# 1. 在 GitHub 创建新仓库
# 访问：https://github.com/new
# 仓库名：GameMoven

# 2. 本地关联
cd /Users/xiongshi/Desktop/prodect/GameMoven
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/你的用户名/GameMoven.git
git push -u origin main
```

#### Step 2: 生成 GitHub Token

```
1. 打开 https://github.com/settings/tokens
2. Generate new token (classic)
3. 勾选 write:packages 和 read:packages
4. 复制生成的 token（形如 ghp_xxxxx）
```

#### Step 3: 配置认证

```bash
# 编辑文件
nano ~/.gradle/gradle.properties

# 添加以下内容（替换你的信息）
github.username=你的GitHub用户名
github.token=ghp_你的Token

# 保存退出：Ctrl+O, Enter, Ctrl+X
```

#### Step 4: 修改发布配置

```bash
# 编辑 game-sdk/build.gradle
nano /Users/xiongshi/Desktop/prodect/GameMoven/game-sdk/build.gradle
```

找到 `publishing.repositories` 部分，修改为：

```gradle
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/你的用户名/GameMoven")
        credentials {
            username = project.findProperty("github.username") ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("github.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

#### Step 5: 执行发布

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven

# 先清理
./gradlew :game-sdk:clean

# 发布
./gradlew :game-sdk:publish
```

#### Step 6: 验证发布

访问：`https://github.com/你的用户名/GameMoven/packages`

应该能看到发布的包。

#### Step 7: 客户端测试

创建测试项目 `settings.gradle.kts`：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/你的用户名/GameMoven")
            credentials {
                username = project.findProperty("github.username") as String?
                password = project.findProperty("github.token") as String?
            }
        }
    }
}
```

`build.gradle.kts`：

```kotlin
dependencies {
    implementation("com.sdk.gamemoven:game-sdk:1.0.0")
}
```

---

## 🐛 常见问题

### 问题1：401 Unauthorized

**原因**：认证信息错误

**解决**：
```bash
# 检查 gradle.properties
cat ~/.gradle/gradle.properties

# 确保 token 正确
# GitHub token 应该以 ghp_ 开头
```

### 问题2：Could not publish

**原因**：仓库地址错误或权限不足

**解决**：
```bash
# 1. 检查仓库URL是否正确
# 2. 确保 GitHub token 有 write:packages 权限
# 3. 确保仓库名称大小写正确
```

### 问题3：Dependency not found

**原因**：客户端认证配置缺失

**解决**：
```gradle
// 客户端也需要配置 GitHub 认证
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/...")
        credentials {
            username = "..."  // 必须配置
            password = "..."  // 必须配置
        }
    }
}
```

### 问题4：版本冲突

**原因**：相同版本发布多次

**解决**：
```bash
# 方案1：修改版本号
# gradle.properties
GAME_SDK_VERSION=1.0.1

# 方案2：删除远程版本（GitHub Packages）
# 在 GitHub 仓库的 Packages 页面删除旧版本
```

---

## 💡 最佳实践

### 1. 版本管理

```properties
# gradle.properties
GAME_SDK_VERSION=1.0.0

# 发布新版本时递增
# 1.0.0 → 1.0.1 (bugfix)
# 1.0.0 → 1.1.0 (新功能)
# 1.0.0 → 2.0.0 (破坏性更新)
```

### 2. 发布前检查

```bash
# 1. 清理旧构建
./gradlew clean

# 2. 运行测试
./gradlew :game-sdk:test

# 3. 构建 AAR
./gradlew :game-sdk:assembleRelease

# 4. 发布
./gradlew :game-sdk:publish
```

### 3. 安全性

```bash
# ❌ 不要提交到 Git
# .gitignore 应该包含：
local.properties
gradle.properties  # 如果包含密码

# ✅ 使用环境变量（CI/CD）
export GITHUB_TOKEN="ghp_xxx"
./gradlew publish
```

### 4. 多仓库发布

```gradle
repositories {
    // 本地测试
    maven {
        name = "Local"
        url = uri("${rootProject.buildDir}/maven")
    }
    
    // GitHub Packages
    maven {
        name = "GitHub"
        url = uri("https://maven.pkg.github.com/...")
        credentials { ... }
    }
    
    // Nexus 私服
    maven {
        name = "Nexus"
        url = uri("https://nexus.company.com/...")
        credentials { ... }
    }
}

// 发布到特定仓库
// ./gradlew publishReleasePublicationToGitHubRepository
// ./gradlew publishReleasePublicationToNexusRepository
```

---

## 🎯 推荐：你的第一次发布

### 我建议你按这个顺序学习：

#### 第1天：本地测试（最简单）
```bash
./gradlew :game-sdk:publish
# 查看 game-sdk/build/maven/
```

#### 第2天：JitPack（如果有 GitHub）
```bash
git tag v1.0.0
git push origin v1.0.0
# 访问 https://jitpack.io
```

#### 第3天：GitHub Packages（团队协作）
```bash
# 配置 GitHub Token
./gradlew :game-sdk:publish
```

---

## 🏭 高级：使用 SDK Factory 为多客户生成定制 SDK

### 📋 什么是 SDK Factory？

SDK Factory 可以**从一份核心代码，为每个客户生成完全不同的 SDK**：
- ✅ 包名完全不同（客户看不出是同一SDK）
- ✅ 类名完全不同
- ✅ 混淆规则不同（seed、字典都不同）
- ✅ Maven 坐标不同

### 🎯 完整工作流程

```
步骤1: 配置客户信息 (client-a.yaml)
    ↓
步骤2: 生成定制源码 (sdk-factory)
    ↓
步骤3: 复制到 GameMoven 创建专属模块
    ↓
步骤4: 编译成 AAR
    ↓
步骤5: 发布到 Maven
    ↓
步骤6: 客户集成使用
```

---

### 🚀 实战演练：为客户 A 生成并发布 SDK

#### 步骤1：检查客户配置

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven/sdk-factory

# 查看客户 A 的配置
cat clients/client-a.yaml
```

**关键配置**：
```yaml
client:
  id: client-a
  name: "Client A Gaming Platform"

packages:
  base: "com.gaming.platform.core"      # 内部包名
  public: "com.clienta.game.sdk"        # 对外包名

classes:
  main: "ClientAGameSDK"                # 入口类名
  core: "GameCore"
  analytics: "AnalyticsEngine"
  ads: "AdEngine"

maven:
  groupId: "com.clienta"
  artifactId: "game-sdk"
  version: "1.0.0"
```

---

#### 步骤2：生成客户 A 的定制源码

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven/sdk-factory

# 生成源码
./gradlew generateClientSources

# 生成混淆规则
./gradlew generateProguardRules
```

**验证生成结果**：
```bash
# 查看生成的源码结构
find generated/client-a-sdk/src/main/kotlin -name "*.kt"

# 应该看到：
# generated/client-a-sdk/src/main/kotlin/wrapper/com/clienta/game/sdk/ClientAGameSDK.kt
# generated/client-a-sdk/src/main/kotlin/core/com/gaming/platform/core/GameCoreCore.kt
# generated/client-a-sdk/src/main/kotlin/core/com/gaming/platform/core/analytics/AnalyticsEngine.kt
# generated/client-a-sdk/src/main/kotlin/core/com/gaming/platform/core/ads/AdEngine.kt
```

```bash
# 查看混淆规则
cat generated/client-a-sdk/proguard-rules.pro | head -20

# 应该看到：
# -keep public class com.clienta.game.sdk.ClientAGameSDK { ... }
# -obfuscationdictionary clients/dict-clienta.txt
# -adaptclassstrings clienta_unique_seed_2024_v1
```

---

#### 步骤3：在 GameMoven 工程创建客户 A 专属模块

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven

# 创建客户 A 的 SDK 模块目录
mkdir -p client-a-sdk/src/main/kotlin
mkdir -p client-a-sdk/src/main/res

# 复制生成的源码（注意保持目录结构）
cp -r sdk-factory/generated/client-a-sdk/src/main/kotlin/wrapper/* client-a-sdk/src/main/kotlin/
cp -r sdk-factory/generated/client-a-sdk/src/main/kotlin/core/* client-a-sdk/src/main/kotlin/

# 复制混淆规则
cp sdk-factory/generated/client-a-sdk/proguard-rules.pro client-a-sdk/
```

**创建 AndroidManifest.xml**：
```bash
cat > client-a-sdk/src/main/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
</manifest>
EOF
```

**创建 build.gradle**：
```bash
cat > client-a-sdk/build.gradle << 'EOF'
apply plugin: 'com.android.library'
apply plugin: 'kotlin-android'
apply plugin: 'maven-publish'

android {
    compileSdkVersion 36
    namespace "com.clienta.game.sdk"

    defaultConfig {
        minSdkVersion 24
        targetSdkVersion 36
        
        // 使用客户专属的混淆规则
        consumerProguardFiles 'proguard-rules.pro'
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig true
    }
}

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib:2.0.21"
    implementation "androidx.core:core-ktx:1.17.0"
    
    // Cocos 依赖（编译期）
    compileOnly project(':libservice')
    
    // 继承 game-sdk 的所有第三方依赖
    // 方式1：直接依赖 game-sdk（推荐，避免重复配置）
    api project(':game-sdk')
    
    // 方式2：手动列出所有依赖（如需完全独立）
    // api 'com.applovin:applovin-sdk:13.5.0'
    // api 'com.adjust.sdk:adjust-android:5.4.5'
    // ... 其他依赖
}

afterEvaluate {
    publishing {
        publications {
            create("release", MavenPublication) {
                from components.release
                
                // 从 client-a.yaml 配置读取
                groupId = 'com.clienta'
                artifactId = 'game-sdk'
                version = '1.0.0'

                pom {
                    name = 'Client A Game SDK'
                    description = 'Client A Gaming Platform SDK'
                    url = 'https://maven.clienta.com'
                }
            }
        }

        repositories {
            maven {
                name = "Local"
                url = uri("${rootProject.buildDir}/maven-clienta")
            }
        }
    }
}
EOF
```

---

#### 步骤4：注册模块到 settings.gradle.kts

```bash
# 编辑 settings.gradle.kts
nano settings.gradle.kts
```

添加：
```kotlin
include(":client-a-sdk")
```

完整内容：
```kotlin
rootProject.name = "GameMoven"
include(":app")
include(":game-sdk")
include(":libservice")
include(":client-a-sdk")  // ← 新增客户 A 的 SDK 模块
```

---

#### 步骤5：编译客户 A 的 SDK

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven

# 清理
./gradlew :client-a-sdk:clean

# 构建
./gradlew :client-a-sdk:assembleRelease
```

**验证产物**：
```bash
ls -lh client-a-sdk/build/outputs/aar/

# 应该看到：
# client-a-sdk-release.aar
```

**验证内容**：
```bash
# 查看 AAR 中的类
unzip -p client-a-sdk/build/outputs/aar/client-a-sdk-release.aar classes.jar > /tmp/client-a.jar
jar tf /tmp/client-a.jar | grep "ClientAGameSDK"

# 应该看到：
# com/clienta/game/sdk/ClientAGameSDK.class
```

---

#### 步骤6：发布客户 A 的 SDK 到 Maven

```bash
# 发布到本地 Maven 仓库
./gradlew :client-a-sdk:publishToMavenLocal

# 或发布到项目目录
./gradlew :client-a-sdk:publish
```

**验证发布**：
```bash
# 检查系统 Maven 仓库
ls -lh ~/.m2/repository/com/clienta/game-sdk/1.0.0/

# 或检查项目 Maven 仓库
ls -lh build/maven-clienta/com/clienta/game-sdk/1.0.0/
```

---

#### 步骤7：客户 A 集成使用

**客户 A 的项目配置**：

```kotlin
// settings.gradle.kts
repositories {
    mavenLocal()  // 或远程仓库
    google()
    mavenCentral()
}

// build.gradle.kts
dependencies {
    implementation("com.clienta:game-sdk:1.0.0")  // ← 客户 A 专属坐标
}

// Application.kt
import com.clienta.game.sdk.ClientAGameSDK  // ← 客户 A 专属包名和类名

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ClientAGameSDK.getInstance().init(this)
        ClientAGameSDK.getInstance().logEvent("app_launch")
    }
}
```

---

### 🎯 为客户 B 重复相同流程

```bash
# 1. 生成客户 B 的源码（已完成）
cd sdk-factory
./gradlew generateClientSources

# 2. 创建客户 B 模块
cd ..
mkdir -p client-b-sdk/src/main/kotlin
cp -r sdk-factory/generated/client-b-sdk/src/main/kotlin/wrapper/* client-b-sdk/src/main/kotlin/
cp -r sdk-factory/generated/client-b-sdk/src/main/kotlin/core/* client-b-sdk/src/main/kotlin/
cp sdk-factory/generated/client-b-sdk/proguard-rules.pro client-b-sdk/

# 3. 创建 build.gradle（类似 client-a-sdk，修改 Maven 坐标）
# groupId = 'com.clientb'
# artifactId = 'mobile-sdk'

# 4. 注册模块
# include(":client-b-sdk")

# 5. 编译和发布
./gradlew :client-b-sdk:assembleRelease
./gradlew :client-b-sdk:publishToMavenLocal
```

**客户 B 的集成**：
```kotlin
import com.clientb.mobile.sdk.ClientBMobileSDK  // ← 完全不同的包名！

ClientBMobileSDK.getInstance().init(this)
```

---

### 🎯 一键脚本：自动化整个流程

**创建自动化脚本** `build-client-sdk.sh`：

```bash
#!/bin/bash
# 为指定客户生成、编译、发布 SDK

CLIENT_ID=$1

if [ -z "$CLIENT_ID" ]; then
    echo "用法: ./build-client-sdk.sh client-a"
    exit 1
fi

echo "🏭 开始为 $CLIENT_ID 生成 SDK..."

# 1. 生成源码
cd sdk-factory
./gradlew generateClientSources generateProguardRules

# 2. 创建模块目录
cd ..
SDK_DIR="${CLIENT_ID}-sdk"
mkdir -p "$SDK_DIR/src/main/kotlin"
mkdir -p "$SDK_DIR/src/main/res"

# 3. 复制生成的代码
cp -r "sdk-factory/generated/${CLIENT_ID}-sdk/src/main/kotlin/wrapper/"* "$SDK_DIR/src/main/kotlin/"
cp -r "sdk-factory/generated/${CLIENT_ID}-sdk/src/main/kotlin/core/"* "$SDK_DIR/src/main/kotlin/"
cp "sdk-factory/generated/${CLIENT_ID}-sdk/proguard-rules.pro" "$SDK_DIR/"

# 4. 复制 AndroidManifest.xml（从 game-sdk）
cp game-sdk/src/main/AndroidManifest.xml "$SDK_DIR/src/main/"

# 5. 生成 build.gradle（根据配置）
# TODO: 读取 yaml 生成配置

# 6. 编译
./gradlew ":${SDK_DIR}:assembleRelease"

# 7. 发布
./gradlew ":${SDK_DIR}:publishToMavenLocal"

echo "✅ 完成！SDK 已发布到本地 Maven 仓库"
```

**使用方法**：
```bash
chmod +x build-client-sdk.sh
./build-client-sdk.sh client-a
./build-client-sdk.sh client-b
```

---

### 📊 对比：不同方案的优缺点

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|---------|
| **直接用 game-sdk** | 简单、快速 | 所有客户用同一个SDK | 内部使用 |
| **sdk-factory 生成源码** | 完全定制、客户看不出 | 需要手动创建模块 | 多客户定制 |
| **自动化脚本** | 一键完成全流程 | 需要编写脚本 | 生产环境 |

---

### 💡 建议的项目结构

```
GameMoven/
├── game-sdk/              # 核心 SDK（内部使用）
├── libservice/            # Cocos 服务
├── sdk-factory/           # SDK 工厂（生成定制代码）
│   ├── clients/
│   │   ├── client-a.yaml  # 客户 A 配置
│   │   └── client-b.yaml  # 客户 B 配置
│   └── generated/         # 生成的源码
│
├── client-a-sdk/          # 客户 A 专属 SDK 模块
│   ├── src/main/kotlin/   # 从 generated/client-a-sdk 复制
│   └── build.gradle       # Maven 坐标: com.clienta:game-sdk
│
└── client-b-sdk/          # 客户 B 专属 SDK 模块
    ├── src/main/kotlin/   # 从 generated/client-b-sdk 复制
    └── build.gradle       # Maven 坐标: com.clientb:mobile-sdk
```

---

### 🎯 快速命令参考

```bash
# === SDK Factory 操作 ===

# 查看所有客户
cd sdk-factory
./gradlew listClients

# 生成所有客户的源码
./gradlew generateClientSources

# 生成混淆规则
./gradlew generateProguardRules

# === 编译和发布（回到 GameMoven 根目录）===

cd ..

# 编译客户 A 的 SDK
./gradlew :client-a-sdk:assembleRelease

# 发布客户 A 的 SDK
./gradlew :client-a-sdk:publishToMavenLocal

# 编译客户 B 的 SDK
./gradlew :client-b-sdk:assembleRelease

# 发布客户 B 的 SDK
./gradlew :client-b-sdk:publishToMavenLocal
```

---

### ✅ 最终效果

**客户 A 看到的**：
```kotlin
import com.clienta.game.sdk.ClientAGameSDK
ClientAGameSDK.getInstance().init(this)
```

**客户 B 看到的**（完全不同）：
```kotlin
import com.clientb.mobile.sdk.ClientBMobileSDK
ClientBMobileSDK.getInstance().init(this)
```

**混淆后反编译**：
- 客户 A：`apple.banana.A`（使用 apple、banana 字典）
- 客户 B：`alpha.beta.A`（使用 alpha、beta 字典）

✅ **完全无法识别是同一 SDK！**

---

### 📝 下一步：自动化完整流程

想要我帮你创建：
1. 自动化脚本（一键生成、编译、发布）
2. CI/CD 配置（GitHub Actions 自动发布）
3. 模块自动创建工具（避免手动复制）

告诉我你的需求，我会继续完善！

---

## 📞 需要帮助？

如果在任何步骤遇到问题，请告诉我：
1. 你选择了哪个方案？
2. 执行到哪一步了？
3. 遇到了什么错误信息？

我会手把手帮你解决！🚀

