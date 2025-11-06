package com.twist.screw.sdk.analytics.strategy

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.analytics.AnalyticsUtils
import com.twist.screw.sdk.analytics.IAnalyticsStrategy

class FacebookStrategy : IAnalyticsStrategy {

    private var logger: AppEventsLogger? = null
    private val TAG = "FacebookStrategy"

    @RequiresApi(Build.VERSION_CODES.N)
    override fun init() {
        try {
            val context = GameSDK.getApplication()

            // 通过反射安全获取 isFullyInitialized 状态（兼容 v18+）
            val isFullyInitialized = runCatching {
                val field = FacebookSdk::class.java.getDeclaredField("isFullyInitialized")
                field.isAccessible = true
                field.getBoolean(null)
            }.getOrDefault(false)

            Log.i(
                TAG,
                "Facebook SDK init start, isInitialized=${FacebookSdk.isInitialized()}, isFullyInitialized=$isFullyInitialized"
            )

            // ============ 1️⃣ 如果 SDK 已经完全初始化 ============
            if (isFullyInitialized) {
                logger = AppEventsLogger.newLogger(context)
                Log.i(TAG, "Facebook SDK already fully initialized")

                AnalyticsUtils.logProgramEvent(
                    "Android_Facebook_Init_AlreadyInitialized",
                    mutableMapOf("status" to "already_initialized_fully")
                )
                return
            }

            // ============ 2️⃣ 手动初始化流程 ============
            AnalyticsUtils.logProgramEvent(
                "Android_Facebook_Init_Start",
                mutableMapOf("action" to "start_init")
            )

            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.setAutoLogAppEventsEnabled(true)
            FacebookSdk.setAdvertiserIDCollectionEnabled(true)

            // ✅ 优先尝试新版 SDK（v15+）异步初始化
            try {
                val fullyInitMethod =
                    FacebookSdk::class.java.methods.find { it.name == "fullyInitializeAsync" }
                if (fullyInitMethod != null) {
                    Log.i(TAG, "Facebook SDK using fullyInitializeAsync()")

                    val future =
                        fullyInitMethod.invoke(null) as? java.util.concurrent.CompletableFuture<*>
                    future?.thenRun {
                        Log.i(TAG, "Facebook SDK fully initialized (async)")

                        Handler(Looper.getMainLooper()).post {
                            AppEventsLogger.activateApp(context)
                            logger = AppEventsLogger.newLogger(context)
                        }

                        AnalyticsUtils.logProgramEvent(
                            "Android_Facebook_Init_Success",
                            mutableMapOf(
                                "status" to "success_async",
                                "isFullyInitialized" to "true"
                            )
                        )
                    }?.exceptionally { throwable ->
                        Log.e(TAG, "Facebook SDK fullyInitializeAsync() failed", throwable)
                        AnalyticsUtils.logProgramEvent(
                            "Android_Facebook_Init_Failed",
                            mutableMapOf(
                                "status" to "failed_async",
                                "error" to (throwable.message ?: "unknown")
                            )
                        )
                        null
                    }

                    // 注意：异步初始化，直接 return
                    return
                }
            } catch (asyncEx: Throwable) {
                Log.w(
                    TAG,
                    "fullyInitializeAsync not found, fallback to sync init: ${asyncEx.message}"
                )
            }

            // ✅ 旧版 SDK（v14 及以下）回退同步初始化
            try {
                FacebookSdk.fullyInitialize()
            } catch (ignored: Throwable) {
                FacebookSdk.sdkInitialize(context)
            }

            // 确保在主线程激活应用
            Handler(Looper.getMainLooper()).post {
                AppEventsLogger.activateApp(context)
                logger = AppEventsLogger.newLogger(context)
            }

            Log.i(
                TAG,
                "Facebook SDK initialized successfully (sync mode), isInitialized=${FacebookSdk.isInitialized()}"
            )

            AnalyticsUtils.logProgramEvent(
                "Android_Facebook_Init_Success",
                mutableMapOf(
                    "status" to "success_sync",
                    "isInitialized" to FacebookSdk.isInitialized().toString()
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Facebook SDK", e)
            AnalyticsUtils.logProgramEvent(
                "Android_Facebook_Init_Failed",
                mutableMapOf(
                    "status" to "failed",
                    "error" to (e.message ?: "unknown"),
                    "errorType" to e.javaClass.simpleName
                )
            )
        }
    }


    override fun logEvent(name: String, params: Map<String?, Any?>, isProgram: Boolean) {
        // ============ 0️⃣ SDK 就绪检查 ============
        val sdkReady = runCatching {
            val field = FacebookSdk::class.java.getDeclaredField("isFullyInitialized")
            field.isAccessible = true
            field.getBoolean(null)
        }.getOrDefault(false)

        if (!sdkReady) {
            Log.w(TAG, "Facebook SDK not fully initialized, skip event: $name")

            AnalyticsUtils.logProgramEvent(
                "Android_Facebook_Event_Skip_NotReady",
                mutableMapOf(
                    "event_name" to name,
                    "param_count" to params.size.toString(),
                    "error" to "sdk_not_fully_initialized"
                )
            )
            return
        }

        val l = logger ?: run {
            Log.w(TAG, "Facebook logger is null, event ignored: $name")

            AnalyticsUtils.logProgramEvent(
                "Android_Facebook_Event_NotInitialized",
                mutableMapOf(
                    "event_name" to name,
                    "param_count" to params.size.toString(),
                    "error" to "logger_is_null"
                )
            )
            return
        }

        if (params.isEmpty()) {
            l.logEvent(name)
            Log.d(TAG, "Facebook event logged (no params): $name")
            return
        }

        // ============ 1️⃣ 构建参数 ============
        try {
            val b = Bundle()
            var validParamCount = 0

            params.forEach { (k, v) ->
                if (k.isNullOrEmpty() || v == null) return@forEach
                when (v) {
                    is String -> {
                        b.putString(k, v)
                        validParamCount++
                    }

                    is Int -> {
                        b.putInt(k, v)
                        validParamCount++
                    }

                    is Long -> {
                        b.putLong(k, v)
                        validParamCount++
                    }

                    is Double -> {
                        if (v.isFinite()) {
                            b.putDouble(k, v)
                            validParamCount++
                        }
                    }

                    is Float -> {
                        if (v.isFinite()) {
                            b.putDouble(k, v.toDouble())
                            validParamCount++
                        }
                    }

                    is Boolean -> {
                        // ✅ 保留为字符串形式，便于分析端兼容
                        b.putString(k, v.toString())
                        validParamCount++
                    }

                    else -> {
                        // 非支持类型，忽略但记录一次警告
                        Log.w(
                            TAG,
                            "Unsupported param type for key=$k, type=${v::class.java.simpleName}"
                        )
                    }
                }
            }

            // ============ 2️⃣ 上报事件 ============
            l.logEvent(name, b)
            Log.d(
                TAG,
                "Facebook event logged: $name with $validParamCount params (total=${params.size})"
            )

            AnalyticsUtils.logProgramEvent(
                "Android_Facebook_Event_Success",
                mutableMapOf(
                    "event_name" to name,
                    "total_params" to params.size.toString(),
                    "valid_params" to validParamCount.toString()
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to log Facebook event: $name", e)
            AnalyticsUtils.logProgramEvent(
                "Android_Facebook_Event_Failed",
                mutableMapOf(
                    "event_name" to name,
                    "param_count" to params.size.toString(),
                    "error" to (e.message ?: "unknown"),
                    "errorType" to e.javaClass.simpleName
                )
            )
        }
    }


    override fun logAdRevenue(
        revenue: Double?,
        network: String?,
        adUnitId: String?,
        format: String?,
        placement: String?,
        currency: String?
    ) {
//        val l = logger ?: return
//        // Facebook 对第三方广告收入没有专用 API，用自定义事件上报
//        val b = Bundle().apply {
//            putString("ad_network", network ?: "")
//            putString("ad_unit_id", adUnitId ?: "")
//            putString("ad_format", format ?: "")
//            putString("placement", placement ?: "")
//            if (revenue != null && revenue.isFinite()) putDouble("value", revenue)
//            putString("currency", currency ?: "USD")
//            putString("sdk", "applovin_max")
//        }
//        l.logEvent("ad_revenue", b)
    }

    override fun setUserProperty(name: String, value: String) {
        // App Events没有通用的自定义属性API：仅支持 userID / userData
        // 这里做常见映射：user_id → setUserID；其余忽略或走 setUserData（需要更多 PII 字段时）
        if (name.equals("user_id", ignoreCase = true)) {
            AppEventsLogger.setUserID(value)
        }
        // AppEventsLogger.setUserData() 仅在你要做高级匹配（email/phone）时使用
    }
}
