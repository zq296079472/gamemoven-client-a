# 📘 Game SDK 接入文档

## 📋 目录

1. [SDK简介](#sdk简介)
2. [集成步骤](#集成步骤)
3. [完整接入示例](#完整接入示例)
4. [API参考](#api参考)
5. [常见问题](#常见问题)

---

## SDK简介

### SDK功能

Game SDK是一个集成了以下功能的Android SDK：

- ✅ **广告管理** - AppLovin MAX集成（Banner、插屏、激励视频）
- ✅ **分析统计** - Firebase、Adjust、Facebook、阿里云日志
- ✅ **工具类** - 网络、设备、缓存、文件、本地化等
- ✅ **网络监听** - 网络状态变化监听
- ✅ **Cocos桥接** - 原生与Cocos通信框架

### SDK信息

- **包名**: `com.twist.screw.sdk`
- **最低Android版本**: API 23 (Android 6.0)
- **目标Android版本**: API 36
- **SDK大小**: 约131KB (AAR)

---

## 集成步骤

### 步骤1: 添加SDK依赖

在APP的`build.gradle`中添加：

```gradle
dependencies {
    // 添加SDK依赖
    implementation project(':game-sdk')
    
    // 其他依赖保持不变...
}
```

在项目的`settings.gradle`中添加：

```gradle
include ':libcocos',':libservice',':game-sdk',':app'
project(':game-sdk').projectDir = new File(settingsDir, 'game-sdk')
```

---

### 步骤2: 配置埋点Key（可选）

如果需要自定义埋点key，在`JellyfishConstantsRabbit.kt`中修改：

```kotlin
object JellyfishConstantsRabbit {
    // ... 现有常量 ...
    
    // 埋点事件名称（可自定义）
    const val EVENT_ADJUST_INIT = "ascrewssInit"          // Adjust初始化事件
    const val EVENT_AD_PLAY_START = "ascrewssPlay"        // 广告播放开始
    const val EVENT_AD_PLAY_FINISH = "ascrewsf"          // 广告播放结束
    const val EVENT_AD_PLAY_ERROR = "ascrewse"           // 广告播放错误
    
    // 埋点参数key（可自定义）
    const val PARAM_AD_REVENUE = "adscrewsru"            // 广告收益
    const val PARAM_AD_TYPE = "adscrewstyp"              // 广告类型
    const val PARAM_AD_ERROR = "adError"                  // 广告错误
    
    // AliLog参数key（可自定义）
    const val ALILOG_SOFTWARE_NAME = "softwarescrewsname"  // 软件名称
    const val ALILOG_NATION = "screwsnation"               // 国家/地区
    // ... 其他AliLog参数key
}
```

**注意**: 如果不需要自定义，可跳过此步骤，使用默认值。

---

### 步骤3: 在Application中初始化SDK

在`JellyfishRabbitApplication.kt`的`onCreate()`中：

```kotlin
class JellyfishRabbitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        init(this)
        if (packageName != getProcessName()) return
        
        // ==================== SDK初始化（必须） ====================
        
        // 1. 创建SDK配置
        val sdkConfig = SDKConfigHelper.createConfig(this)
        
        // 2. 创建Cocos回调（必须）
        val cocosCallback = object : ICocosCallback {
            override fun notifyCocos(api: String, data: String) {
                // 通用Cocos通知
            }
            
            override fun onAdPlayStart(adType: String, revenue: Double) {
                val adTypeEnum = AdZebraRatType.fromName(adType)
                if (adTypeEnum != null) {
                    GameFrameworkConnector.adStatusHandler.onAdPlayStart(adTypeEnum, revenue)
                }
            }
            
            override fun onAdPlayOver(adType: String, revenue: Double) {
                val adTypeEnum = AdZebraRatType.fromName(adType)
                if (adTypeEnum != null) {
                    GameFrameworkConnector.adStatusHandler.onAdPlayOver(adTypeEnum, revenue)
                }
            }
            
            override fun onAdPlayError(adType: String) {
                val adTypeEnum = AdZebraRatType.fromName(adType)
                if (adTypeEnum != null) {
                    GameFrameworkConnector.adStatusHandler.onAdPlayError(adTypeEnum)
                }
            }
            
            override fun onAdjustAttribution(network: String) {
                GameFrameworkConnector.adStatusHandler.onAdjustAttribution(network)
            }
        }
        
        // 3. 创建UI回调（必须）
        val uiCallback = object : IUICallback {
            override fun showNetworkDialog() {
                val activity = SDKWrapper.shared().activity
                if (activity != null) {
                    AlertDialogManager.showNetworkDialogIfNeeded(activity)
                }
            }
            
            override fun updateLoadingState(isLoading: Boolean, showLoading: Boolean) {
                val activity = SDKWrapper.shared().activity
                if (activity is JellyfishRabbitActivity) {
                    activity.updateLoadingState(isLoading, showLoading)
                }
            }
            
            override fun hideSplash() {
                val activity = SDKWrapper.shared().activity
                if (activity is JellyfishRabbitActivity) {
                    activity.hideSplash()
                }
            }
        }
        
        // 4. 创建AliLog参数扩展器（可选）
        val aliLogParamsBuilder = object : IAliLogParamsBuilder {
            override fun buildExtraParams(
                eventName: String,
                originalParams: Map<String?, Any?>
            ): Map<String?, Any?> {
                // 返回空Map = 不添加额外参数
                return emptyMap()
                
                // 如需添加额外参数，返回Map：
                // return mapOf(
                //     "channel_id" to "google_play",
                //     "ab_test_group" to getABTestGroup()
                // )
            }
        }
        
        // 5. 初始化SDK（必须）
        GameSDK.init(
            app = this,
            config = sdkConfig,
            cocosCallback = cocosCallback,
            uiCallback = uiCallback,
            aliLogParamsBuilder = aliLogParamsBuilder  // 可选
        )
        
        // 6. 注入Tiger转换器委托（必须，如使用Cocos通信）
        val converterDelegate = TigerConverFlamingoManagerDelegate.create()
        ConvertTigerManagerSeahorse.getInstance().setDelegate(converterDelegate)
        ConvertTigerManagerSeahorse.getInstance().addAdapterConvert()
        
        // ==================== 原有业务逻辑（保持不变） ====================
        
        AnalyticsSysUtils.logProgramEvent("cold_start")
        initAdid()
        AnalyticsSysUtils.init(this)
        AdsZebraRatMgr.init(this)
    }
}
```

---

### 步骤4: 在Activity中连接生命周期

在`JellyfishRabbitActivity.kt`中（如果使用生命周期功能）：

```kotlin
class JellyfishRabbitActivity : JellyfishBaseRabbitActivity() {
    
    override fun onResume() {
        super.onResume()
        // 分发生命周期事件到所有转换器
        GlobalLifecycleTigerDispatcherSeahorse.onResume(this)
    }
    
    override fun onDestroy() {
        // 分发生命周期事件到所有转换器
        GlobalLifecycleTigerDispatcherSeahorse.onDestroy()
        super.onDestroy()
    }
}
```

---

### 步骤5: 验证编译

运行编译命令：

```bash
cd /path/to/project/build/android/proj
./gradlew clean :game-sdk:assembleRelease assembleLuosiRelease
```

期望结果：

```
BUILD SUCCESSFUL
```

---

## 完整接入示例

### JellyfishRabbitApplication.kt 完整代码

```kotlin
package com.twist.screw.master

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.cocos.service.SDKWrapper
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.twist.screw.master.DraTigerDialog.AlertDialogManager
import com.twist.screw.master.FlyBired.GameFrameworkConnector
import com.twist.screw.master.GiraffeRatads.AdZebraRatType
import com.twist.screw.master.GiraffeRatads.AdsZebraRatMgr
import com.twist.screw.master.OxCrocodileGorillaManager.AnalyticsSysUtils
import com.twist.screw.master.OxCrocodileGorillaManager.AnalyticsSysUtils.setUserProperty
import com.twist.screw.master.SysSheepUtils.CacheBeeMgrSysSheepUtils
import com.twist.screw.master.TigerProviderCricket.ConvertTigerManagerSeahorse
import java.util.concurrent.Executors

class JellyfishRabbitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        init(this)
        if (packageName != getProcessName()) return
        
        initializeSDK()
        initializeBusinessLogic()
    }
    
    /**
     * 初始化SDK
     */
    private fun initializeSDK() {
        // 1. 创建SDK配置
        val sdkConfig = SDKConfigHelper.createConfig(this)
        
        // 2. 初始化SDK（包含所有回调）
        GameSDK.init(
            app = this,
            config = sdkConfig,
            cocosCallback = createCocosCallback(),
            uiCallback = createUICallback(),
            aliLogParamsBuilder = createAliLogParamsBuilder()  // 可选
        )
        
        // 3. 注入Tiger转换器委托
        val converterDelegate = com.twist.screw.master.TigerProviderCricket
            .BaseTigerWidgetCricketConvert.TigerDelegateRaven
            .TigerConverFlamingoManagerDelegate.create()
        ConvertTigerManagerSeahorse.getInstance().setDelegate(converterDelegate)
        ConvertTigerManagerSeahorse.getInstance().addAdapterConvert()
        
        Log.i(TAG, "✅ SDK初始化完成")
    }
    
    /**
     * 创建Cocos回调
     */
    private fun createCocosCallback() = object : ICocosCallback {
        override fun notifyCocos(api: String, data: String) {
            Log.d(TAG, "Cocos通知: $api")
        }
        
        override fun onAdPlayStart(adType: String, revenue: Double) {
            AdZebraRatType.fromName(adType)?.let {
                GameFrameworkConnector.adStatusHandler.onAdPlayStart(it, revenue)
            }
        }
        
        override fun onAdPlayOver(adType: String, revenue: Double) {
            AdZebraRatType.fromName(adType)?.let {
                GameFrameworkConnector.adStatusHandler.onAdPlayOver(it, revenue)
            }
        }
        
        override fun onAdPlayError(adType: String) {
            AdZebraRatType.fromName(adType)?.let {
                GameFrameworkConnector.adStatusHandler.onAdPlayError(it)
            }
        }
        
        override fun onAdjustAttribution(network: String) {
            GameFrameworkConnector.adStatusHandler.onAdjustAttribution(network)
        }
    }
    
    /**
     * 创建UI回调
     */
    private fun createUICallback() = object : IUICallback {
        override fun showNetworkDialog() {
            SDKWrapper.shared().activity?.let {
                AlertDialogManager.showNetworkDialogIfNeeded(it)
            }
        }
        
        override fun updateLoadingState(isLoading: Boolean, showLoading: Boolean) {
            val activity = SDKWrapper.shared().activity
            if (activity is JellyfishRabbitActivity) {
                activity.updateLoadingState(isLoading, showLoading)
            }
        }
        
        override fun hideSplash() {
            val activity = SDKWrapper.shared().activity
            if (activity is JellyfishRabbitActivity) {
                activity.hideSplash()
            }
        }
    }
    
    /**
     * 创建AliLog参数扩展器（可选）
     */
    private fun createAliLogParamsBuilder() = object : IAliLogParamsBuilder {
        override fun buildExtraParams(
            eventName: String,
            originalParams: Map<String?, Any?>
        ): Map<String?, Any?> {
            // 不需要额外参数，返回空Map
            return emptyMap()
            
            // 如需添加额外参数：
            // return mapOf(
            //     "channel_id" to "google_play",
            //     "ab_test_group" to "A"
            // )
        }
    }
    
    /**
     * 初始化业务逻辑
     */
    private fun initializeBusinessLogic() {
        AnalyticsSysUtils.logProgramEvent("cold_start")
        initAdid()
        AnalyticsSysUtils.init(this)
        AdsZebraRatMgr.init(this)
    }

    @SuppressLint("AdvertisingIdPolicy")
    private fun initAdid() {
        Executors.newSingleThreadExecutor().execute {
            try {
                val advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
                Log.e(TAG, "ADID from Self: " + advertisingIdInfo.id)
                if (advertisingIdInfo.id != null && advertisingIdInfo.id!!.isNotEmpty()) {
                    CacheBeeMgrSysSheepUtils.aDID = (advertisingIdInfo.id) ?: ""
                    setUserProperty("adid", CacheBeeMgrSysSheepUtils.aDID)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        lateinit var instance: JellyfishRabbitApplication
        private val TAG = JellyfishRabbitApplication.javaClass.simpleName

        fun init(ins: JellyfishRabbitApplication) {
            instance = ins
        }
    }
}
```

---

## API参考

### GameSDK - SDK主类

#### 初始化

```kotlin
GameSDK.init(
    app: Application,                          // 必须：Application实例
    config: SDKConfig,                         // 必须：SDK配置
    cocosCallback: ICocosCallback? = null,     // 可选：Cocos回调
    uiCallback: IUICallback? = null,           // 可选：UI回调
    aliLogParamsBuilder: IAliLogParamsBuilder? = null  // 可选：AliLog扩展
)
```

#### 获取方法

```kotlin
// 获取Application Context
val app: Application = GameSDK.getApplication()

// 获取Context
val context: Context = GameSDK.getContext()

// 获取SDK配置
val config: SDKConfig = GameSDK.getConfig()

// 获取Cocos回调
val callback: ICocosCallback? = GameSDK.getCocosCallback()

// 获取UI回调
val uiCallback: IUICallback? = GameSDK.getUICallback()

// 获取AliLog参数构建器
val builder: IAliLogParamsBuilder? = GameSDK.getAliLogParamsBuilder()

// 检查是否已初始化
val initialized: Boolean = GameSDK.isInitialized
```

---

### SDKConfigHelper - 配置辅助类

#### 创建配置

```kotlin
// 一键创建SDK配置（自动读取BuildConfig和JellyfishConstantsRabbit）
val sdkConfig: SDKConfig = SDKConfigHelper.createConfig(application)
```

---

### SDKConfig - 配置数据类

#### 配置字段

```kotlin
data class SDKConfig(
    // AppLovin配置
    val applovinSdkKey: String,
    val applovinBannerUnit: String,
    val applovinInterstitialUnit: String,
    val applovinRewardItem: String,
    val applovinRewardCash: String,
    
    // Adjust配置
    val adjustAppId: String,
    
    // AliLog配置
    val aliLogEndpoint: String,
    val aliLogProject: String,
    val aliLogLogstore: String,
    val aliLogProgramLogstore: String,
    val aliLogAccessKeyID: String,
    val aliLogAccessKeySecret: String,
    val logType: String,
    
    // Facebook配置
    val facebookAppId: String,
    val facebookClientToken: String,
    val facebookAutoInit: String,
    val facebookAutoEvents: String,
    
    // APP信息
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val debugMode: Boolean = false,
    
    // 埋点Key配置
    val analyticsKeys: AnalyticsKeysConfig = AnalyticsKeysConfig()
)
```

---

### ICocosCallback - Cocos回调接口

#### 必须实现的方法

```kotlin
interface ICocosCallback {
    /**
     * 通用Cocos通知
     * @param api 接口名称
     * @param data JSON数据字符串
     */
    fun notifyCocos(api: String, data: String)
    
    /**
     * 广告播放开始回调
     * @param adType 广告类型名称（REWARD、INTERSTITIAL、BANNER）
     * @param revenue 广告收益
     */
    fun onAdPlayStart(adType: String, revenue: Double)
    
    /**
     * 广告播放结束回调
     * @param adType 广告类型名称
     * @param revenue 广告收益
     */
    fun onAdPlayOver(adType: String, revenue: Double)
    
    /**
     * 广告播放错误回调
     * @param adType 广告类型名称
     */
    fun onAdPlayError(adType: String)
    
    /**
     * Adjust归因回调
     * @param network 广告网络名称
     */
    fun onAdjustAttribution(network: String)
}
```

#### 默认实现

所有方法都有默认实现（空实现），可以选择性覆盖需要的方法。

---

### IUICallback - UI回调接口

#### 必须实现的方法

```kotlin
interface IUICallback {
    /**
     * 显示网络错误对话框
     * SDK检测到网络断开时会调用此方法
     */
    fun showNetworkDialog()
    
    /**
     * 更新加载状态
     * @param isLoading 是否正在加载
     * @param showLoading 是否显示加载动画
     */
    fun updateLoadingState(isLoading: Boolean, showLoading: Boolean = false)
    
    /**
     * 隐藏启动画面
     */
    fun hideSplash()
}
```

---

### IAliLogParamsBuilder - AliLog参数扩展接口（可选）

#### 接口定义

```kotlin
interface IAliLogParamsBuilder {
    /**
     * 构建额外的AliLog参数
     * @param eventName 事件名称
     * @param originalParams 原始参数
     * @return 额外的参数Map，会被合并到最终参数中
     */
    fun buildExtraParams(
        eventName: String,
        originalParams: Map<String?, Any?>
    ): Map<String?, Any?>
}
```

#### 使用示例

```kotlin
// 不需要额外参数
val builder = object : IAliLogParamsBuilder {
    override fun buildExtraParams(...) = emptyMap()
}

// 添加固定的额外参数
val builder = object : IAliLogParamsBuilder {
    override fun buildExtraParams(...): Map<String?, Any?> {
        return mapOf(
            "channel_id" to "google_play",
            "server_id" to "server_001"
        )
    }
}

// 根据事件动态添加参数
val builder = object : IAliLogParamsBuilder {
    override fun buildExtraParams(
        eventName: String,
        originalParams: Map<String?, Any?>
    ): Map<String?, Any?> {
        return when {
            eventName.startsWith("ad_") -> mapOf("ad_source" to "organic")
            eventName.startsWith("game_") -> mapOf("game_level" to getCurrentLevel())
            else -> emptyMap()
        }
    }
}
```

---

### ConvertTigerManagerSeahorse - Tiger转换器管理器

#### 设置委托

```kotlin
// 必须在使用前设置委托
val delegate = TigerConverFlamingoManagerDelegate.create()
ConvertTigerManagerSeahorse.getInstance().setDelegate(delegate)
```

#### 注册转换器

```kotlin
// 注册所有转换器
ConvertTigerManagerSeahorse.getInstance().addAdapterConvert()
```

#### 获取转换器

```kotlin
// 获取所有已注册的转换器
val converters = ConvertTigerManagerSeahorse.getInstance().getConvert()
```

---

## 常见问题

### Q1: SDK初始化失败怎么办？

**检查清单**:

1. ✅ 确认在`Application.onCreate()`中调用
2. ✅ 确认在进程检查之后初始化
3. ✅ 确认SDKConfig所有必须字段都已填写
4. ✅ 检查日志中是否有异常信息

**解决方案**:

```kotlin
try {
    GameSDK.init(this, sdkConfig, cocosCallback, uiCallback)
    Log.i(TAG, "SDK初始化成功")
} catch (e: Exception) {
    Log.e(TAG, "SDK初始化失败", e)
    // 处理初始化失败
}
```

---

### Q2: 如何检查SDK是否已初始化？

```kotlin
if (GameSDK.isInitialized) {
    // SDK已初始化，可以使用
    val context = GameSDK.getContext()
} else {
    // SDK未初始化
    Log.w(TAG, "SDK未初始化")
}
```

---

### Q3: 埋点key如何自定义？

在`JellyfishConstantsRabbit.kt`中修改对应的常量值：

```kotlin
object JellyfishConstantsRabbit {
    // 修改事件名称
    const val EVENT_AD_PLAY_START = "你的自定义事件名"
    
    // 修改参数key
    const val PARAM_AD_REVENUE = "你的自定义参数key"
    
    // 修改AliLog参数key
    const val ALILOG_SOFTWARE_NAME = "你的自定义key"
}
```

然后重新编译即可，SDK会自动使用新的key值。

---

### Q4: 如何添加额外的AliLog参数？

实现`IAliLogParamsBuilder`接口：

```kotlin
val aliLogParamsBuilder = object : IAliLogParamsBuilder {
    override fun buildExtraParams(
        eventName: String,
        originalParams: Map<String?, Any?>
    ): Map<String?, Any?> {
        return mapOf(
            "custom_param1" to "value1",
            "custom_param2" to "value2"
        )
    }
}

GameSDK.init(..., aliLogParamsBuilder = aliLogParamsBuilder)
```

---

### Q5: 如何新增Tiger转换器？

#### 步骤1: 创建转换器类

```kotlin
package com.twist.screw.master.TigerProviderCricket.TigerWidgetConvertActionCricket

import com.twist.screw.master.TigerProviderCricket.BaseTigerGroupConvertSeahorse
import com.twist.screw.master.TigerProviderCricket.TigerJavaToCocosCricket.TigerSeahorseNativeParams

class TigerNewFeatureConvert : BaseTigerGroupConvertSeahorse() {
    override fun onReceive(nativeParams: TigerSeahorseNativeParams) {
        // 业务逻辑
        val param = nativeParams.getRequestParam<String>("key")
        
        // 回调Cocos
        nativeParams.putResponseData("result", "success")
        nativeParams.callCocos()
    }
}
```

#### 步骤2: 在JellyfishConstantsRabbit中定义API名称

```kotlin
object JellyfishConstantsRabbit {
    const val NEW_FEATURE = "NEW_FEAT"
}
```

#### 步骤3: 在TigerConvertFlamingoManagerDelegateImpl中注册

```kotlin
private val convert: MutableMap<String, BaseTigerGroupConvertSeahorse> by lazy {
    mutableMapOf<String, BaseTigerGroupConvertSeahorse>().apply {
        // ... 现有注册 ...
        
        // 新增
        put(JellyfishConstantsRabbit.NEW_FEATURE, TigerNewFeatureConvert())
    }
}
```

---

### Q6: SDK支持哪些第三方库？

#### 广告SDK

- **AppLovin MAX** 13.5.0
- 多个广告网络适配器（InMobi、Vungle、Mintegral、ByteDance等）

#### 分析SDK

- **Firebase Analytics** (BOM 34.5.0)
- **Adjust** 5.4.5
- **Facebook Analytics** 18.1.3
- **阿里云日志** 2.7.13

#### 其他

- **Firebase Crashlytics**
- **Firebase Messaging**
- **Google Play Services**

---

### Q7: 如何在代码中使用SDK的工具类？

```kotlin
// 网络检测
import com.twist.screw.master.SysSheepUtils.NetworkAntCheckerSysSheepUtilsc

val isOnline = NetworkAntCheckerSysSheepUtilsc.isActuallyOnline()
val isWifi = NetworkAntCheckerSysSheepUtilsc.isWifiConnected(context)

// 设备信息
import com.twist.screw.master.SysSheepUtils.DeviceBeeSysSheepUtils

val deviceId = DeviceBeeSysSheepUtils.deviceId
val statusBarHeight = DeviceBeeSysSheepUtils.statusBarHeight

// 缓存管理
import com.twist.screw.master.SysSheepUtils.CacheBeeMgrSysSheepUtils

CacheBeeMgrSysSheepUtils.userId = "user123"
val userId = CacheBeeMgrSysSheepUtils.userId

// 广告管理
import com.twist.screw.master.GiraffeRatads.AdsZebraRatMgr

AdsZebraRatMgr.init(context)
AdsZebraRatMgr.showAdvertiseMent(AdZebraRatType.REWARD)

// 分析统计
import com.twist.screw.master.OxCrocodileGorillaManager.AnalyticsSysUtils

AnalyticsSysUtils.logEvent("event_name", mapOf("key" to "value"))
AnalyticsSysUtils.logProgramEvent("program_event")
```

---

### Q8: 广告如何显示？

```kotlin
import com.twist.screw.master.GiraffeRatads.AdsZebraRatMgr
import com.twist.screw.master.GiraffeRatads.AdZebraRatType

// 显示激励视频
AdsZebraRatMgr.showAdvertiseMent(AdZebraRatType.REWARD)

// 显示插屏广告
AdsZebraRatMgr.showAdvertiseMent(AdZebraRatType.INTERSTITIAL)

// 检查广告是否准备好
val isReady = AdsZebraRatMgr.isAdReady(AdZebraRatType.REWARD)
```

---

### Q9: 分析事件如何上报？

```kotlin
import com.twist.screw.master.OxCrocodileGorillaManager.AnalyticsSysUtils

// 普通事件（上报到用户日志）
AnalyticsSysUtils.logEvent(
    name = "purchase_complete",
    params = mapOf(
        "item_id" to "item_001",
        "price" to 9.99,
        "currency" to "USD"
    )
)

// 程序事件（上报到程序日志）
AnalyticsSysUtils.logProgramEvent(
    name = "app_crash",
    params = mapOf(
        "error_type" to "network_error",
        "error_message" to "Connection timeout"
    )
)

// Facebook专用事件
AnalyticsSysUtils.logToFacebook(
    name = "fb_mobile_purchase",
    value = 9.99,
    currency = "USD",
    params = mapOf("content_type" to "product")
)

// Adjust专用事件
AnalyticsSysUtils.logAdjust(
    eventToken = "abc123",
    revenue = 9.99,
    currency = "USD"
)
```

---

### Q10: 如何处理网络变化？

SDK的网络接收器会自动监听网络变化，并通过`IUICallback.showNetworkDialog()`通知APP。

APP只需实现回调即可：

```kotlin
val uiCallback = object : IUICallback {
    override fun showNetworkDialog() {
        // SDK检测到网络断开，在这里显示对话框
        AlertDialogManager.showNetworkDialogIfNeeded(activity)
    }
}
```

---

## 集成检查清单

### 必须完成的步骤

- [ ] 
    1. 在`build.gradle`中添加SDK依赖
- [ ] 
    2. 在`settings.gradle`中添加SDK模块
- [ ] 
    3. 在`JellyfishConstantsRabbit`中配置埋点key（如需自定义）
- [ ] 
    4. 创建`SDKConfigHelper`（已提供）
- [ ] 
    5. 在`Application.onCreate()`中初始化SDK
- [ ] 
    6. 实现`ICocosCallback`回调
- [ ] 
    7. 实现`IUICallback`回调
- [ ] 
    8. 注入Tiger转换器委托
- [ ] 
    9. 验证编译通过
- [ ] 
    10. 测试功能是否正常

### 可选步骤

- [ ] 实现`IAliLogParamsBuilder`添加额外参数
- [ ] 在Activity中连接生命周期事件
- [ ] 添加SDK初始化失败的错误处理

---

## 迁移对比

### 原有代码（迁移前）

```kotlin
class JellyfishRabbitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        init(this)
        if (packageName != getProcessName()) return
        
        // 直接初始化各个模块
        AnalyticsSysUtils.logProgramEvent("cold_start")
        initAdid()
        AnalyticsSysUtils.init(this)
        AdsZebraRatMgr.init(this)
        ConvertTigerManagerSeahorse.getInstance().addAdapterConvert()
    }
}
```

### 新代码（使用SDK）

```kotlin
class JellyfishRabbitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        init(this)
        if (packageName != getProcessName()) return
        
        // 1. 初始化SDK
        val sdkConfig = SDKConfigHelper.createConfig(this)
        GameSDK.init(this, sdkConfig, cocosCallback, uiCallback)
        
        // 2. 注入转换器委托
        val delegate = TigerConverFlamingoManagerDelegate.create()
        ConvertTigerManagerSeahorse.getInstance().setDelegate(delegate)
        ConvertTigerManagerSeahorse.getInstance().addAdapterConvert()
        
        // 3. 原有业务逻辑（完全不变）
        AnalyticsSysUtils.logProgramEvent("cold_start")
        initAdid()
        AnalyticsSysUtils.init(this)
        AdsZebraRatMgr.init(this)
    }
}
```

**差异**:

- ✅ 添加了SDK初始化
- ✅ 添加了回调实现
- ✅ 添加了委托注入
- ✅ 原有业务逻辑**完全保持不变**

---

## 技术支持

如有问题，请参考：

1. `SDK扩展使用文档.md` - 高级用法和扩展指南
2. `🎯最终架构总览.md` - 完整架构说明
3. `📚埋点Key管理说明.md` - 埋点配置详解
4. `AliLog参数扩展指南.md` - AliLog扩展机制
5. `🏗️TigerProvider框架架构说明.md` - Tiger框架详解

---

📅 文档版本: 1.0.0  
📅 更新日期: 2025-11-05  
✅ SDK版本: 1.0.0

