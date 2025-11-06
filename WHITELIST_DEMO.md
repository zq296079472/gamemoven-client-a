# 🎨 白标SDK效果演示

## 客户对比 - 完全不同的SDK体验

### 📱 客户A收到的SDK

**Maven依赖**:
```gradle
implementation 'com.clienta:client-a-sdk:2.0.0'
```

**使用代码**:
```kotlin
import com.clienta.game.sdk.ClientAGameSDK

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        ClientAGameSDK.getInstance().init(
            app = this,
            config = sdkConfig
        )
    }
}
```

**可见的类**:
- `com.clienta.game.sdk.ClientAGameSDK`
- `com.clienta.game.sdk.*`

---

### 📱 客户B收到的SDK

**Maven依赖**:
```gradle
implementation 'com.clientb:game-platform-sdk:1.0.0'
```

**使用代码**:
```kotlin
import com.clientb.platform.api.GamePlatformSDK

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        GamePlatformSDK.getInstance().init(
            app = this,
            config = sdkConfig
        )
    }
}
```

**可见的类**:
- `com.clientb.platform.api.GamePlatformSDK`
- `com.clientb.platform.api.*`

---

## 🔍 客户视角对比

| 项目 | 客户A看到的 | 客户B看到的 | 是否相同? |
|------|-----------|-----------|---------|
| **Maven坐标** | `com.clienta:client-a-sdk` | `com.clientb:game-platform-sdk` | ❌ 完全不同 |
| **包名** | `com.clienta.game.sdk` | `com.clientb.platform.api` | ❌ 完全不同 |
| **主类名** | `ClientAGameSDK` | `GamePlatformSDK` | ❌ 完全不同 |
| **API风格** | Client A风格 | GamePlatform风格 | ❌ 完全不同 |
| **文档链接** | docs.clienta.com | docs.clientb.com | ❌ 完全不同 |

**结论**: 两个客户拿到的SDK从任何角度看都是**完全独立的产品**! ✨

---

## ⚙️ 为新客户生成SDK只需3个文件

### 1️⃣ 客户配置 (clients/client-x.yaml)

```yaml
client:
  id: client-x
  name: "客户X"

packages:
  public: "com.clientx.sdk"
  
classes:
  main: "ClientXSDK"
  
maven:
  groupId: "com.clientx"
  artifactId: "game-sdk"
  version: "1.0.0"
```

### 2️⃣ 运行生成脚本

```bash
./sdk-factory/scripts/generate-client-sdk.sh client-x
```

### 3️⃣ 构建发布

```bash
./gradlew :client-x-sdk:assembleRelease
```

**总耗时: 5分钟** ⏱️

---

## 🧬 核心技术

### 架构设计

```
game-sdk (核心引擎 - 1份代码)
    ├── 广告、分析、支付等核心功能
    └── 统一维护,所有客户共享
    
    ↓ 通过白标层包装
    
client-a-sdk (客户A专属)
    ├── ClientAGameSDK (包装GameSDK)
    └── 包名: com.clienta.game.sdk
    
client-b-sdk (客户B专属)
    ├── GamePlatformSDK (包装GameSDK)
    └── 包名: com.clientb.platform.api
    
client-x-sdk (客户X专属)
    ├── ClientXSDK (包装GameSDK)
    └── 包名: com.clientx.sdk
```

### 关键特性

1. **单一代码源**: `game-sdk`包含所有核心功能
2. **自动包装**: 生成脚本自动创建白标层
3. **完全定制**: 每个客户看到完全不同的API
4. **易于维护**: 改一处,自动同步所有客户

---

## 📈 扩展性

支持的客户数量: **10+ 客户无压力**

每增加一个客户:
- 配置时间: 2分钟
- 生成时间: 10秒
- 构建时间: 1分钟
- 总计: **不到5分钟**

---

## 🎓 最佳实践

### 版本管理

建议为每个客户使用独立的Git标签:

```bash
git tag client-a-2.0.0  # 客户A版本2.0.0
git tag client-b-1.0.0  # 客户B版本1.0.0
git tag client-x-1.5.0  # 客户X版本1.5.0
```

### 批量更新

创建脚本批量重新生成所有客户SDK:

```bash
#!/bin/bash
for client in client-a client-b client-x; do
    sdk-factory/scripts/generate-client-sdk.sh $client
    ./gradlew :${client}-sdk:assembleRelease
done
```

---

## 🚨 注意事项

1. **配置文件命名**: 必须为`client-*.yaml`格式
2. **包名唯一性**: 确保不同客户的包名不冲突
3. **版本号**: 建议每个客户独立维护版本号
4. **Maven发布**: 使用JitPack或私有Maven仓库

---

## 📝 示例客户列表

当前已配置的客户:

| ID | 名称 | 包名 | 主类 | 状态 |
|----|------|------|------|------|
| client-a | Client A Gaming Platform | com.clienta.game.sdk | ClientAGameSDK | ✅ 生产中 |
| client-b | GamePlatform Pro | com.clientb.platform.api | GamePlatformSDK | ✅ 已生成 |

---

## 🏆 成就解锁

✅ **一次开发,多次销售** - 统一维护,降低成本
✅ **完全定制** - 客户感受专属定制服务
✅ **快速交付** - 5分钟生成新客户SDK
✅ **易于迭代** - 核心功能统一升级

**这就是白标SDK的威力!** 💪

