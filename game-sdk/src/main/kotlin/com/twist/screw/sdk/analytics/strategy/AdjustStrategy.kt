package com.twist.screw.sdk.analytics.strategy

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.LogLevel
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.analytics.AnalyticsUtils
import com.twist.screw.sdk.analytics.IAnalyticsStrategy
import com.twist.screw.sdk.utils.CacheManager
import com.twist.screw.sdk.utils.DeviceUtils

class AdjustStrategy : IAnalyticsStrategy {
    private val tag = "Analytics"
    private val adjustEvent = mutableMapOf<String, String>()
    private var isInitSuccess = false

    override fun init() {
        val environment =
            if (GameSDK.getConfig().debugMode) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION
        val adjustConfig =
            AdjustConfig(GameSDK.getContext(), GameSDK.getConfig().adjustAppId, environment)
        adjustConfig.externalDeviceId = DeviceUtils.deviceId
        adjustConfig.setLogLevel(if (GameSDK.getConfig().debugMode) LogLevel.VERBOSE else LogLevel.INFO)
        adjustConfig.setOnAttributionChangedListener { attribution ->
            Log.d(tag, "Adjust onChange attr: $attribution")
        }
        Adjust.addGlobalCallbackParameter("machineEquipmentId", DeviceUtils.deviceId)
        Adjust.addGlobalCallbackParameter("version", GameSDK.getConfig().versionCode.toString())
        if (CacheManager.aDID.isNotEmpty()) {
            Adjust.addGlobalCallbackParameter("adid", CacheManager.aDID)
        }
        AnalyticsUtils.logEvent("user_initAdjust")
        Adjust.initSdk(adjustConfig)
        Adjust.getAttribution { adjustAttribution ->
            isInitSuccess = true
            Log.e(tag, "Adjust init Success Attr: $adjustAttribution")
            GameSDK.getCocosCallback()?.onAdjustAttribution(adjustAttribution.network.lowercase())
            val params = mutableMapOf<String?, Any?>(
                "cost_amount" to if (adjustAttribution.costAmount.isNaN()) null else adjustAttribution.costAmount,
                "network" to adjustAttribution.network,
                "campaign" to adjustAttribution.campaign,
                "adid" to CacheManager.aDID
            )
            AnalyticsUtils.logProgramEvent(
                GameSDK.getConfig().analyticsKeys.eventNames.adjustInit,
                params
            )

            if (CacheManager.isAdjustInit) return@getAttribution

            CacheManager.isAdjustInit = true
            Adjust.getAdid { adid ->
                Log.d("Adjust", "adid = $adid")
                val params = mutableMapOf<String?, Any?>(
                    "cost_amount" to if (adjustAttribution.costAmount.isNaN()) null else adjustAttribution.costAmount,
                    "network" to adjustAttribution.network,
                    "campaign" to adjustAttribution.campaign,
                    "adid" to adid
                )
                params.putAll(adjustAttribution.toMap())
                AnalyticsUtils.logEvent(
                    GameSDK.getConfig().analyticsKeys.eventNames.adjustInit,
                    params
                )
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isInitSuccess) AnalyticsUtils.logEvent("user_init_adjust_fail")
        }, 5000)
    }

    override fun logEvent(name: String, params: Map<String?, Any?>, isProgram: Boolean) {
        if (!isInitSuccess) return
        if (!adjustEvent[name].isNullOrEmpty()) {
            Adjust.trackEvent(AdjustEvent(adjustEvent[name]))
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
        if (!isInitSuccess) return
        val adjustAdRevenue = AdjustAdRevenue("applovin_max_sdk")
        adjustAdRevenue.setRevenue(revenue ?: .0, currency ?: "")
        adjustAdRevenue.adRevenueNetwork = network ?: ""
        adjustAdRevenue.adRevenueUnit = adUnitId ?: ""
        adjustAdRevenue.adRevenuePlacement = placement ?: ""
        Adjust.trackAdRevenue(adjustAdRevenue)
        
        // 向 AliLog 发送技术日志上报
        val logParams = mutableMapOf<String?, Any?>(
            "revenue" to adjustAdRevenue.revenue,
            "network" to adjustAdRevenue.adRevenueNetwork,
            "adUnitId" to adjustAdRevenue.adRevenueUnit,
            "format" to format,
            "placement" to adjustAdRevenue.adRevenuePlacement,
            "currency" to adjustAdRevenue.currency
        )
        AnalyticsUtils.logProgramEvent("adjust_ad_revenue", logParams)
    }

    override fun setUserProperty(name: String, value: String) {
        if (isInitSuccess) Adjust.addGlobalCallbackParameter(name, value)
    }
}
