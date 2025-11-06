# 🏭 白标SDK工厂 - 5分钟为新客户生成定制SDK

## 📋 概述

这是一个自动化的白标SDK生成系统,可以为不同客户生成**完全定制**的游戏SDK:
- ✅ **不同的包名和类名** - 客户看不出是同一来源
- ✅ **统一维护** - 改一次代码,所有客户自动同步
- ✅ **5分钟生成** - 只需配置YAML,自动生成全套SDK

---

## 🚀 为新客户生成SDK (5分钟)

### 步骤1: 创建客户配置 (2分钟)

复制`clients/client-a.yaml`并修改:

```bash
cd sdk-factory/clients
cp client-a.yaml client-x.yaml
```

编辑`client-x.yaml`:

```yaml
client:
  id: client-x
  name: "客户X平台"

packages:
  public: "com.clientx.game.sdk"  # 定制包名
  
classes:
  main: "ClientXGameSDK"  # 定制类名

maven:
  groupId: "com.clientx"
  artifactId: "game-sdk"
  version: "1.0.0"
```

### 步骤2: 自动生成SDK (1分钟)

```bash
cd GameMoven
sdk-factory/scripts/generate-client-sdk.sh client-x
```

输出示例:
```
✅ 客户ID: client-x
✅ SDK类名: ClientXGameSDK
✅ 公开包名: com.clientx.game.sdk
✅ Maven坐标: com.clientx:game-sdk:1.0.0
🎉 客户client-x的SDK已生成!
```

### 步骤3: 构建发布 (2分钟)

```bash
./gradlew :client-x-sdk:assembleRelease
git add client-x-sdk
git commit -m "feat: 添加客户X SDK"
git push origin main
git tag client-x-1.0.0
git push origin client-x-1.0.0
```

---

## 📦 客户使用SDK

### 客户A (ClientAGameSDK)

```kotlin
import com.clienta.game.sdk.ClientAGameSDK

ClientAGameSDK.getInstance().init(
    app = this,
    config = sdkConfig,
    // ...
)
```

### 客户B (GamePlatformSDK)  

```kotlin
import com.clientb.platform.api.GamePlatformSDK

GamePlatformSDK.getInstance().init(
    app = this,
    config = sdkConfig,
    // ...
)
```

### 客户X (ClientXGameSDK)

```kotlin
import com.clientx.game.sdk.ClientXGameSDK

ClientXGameSDK.getInstance().init(
    app = this,
    config = sdkConfig,
    // ...
)
```

**完全不同的API,客户无法看出是同一SDK!** ✨

---

## 🔧 维护和迭代

### 修改核心功能

所有改动只需在`game-sdk`中修改一次:

```bash
# 修改game-sdk源码
vim game-sdk/src/main/kotlin/com/twist/screw/sdk/...

# 重新生成所有客户SDK
sdk-factory/scripts/generate-client-sdk.sh client-a
sdk-factory/scripts/generate-client-sdk.sh client-b
sdk-factory/scripts/generate-client-sdk.sh client-x

# 统一发布
./gradlew :client-a-sdk:assembleRelease
./gradlew :client-b-sdk:assembleRelease
./gradlew :client-x-sdk:assembleRelease
```

### 批量生成脚本

```bash
# 为所有客户重新生成SDK
for client in client-a client-b client-x; do
    sdk-factory/scripts/generate-client-sdk.sh $client
done
```

---

## 📊 架构图

```
game-sdk (核心实现)
    ↓ api依赖
client-a-sdk
    ├── ClientAGameSDK (包装层)
    └── 包名: com.clienta.game.sdk

client-b-sdk  
    ├── GamePlatformSDK (包装层)
    └── 包名: com.clientb.platform.api

client-x-sdk
    ├── ClientXGameSDK (包装层)
    └── 包名: com.clientx.game.sdk
```

---

## ✅ 已生成的客户SDK

| 客户 | 主类 | 包名 | Maven坐标 | 状态 |
|------|------|------|-----------|------|
| Client A | `ClientAGameSDK` | `com.clienta.game.sdk` | `com.clienta:client-a-sdk:2.0.0` | ✅ 生产中 |
| Client B | `GamePlatformSDK` | `com.clientb.platform.api` | `com.clientb:game-platform-sdk:1.0.0` | ✅ 已生成 |

---

## 🎯 优势总结

1. **5分钟为新客户生成SDK** - 只需配置YAML
2. **完全定制化** - 不同包名、类名、品牌标识
3. **维护成本低** - 改一处,所有客户同步
4. **易于扩展** - 支持10+客户无压力
5. **自动化流程** - 脚本化生成,减少人为错误

---

## 📞 技术支持

遇到问题请查看:
- `clients/template.yaml` - 配置模板
- `scripts/generate-client-sdk.sh` - 生成脚本
- `../game-sdk/SDK接入文档.md` - SDK使用文档
