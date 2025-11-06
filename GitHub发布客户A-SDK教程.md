# 🚀 GitHub Packages 发布客户 A SDK 完整教程

## 📋 目录
1. [准备工作](#准备工作)
2. [创建 GitHub 仓库](#创建-github-仓库)
3. [生成 GitHub Token](#生成-github-token)
4. [配置本地认证](#配置本地认证)
5. [修改发布配置](#修改发布配置)
6. [执行发布](#执行发布)
7. [验证发布](#验证发布)
8. [客户集成使用](#客户集成使用)
9. [常见问题解决](#常见问题解决)

---

## 准备工作

### ✅ 你需要准备

1. **GitHub 账号**（如果没有，先去 https://github.com 注册）
2. **Git 已安装**（检查：`git --version`）
3. **客户 A 的 SDK 已构建成功**（已完成 ✅）

### 📦 当前状态

- ✅ 客户 A SDK 已生成：`client-a-sdk/`
- ✅ 已编译成 AAR：`client-a-sdk-release.aar`
- ✅ 已发布到本地：`~/.m2/repository/com/clienta/game-sdk/1.0.0/`

---

## 创建 GitHub 仓库

### 步骤1：在 GitHub 创建新仓库

1. 打开浏览器访问：https://github.com/new

2. 填写仓库信息：
   ```
   Repository name: GameMoven-ClientA-SDK
   Description: Client A Gaming Platform SDK
   Visibility: Public (或 Private，推荐 Private)
   
   ❌ 不要勾选 Add a README file
   ❌ 不要勾选 Add .gitignore
   ❌ 不要勾选 Choose a license
   ```

3. 点击 **"Create repository"**

4. 记录下仓库地址（后面要用）：
   ```
   https://github.com/你的用户名/GameMoven-ClientA-SDK.git
   ```

### 步骤2：初始化本地 Git 仓库

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven

# 如果还没有 git 仓库，初始化
git init

# 查看当前状态
git status
```

### 步骤3：创建 .gitignore

```bash
cat > .gitignore << 'EOF'
# Gradle
.gradle/
build/
**/build/
local.properties

# Android
*.apk
*.ap_
*.dex
*.class

# IntelliJ IDEA
.idea/
*.iml

# Mac
.DS_Store

# 敏感文件
gradle.properties  # 如果包含密码就忽略
EOF
```

### 步骤4：提交代码到 Git

```bash
# 添加文件
git add client-a-sdk/
git add game-sdk/
git add libservice/
git add sdk-factory/
git add settings.gradle.kts
git add build.gradle.kts
git add gradle/
git add gradlew
git add gradlew.bat
git add .gitignore

# 提交
git commit -m "feat: 添加客户 A SDK 及构建配置"

# 设置主分支名
git branch -M main
```

### 步骤5：关联远程仓库

```bash
# 替换成你的仓库地址
git remote add origin https://github.com/你的用户名/GameMoven-ClientA-SDK.git

# 推送到 GitHub
git push -u origin main
```

**遇到需要登录？**
- 用户名：你的 GitHub 用户名
- 密码：使用后面生成的 Token（不是 GitHub 密码）

---

## 生成 GitHub Token

### 详细步骤（带截图说明）

#### 1. 登录 GitHub

打开：https://github.com

#### 2. 进入 Settings

点击右上角头像 → **Settings**

#### 3. 进入 Developer settings

左侧菜单滑到最底部 → **Developer settings**

#### 4. 生成 Token

左侧菜单：**Personal access tokens** → **Tokens (classic)**

点击右上角：**Generate new token** → **Generate new token (classic)**

#### 5. 配置 Token 权限

```
Note: Client A SDK Maven Publishing
Expiration: 90 days (或选择 No expiration)

Select scopes (勾选以下权限)：
✅ write:packages    - 上传包到 GitHub Packages
✅ read:packages     - 下载包
✅ delete:packages   - 删除包（可选）
✅ repo              - 如果是私有仓库需要勾选
```

#### 6. 生成并复制 Token

1. 点击页面底部绿色按钮：**Generate token**

2. **立即复制 Token**（只显示一次！）
   ```
   ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```

3. 保存到安全的地方（密码管理器或记事本）

---

## 配置本地认证

### 方式一：使用 gradle.properties（推荐）

#### 1. 编辑配置文件

```bash
# 编辑用户级别的 gradle.properties
nano ~/.gradle/gradle.properties
```

#### 2. 添加认证信息

```properties
# GitHub Packages 认证（客户 A SDK）
github.username=你的GitHub用户名
github.token=ghp_你刚才复制的Token
```

**示例**：
```properties
github.username=xiongshi
github.token=ghp_1234567890abcdefghijklmnopqrstuvwxyz
```

#### 3. 保存并退出

```
按 Ctrl+O 保存
按 Enter 确认
按 Ctrl+X 退出
```

#### 4. 验证配置

```bash
# 查看配置（确保 token 已保存）
cat ~/.gradle/gradle.properties | grep github

# 应该看到：
# github.username=你的用户名
# github.token=ghp_xxxxx
```

---

### 方式二：使用环境变量（临时使用）

```bash
# 编辑 shell 配置文件
nano ~/.zshrc

# 添加以下内容
export GITHUB_USERNAME="你的GitHub用户名"
export GITHUB_TOKEN="ghp_你的Token"

# 保存后重新加载
source ~/.zshrc

# 验证
echo $GITHUB_USERNAME
echo $GITHUB_TOKEN
```

---

## 修改发布配置

### 步骤1：编辑 client-a-sdk/build.gradle

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven
nano client-a-sdk/build.gradle
```

### 步骤2：找到 publishing 块并修改

找到这部分：

```gradle
afterEvaluate {
    publishing {
        publications {
            create("release", MavenPublication) {
                from components.release
                
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
```

### 步骤3：替换为 GitHub Packages 配置

```gradle
afterEvaluate {
    publishing {
        publications {
            create("release", MavenPublication) {
                from components.release
                
                groupId = 'com.clienta'
                artifactId = 'game-sdk'
                version = '1.0.0'

                pom {
                    name = 'Client A Game SDK'
                    description = 'Client A Gaming Platform SDK'
                    url = 'https://github.com/你的用户名/GameMoven-ClientA-SDK'
                    
                    licenses {
                        license {
                            name = 'The Apache License, Version 2.0'
                            url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                        }
                    }
                    
                    developers {
                        developer {
                            id = 'clienta'
                            name = 'Client A Team'
                            email = 'dev@clienta.com'
                        }
                    }
                    
                    scm {
                        connection = 'scm:git:git://github.com/你的用户名/GameMoven-ClientA-SDK.git'
                        developerConnection = 'scm:git:ssh://github.com/你的用户名/GameMoven-ClientA-SDK.git'
                        url = 'https://github.com/你的用户名/GameMoven-ClientA-SDK'
                    }
                }
            }
        }

        repositories {
            // 本地测试仓库（保留，方便测试）
            maven {
                name = "Local"
                url = uri("${rootProject.buildDir}/maven-clienta")
            }
            
            // GitHub Packages（正式发布）
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/你的用户名/GameMoven-ClientA-SDK")
                credentials {
                    username = project.findProperty("github.username") ?: System.getenv("GITHUB_USERNAME")
                    password = project.findProperty("github.token") ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}
```

**⚠️ 重要**：将所有 `你的用户名` 替换成你的真实 GitHub 用户名！

---

## 执行发布

### 方式一：发布到本地测试（先测试）

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven

# 清理
./gradlew :client-a-sdk:clean

# 发布到本地（测试配置是否正确）
./gradlew :client-a-sdk:publishReleasePublicationToLocalRepository
```

**验证**：
```bash
ls -lh build/maven-clienta/com/clienta/game-sdk/1.0.0/
```

---

### 方式二：发布到 GitHub Packages（正式发布）

```bash
# 确保已经 git push 到 GitHub
git status
git push

# 发布到 GitHub Packages
./gradlew :client-a-sdk:publishReleasePublicationToGitHubPackagesRepository
```

**预期输出**：
```
> Task :client-a-sdk:generatePomFileForReleasePublication
> Task :client-a-sdk:publishReleasePublicationToGitHubPackagesRepository
Publishing to repository 'GitHubPackages'
Uploading game-sdk-1.0.0.aar to https://maven.pkg.github.com/...
Uploading game-sdk-1.0.0.pom to https://maven.pkg.github.com/...

BUILD SUCCESSFUL in 15s
```

---

### 方式三：同时发布到所有仓库

```bash
# 发布到本地 + GitHub Packages
./gradlew :client-a-sdk:publish
```

---

## 验证发布

### 步骤1：在 GitHub 网站查看

1. 打开你的仓库：
   ```
   https://github.com/你的用户名/GameMoven-ClientA-SDK
   ```

2. 点击右侧的 **"Packages"** 标签

3. 应该能看到：
   ```
   📦 game-sdk
   Latest: 1.0.0
   Published: just now
   ```

4. 点击进入，查看详细信息

### 步骤2：验证 Maven 坐标

在 Package 页面应该看到：

```
Install from the command line:
implementation("com.clienta:game-sdk:1.0.0")

Repository URL:
https://maven.pkg.github.com/你的用户名/GameMoven-ClientA-SDK
```

### 步骤3：验证文件完整性

点击 **"Assets"** 查看上传的文件：
- ✅ game-sdk-1.0.0.aar
- ✅ game-sdk-1.0.0.pom
- ✅ game-sdk-1.0.0.module
- ✅ game-sdk-1.0.0-sources.jar

---

## 客户集成使用

### 客户 A 项目的配置

#### 1. 配置仓库和认证

**客户需要生成自己的 Token**（只需要 `read:packages` 权限）

编辑客户项目的 `~/.gradle/gradle.properties`：
```properties
github.username=客户的GitHub用户名
github.token=客户的GitHub_Token
```

#### 2. 配置项目的 settings.gradle.kts

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        
        // 客户 A SDK 的 GitHub Packages 仓库
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/你的用户名/GameMoven-ClientA-SDK")
            credentials {
                username = project.findProperty("github.username") as String?
                password = project.findProperty("github.token") as String?
            }
        }
    }
}
```

#### 3. 添加依赖

在 `build.gradle.kts` 中：

```kotlin
dependencies {
    // 客户 A 专属 SDK
    implementation("com.clienta:game-sdk:1.0.0")
    
    // 其他依赖...
}
```

#### 4. 在代码中使用

```kotlin
// Application.kt
import com.clienta.game.sdk.ClientAGameSDK

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化客户 A SDK
        ClientAGameSDK.getInstance().init(this)
        
        // 记录事件
        ClientAGameSDK.getInstance().logEvent("app_launch")
        
        // 加载广告
        ClientAGameSDK.getInstance().loadAd("your_ad_unit_id")
    }
}
```

---

## 完整实战演练

### 🎯 从零到发布的完整步骤

#### 第1步：创建 GitHub 仓库（5分钟）

```bash
# 在浏览器操作：
1. 访问 https://github.com/new
2. 仓库名：GameMoven-ClientA-SDK
3. 选择 Private（推荐）
4. 点击 Create repository
5. 记录仓库地址：https://github.com/你的用户名/GameMoven-ClientA-SDK.git
```

#### 第2步：生成 GitHub Token（3分钟）

```bash
# 在浏览器操作：
1. 访问 https://github.com/settings/tokens
2. 点击 Generate new token (classic)
3. Note: ClientA SDK Publishing
4. 勾选权限：
   ✅ write:packages
   ✅ read:packages
   ✅ repo (如果是私有仓库)
5. 点击 Generate token
6. 复制 Token：ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

#### 第3步：配置本地认证（2分钟）

```bash
# 打开配置文件
nano ~/.gradle/gradle.properties

# 添加以下内容（替换你的信息）
github.username=你的GitHub用户名
github.token=ghp_你刚才复制的Token

# 保存：Ctrl+O, Enter, Ctrl+X
```

#### 第4步：修改发布配置（5分钟）

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven
nano client-a-sdk/build.gradle
```

在 `publishing.repositories` 块添加 GitHub Packages：

```gradle
repositories {
    // 保留本地测试
    maven {
        name = "Local"
        url = uri("${rootProject.buildDir}/maven-clienta")
    }
    
    // 添加 GitHub Packages
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/你的用户名/GameMoven-ClientA-SDK")
        credentials {
            username = project.findProperty("github.username") ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("github.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

**替换 `你的用户名`**！

#### 第5步：推送代码到 GitHub（3分钟）

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven

# 初始化 git（如果还没有）
git init
git add .
git commit -m "feat: 客户 A SDK 初始版本"
git branch -M main

# 关联远程仓库（替换你的用户名）
git remote add origin https://github.com/你的用户名/GameMoven-ClientA-SDK.git

# 推送
git push -u origin main
```

**如果提示需要认证**：
- Username: 你的 GitHub 用户名
- Password: 粘贴你的 Token（ghp_xxxxx）

#### 第6步：发布到 GitHub Packages（2分钟）

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven

# 清理旧构建
./gradlew :client-a-sdk:clean

# 发布到 GitHub Packages
./gradlew :client-a-sdk:publishReleasePublicationToGitHubPackagesRepository
```

**预期输出**：
```
> Configure project :game-sdk
[game-sdk] 跳过可选的广告网络依赖...

> Task :client-a-sdk:generatePomFileForReleasePublication
> Task :client-a-sdk:publishReleasePublicationToGitHubPackagesRepository
Publishing to repository 'GitHubPackages' (https://maven.pkg.github.com/...)
Uploading game-sdk-1.0.0.aar
Uploading game-sdk-1.0.0.pom
Uploading game-sdk-1.0.0.module
Uploading game-sdk-1.0.0-sources.jar

BUILD SUCCESSFUL in 25s
```

#### 第7步：在 GitHub 验证（1分钟）

```bash
# 在浏览器访问：
https://github.com/你的用户名/GameMoven-ClientA-SDK/packages
```

应该看到：
```
📦 game-sdk
   Version: 1.0.0
   Published: just now
   
Maven:
implementation("com.clienta:game-sdk:1.0.0")
```

---

## 客户测试集成

### 创建测试项目验证

#### 1. 创建新的 Android 项目（或使用现有项目）

#### 2. 配置 settings.gradle.kts

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        
        // 客户 A SDK 的 GitHub Packages
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/你的用户名/GameMoven-ClientA-SDK")
            credentials {
                // 读取本地配置
                username = project.findProperty("github.username") as String?
                password = project.findProperty("github.token") as String?
            }
        }
    }
}
```

#### 3. 添加依赖

在 `app/build.gradle.kts` 中：

```kotlin
dependencies {
    implementation("com.clienta:game-sdk:1.0.0")
    
    // 其他依赖...
}
```

#### 4. Sync 并验证

```bash
# 在 Android Studio 点击 Sync Now
# 或命令行执行：
./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep clienta
```

应该看到：
```
+--- com.clienta:game-sdk:1.0.0
     +--- com.sdk.gamemoven:game-sdk:1.0.0
          +--- com.applovin:applovin-sdk:13.5.0
          ...
```

#### 5. 在代码中使用

```kotlin
import com.clienta.game.sdk.ClientAGameSDK

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 测试 SDK
        ClientAGameSDK.getInstance().logEvent("test_event")
        Toast.makeText(this, "客户 A SDK 集成成功！", Toast.LENGTH_SHORT).show()
    }
}
```

---

## 常见问题解决

### ❌ 问题1：401 Unauthorized

**错误信息**：
```
Could not GET 'https://maven.pkg.github.com/...'
Received status code 401 from server: Unauthorized
```

**原因**：认证信息错误或缺失

**解决方法**：

```bash
# 1. 检查 gradle.properties
cat ~/.gradle/gradle.properties | grep github

# 2. 确保 Token 正确（以 ghp_ 开头）
# 3. 确保 Token 有 write:packages 权限

# 4. 重新生成 Token
# 访问 https://github.com/settings/tokens

# 5. 更新配置
nano ~/.gradle/gradle.properties
```

---

### ❌ 问题2：404 Not Found

**错误信息**：
```
Could not PUT 'https://maven.pkg.github.com/...'
Received status code 404 from server: Not Found
```

**原因**：
1. GitHub 仓库不存在
2. 仓库名称拼写错误
3. 没有访问权限

**解决方法**：

```bash
# 1. 确认仓库存在
# 访问 https://github.com/你的用户名/GameMoven-ClientA-SDK

# 2. 检查配置中的 URL
cat client-a-sdk/build.gradle | grep "maven.pkg.github.com"

# 3. 确保用户名大小写正确
# GitHub 用户名是大小写敏感的！
```

---

### ❌ 问题3：包已存在，无法重新发布

**错误信息**：
```
Failed to publish: version 1.0.0 already exists
```

**解决方法**：

**方式A：删除旧版本**
```bash
# 1. 访问 GitHub Packages 页面
https://github.com/你的用户名/GameMoven-ClientA-SDK/packages

# 2. 点击包名进入详情
# 3. 点击右侧 Package settings
# 4. 滑到底部点击 Delete this package 或 Delete this version
```

**方式B：发布新版本**
```gradle
// 修改 build.gradle
version = '1.0.1'  // 改成新版本号
```

```bash
# 重新发布
./gradlew :client-a-sdk:publish
```

---

### ❌ 问题4：客户端下载失败

**错误信息**：
```
Could not resolve com.clienta:game-sdk:1.0.0
```

**原因**：客户端没有配置认证或仓库

**解决方法**：

**客户端也需要配置认证**：

```bash
# 客户编辑 ~/.gradle/gradle.properties
github.username=客户的GitHub用户名
github.token=客户的GitHub_Token  # 客户需要生成自己的 Token（read:packages 权限）
```

**客户端项目配置**：
```kotlin
// settings.gradle.kts
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/你的用户名/GameMoven-ClientA-SDK")
        credentials {
            username = project.findProperty("github.username") as String?
            password = project.findProperty("github.token") as String?
        }
    }
}
```

---

### ❌ 问题5：Private 仓库客户无法访问

**原因**：GitHub Packages 的访问权限绑定仓库权限

**解决方法**：

**方式A：设为 Public 仓库**
```bash
# 在 GitHub 仓库页面：
Settings → Danger Zone → Change visibility → Make public
```

**方式B：邀请客户为 Collaborator**
```bash
# 在 GitHub 仓库页面：
Settings → Collaborators → Add people
# 输入客户的 GitHub 用户名
```

**方式C：使用 Personal Access Token（推荐）**
```bash
# 1. 你生成一个只读 Token
# 访问 https://github.com/settings/tokens
# 只勾选 read:packages

# 2. 将 Token 提供给客户
# 客户使用这个 Token 下载包
```

---

## 🎯 发布新版本流程

### 场景：修复 bug 或添加新功能

#### 1. 修改源码

```bash
cd /Users/xiongshi/Desktop/prodect/GameMoven

# 修改 sdk-factory 的模板或 game-sdk 的代码
# ...

# 重新生成客户 A 的源码
cd sdk-factory
./gradlew generateClientSources

# 复制到 client-a-sdk
cd ..
rm -rf client-a-sdk/src/main/kotlin/*
cp -r sdk-factory/generated/client-a-sdk/src/main/kotlin/wrapper/* client-a-sdk/src/main/kotlin/
cp -r sdk-factory/generated/client-a-sdk/src/main/kotlin/core/* client-a-sdk/src/main/kotlin/
```

#### 2. 更新版本号

```bash
# 编辑 client-a-sdk/build.gradle
nano client-a-sdk/build.gradle

# 修改版本号
version = '1.0.1'  # 从 1.0.0 改为 1.0.1
```

#### 3. 提交代码

```bash
git add .
git commit -m "feat: 客户 A SDK v1.0.1 - 修复xxx问题"
git push
```

#### 4. 发布新版本

```bash
./gradlew :client-a-sdk:clean
./gradlew :client-a-sdk:publish
```

#### 5. 通知客户更新

客户只需修改依赖版本：
```kotlin
implementation("com.clienta:game-sdk:1.0.1")  // 改版本号
```

---

## 🔐 安全最佳实践

### ✅ DO（推荐做法）

1. **Token 存在本地配置中**
   ```bash
   ~/.gradle/gradle.properties  # ✅ 正确
   ```

2. **不要提交 Token 到 Git**
   ```bash
   # .gitignore
   local.properties
   gradle.properties  # 如果包含 Token
   ```

3. **使用最小权限 Token**
   ```
   发布端：write:packages + read:packages
   客户端：read:packages（只读）
   ```

4. **定期轮换 Token**
   ```
   建议：每 90 天更新一次 Token
   ```

5. **为每个客户创建独立仓库**
   ```
   GameMoven-ClientA-SDK  # 客户 A
   GameMoven-ClientB-SDK  # 客户 B
   GameMoven-ClientC-SDK  # 客户 C
   ```

### ❌ DON'T（避免做法）

1. **不要在代码中硬编码 Token**
   ```gradle
   // ❌ 错误
   password = "ghp_1234567890..."
   ```

2. **不要使用主账号密码**
   ```gradle
   // ❌ 错误
   password = "你的GitHub密码"
   ```

3. **不要提交 gradle.properties 到 Git**
   ```bash
   # ❌ 错误
   git add gradle.properties  # 如果包含 Token
   ```

4. **不要给 Token 过多权限**
   ```
   ❌ 不要勾选 repo (delete)
   ❌ 不要勾选 admin:org
   ❌ 不要勾选 admin:repo_hook
   ```

---

## 📊 成本和限制

### GitHub Packages 免费额度

| 账户类型 | 存储空间 | 数据传输 |
|---------|---------|---------|
| **Public 仓库** | 无限制 | 无限制 |
| **Private 仓库** | 500MB | 1GB/月 |

### 超出限制后

- 存储：$0.008/GB/天
- 传输：$0.50/GB

### 💡 省钱技巧

1. **使用 Public 仓库**（免费无限制）
2. **定期清理旧版本**
3. **为核心客户使用，测试用本地仓库**

---

## 🎯 多客户管理策略

### 方案A：一个仓库，多个 Package

```
仓库：GameMoven-SDK
Packages：
  - com.clienta:game-sdk:1.0.0
  - com.clientb:mobile-sdk:1.0.0
  - com.clientc:platform-sdk:1.0.0
```

**优点**：统一管理  
**缺点**：客户能看到其他客户的包

---

### 方案B：每个客户独立仓库（推荐）

```
仓库1：GameMoven-ClientA-SDK
  Package: com.clienta:game-sdk:1.0.0

仓库2：GameMoven-ClientB-SDK
  Package: com.clientb:mobile-sdk:1.0.0

仓库3：GameMoven-ClientC-SDK
  Package: com.clientc:platform-sdk:1.0.0
```

**优点**：完全隔离，客户看不到其他客户  
**缺点**：需要管理多个仓库

---

## 🎉 完整流程总结

```bash
# === 一次性设置（只需做一次）===

# 1. 创建 GitHub 仓库
https://github.com/new → GameMoven-ClientA-SDK

# 2. 生成 GitHub Token
https://github.com/settings/tokens → Generate new token

# 3. 配置认证
echo "github.username=你的用户名" >> ~/.gradle/gradle.properties
echo "github.token=ghp_你的Token" >> ~/.gradle/gradle.properties

# 4. 修改 build.gradle 添加 GitHub Packages 配置
nano client-a-sdk/build.gradle

# === 每次发布新版本 ===

cd /Users/xiongshi/Desktop/prodect/GameMoven

# 1. 修改版本号
# client-a-sdk/build.gradle: version = '1.0.x'

# 2. 提交代码
git add .
git commit -m "release: v1.0.x"
git push

# 3. 发布
./gradlew :client-a-sdk:clean
./gradlew :client-a-sdk:publishReleasePublicationToGitHubPackagesRepository

# 4. 验证
https://github.com/你的用户名/GameMoven-ClientA-SDK/packages
```

---

## 📞 快速帮助

### 我现在在哪一步？

- [ ] 步骤1：创建 GitHub 仓库 → [跳转](#第1步创建-github-仓库5分钟)
- [ ] 步骤2：生成 Token → [跳转](#第2步生成-github-token3分钟)
- [ ] 步骤3：配置认证 → [跳转](#第3步配置本地认证2分钟)
- [ ] 步骤4：修改配置 → [跳转](#第4步修改发布配置5分钟)
- [ ] 步骤5：推送代码 → [跳转](#第5步推送代码到-github3分钟)
- [ ] 步骤6：执行发布 → [跳转](#第6步发布到-github-packages2分钟)
- [ ] 步骤7：验证结果 → [跳转](#第7步在-github-验证1分钟)

### 需要帮助？

告诉我：
1. 你的 GitHub 用户名是什么？
2. 你在哪一步遇到问题？
3. 完整的错误信息是什么？

我会立即帮你解决！🚀

---

## ⏱️ 预计总耗时

| 步骤 | 时间 | 难度 |
|------|------|------|
| 创建仓库 | 5分钟 | ⭐ 简单 |
| 生成 Token | 3分钟 | ⭐ 简单 |
| 配置认证 | 2分钟 | ⭐ 简单 |
| 修改配置 | 5分钟 | ⭐⭐ 中等 |
| 推送代码 | 3分钟 | ⭐ 简单 |
| 执行发布 | 2分钟 | ⭐ 简单 |
| 验证测试 | 5分钟 | ⭐ 简单 |

**总计：约 25 分钟完成首次发布！**

之后每次发布新版本只需 **5 分钟**！

🎉 **准备开始了吗？告诉我你的 GitHub 用户名，我帮你填写配置！**

