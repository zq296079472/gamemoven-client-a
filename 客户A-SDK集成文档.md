# 📦 客户 A SDK 集成文档

## 版本信息
- **SDK 版本**：1.0.0
- **最低 Android 版本**：API 24 (Android 7.0)
- **更新日期**：2025-11-06

---

## 快速集成（5分钟）

### 步骤1：配置 Maven 仓库

在项目的 `settings.gradle.kts` 文件中添加：

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        
        // 客户 A SDK 仓库（添加这段）
        maven {
            url = uri("https://maven.pkg.github.com/zq296079472/gamemoven-client-a")
            credentials {
                username = "zq296079472"
                password = "<YOUR_GITHUB_TOKEN>"
            }
        }
    }
}
```

**如果是老版本 Gradle 项目（build.gradle）**：

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        
        maven {
            url "https://maven.pkg.github.com/zq296079472/gamemoven-client-a"
            credentials {
                username "zq296079472"
                password "<YOUR_GITHUB_TOKEN>"
            }
        }
    }
}
```

---

### 步骤2：添加依赖

在 app 模块的 `build.gradle.kts` 或 `build.gradle` 中添加：

**Kotlin DSL (build.gradle.kts)**：
```kotlin
dependencies {
    implementation("com.clienta:game-sdk:1.0.0")
    
    // 其他依赖...
}
```

**Groovy (build.gradle)**：
```gradle
dependencies {
    implementation 'com.clienta:game-sdk:1.0.0'
    
    // 其他依赖...
}
```

---

### 步骤3：Sync 项目

在 Android Studio 中：
1. 点击右上角 **"Sync Now"**
2. 等待依赖下载完成（首次约 1-2 分钟）
3. 看到 "BUILD SUCCESSFUL" 即成功

---

### 步骤4：初始化 SDK

在你的 `Application` 类中初始化：

```kotlin
import android.app.Application
import com.clienta.game.sdk.ClientAGameSDK

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 SDK
        ClientAGameSDK.getInstance().init(this)
    }
}
```

**不要忘记在 AndroidManifest.xml 注册 Application**：

```xml
<application
    android:name=".MyApplication"
    ...>
</application>
```

---

## API 使用说明

### 记录事件

```kotlin
// 简单事件
ClientAGameSDK.getInstance().logEvent("button_click")

// 带参数的事件
ClientAGameSDK.getInstance().logEvent("purchase", mapOf(
    "item_id" to "item_001",
    "price" to 9.99,
    "currency" to "USD"
))
```

### 设置用户属性

```kotlin
ClientAGameSDK.getInstance().setUserProperty("vip_level", "5")
ClientAGameSDK.getInstance().setUserProperty("user_type", "premium")
```

### 广告功能

```kotlin
// 加载广告
ClientAGameSDK.getInstance().loadAd("your_ad_unit_id")

// 展示广告
ClientAGameSDK.getInstance().showAd()
```

---

## 常见问题

### ❓ 问题1：依赖下载失败

**错误信息**：
```
Could not resolve com.clienta:game-sdk:1.0.0
```

**解决方法**：
1. 检查 `settings.gradle.kts` 中的仓库配置是否正确
2. 确认 `username` 和 `password` 已正确填写
3. 检查网络连接

---

### ❓ 问题2：401 Unauthorized

**错误信息**：
```
Received status code 401 from server: Unauthorized
```

**解决方法**：
- 检查 Token 是否正确
- Token 应该是：`<YOUR_GITHUB_TOKEN>`

---

### ❓ 问题3：编译报错找不到类

**错误信息**：
```
Unresolved reference: ClientAGameSDK
```

**解决方法**：
1. 确认依赖已添加
2. 点击 "Sync Now"
3. Clean Project 后 Rebuild

---

## 📝 完整示例项目

### settings.gradle.kts（完整）

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        
        // 客户 A SDK
        maven {
            url = uri("https://maven.pkg.github.com/zq296079472/gamemoven-client-a")
            credentials {
                username = "zq296079472"
                password = "<YOUR_GITHUB_TOKEN>"
            }
        }
    }
}

rootProject.name = "MyApp"
include(":app")
```

### app/build.gradle.kts（完整）

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.myapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // 客户 A SDK
    implementation("com.clienta:game-sdk:1.0.0")
    
    // Android 基础库
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}
```

### MyApplication.kt（完整）

```kotlin
package com.example.myapp

import android.app.Application
import com.clienta.game.sdk.ClientAGameSDK

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 SDK
        ClientAGameSDK.getInstance().init(this)
        
        // 测试事件
        ClientAGameSDK.getInstance().logEvent("app_start")
    }
}
```

### AndroidManifest.xml（关键部分）

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <application
        android:name=".MyApplication"
        android:label="@string/app_name"
        ...>
        
        <activity android:name=".MainActivity" ...>
        </activity>
    </application>
</manifest>
```

---

## ✅ 集成检查清单

客户完成后检查：

- [ ] `settings.gradle.kts` 已添加 Maven 仓库配置
- [ ] `build.gradle.kts` 已添加依赖：`com.clienta:game-sdk:1.0.0`
- [ ] 点击 Sync Now 成功
- [ ] 创建了 `Application` 类
- [ ] 在 `Application.onCreate()` 中调用 `init()`
- [ ] 在 `AndroidManifest.xml` 注册了 Application
- [ ] 编译成功，无报错

---

## 📞 技术支持

如有问题，请联系：
- 邮箱：support@example.com
- 文档：https://github.com/zq296079472/gamemoven-client-a

---

## 🔄 版本更新

当有新版本发布时，客户只需修改版本号：

```kotlin
implementation("com.clienta:game-sdk:1.0.1")  // 改版本号
```

然后 Sync 即可自动下载新版本。

---

## ⚠️ 重要提示

1. **Token 是公开的**：任何人只要有这个 Token 都可以下载包
2. **Token 权限**：这个 Token 只有读取权限，无法修改你的代码
3. **如需撤销**：可以随时在 GitHub 删除这个 Token，生成新的

---

**集成完成后，客户即可使用所有 SDK 功能！** 🎉

