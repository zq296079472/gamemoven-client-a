package com.twist.screw.sdk

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.twist.screw.sdk.ads.AdManager
import com.twist.screw.sdk.analytics.AnalyticsUtils
import com.twist.screw.sdk.bridge.ConverterManager
import com.twist.screw.sdk.bridge.IConverterDelegate
import com.twist.screw.sdk.utils.CacheManager
import java.util.concurrent.Executors

/**
 * Game SDK主类 - 管理SDK的初始化和全局访问
 */
object GameSDK {
    private const val TAG = "GameSDK"

    private var application: Application? = null
    private var sdkConfig: SDKConfig? = null
    private var cocosCallback: ICocosCallback? = null
    private var uiCallback: IUICallback? = null
    private var aliLogParamsBuilder: IAliLogParamsBuilder? = null
    private var converterDelegate: IConverterDelegate? = null

    /**
     * SDK是否已初始化
     */
    val isInitialized: Boolean
        get() = application != null && sdkConfig != null

    /**
     * 初始化SDK（自动处理通用逻辑）
     */
    fun init(
        app: Application,
        config: SDKConfig,
        cocosCallback: ICocosCallback? = null,
        uiCallback: IUICallback? = null,
        aliLogParamsBuilder: IAliLogParamsBuilder? = null,
        converterDelegate: IConverterDelegate? = null
    ) {
        // 1. 多进程检查（自动）
        if (app.packageName != getProcessName(app)) {
            Log.i(TAG, "Not main process, skip SDK initialization")
            return
        }

        // 2. 保存配置
        this.application = app
        this.sdkConfig = config
        this.cocosCallback = cocosCallback
        this.uiCallback = uiCallback
        this.aliLogParamsBuilder = aliLogParamsBuilder
        this.converterDelegate = converterDelegate

        // 3. 初始化ADID（自动，后台线程）
        initADID()

        // 4. 记录冷启动（自动）
        AnalyticsUtils.logProgramEvent("cold_start")

        // 5. 初始化分析和广告（自动）
        AnalyticsUtils.init(app)
        AdManager.init(app)

        // 6. 注册转换器（自动）
        ConverterManager.getInstance().addAdapterConvert()

        Log.i(TAG, "✅ SDK初始化完成")
    }

    /**
     * 获取进程名称
     */
    private fun getProcessName(app: Application): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else {
            // 兼容旧版本
            try {
                val activityThread = Class.forName("android.app.ActivityThread")
                val currentProcessName = activityThread.getDeclaredMethod("currentProcessName")
                currentProcessName.invoke(null) as? String
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get process name", e)
                null
            }
        }
    }

    /**
     * 初始化Google Advertising ID（自动，后台线程）
     */
    @SuppressLint("AdvertisingIdPolicy")
    private fun initADID() {
        Executors.newSingleThreadExecutor().execute {
            try {
                val advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(application!!)
                Log.i(TAG, "ADID from SDK: ${advertisingIdInfo.id}")
                if (!advertisingIdInfo.id.isNullOrEmpty()) {
                    CacheManager.aDID = advertisingIdInfo.id ?: ""
                    AnalyticsUtils.setUserProperty("adid", CacheManager.aDID)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get ADID", e)
            }
        }
    }

    /**
     * 获取Application Context
     */
    fun getApplication(): Application {
        return application ?: throw IllegalStateException("SDK未初始化，请先调用GameSDK.init()")
    }

    /**
     * 获取Application Context（兼容）
     */
    fun getContext(): Context {
        return getApplication()
    }

    /**
     * 获取SDK配置
     */
    fun getConfig(): SDKConfig {
        return sdkConfig ?: throw IllegalStateException("SDK未初始化，请先调用GameSDK.init()")
    }

    /**
     * 获取Cocos回调
     */
    fun getCocosCallback(): ICocosCallback? {
        return cocosCallback
    }

    /**
     * 获取UI回调
     */
    fun getUICallback(): IUICallback? {
        return uiCallback
    }

    /**
     * 获取AliLog参数构建器
     */
    fun getAliLogParamsBuilder(): IAliLogParamsBuilder? {
        return aliLogParamsBuilder
    }

    /**
     * 获取转换器委托
     */
    fun getConverterDelegate(): IConverterDelegate? {
        return converterDelegate
    }
}

/**
 * Cocos通信回调接口
 */
interface ICocosCallback {
    /**
     * 通知Cocos层
     * @param api 接口名称
     * @param data JSON数据
     */
    fun notifyCocos(api: String, data: String)

    /**
     * 通知广告播放开始
     */
    fun onAdPlayStart(adType: String, revenue: Double) {
        // 默认实现为空
    }

    /**
     * 通知广告播放结束
     */
    fun onAdPlayOver(adType: String, revenue: Double) {
        // 默认实现为空
    }

    /**
     * 通知广告播放错误
     */
    fun onAdPlayError(adType: String) {
        // 默认实现为空
    }

    /**
     * 通知Adjust归因
     */
    fun onAdjustAttribution(network: String) {
        // 默认实现为空
    }
}

/**
 * UI交互回调接口
 */
interface IUICallback {
    /**
     * 显示网络错误对话框
     */
    fun showNetworkDialog()

    /**
     * 更新加载状态
     * @param isLoading 是否加载中
     * @param showLoading 是否显示加载动画
     */
    fun updateLoadingState(isLoading: Boolean, showLoading: Boolean = false)

    /**
     * 隐藏启动画面
     */
    fun hideSplash()
}

/**
 * AliLog额外参数构建器接口
 * APP层可以实现此接口来添加自定义的额外参数
 */
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
    ): Map<String?, Any?> {
        // 默认不添加额外参数
        return emptyMap()
    }
}

