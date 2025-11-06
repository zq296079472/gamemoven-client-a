# 📘 Game SDK 扩展使用文档

## 目录

1. [自定义埋点Key](#自定义埋点key)
2. [扩展AliLog参数](#扩展alilog参数)
3. [新增Tiger转换器](#新增tiger转换器)
4. [自定义回调处理](#自定义回调处理)
5. [高级配置](#高级配置)

---

## 自定义埋点Key

### 场景

不同的APP可能使用不同的埋点事件名称和参数key。SDK支持通过配置来自定义所有埋点key。

### 步骤

#### 1. 修改JellyfishConstantsRabbit.kt

```kotlin
object JellyfishConstantsRabbit {
    // ================ 埋点事件名称 ================

    // 自定义Adjust初始化事件名
    const val EVENT_ADJUST_INIT = "my_app_adjust_init"  // 原: ascrewssInit

    // 自定义广告播放开始事件名
    const val EVENT_AD_PLAY_START = "my_app_ad_start"   // 原: ascrewssPlay

    // 自定义广告播放结束事件名
    const val EVENT_AD_PLAY_FINISH = "my_app_ad_finish" // 原: ascrewsf

    // 自定义广告播放错误事件名
    const val EVENT_AD_PLAY_ERROR = "my_app_ad_error"   // 原: ascrewse

    // ================ 埋点参数key ================

    // 自定义广告收益参数key
    const val PARAM_AD_REVENUE = "my_revenue"            // 原: adscrewsru

    // 自定义广告类型参数key
    const val PARAM_AD_TYPE = "my_ad_type"               // 原: adscrewstyp

    // ================ AliLog参数key ================

    // 自定义软件名称参数key
    const val ALILOG_SOFTWARE_NAME = "app_name"          // 原: softwarescrewsname

    // 自定义国家参数key
    const val ALILOG_NATION = "country"                  // 原: screwsnation

    // ... 其他AliLog参数key
}
```

#### 2. SDK自动使用新的key值

无需修改其他代码，SDK会自动使用`JellyfishConstantsRabbit`中定义的新key值。

#### 3. 验证

编译并运行APP，检查日志中上报的事件名和参数key是否为新定义的值。

---

## 扩展AliLog参数

### 场景

需要在所有AliLog埋点中添加额外的自定义参数，如渠道ID、AB测试分组等。

### 基础用法

#### 1. 实现IAliLogParamsBuilder接口

```kotlin
private fun createAliLogParamsBuilder() = object : IAliLogParamsBuilder {
    override fun buildExtraParams(
        eventName: String,
        originalParams: Map<String?, Any?>
    ): Map<String?, Any?> {
        // 返回额外参数Map
        return mapOf(
            "channel_id" to "google_play",
            "ab_test_group" to "group_A",
            "build_type" to if (BuildConfig.DEBUG) "debug" else "release"
        )
    }
}
```

#### 2. 在SDK初始化时传入

```kotlin
GameSDK.init(
    app = this,
    config = sdkConfig,
    cocosCallback = cocosCallback,
    uiCallback = uiCallback,
    aliLogParamsBuilder = createAliLogParamsBuilder()  // 传入扩展器
)
```

### 高级用法

#### 场景1: 根据事件类型添加不同参数

```kotlin
override fun buildExtraParams(
    eventName: String,
    originalParams: Map<String?, Any?>
): Map<String?, Any?> {
    val extraParams = mutableMapOf<String?, Any?>()

    when {
        // 广告相关事件
        eventName.startsWith("ad_") || eventName.contains("screws") -> {
            extraParams["ad_source"] = "organic"
            extraParams["ad_mediation"] = "applovin"
        }

        // 游戏相关事件
        eventName.startsWith("game_") -> {
            extraParams["game_level"] = getCurrentGameLevel()
            extraParams["game_mode"] = getCurrentGameMode()
        }

        // 购买相关事件
        eventName.startsWith("purchase_") -> {
            extraParams["payment_method"] = "google_play"
            extraParams["currency"] = "USD"
        }
    }

    return extraParams
}
```

#### 场景2: 根据原始参数动态添加

```kotlin
override fun buildExtraParams(
    eventName: String,
    originalParams: Map<String?, Any?>
): Map<String?, Any?> {
    val extraParams = mutableMapOf<String?, Any?>()

    // 如果原始参数中有vip_level，添加VIP相关信息
    if (originalParams.containsKey("vip_level")) {
        val vipLevel = originalParams["vip_level"] as? Int ?: 0
        extraParams["is_vip"] = vipLevel > 0
        extraParams["vip_benefits"] = getVipBenefits(vipLevel)
    }

    // 如果是特定事件，添加时间戳
    if (eventName in listOf("critical_event", "important_action")) {
        extraParams["precise_timestamp"] = System.currentTimeMillis()
    }

    return extraParams
}
```

#### 场景3: 添加用户画像数据

```kotlin
override fun buildExtraParams(
    eventName: String,
    originalParams: Map<String?, Any?>
): Map<String?, Any?> {
    val userProfile = getUserProfile()  // 从缓存或数据库获取

    return mapOf(
        "user_age_group" to userProfile.ageGroup,
        "user_gender" to userProfile.gender,
        "user_region" to userProfile.region,
        "user_language" to userProfile.language,
        "user_interests" to userProfile.interests.joinToString(",")
    )
}
```

#### 场景4: 添加设备详细信息

```kotlin
override fun buildExtraParams(
    eventName: String,
    originalParams: Map<String?, Any?>
): Map<String?, Any?> {
    return mapOf(
        "screen_width" to getScreenWidth(),
        "screen_height" to getScreenHeight(),
        "screen_density" to getScreenDensity(),
        "os_version" to Build.VERSION.RELEASE,
        "manufacturer" to Build.MANUFACTURER,
        "total_memory" to getTotalMemory(),
        "available_storage" to getAvailableStorage(),
        "battery_level" to getBatteryLevel(),
        "is_charging" to isCharging()
    )
}
```

### AliLog参数构成

```
最终上报的参数 =
    基础参数（13个标准参数，SDK自动添加）
    + 传入参数（logEvent调用时传入）
    + 额外参数（IAliLogParamsBuilder返回）
```

**标准的13个参数**:

1. `softwarescrewsname` - 软件名称
2. `screwsnation` - 国家/地区
3. `machinescrewsid` - 设备ID
4. `device_type` - 设备型号
5. `activityscrewstitle` - 事件名称
6. `activityscrewstime` - 事件时间
7. `activityscrewsvariety` - 事件类型
8. `releasescrewspkg` - 包名
9. `screwsframework` - 框架（android）
10. `playerscrewsid` - 玩家ID
11. `version` - 版本号
12. `releasescrewstag` - 版本名
13. `uid` - 用户ID（特殊处理）

---

## 新增Tiger转换器

### 场景

需要添加新的Cocos调用原生的功能。

### 完整步骤

#### 步骤1: 在JellyfishConstantsRabbit中定义API名称

```kotlin
object JellyfishConstantsRabbit {
    // ... 现有常量 ...

    // 新增API名称
    const val GET_USER_INFO = "GET_USR_INF"    // 获取用户信息
    const val UPDATE_AVATAR = "UPD_AVT"        // 更新头像
}
```

#### 步骤2: 创建转换器类

```kotlin
package com.twist.screw.master.TigerProviderCricket.TigerWidgetConvertActionCricket

import com.twist.screw.master.TigerProviderCricket.BaseTigerGroupConvertSeahorse
import com.twist.screw.master.TigerProviderCricket.TigerJavaToCocosCricket.TigerSeahorseNativeParams

/**
 * 获取用户信息转换器
 */
class TigerGetUserInfoConvert : BaseTigerGroupConvertSeahorse() {
    override fun onReceive(nativeParams: TigerSeahorseNativeParams) {
        // 1. 获取请求参数
        val userId = nativeParams.getRequestParam<String>("userId")

        // 2. 执行业务逻辑
        val userInfo = getUserInfo(userId)

        // 3. 设置响应数据
        nativeParams.putResponseData("userName", userInfo.name)
        nativeParams.putResponseData("userLevel", userInfo.level)
        nativeParams.putResponseData("userAvatar", userInfo.avatar)

        // 4. 回调Cocos
        nativeParams.callCocos()
    }

    private fun getUserInfo(userId: String?): UserInfo {
        // 业务逻辑实现
        return UserInfo("User", 10, "avatar_url")
    }

    data class UserInfo(val name: String, val level: Int, val avatar: String)
}
```

#### 步骤3: 注册转换器

在`TigerConvertFlamingoManagerDelegateImpl.kt`中：

```kotlin
class TigerConvertFlamingoManagerDelegateImpl : TigerConverFlamingoManagerDelegate() {
    private val convert: MutableMap<String, BaseTigerGroupConvertSeahorse> by lazy {
        mutableMapOf<String, BaseTigerGroupConvertSeahorse>().apply {
            // ... 现有21个转换器 ...

            // 新增转换器
            put(JellyfishConstantsRabbit.GET_USER_INFO, TigerGetUserInfoConvert())
            put(JellyfishConstantsRabbit.UPDATE_AVATAR, TigerUpdateAvatarConvert())
        }
    }
}
```

#### 步骤4: Cocos层调用

```javascript
// Cocos JS代码
jsb.reflection.callStaticMethod(
    "com/twist/screw/master/FlyBired/GameFrameworkConnector",
    "callNative",
    "(Ljava/lang/String;Ljava/lang/String;)V",
    "GET_USR_INF",
    JSON.stringify({
        cc_token: "token123",
        data: {
            userId: "user_001"
        }
    })
);

// 接收回调
AndroidNative.callByNative = function(api, responseJson) {
    if (api === "GET_USR_INF") {
        const response = JSON.parse(responseJson);
        console.log("用户信息:", response.data);
        // response.data.userName
        // response.data.userLevel
        // response.data.userAvatar
    }
};
```

### 生命周期感知的转换器

如果转换器需要监听Activity生命周期：

```kotlin
class TigerMyFeatureConvert : BaseTigerGroupConvertSeahorse() {
    override fun onReceive(nativeParams: TigerSeahorseNativeParams) {
        // 处理Cocos调用
    }

    override fun onResume(context: Activity) {
        // Activity恢复时调用
        Log.d(TAG, "Activity resumed")
    }

    override fun onDestroy() {
        // Activity销毁时调用
        Log.d(TAG, "Activity destroyed")
        // 清理资源
    }
}
```

---

## 自定义回调处理

### 扩展ICocosCallback

#### 基础实现

```kotlin
val cocosCallback = object : ICocosCallback {
    override fun notifyCocos(api: String, data: String) {
        Log.d(TAG, "Cocos通知: $api, $data")
        // 可以在这里添加通用的Cocos通知逻辑
    }

    override fun onAdPlayStart(adType: String, revenue: Double) {
        // 广告开始播放
        // 1. 通知原有的GameFrameworkConnector
        AdZebraRatType.fromName(adType)?.let {
            GameFrameworkConnector.adStatusHandler.onAdPlayStart(it, revenue)
        }

        // 2. 可以添加额外的业务逻辑
        logAdEvent("ad_started", adType, revenue)
        updateUIForAdPlaying()
    }

    override fun onAdPlayOver(adType: String, revenue: Double) {
        // 广告播放结束
        AdZebraRatType.fromName(adType)?.let {
            GameFrameworkConnector.adStatusHandler.onAdPlayOver(it, revenue)
        }

        // 额外逻辑
        grantReward(adType, revenue)
        logAdEvent("ad_completed", adType, revenue)
    }

    override fun onAdPlayError(adType: String) {
        // 广告播放错误
        AdZebraRatType.fromName(adType)?.let {
            GameFrameworkConnector.adStatusHandler.onAdPlayError(it)
        }

        // 额外逻辑
        showAdErrorMessage()
        logAdEvent("ad_failed", adType, 0.0)
    }

    override fun onAdjustAttribution(network: String) {
        // Adjust归因
        GameFrameworkConnector.adStatusHandler.onAdjustAttribution(network)

        // 额外逻辑
        saveAttributionData(network)
        logAttributionEvent(network)
    }
}
```

### 扩展IUICallback

```kotlin
val uiCallback = object : IUICallback {
    override fun showNetworkDialog() {
        // 显示网络错误对话框
        SDKWrapper.shared().activity?.let { activity ->
            // 1. 显示原有的对话框
            AlertDialogManager.showNetworkDialogIfNeeded(activity)

            // 2. 可以添加额外的UI反馈
            showToast("网络连接已断开")
            pauseGameIfPlaying()
        }
    }

    override fun updateLoadingState(isLoading: Boolean, showLoading: Boolean) {
        val activity = SDKWrapper.shared().activity as? JellyfishRabbitActivity
        activity?.let {
            // 1. 更新原有的加载状态
            it.updateLoadingState(isLoading, showLoading)

            // 2. 可以添加额外的UI更新
            if (isLoading) {
                it.disableUserInput()
            } else {
                it.enableUserInput()
            }
        }
    }

    override fun hideSplash() {
        val activity = SDKWrapper.shared().activity as? JellyfishRabbitActivity
        activity?.let {
            // 1. 隐藏原有的启动画面
            it.hideSplash()

            // 2. 可以添加额外的启动完成逻辑
            startBackgroundMusic()
            loadInitialData()
        }
    }
}
```

---

## 高级配置

### 配置结构

SDK配置采用分层结构：

```
SDKConfig
├── 第三方SDK配置
│   ├── AppLovin配置（5个字段）
│   ├── Adjust配置（1个字段）
│   ├── AliLog配置（7个字段）
│   └── Facebook配置（4个字段）
│
├── APP信息
│   ├── appName
│   ├── packageName
│   ├── versionName
│   ├── versionCode
│   └── debugMode
│
└── analyticsKeys（埋点Key配置）
    ├── eventNames（事件名称，4个）
    ├── paramKeys（参数key，3个）
    └── aliLogKeys（AliLog参数key，13个）
```

### 完全自定义配置

如果不使用`SDKConfigHelper`，可以手动创建所有配置：

```kotlin
val sdkConfig = SDKConfig(
    // AppLovin配置
    applovinSdkKey = "your_sdk_key",
    applovinBannerUnit = "banner_unit_id",
    applovinInterstitialUnit = "interstitial_unit_id",
    applovinRewardItem = "reward_item_unit_id",
    applovinRewardCash = "reward_cash_unit_id",

    // Adjust配置
    adjustAppId = "your_adjust_app_id",

    // AliLog配置
    aliLogEndpoint = "your_endpoint",
    aliLogProject = "your_project",
    aliLogLogstore = "your_logstore",
    aliLogProgramLogstore = "your_program_logstore",
    aliLogAccessKeyID = "your_access_key_id",
    aliLogAccessKeySecret = "your_access_key_secret",
    logType = "release",

    // Facebook配置
    facebookAppId = "your_facebook_app_id",
    facebookClientToken = "your_client_token",
    facebookAutoInit = "true",
    facebookAutoEvents = "true",

    // APP信息
    appName = "My Game",
    packageName = "com.example.game",
    versionName = "1.0.0",
    versionCode = 1,
    debugMode = false,

    // 埋点Key配置（完全自定义）
    analyticsKeys = AnalyticsKeysConfig(
        eventNames = AnalyticsKeysConfig.EventNames(
            adjustInit = "my_adjust_init",
            adPlayStart = "my_ad_start",
            adPlayFinish = "my_ad_finish",
            adPlayError = "my_ad_error"
        ),
        paramKeys = AnalyticsKeysConfig.ParamKeys(
            adRevenue = "my_revenue",
            adType = "my_type",
            adError = "my_error"
        ),
        aliLogKeys = AnalyticsKeysConfig.AliLogKeys(
            softwareName = "my_app_name",
            nation = "my_country",
            machineId = "my_device_id",
            deviceType = "my_device_type",
            activityTitle = "my_event_name",
            activityTime = "my_event_time",
            activityVariety = "my_event_type",
            releasePackage = "my_package",
            framework = "my_framework",
            playerId = "my_player_id",
            version = "my_version",
            releaseTag = "my_version_name"
        )
    )
)
```

### 部分自定义配置

使用`SDKConfigHelper`创建基础配置，然后修改部分字段：

```kotlin
val sdkConfig = SDKConfigHelper.createConfig(this).copy(
    debugMode = true,  // 覆盖调试模式
    analyticsKeys = AnalyticsKeysConfig(
        // 只自定义事件名称
        eventNames = AnalyticsKeysConfig.EventNames(
            adjustInit = JellyfishConstantsRabbit.EVENT_ADJUST_INIT,
            adPlayStart = "custom_ad_start",  // 自定义
            adPlayFinish = JellyfishConstantsRabbit.EVENT_AD_PLAY_FINISH,
            adPlayError = JellyfishConstantsRabbit.EVENT_AD_PLAY_ERROR
        )
        // paramKeys和aliLogKeys使用默认值
    )
)
```

---

## 实战示例

### 示例1: 完整的SDK接入（带扩展）

```kotlin
class MyGameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MyGameApplication.instance = this

        if (packageName != getProcessName()) return

        // 1. 创建配置
        val sdkConfig = SDKConfigHelper.createConfig(this)

        // 2. 创建回调
        val callbacks = SDKCallbacksFactory.create(this)

        // 3. 初始化SDK
        GameSDK.init(
            app = this,
            config = sdkConfig,
            cocosCallback = callbacks.cocosCallback,
            uiCallback = callbacks.uiCallback,
            aliLogParamsBuilder = callbacks.aliLogParamsBuilder
        )

        // 4. 注入Tiger委托
        setupTigerConverters()

        // 5. 初始化业务模块
        initializeModules()

        Log.i(TAG, "✅ 应用初始化完成")
    }

    private fun setupTigerConverters() {
        val delegate = TigerConverFlamingoManagerDelegate.create()
        ConvertTigerManagerSeahorse.getInstance().setDelegate(delegate)
        ConvertTigerManagerSeahorse.getInstance().addAdapterConvert()
    }

    private fun initializeModules() {
        AnalyticsSysUtils.logProgramEvent("app_launch")
        AnalyticsSysUtils.init(this)
        AdsZebraRatMgr.init(this)
        initAdId()
    }
}

/**
 * 回调工厂类（可复用）
 */
object SDKCallbacksFactory {
    fun create(app: Application) = Callbacks(
        cocosCallback = createCocosCallback(),
        uiCallback = createUICallback(),
        aliLogParamsBuilder = createAliLogParamsBuilder()
    )

    private fun createCocosCallback() = object : ICocosCallback {
        override fun notifyCocos(api: String, data: String) {
            Log.d(TAG, "Cocos: $api")
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

    private fun createUICallback() = object : IUICallback {
        override fun showNetworkDialog() {
            SDKWrapper.shared().activity?.let {
                AlertDialogManager.showNetworkDialogIfNeeded(it)
            }
        }

        override fun updateLoadingState(isLoading: Boolean, showLoading: Boolean) {
            (SDKWrapper.shared().activity as? JellyfishRabbitActivity)?.let {
                it.updateLoadingState(isLoading, showLoading)
            }
        }

        override fun hideSplash() {
            (SDKWrapper.shared().activity as? JellyfishRabbitActivity)?.let {
                it.hideSplash()
            }
        }
    }

    private fun createAliLogParamsBuilder() = object : IAliLogParamsBuilder {
        override fun buildExtraParams(
            eventName: String,
            originalParams: Map<String?, Any?>
        ): Map<String?, Any?> {
            return mapOf(
                "channel_id" to "google_play",
                "ab_test_group" to getABTestGroup()
            )
        }

        private fun getABTestGroup(): String {
            // 从配置或缓存获取AB测试分组
            return "group_A"
        }
    }

    data class Callbacks(
        val cocosCallback: ICocosCallback,
        val uiCallback: IUICallback,
        val aliLogParamsBuilder: IAliLogParamsBuilder
    )

    private const val TAG = "SDKCallbacksFactory"
}
```

---

### 示例2: 添加会话管理的额外参数

```kotlin
class SessionManager {
    private var sessionStartTime: Long = 0
    private var sessionId: String = ""

    fun start() {
        sessionStartTime = System.currentTimeMillis()
        sessionId = UUID.randomUUID().toString()
    }

    fun getSessionDuration(): Long {
        return System.currentTimeMillis() - sessionStartTime
    }
}

// 在Application中
private val sessionManager = SessionManager()

private fun createAliLogParamsBuilder() = object : IAliLogParamsBuilder {
    override fun buildExtraParams(
        eventName: String,
        originalParams: Map<String?, Any?>
    ): Map<String?, Any?> {
        return mapOf(
            "session_id" to sessionManager.sessionId,
            "session_duration" to sessionManager.getSessionDuration(),
            "session_events_count" to getSessionEventsCount()
        )
    }
}

// 在onCreate中
override fun onCreate() {
    super.onCreate()
    sessionManager.start()
    // ... 初始化SDK
}
```

---

### 示例3: 多渠道配置

不同渠道使用不同的埋点key：

```kotlin
object ChannelConfig {
    fun getEventNames(channel: String): AnalyticsKeysConfig.EventNames {
        return when (channel) {
            "google_play" -> AnalyticsKeysConfig.EventNames(
                adjustInit = "gp_adjust_init",
                adPlayStart = "gp_ad_start",
                adPlayFinish = "gp_ad_finish",
                adPlayError = "gp_ad_error"
            )
            "amazon" -> AnalyticsKeysConfig.EventNames(
                adjustInit = "amz_adjust_init",
                adPlayStart = "amz_ad_start",
                adPlayFinish = "amz_ad_finish",
                adPlayError = "amz_ad_error"
            )
            else -> AnalyticsKeysConfig.EventNames()  // 默认值
        }
    }
}

// 在初始化时
val channel = getChannel()  // 从BuildConfig或配置文件获取
val sdkConfig = SDKConfig(
    // ... 其他配置 ...
    analyticsKeys = AnalyticsKeysConfig(
        eventNames = ChannelConfig.getEventNames(channel)
    )
)
```

---

## 调试技巧

### 启用SDK调试日志

```kotlin
val sdkConfig = SDKConfigHelper.createConfig(this).copy(
    debugMode = true  // 启用调试模式
)

// SDK会输出更详细的日志
```

### 检查SDK状态

```kotlin
// 检查SDK是否初始化
if (!GameSDK.isInitialized) {
    Log.e(TAG, "SDK未初始化！")
    return
}

// 检查配置
val config = GameSDK.getConfig()
Log.d(TAG, "Package: ${config.packageName}")
Log.d(TAG, "Version: ${config.versionName}")
Log.d(TAG, "Debug: ${config.debugMode}")

// 检查回调是否设置
val hasCocosCallback = GameSDK.getCocosCallback() != null
val hasUICallback = GameSDK.getUICallback() != null
Log.d(TAG, "Cocos回调: $hasCocosCallback, UI回调: $hasUICallback")
```

### 检查Tiger转换器

```kotlin
val converters = ConvertTigerManagerSeahorse.getInstance().getConvert()
Log.d(TAG, "已注册的转换器数量: ${converters.size}")
Log.d(TAG, "转换器列表: ${converters.keys}")

// 检查特定转换器是否注册
val hasAdConverter = converters.containsKey(JellyfishConstantsRabbit.AD)
Log.d(TAG, "广告转换器已注册: $hasAdConverter")
```

---

## 性能优化

### 懒加载策略

Tiger转换器使用懒加载，只在第一次调用时创建：

```kotlin
private val convert: MutableMap<String, BaseTigerGroupConvertSeahorse> by lazy {
    mutableMapOf<String, BaseTigerGroupConvertSeahorse>().apply {
        // 21个转换器会在这里一次性创建
        put(JellyfishConstantsRabbit.DEVICE_ID, TigerDeviceIdConvert())
        // ...
    }
}
```

### SDK初始化时机

建议在`Application.onCreate()`中尽早初始化SDK：

```kotlin
override fun onCreate() {
    super.onCreate()
    init(this)

    // 进程检查后立即初始化SDK
    if (packageName != getProcessName()) return

    initializeSDK()  // ← 尽早初始化

    // 其他业务逻辑
    initializeBusinessLogic()
}
```

### 异步初始化（高级）

如果SDK初始化较慢，可以考虑异步：

```kotlin
override fun onCreate() {
    super.onCreate()
    init(this)
    if (packageName != getProcessName()) return

    // 异步初始化SDK
    Executors.newSingleThreadExecutor().execute {
        val sdkConfig = SDKConfigHelper.createConfig(this)
        GameSDK.init(this, sdkConfig, cocosCallback, uiCallback)

        runOnUiThread {
            // SDK初始化完成后的回调
            onSDKInitialized()
        }
    }

    // 继续其他初始化（不依赖SDK的部分）
    initializeNonSDKModules()
}
```

---

## 常见错误处理

### 错误1: SDK未初始化就使用

**错误信息**:

```
IllegalStateException: SDK未初始化，请先调用GameSDK.init()
```

**解决方案**:

```kotlin
// 在使用SDK功能前检查
if (!GameSDK.isInitialized) {
    Log.e(TAG, "SDK未初始化")
    return
}

// 或者添加try-catch
try {
    val context = GameSDK.getContext()
} catch (e: IllegalStateException) {
    Log.e(TAG, "SDK未初始化", e)
}
```

### 错误2: 回调未实现导致功能异常

**症状**: 广告播放后Cocos层没有收到回调

**原因**: 未实现或未传入`ICocosCallback`

**解决方案**:

```kotlin
// 确保传入回调
GameSDK.init(
    app = this,
    config = sdkConfig,
    cocosCallback = createCocosCallback(),  // ← 必须传入
    uiCallback = createUICallback()         // ← 必须传入
)
```

### 错误3: Tiger转换器未注册

**症状**: Cocos调用后没有响应

**原因**: 未设置委托或未调用`addAdapterConvert()`

**解决方案**:

```kotlin
// 确保设置委托并注册转换器
val delegate = TigerConverFlamingoManagerDelegate.create()
ConvertTigerManagerSeahorse.getInstance().setDelegate(delegate)
ConvertTigerManagerSeahorse.getInstance().addAdapterConvert()  // ← 必须调用
```

---

## 版本兼容性

### Android版本要求

- **最低版本**: Android 6.0 (API 23)
- **目标版本**: Android 14+ (API 36)
- **推荐版本**: Android 8.0+ (API 26)

### Kotlin版本

- **Kotlin**: 2.1.20
- **Java**: 17

### 第三方SDK版本

| SDK          | 版本     |
|--------------|--------|
| AppLovin MAX | 13.5.0 |
| Firebase BOM | 34.5.0 |
| Adjust       | 5.4.5  |
| Facebook     | 18.1.3 |
| AliLog       | 2.7.13 |

---

## 下一步

阅读更多文档：

- `SDK扩展使用文档.md` - 本文档
- `SDK接入文档.md` - 基础接入指南
- `🎯最终架构总览.md` - 架构详解
- `📚埋点Key管理说明.md` - 埋点配置
- `AliLog参数扩展指南.md` - AliLog扩展
- `🏗️TigerProvider框架架构说明.md` - Tiger框架

---

📅 文档版本: 1.0.0  
📅 更新日期: 2025-11-05  
✅ SDK版本: 1.0.0

