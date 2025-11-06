package com.twist.screw.sdk.ads.max

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.ads.AdType
import com.twist.screw.sdk.analytics.AnalyticsUtils
import com.twist.screw.sdk.utils.CacheManager
import com.twist.screw.sdk.utils.DeviceUtils
import com.twist.screw.sdk.utils.LocaleUtils

object MaxAdManager : Application.ActivityLifecycleCallbacks {
    private val TAG = MaxAdManager::class.java.simpleName
    private var handler = Handler(Looper.getMainLooper())
    private var waitApplovinInitCb: (() -> Unit)? = null
    private var consentInformation: ConsentInformation? = null
    private var consentForm: ConsentForm? = null
    private var appContext: Application? = null

    @SuppressLint("StaticFieldLeak")
    private var bannerAd: BannerAd? = null

    @SuppressLint("StaticFieldLeak")
    var interstitialAd: InterstitialAd? = null

    @SuppressLint("StaticFieldLeak")
    var rewardItemAd: MaxRewardItemAd? = null

    @SuppressLint("StaticFieldLeak")
    var rewardCashAd: MaxRewardCashAd? = null

    fun init(context: Application) {
        Log.i(TAG, "MaxAdsManager sdkStatus:" + sdkStatus(context))
        val logParams = mutableMapOf<String?, Any?>(
            "MaxAdsManager sdkStatus:" to sdkStatus(context),
        )
        AnalyticsUtils.logProgramEvent(
            "Android_AppLovin_MaxAdsManager_sdkStatus",
            logParams
        )
        if (sdkStatus(context)) return

        appContext = context
        Log.i(TAG, "MaxAdsManager appContext:" + appContext)
        val logParams1 = mutableMapOf<String?, Any?>(
            "MaxAdsManager appContext:" to appContext,
        )
        AnalyticsUtils.logProgramEvent(
            "Android_AppLovin_MaxAdsManager_appContext",
            logParams1
        )
        if (consentInformation == null) {
            consentInformation = UserMessagingPlatform.getConsentInformation(context)
        }

        context.registerActivityLifecycleCallbacks(this)
    }

    fun tryRequestConsentAndInitSdk(activity: Activity) {
        Log.i(TAG, "MaxAdsManager tryRequestConsentAndInitSdk activity: $activity")
        val logParams = mutableMapOf<String?, Any?>(
            "MaxAdsManager tryRequestConsentAndInitSdk activity" to activity,
        )
        AnalyticsUtils.logProgramEvent("Android_AppLovin_tryRequestConsentAndInitSdk", logParams)
        requestConsent(activity) {
            initApplovinSdk(activity.applicationContext as Application)
        }

        waitApplovinInitCb = {
            Log.i(TAG, "waitApplovinInitCb -> loadAd(activity=$activity)")
            val logParams = mutableMapOf<String?, Any?>(
                "Introduce local variable" to activity,
            )
            AnalyticsUtils.logProgramEvent(
                "Android_AppLovin_tryRequestConsentAndInitSdk_waitApplovinInitCb",
                logParams
            )
            loadAd(activity)
        }
    }

    private fun sdkStatus(activity: Context): Boolean {
        val sdkstatus = AppLovinSdk.getInstance(activity).isInitialized
        return sdkstatus
    }

    fun initApplovinSdk(context: Application) {
        // ===== 步骤 1: 设置隐私参数（在初始化前）=====
        val consentStatus = consentInformation?.consentStatus

        // 假设默认是非 GDPR 地区（初始化后会更新）
        val hasConsent: Boolean
        val doNotSell: Boolean

        when (consentStatus) {
            ConsentInformation.ConsentStatus.OBTAINED -> {
                // 用户明确同意
                hasConsent = true
                doNotSell = false
            }

            ConsentInformation.ConsentStatus.NOT_REQUIRED -> {
                // 不需要同意
                hasConsent = true
                doNotSell = false
            }

            ConsentInformation.ConsentStatus.REQUIRED,
            ConsentInformation.ConsentStatus.UNKNOWN,
            null -> {
                // 保守处理：未获得同意
                hasConsent = false
                doNotSell = true
            }

            else -> {
                // 其他状态：保守处理
                hasConsent = false
                doNotSell = true
            }
        }

        // ⚠️ 重要：在初始化 SDK 之前设置隐私参数
        AppLovinPrivacySettings.setHasUserConsent(hasConsent, context)
        AppLovinPrivacySettings.setDoNotSell(doNotSell, context)

        Log.i(
            TAG,
            "Privacy Settings (Before Init) - consentStatus: $consentStatus, hasConsent: $hasConsent, doNotSell: $doNotSell"
        )
        AnalyticsUtils.logProgramEvent(
            "Android_AppLovin_PrivacySettings_BeforeInit",
            mutableMapOf(
                "consentStatus" to (consentStatus?.toString() ?: "NULL"),
                "hasConsent" to hasConsent.toString(),
                "doNotSell" to doNotSell.toString()
            )
        )

        // ===== 步骤 2: 初始化 SDK =====
        val initConfig = AppLovinSdkInitializationConfiguration
            .builder(GameSDK.getConfig().applovinSdkKey, context)
            .setMediationProvider(AppLovinMediationProvider.MAX)
            .build()

        val settings = AppLovinSdk.getInstance(context).settings

        settings.setVerboseLogging(GameSDK.getConfig().debugMode)
        settings.userIdentifier = DeviceUtils.deviceId
        settings.isMuted = true

        Log.i(TAG, "ADID applovin init adid:" + CacheManager.aDID)
        val logParams = mutableMapOf<String?, Any?>(
            "ADID applovin init adid:" to CacheManager.aDID,
        )
        AnalyticsUtils.logProgramEvent("Android_AppLovin_initApplovinSdk_ADID", logParams)

        AppLovinSdk.getInstance(context)
            .initialize(initConfig) { sdkConfig: AppLovinSdkConfiguration ->
                // 记录 SDK 初始化后的地区信息（用于验证）
                val isGdprRegion = sdkConfig.consentFlowUserGeography ==
                        AppLovinSdkConfiguration.ConsentFlowUserGeography.GDPR

                Log.i(
                    TAG,
                    "SDK Initialized - Geography: ${sdkConfig.consentFlowUserGeography}, isGdprRegion: $isGdprRegion"
                )
                val geoLogParams = mutableMapOf<String?, Any?>(
                    "geography" to sdkConfig.consentFlowUserGeography.toString(),
                    "isGdprRegion" to isGdprRegion
                )
                AnalyticsUtils.logProgramEvent("Android_AppLovin_SDK_Geography", geoLogParams)

                Log.i(TAG, "onSdkInitialized: $sdkConfig")
                val logParams = mutableMapOf<String?, Any?>(
                    "onSdkInitialized" to sdkConfig,
                )
                AnalyticsUtils.logProgramEvent(
                    "Android_AppLovin_initApplovinSdk_onSdkInitialized",
                    logParams
                )
                if (!CacheManager.deviceLogRecord.isUserInitMax) {
                    AnalyticsUtils.logProgramEvent("user_init_max")
                    val deviceLogRecord = CacheManager.deviceLogRecord
                    deviceLogRecord.isUserInitMax = true
                    CacheManager.deviceLogRecord = (deviceLogRecord)
                    CacheManager.userMaxInitTime = (LocaleUtils.getCurrentUTCDate())
                }
                AnalyticsUtils.logProgramEvent("user_init_max_success")
                val logParams1 = mutableMapOf<String?, Any?>(
                    "user_init_max_success" to "user_init_max_success",
                )
                AnalyticsUtils.logProgramEvent(
                    "Android_AppLovin_user_init_max_success",
                    logParams1
                )
                waitApplovinInitCb?.invoke()
            }
        startCheckingAdInitialization(context)
    }

    private fun requestConsent(activity: Activity, onReady: () -> Unit) {
        // 记录开始请求同意信息
        val initialStatus = consentInformation?.consentStatus
        Log.i(
            TAG,
            "requestConsent - initialStatus: $initialStatus, hasConsentInfo: ${consentInformation != null}"
        )
        AnalyticsUtils.logProgramEvent(
            "UMP_Consent_Request_Start",
            mutableMapOf(
                "initialStatus" to (initialStatus?.toString() ?: "NULL"),
                "isConsentFormAvailable" to (consentInformation?.isConsentFormAvailable ?: false),
                "hasConsentInfo" to (consentInformation != null)
            )
        )

        // 如果 consentInformation 为 null，直接继续（保守处理）
        if (consentInformation == null) {
            Log.w(TAG, "consentInformation is null, proceeding with limited ads")
            AnalyticsUtils.logProgramEvent(
                "UMP_Consent_Null",
                mutableMapOf("reason" to "consentInformation_is_null")
            )
            onReady()
            return
        }

        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation?.requestConsentInfoUpdate(
            activity,
            params,
            {

                val status = consentInformation?.consentStatus

                val logParams = mutableMapOf<String?, Any?>(
                    "consentStatus" to status,
                    "isConsentFormAvailable" to consentInformation?.isConsentFormAvailable,
                )
                AnalyticsUtils.logProgramEvent("UMP_Consent", logParams)


                if (status == ConsentInformation.ConsentStatus.OBTAINED) {
                    onReady()
                } else if (status == ConsentInformation.ConsentStatus.NOT_REQUIRED) {
                    onReady()
                } else if (status == ConsentInformation.ConsentStatus.REQUIRED && consentInformation?.isConsentFormAvailable == true) {
                    loadConsentForm(activity, onReady)
                } else {
                    Log.w(
                        TAG,
                        "Consent status unknown or not obtained, proceeding with limited ads"
                    )
                    onReady()
                }
            },
            { formError ->
                Log.w(TAG, "UMP Consent request failed: ${formError.message}")

                // 记录请求失败日志
                AnalyticsUtils.logProgramEvent(
                    "UMP_Consent_Request_Failed",
                    mutableMapOf(
                        "errorMessage" to (formError.message ?: "unknown"),
                        "errorCode" to formError.errorCode
                    )
                )

                onReady()
            }
        )
    }

    private fun loadConsentForm(context: Context, onReady: () -> Unit) {
        UserMessagingPlatform.loadConsentForm(
            context,
            { form ->
                consentForm = form
                if (consentInformation?.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    form.show(context as Activity) { formError ->
                        // 用户操作后，延迟一下再初始化，确保 consentStatus 已更新
                        handler.postDelayed({
                            // 记录用户操作后的状态
                            val finalStatus = consentInformation?.consentStatus
                            Log.i(
                                TAG,
                                "After form dismissed - consentStatus: $finalStatus, formError: ${formError?.message}"
                            )
                            AnalyticsUtils.logProgramEvent(
                                "UMP_Consent_AfterUserAction",
                                mutableMapOf(
                                    "consentStatus" to (finalStatus?.toString() ?: "NULL"),
                                    "hasError" to (formError != null),
                                    "errorMessage" to (formError?.message ?: "none")
                                )
                            )
                            onReady()
                        }, 300) // 延迟 300ms 等待状态同步
                    }
                } else {
                    onReady()
                }
            },
            { formError ->
                Log.w(TAG, "UMP loadConsentForm failed: ${formError.message}")

                // 记录表单加载失败日志
                AnalyticsUtils.logProgramEvent(
                    "UMP_ConsentForm_Load_Failed",
                    mutableMapOf(
                        "errorMessage" to (formError.message ?: "unknown"),
                        "errorCode" to formError.errorCode
                    )
                )

                onReady()
            }
        )
    }

    private fun startCheckingAdInitialization(context: Application) {
        val delay = 1000L * 10
        val checkAdStatusRunnable = Runnable {
            if (!sdkStatus(context)) {
                val logParams1 = mutableMapOf<String?, Any?>(
                    "user_init_max_fail" to "user_init_max_fail",
                )
                AnalyticsUtils.logProgramEvent("Android_AppLovin_user_init_max_fail", logParams1)
            }
        }
        handler.postDelayed(checkAdStatusRunnable, delay)
    }

    private fun loadAd(activity: Activity) {
        AnalyticsUtils.logProgramEvent(
            "Android_AppLovin_loadAd", mutableMapOf(
                "sdkStatus" to sdkStatus(activity),
                "hasBanner" to (bannerAd != null),
                "hasInterstitial" to (interstitialAd != null),
                "hasRewardItem" to (rewardItemAd != null),
                "hasRewardCash" to (rewardCashAd != null),
            )
        )
        if (!sdkStatus(activity)) {
            waitApplovinInitCb = { loadAd(activity) }
            return
        }
        if (bannerAd == null) {
            bannerAd = BannerAd(activity)
        }
        bannerAd?.load()

        loadAdByType(AdType.interstitial, activity)
        loadAdByType(AdType.item_reward, activity)
        loadAdByType(AdType.cash_reward, activity)
    }

    fun initBanner(): Boolean {
        return bannerAd != null
    }

    fun showBanner(isShow: Boolean) {
        if (isShow) {
            bannerAd?.show()
        } else {
            bannerAd?.hide()
        }
    }

    fun showRewardItemAd() {
        rewardItemAd?.show()
    }

    fun showRewardCashAd() {
        rewardCashAd?.show()
    }

    fun showInterstitialAd() {
        interstitialAd?.show()
    }

    private fun destroy() {
        waitApplovinInitCb = null
        bannerAd?.destroy()
        bannerAd = null
        interstitialAd?.destroy()
        interstitialAd = null
        rewardItemAd?.destroy()
        rewardItemAd = null
        rewardCashAd?.destroy()
        rewardCashAd = null
    }

    fun getAdRevenue(adType: AdType): Double? {
        return when (adType) {
            AdType.interstitial -> {
                interstitialAd?.getAdRevenue()
            }

            AdType.item_reward -> {
                rewardItemAd?.getAdRevenue()
            }

            AdType.cash_reward -> {
                rewardCashAd?.getAdRevenue()
            }

            else -> null
        }
    }

    fun ensureAdObjectCreated(adType: AdType, activity: Activity) {
        when (adType) {
            AdType.interstitial -> {
                if (interstitialAd == null) {
                    interstitialAd = InterstitialAd(activity)
                    AnalyticsUtils.logProgramEvent("create interstitialAd")
                }
            }

            AdType.item_reward -> {
                if (rewardItemAd == null) {
                    rewardItemAd = MaxRewardItemAd(activity)
                    AnalyticsUtils.logProgramEvent("create rewardItemAd")
                }
            }

            AdType.cash_reward -> {
                if (rewardCashAd == null) {
                    rewardCashAd = MaxRewardCashAd(activity)
                    AnalyticsUtils.logProgramEvent("create rewardCashAd")
                }
            }

            else -> {}
        }
    }

    private fun ensureInitialized(then: () -> Unit) {
        AnalyticsUtils.logProgramEvent(
            "Android_ensureInitialized_ADSdkstatus", mutableMapOf<String?, Any?>(
                "sdkStatus" to (appContext?.let { sdkStatus(it) } ?: false),
            )
        )
        if (appContext != null && sdkStatus(appContext!!)) {
            then()
        } else {
            waitApplovinInitCb = then
            if (appContext != null) {
                init(appContext!!)
            } else {
                val logParams = mutableMapOf<String?, Any?>(
                    "appContext" to "AppLovin 未初始化，appContext 为空，无法初始化",
                )
                AnalyticsUtils.logProgramEvent(
                    "Android_ensureInitialized_appContextIsNull",
                    logParams
                )
                Log.w(TAG, "AppLovin 未初始化，appContext 为空，无法初始化")
            }
        }
    }

    fun loadAdByType(adType: AdType, activity: Activity) {
        ensureInitialized {
            ensureAdObjectCreated(adType, activity)
            val ad = when (adType) {
                AdType.interstitial -> interstitialAd
                AdType.item_reward -> rewardItemAd
                AdType.cash_reward -> rewardCashAd
                else -> null
            } ?: return@ensureInitialized

            if (ad.isReady()) return@ensureInitialized
            if (ad.isLoading && System.currentTimeMillis() - ad.loadStartTime < 10000) return@ensureInitialized

            AnalyticsUtils.logProgramEvent(
                "Android_LoadAd_$adType", mutableMapOf(
                    "adType" to adType.name,
                    "isLoading" to ad.isLoading,
                    "loadStart" to ad.loadStartTime,
                    "ready" to ad.isReady()
                )
            )

            ad.load()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (!sdkStatus(activity) && activity != null) {
            Log.i(TAG, "onActivityCreated -> 触发 AppLovin 初始化")
            val logParams = mutableMapOf<String?, Any?>(
                "onActivityCreated" to "onActivityCreated -> 触发 AppLovin 初始化",
            )
            AnalyticsUtils.logProgramEvent(
                "Android_onActivityCreated_AppLovintryRequestConsentAndInitSdk",
                logParams
            )
            tryRequestConsentAndInitSdk(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        if (!sdkStatus(activity)) {
            tryRequestConsentAndInitSdk(activity)
            val logParams = mutableMapOf<String?, Any?>(
                "content" to "onActivityResumed Max Sdk fail",
            )
            AnalyticsUtils.logProgramEvent("Android_onActivityResumed_MaxSdk_fail", logParams)
        } else {
            val logParams = mutableMapOf<String?, Any?>(
                "content" to "onActivityResumed loadAd",
            )
            AnalyticsUtils.logProgramEvent("Android_onActivityResumed_loadAd", logParams)
            loadAd(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity != null) {
            destroy()
        }
    }
}