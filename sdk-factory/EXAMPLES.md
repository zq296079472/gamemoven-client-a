# 📚 SDK Factory 使用示例

## 🎯 示例1：为客户A构建定制SDK

### 场景
客户A是一家游戏公司，需要一个带有分析和广告功能的SDK，包名要求使用 `com.clienta.game.sdk`。

### 配置文件：clients/client-a.yaml

已配置好，可以直接使用。关键配置：
```yaml
packages:
  base: "com.gaming.platform.core"    # 内部包名（会被混淆）
  public: "com.clienta.game.sdk"      # 对外包名（客户看到的）

classes:
  main: "ClientAGameSDK"              # 入口类名

obfuscation:
  proguard:
    dictionary: "dict-clienta.txt"    # 使用apple、banana等词汇
    seed: "clienta_unique_seed_2024_v1"
```

### 构建步骤

```bash
# 1. 生成源码
./gradlew generateClientSources

# 2. 查看生成的代码（验证包名正确）
cat generated/client-a-sdk/src/main/kotlin/com/clienta/game/sdk/ClientAGameSDK.kt

# 3. 构建
python scripts/build_sdk.py --client client-a

# 4. 本地测试
python scripts/build_sdk.py --client client-a --publish --dry-run

# 5. 在测试项目中验证
# 在 ~/.m2/repository/com/clienta/game-sdk/1.0.0/ 找到AAR

# 6. 真实发布到远程Maven
python scripts/build_sdk.py --client client-a --publish
```

### 客户A的集成代码

```kotlin
// build.gradle
repositories {
    maven { url = uri("https://maven.clienta.com/releases") }
}

dependencies {
    implementation("com.clienta:game-sdk:1.0.0")
}

// Application.kt
import com.clienta.game.sdk.ClientAGameSDK

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ClientAGameSDK.getInstance().init(this)
    }
}
```

---

## 🎯 示例2：为客户B构建不同包名的SDK

### 场景
客户B是一家社交平台，需要移动SDK，包名要求完全不同，且要更强的混淆。

### 配置差异对比

| 配置项 | 客户A | 客户B | 差异性 |
|-------|------|------|--------|
| 基础包名 | `com.gaming.platform.core` | `com.mobile.core.engine` | ✅ 完全不同 |
| 公共包名 | `com.clienta.game.sdk` | `com.clientb.mobile.sdk` | ✅ 完全不同 |
| 主类名 | `ClientAGameSDK` | `ClientBMobileSDK` | ✅ 完全不同 |
| 混淆字典 | apple, banana... | alpha, beta... | ✅ 不同词汇 |
| 混淆seed | clienta_seed | clientb_seed | ✅ 不同seed |

### 反编译对比

**客户A反编译看到**：
```
com.clienta.game.sdk.ClientAGameSDK
└── com.gaming.platform.core.apple.banana.A
```

**客户B反编译看到**：
```
com.clientb.mobile.sdk.ClientBMobileSDK
└── com.mobile.core.engine.alpha.beta.A
```

✅ **完全无法看出是同一SDK！**

---

## 🎯 示例3：核心代码迭代更新

### 场景
核心SDK新增了一个功能，需要同步到所有客户。

### 操作步骤

```bash
# 1. 修改核心模板
vim sdk-template/core/{{BASE_PACKAGE}}/{{SDK_NAME}}Core.kt

# 添加新方法
fun newFeature() {
    println("New feature for {{CLIENT_NAME}}")
}

# 2. 修改wrapper模板（对外API）
vim sdk-template/wrapper/{{CLIENT_PACKAGE}}/{{CLIENT_SDK_NAME}}.kt

# 暴露新方法
fun callNewFeature() {
    core.newFeature()
}

# 3. 更新全局版本
vim clients/versions.yaml
# sdk_version: "1.1.0"

# 4. 重新构建所有客户
python scripts/build_sdk.py --all

# 5. 查看生成的代码验证
cat generated/client-a-sdk/src/main/kotlin/com/clienta/game/sdk/ClientAGameSDK.kt
cat generated/client-b-sdk/src/main/kotlin/com/clientb/mobile/sdk/ClientBMobileSDK.kt

# 6. 全部发布
python scripts/build_sdk.py --all --publish
```

**结果**：所有客户的SDK都自动更新到1.1.0，无需手动同步！

---

## 🎯 示例4：为特定客户定制功能

### 场景
客户C需要内购功能，但其他客户不需要。

### 配置：clients/client-c.yaml

```yaml
features:
  enable_analytics: true
  enable_ads: true
  enable_iap: true  # ✅ 只有客户C启用
```

### 模板代码：sdk-template/core/.../{{SDK_NAME}}Core.kt

```kotlin
{{#IF_FEATURE_IAP}}
private val iap by lazy { IAPModule() }

fun purchaseItem(itemId: String) {
    iap.purchase(itemId)
}
{{/IF_FEATURE_IAP}}
```

### 生成结果

**客户C的代码**（包含IAP）：
```kotlin
class ClientCSDKCore {
    private val iap by lazy { IAPModule() }
    
    fun purchaseItem(itemId: String) {
        iap.purchase(itemId)
    }
}
```

**客户A的代码**（不包含IAP）：
```kotlin
class GameCore {
    // IAP代码被自动删除
}
```

✅ **条件编译生效！**

---

## 🎯 示例5：使用CI/CD自动发布

### GitHub Actions工作流

```yaml
# 当推送tag时自动发布
git tag v1.2.0
git push origin v1.2.0

# GitHub Actions自动：
# 1. 生成所有客户源码
# 2. 构建所有客户SDK
# 3. 发布到各自的Maven仓库
```

### 手动触发

在GitHub仓库页面：
1. 点击 "Actions"
2. 选择 "Publish SDK to Maven"
3. 点击 "Run workflow"
4. 输入客户ID（或留空发布所有）
5. 点击 "Run workflow"

---

## 📊 完整工作流示例

### 从零开始为新客户D创建SDK

```bash
# Step 1: 创建配置
cp clients/template.yaml clients/client-d.yaml

# Step 2: 编辑配置
cat > clients/client-d.yaml << 'EOF'
client:
  id: client-d
  name: "Client D Platform SDK"

packages:
  base: "com.platform.sdk.core"
  public: "com.clientd.platform.sdk"

classes:
  main: "ClientDPlatformSDK"
  core: "PlatformCore"
  analytics: "AnalyticsService"
  ads: "AdService"

maven:
  groupId: "com.clientd"
  artifactId: "platform-sdk"
  version: "1.0.0"
  repository: "https://nexus.clientd.com/repository/maven-releases/"

obfuscation:
  proguard:
    dictionary: "dict-clientd.txt"
    seed: "clientd_secret_seed_2024"
EOF

# Step 3: 创建混淆字典
cat > clients/dict-clientd.txt << 'EOF'
ocean
river
mountain
forest
desert
valley
EOF

# Step 4: 配置Maven认证
echo "client-d.maven.username=my_username" >> ~/.gradle/gradle.properties
echo "client-d.maven.password=my_token" >> ~/.gradle/gradle.properties

# Step 5: 构建并测试
python scripts/build_sdk.py --client client-d --publish --dry-run

# Step 6: 验证本地Maven
ls ~/.m2/repository/com/clientd/platform-sdk/1.0.0/

# Step 7: 真实发布
python scripts/build_sdk.py --client client-d --publish

# Step 8: 通知客户集成
# 发送邮件或文档，告知客户集成方式
```

---

## 🎉 总结

SDK Factory让您能够：
- ✅ **一次开发**：只维护一份核心代码
- ✅ **多次定制**：为每个客户生成不同包名/类名的SDK
- ✅ **深度混淆**：客户无法看出是同一SDK
- ✅ **自动发布**：一键发布到各客户的Maven仓库
- ✅ **易于维护**：核心更新自动同步所有客户

**开始使用SDK Factory，让SDK定制自动化！** 🚀

