package com.twist.screw.sdk.ads.max

import android.app.Activity
import android.os.Handler
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxRewardedAd
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.ads.AdType
import com.twist.screw.sdk.utils.CacheManager
import java.util.concurrent.TimeUnit
import kotlin.math.pow

typealias MaxRewardItemAd = MaxRewardAd

class MaxRewardCashAd(activity: Activity) : MaxRewardAd(activity) {
    override fun getUnitId(): String {
        return GameSDK.getConfig().applovinRewardCash
    }

    override fun getAdType(): AdType {
        return AdType.cash_reward
    }
}

open class MaxRewardAd(activity: Activity) : BaseMaxAd(activity) {
    override val tag: String = MaxRewardAd::class.java.simpleName
    private lateinit var maxRewardedAd: MaxRewardedAd
    private var rewardedRetryAttempt = 0
    private var maxAd: MaxAd? = null
    override var isLoading = false
    override var loadStartTime = 0L

    override fun getUnitId(): String {
        return GameSDK.getConfig().applovinRewardItem
    }

    override fun getAdType(): AdType {
        return AdType.item_reward
    }

    override fun load() {
        maxRewardedAd = MaxRewardedAd.getInstance(getUnitId(), activity)
        maxRewardedAd.setListener(object : MaxRewardedAdListener {
            override fun onUserRewarded(maxAd: MaxAd, maxReward: MaxReward) {
                Log.i(tag, "rewarded onUserRewarded:\n$maxAd\n$maxReward")
                onAdPlayFinish(maxAd)
                GameSDK.getCocosCallback()?.onAdPlayOver(getAdType().name, maxAd.revenue)
            }

            override fun onAdLoaded(maxAd: MaxAd) {
                Log.i(tag, "rewarded onAdLoaded:\n$maxAd")
                isLoading = false
                this@MaxRewardAd.maxAd = maxAd
                printfLogWithLoaded(maxAd)
                onMaxAdLoadedFinish(maxAd)
                rewardedRetryAttempt = 0
            }

            override fun onAdDisplayed(maxAd: MaxAd) {
                Log.i(tag, "rewarded onAdDisplayed:\n$maxAd")
                onAdStarPlay(maxAd)
                GameSDK.getCocosCallback()?.onAdPlayStart(getAdType().name, maxAd.revenue)
            }

            override fun onAdHidden(maxAd: MaxAd) {
                Log.i(tag, "rewarded onAdHidden:\n$maxAd")
                maxRewardedAd.loadAd()
            }

            override fun onAdClicked(maxAd: MaxAd) {
                Log.i(tag, "rewarded onAdClicked:\n$maxAd")
            }

            override fun onAdLoadFailed(s: String, maxError: MaxError) {
                Log.i(tag, "rewarded onAdLoadFailed:\n$s\n$maxError")
                isLoading = false
                onAdLoadFail(s, maxError)
                printfLogWithLoaded(s, maxError)
                rewardedRetryAttempt++
                val delayMillis = TimeUnit.SECONDS.toMillis(
                    2.0.pow(6.0.coerceAtMost(rewardedRetryAttempt.toDouble())).toLong()
                )
                Handler().postDelayed({ maxRewardedAd.loadAd() }, delayMillis)
            }

            override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) {
                Log.i(tag, "rewarded onAdDisplayFailed:\n$maxAd\n$maxError")
                isLoading = false
                maxRewardedAd.loadAd()
                onAdPlayErr(maxAd, maxError)
                GameSDK.getCocosCallback()?.onAdPlayError(getAdType().name)
            }
        })
        maxRewardedAd.setRevenueListener(this)
        onMaxAdLoadedStar("cash_reward or item_reward")
        isLoading = true
        loadStartTime = System.currentTimeMillis()
        maxRewardedAd.loadAd()
    }

    override fun show() {
        Log.i(tag, "rewarded show:$maxAd")
        if (maxRewardedAd.isReady) {
            maxRewardedAd.showAd(null, "adid=" + CacheManager.aDID, activity)
        } else {
            GameSDK.getCocosCallback()?.onAdPlayError(getAdType().name)
            maxRewardedAd.loadAd()
        }
    }

    override fun destroy() {
        maxRewardedAd.destroy()
    }

    override fun isReady(): Boolean {
        return this::maxRewardedAd.isInitialized && maxRewardedAd.isReady
    }

    fun getAdRevenue(): Double? {
        return if (maxRewardedAd.isReady) maxAd?.revenue else null
    }
}