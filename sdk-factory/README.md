# 🏭 SDK Factory - 自动化SDK定制工厂

一份核心代码，为每个客户生成完全定制的SDK，包名、类名、混淆规则均不同。

---

## 📋 目录结构

```
sdk-factory/
├── sdk-template/         # 📄 模板层（维护核心逻辑）
│   ├── core/            # 核心SDK模板
│   └── wrapper/         # 对外API模板
│
├── clients/             # 🎯 客户配置
│   ├── versions.yaml    # 全局版本配置
│   ├── client-a.yaml    # 客户A配置
│   ├── client-b.yaml    # 客户B配置
│   ├── dict-clienta.txt # 客户A混淆字典
│   └── dict-clientb.txt # 客户B混淆字典
│
├── build-engine/        # ⚙️ 构建引擎
│   ├── generator.gradle.kts    # 源码生成器
│   ├── obfuscator.gradle.kts   # 混淆配置生成器
│   └── publisher.gradle.kts    # Maven发布器
│
├── scripts/             # 🐍 Python辅助脚本
│   └── build_sdk.py     # 主构建脚本
│
├── generated/           # 📦 生成目录（自动清空，不提交）
│   ├── client-a-sdk/
│   └── client-b-sdk/
│
├── build.gradle.kts     # 主构建文件
├── settings.gradle.kts  # Gradle设置
└── gradle.properties    # Gradle配置
```

---

## 🚀 快速开始

### 1️⃣ 添加新客户

复制配置模板：
```bash
cp clients/client-a.yaml clients/client-c.yaml
```

编辑配置文件 `clients/client-c.yaml`：
```yaml
client:
  id: client-c
  name: "Client C Platform"

packages:
  base: "com.platform.core.engine"
  public: "com.clientc.platform.sdk"

classes:
  main: "ClientCPlatformSDK"
  core: "PlatformCore"
  analytics: "AnalyticsCore"
  ads: "AdsCore"

maven:
  groupId: "com.clientc"
  artifactId: "platform-sdk"
  version: "1.0.0"
  repository: "https://maven.clientc.com/releases"
```

### 2️⃣ 构建SDK

#### 使用Python脚本（推荐）

```bash
# 列出所有客户
python scripts/build_sdk.py --list

# 构建单个客户
python scripts/build_sdk.py --client client-a

# 构建并发布（本地测试）
python scripts/build_sdk.py --client client-a --publish --dry-run

# 构建并发布到远程Maven
python scripts/build_sdk.py --client client-a --publish

# 构建所有客户
python scripts/build_sdk.py --all
```

#### 使用Gradle命令

```bash
# 生成源码
./gradlew generateClientSources

# 生成混淆规则
./gradlew generateProguardRules

# 构建特定客户
./gradlew assembleClient-aRelease

# 发布到Maven
./gradlew publishClient-a

# 列出所有客户
./gradlew listClients

# 构建所有客户
./gradlew buildAllClients
```

---

## 🔐 Maven认证配置

### 方法1：本地开发（推荐）

编辑 `~/.gradle/gradle.properties`:
```properties
# Client A Maven认证
client-a.maven.username=your_username
client-a.maven.password=your_token

# Client B Maven认证
client-b.maven.username=another_username
client-b.maven.password=another_token
```

### 方法2：环境变量

```bash
export CLIENT_A_MAVEN_USERNAME=your_username
export CLIENT_A_MAVEN_PASSWORD=your_token

python scripts/build_sdk.py --client client-a --publish
```

### 方法3：CI/CD (GitHub Secrets)

在GitHub仓库设置中添加Secrets：
- `CLIENT_A_MAVEN_USERNAME`
- `CLIENT_A_MAVEN_PASSWORD`
- `CLIENT_B_MAVEN_USERNAME`
- `CLIENT_B_MAVEN_PASSWORD`

---

## 📦 客户集成示例

### 客户A集成

```gradle
// build.gradle

repositories {
    maven { url 'https://maven.clienta.com/releases' }
}

dependencies {
    implementation 'com.clienta:game-sdk:1.0.0'
}
```

```kotlin
// Application.kt

import com.clienta.game.sdk.ClientAGameSDK

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        ClientAGameSDK.getInstance().init(this)
        ClientAGameSDK.getInstance().logEvent("app_launch")
    }
}
```

### 客户B集成

```gradle
// build.gradle

repositories {
    maven { url 'https://maven.clientb.com/releases' }
}

dependencies {
    implementation 'com.clientb:mobile-sdk:1.0.0'
}
```

```kotlin
// Application.kt

import com.clientb.mobile.sdk.ClientBMobileSDK

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        ClientBMobileSDK.getInstance().init(this)
        ClientBMobileSDK.getInstance().logEvent("app_launch")
    }
}
```

**注意**：客户A和客户B的SDK**包名完全不同**，他们无法看出是同一个SDK！

---

## 🛡️ 混淆保护效果

### 客户A反编译看到的代码：

```
com.clienta.game.sdk.ClientAGameSDK  ← 公共API（可见）
  └── init()
  └── logEvent()

com.gaming.platform.core.apple.banana.A  ← 混淆后（不可读）
com.gaming.platform.core.cherry.B
com.gaming.platform.core.dragon.C
```

### 客户B反编译看到的代码：

```
com.clientb.mobile.sdk.ClientBMobileSDK  ← 公共API（可见）
  └── init()
  └── logEvent()

com.mobile.core.engine.alpha.beta.A  ← 完全不同的混淆结果
com.mobile.core.engine.gamma.B
com.mobile.core.engine.delta.C
```

✅ **无法看出是同一SDK！**

---

## 🔄 更新流程

### 核心代码更新

1. 修改 `sdk-template/` 中的模板文件
2. 运行构建命令
3. 自动为所有客户生成更新

```bash
# 更新所有客户到新版本
python scripts/build_sdk.py --all --publish
```

### 客户特定更新

1. 修改 `clients/client-x.yaml` 配置
2. 只重新构建该客户

```bash
python scripts/build_sdk.py --client client-x --publish
```

---

## 📚 高级功能

### 1. 功能开关

在客户配置中控制功能：

```yaml
features:
  enable_analytics: true   # 启用分析
  enable_ads: true        # 启用广告
  enable_iap: false       # 禁用内购
```

### 2. 增强混淆

```yaml
obfuscation:
  level: "aggressive"
  proguard:
    dictionary: "dict-clienta.txt"  # 自定义字典
    seed: "unique_seed_123"         # 唯一seed
    optimization_passes: 7          # 优化次数
  stringfog:
    enable: true                    # 字符串加密
    key: "secret_key_888"
```

### 3. 版本管理

全局版本在 `clients/versions.yaml`:
```yaml
global:
  sdk_version: "1.2.0"

overrides:
  client-a:
    sdk_version: "1.2.1"  # 客户A特殊版本
```

---

## ⚠️ 安全提示

1. **❌ 不要提交真实的Maven密码到Git！**
2. **✅ 使用 ~/.gradle/gradle.properties 或环境变量**
3. **✅ GitHub Secrets 用于CI/CD**
4. **✅ generated/ 目录已在 .gitignore 中排除**

---

## 🎯 优势总结

| 优势 | 说明 |
|------|------|
| ✅ **单一代码库** | 只维护一份核心代码 |
| ✅ **完全定制** | 包名、类名、文件名都不同 |
| ✅ **层次清晰** | core + wrapper分层，易调试 |
| ✅ **零重复** | 模板化，无冗余代码 |
| ✅ **深度混淆** | 多层保护，难以逆向 |
| ✅ **自动化** | 一条命令完成构建发布 |
| ✅ **可扩展** | 10个客户以内完全胜任 |

---

## 📞 联系方式

如有问题，请联系SDK维护团队。

