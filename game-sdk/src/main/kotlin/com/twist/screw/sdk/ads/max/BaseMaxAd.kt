package com.twist.screw.sdk.ads.max

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdRevenueListener
import com.applovin.mediation.MaxError
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.ads.AdType
import com.twist.screw.sdk.analytics.AnalyticsUtils
import com.twist.screw.sdk.utils.CacheManager
import java.util.concurrent.TimeUnit

abstract class BaseMaxAd(val activity: Activity) : MaxAdRevenueListener {
    abstract val loadStartTime: Long
    abstract val isLoading: Boolean
    open val tag: String = BaseMaxAd::class.java.simpleName
    abstract fun getUnitId(): String
    abstract fun getAdType(): AdType
    abstract fun load()
    abstract fun show()
    abstract fun destroy()
    abstract fun isReady(): Boolean

    fun retryWithExponentialBackoff(attempt: Int, action: () -> Unit) {
        val delay = TimeUnit.SECONDS.toMillis(Math.pow(2.0, minOf(6, attempt).toDouble()).toLong())
        Handler(Looper.getMainLooper()).postDelayed({ action() }, delay)
    }

    fun onAdLoadFail(s: String, maxError: MaxError) {
        AnalyticsUtils.logProgramEvent(
            "ad_loadFail",
            params = mutableMapOf("ad_error" to maxError.toString())
        )
    }

    fun onMaxAdLoadedFinish(maxAd: MaxAd) {
        AnalyticsUtils.logProgramEvent(
            "ad_AdLoadedFinish",
            params = mutableMapOf("ad_revenue" to maxAd.revenue, "ad_type" to getAdType().name)
        )
    }

    fun onMaxAdLoadedStar(s: String) {
        AnalyticsUtils.logProgramEvent("ad_StarAdLoaded", params = mutableMapOf("ad_type" to s))
    }

    fun onAdStarPlay(maxAd: MaxAd) {
        val keys = GameSDK.getConfig().analyticsKeys
        AnalyticsUtils.logEvent(
            keys.eventNames.adPlayStart,
            params = mutableMapOf(
                keys.paramKeys.adRevenue to maxAd.revenue,
                keys.paramKeys.adType to getAdType().name
            )
        )
    }

    fun onAdPlayFinish(maxAd: MaxAd) {
        val keys = GameSDK.getConfig().analyticsKeys
        AnalyticsUtils.logEvent(
            keys.eventNames.adPlayFinish,
            params = mutableMapOf(
                keys.paramKeys.adRevenue to maxAd.revenue,
                keys.paramKeys.adType to getAdType().name
            )
        )
    }

    fun onAdPlayErr(maxAd: MaxAd, maxError: MaxError) {
        val keys = GameSDK.getConfig().analyticsKeys
        AnalyticsUtils.logEvent(
            keys.eventNames.adPlayError,
            params = mutableMapOf(keys.paramKeys.adError to maxError.toString())
        )
    }

    override fun onAdRevenuePaid(maxAd: MaxAd) {
        AnalyticsUtils.logAdRevenue(maxAd)
        val logParams: MutableMap<String?, Any?> = HashMap()
        logParams["adKey"] = maxAd.adUnitId
        logParams["networkName"] = maxAd.networkName
        logParams["revenue"] = maxAd.revenue.toString()
        logParams["format"] = maxAd.format.label
        if (maxAd.format === MaxAdFormat.INTERSTITIAL) {
        } else if (maxAd.format === MaxAdFormat.REWARDED || maxAd.format === MaxAdFormat.REWARDED_INTERSTITIAL) {
            AnalyticsUtils.logProgramEvent("reward_Ad_Show", logParams)
        }
        if (!CacheManager.deviceLogRecord.isUserFirstVideo) {
            val deviceLogRecord = CacheManager.deviceLogRecord
            deviceLogRecord.isUserFirstVideo = true
            CacheManager.deviceLogRecord = (deviceLogRecord)
        }
    }

    fun printfLogWithLoaded(ad: MaxAd) {
        if (GameSDK.getConfig().debugMode) {
            val waterfall = ad.waterfall ?: return
            Log.i(
                tag,
                "Waterfall Name: " + waterfall.name + " and Test Name: " + waterfall.testName
            )
            Log.i(tag, "Waterfall latency was: " + waterfall.latencyMillis + " milliseconds")

            var waterfallInfoStr: String
            for (networkResponse in waterfall.networkResponses) {
                waterfallInfoStr = """Network -> ${networkResponse.mediatedNetwork}
...adLoadState: ${networkResponse.adLoadState}
...latency: ${networkResponse.latencyMillis} milliseconds
...credentials: ${networkResponse.credentials}"""
                if (networkResponse.error != null) {
                    waterfallInfoStr += """
                        
                        ...error: ${networkResponse.error}
                        """.trimIndent()
                }
                Log.i(tag, waterfallInfoStr)
            }
        }
    }

    fun printfLogWithLoaded(adUnitId: String, error: MaxError) {
        val waterfall = error.waterfall ?: return
        Log.i(tag, "Waterfall Name: " + waterfall.name + " and Test Name: " + waterfall.testName)
        Log.i(tag, "Waterfall latency was: " + waterfall.latencyMillis + " milliseconds")

        for (networkResponse in waterfall.networkResponses) {
            Log.i(
                tag, "Network -> " + networkResponse.mediatedNetwork +
                        "...latency: " + networkResponse.latencyMillis +
                        "...credentials: " + networkResponse.credentials + " milliseconds" +
                        "...error: " + networkResponse.error
            )
        }
    }
}