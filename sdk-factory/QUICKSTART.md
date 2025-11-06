# 🚀 SDK Factory 快速入门指南

## 📋 前置要求

- ✅ JDK 17+
- ✅ Python 3.8+
- ✅ 安装 PyYAML: `pip install pyyaml`

---

## 🎯 5分钟快速体验

### 步骤1：列出已配置的客户

```bash
python scripts/build_sdk.py --list
```

**预期输出**：
```
==============================================================
已配置的客户列表
==============================================================

📦 Client A Gaming Platform (client-a)
   包名: com.clienta.game.sdk
   Maven: com.clienta:game-sdk:1.0.0
   Repository: https://maven.clienta.com/releases

📦 Client B Mobile SDK (client-b)
   包名: com.clientb.mobile.sdk
   Maven: com.clientb:mobile-sdk:1.0.0
   Repository: https://maven.clientb.com/releases

==============================================================
```

---

### 步骤2：生成源码并查看

```bash
# 生成所有客户的源码
./gradlew generateClientSources
```

**查看生成的代码**：
```bash
# 客户A的代码
tree generated/client-a-sdk/src/main/kotlin/

generated/client-a-sdk/src/main/kotlin/
├── com/clienta/game/sdk/
│   └── ClientAGameSDK.kt          # 对外API
└── com/gaming/platform/core/
    ├── GameCore.kt                 # 核心实现
    ├── analytics/
    │   └── AnalyticsEngine.kt
    └── ads/
        └── AdEngine.kt

# 客户B的代码（完全不同的包名！）
tree generated/client-b-sdk/src/main/kotlin/

generated/client-b-sdk/src/main/kotlin/
├── com/clientb/mobile/sdk/
│   └── ClientBMobileSDK.kt         # 不同的API类名
└── com/mobile/core/engine/
    ├── MobileCore.kt               # 不同的核心类名
    ├── analytics/
    │   └── AnalyticsModule.kt      # 不同的模块名
    └── ads/
        └── AdModule.kt
```

✅ **可以看到每个客户的包名、类名都完全不同！**

---

### 步骤3：查看混淆规则

```bash
# 生成混淆规则
./gradlew generateProguardRules

# 查看客户A的混淆规则
cat generated/client-a-sdk/proguard-rules.pro
```

**混淆规则特点**：
- ✅ 每个客户不同的混淆seed
- ✅ 使用不同的混淆字典
- ✅ 激进混淆选项
- ✅ 移除调试日志

---

### 步骤4：构建SDK

```bash
# 构建客户A的SDK
python scripts/build_sdk.py --client client-a
```

**构建产物**：
```
generated/client-a-sdk/build/outputs/aar/
└── client-a-release.aar  # 混淆后的AAR，不包含源码
```

---

### 步骤5：本地测试发布

```bash
# 发布到本地Maven仓库测试
python scripts/build_sdk.py --client client-a --publish --dry-run

# 查看本地Maven
ls ~/.m2/repository/com/clienta/game-sdk/1.0.0/
# 输出: game-sdk-1.0.0.aar  game-sdk-1.0.0.pom
```

---

### 步骤6：发布到远程Maven（真实发布）

#### 6.1 配置认证信息

编辑 `~/.gradle/gradle.properties`:
```properties
client-a.maven.username=your_real_username
client-a.maven.password=your_real_token
```

#### 6.2 执行发布

```bash
# 发布到远程Maven仓库
python scripts/build_sdk.py --client client-a --publish
```

**成功后客户可以这样集成**：
```gradle
repositories {
    maven { url 'https://maven.clienta.com/releases' }
}

dependencies {
    implementation 'com.clienta:game-sdk:1.0.0'
}
```

---

## 🎯 常用场景

### 场景1：核心功能更新

```bash
# 1. 修改 sdk-template/ 中的模板代码
vim sdk-template/core/{{BASE_PACKAGE}}/{{SDK_NAME}}Core.kt

# 2. 更新版本号
vim clients/versions.yaml
# 修改: sdk_version: "1.1.0"

# 3. 为所有客户重新构建和发布
python scripts/build_sdk.py --all --publish
```

---

### 场景2：添加新客户

```bash
# 1. 复制配置模板
cp clients/client-a.yaml clients/client-c.yaml

# 2. 编辑新客户配置
vim clients/client-c.yaml

# 3. 创建混淆字典
cat > clients/dict-clientc.txt << EOF
red
green
blue
EOF

# 4. 构建新客户SDK
python scripts/build_sdk.py --client client-c

# 5. 测试后发布
python scripts/build_sdk.py --client client-c --publish
```

---

### 场景3：单个客户特殊版本

```bash
# 1. 在versions.yaml中覆盖版本
vim clients/versions.yaml
# 添加:
# overrides:
#   client-a:
#     sdk_version: "1.0.5"

# 2. 只重新构建客户A
python scripts/build_sdk.py --client client-a --publish
```

---

## 🐛 故障排查

### 问题1：构建失败

```bash
# 查看详细日志
./gradlew assembleClient-aRelease --stacktrace --info
```

### 问题2：Maven发布认证失败

```bash
# 检查认证配置
echo $CLIENT_A_MAVEN_USERNAME
cat ~/.gradle/gradle.properties | grep client-a
```

### 问题3：源码生成错误

```bash
# 验证YAML配置
python -c "import yaml; yaml.safe_load(open('clients/client-a.yaml'))"

# 手动检查生成的代码
ls -la generated/client-a-sdk/src/main/kotlin/
```

---

## 📊 最佳实践

### ✅ DO（推荐做法）

1. **版本管理**：使用 Git tag 触发自动发布
2. **认证安全**：使用环境变量或Secrets
3. **测试先行**：先 `--dry-run` 测试
4. **定期备份**：备份 clients/ 配置目录
5. **文档同步**：更新 README 说明客户集成方式

### ❌ DON'T（避免做法）

1. **不要**提交生成的代码（generated/）到Git
2. **不要**在配置文件中写明文密码
3. **不要**直接修改生成的代码（会被覆盖）
4. **不要**跳过测试直接发布到生产Maven
5. **不要**删除混淆字典文件（影响增量更新）

---

## 🎉 开始使用

```bash
# 第一次使用，先测试
python scripts/build_sdk.py --client client-a

# 查看生成的代码
cat generated/client-a-sdk/src/main/kotlin/com/clienta/game/sdk/ClientAGameSDK.kt

# 满意后发布
python scripts/build_sdk.py --client client-a --publish --dry-run  # 先测试
python scripts/build_sdk.py --client client-a --publish            # 真实发布
```

**祝您使用愉快！** 🚀

