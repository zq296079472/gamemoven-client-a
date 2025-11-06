package com.twist.screw.sdk.ads.max

import android.app.Activity
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.ads.AdType
import com.twist.screw.sdk.utils.CacheManager

class InterstitialAd(activity: Activity) : BaseMaxAd(activity) {
    override val tag: String = MaxInterstitialAd::class.java.simpleName
    private lateinit var maxInterstitialAd: MaxInterstitialAd
    private var interstitialRetryAttempt = 0
    override var isLoading = false
    override var loadStartTime = 0L
    private var maxAd: MaxAd? = null
    override fun getUnitId(): String {
        return GameSDK.getConfig().applovinInterstitialUnit
    }

    override fun getAdType(): AdType {
        return AdType.interstitial
    }

    override fun load() {
        maxInterstitialAd = MaxInterstitialAd(getUnitId(), activity)
        maxInterstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(maxAd: MaxAd) {
                isLoading = false
                this@InterstitialAd.maxAd = maxAd
                Log.i(tag, "interstitial onAdLoaded:\n$maxAd")
                printfLogWithLoaded(maxAd)
                onMaxAdLoadedFinish(maxAd)
                interstitialRetryAttempt = 0
            }

            override fun onAdDisplayed(maxAd: MaxAd) {
                onAdStarPlay(maxAd)
                Log.i(tag, "interstitial onAdDisplayed:\n$maxAd")
                GameSDK.getCocosCallback()?.onAdPlayStart(getAdType().name, maxAd.revenue)
            }

            override fun onAdHidden(maxAd: MaxAd) {
                onAdPlayFinish(maxAd)
                Log.i(tag, "interstitial onAdHidden:\n$maxAd")
                maxInterstitialAd.loadAd()
                GameSDK.getCocosCallback()?.onAdPlayOver(getAdType().name, maxAd.revenue)
            }

            override fun onAdClicked(maxAd: MaxAd) {
                Log.i(tag, "interstitial onAdClicked:\n$maxAd")
            }

            override fun onAdLoadFailed(s: String, maxError: MaxError) {
                Log.i(tag, "interstitial onAdLoadFailed:\n$s\n$maxError")
                isLoading = false
                onAdLoadFail(s, maxError)
                printfLogWithLoaded(s, maxError)
                interstitialRetryAttempt++
                retryWithExponentialBackoff(interstitialRetryAttempt) {
                    maxInterstitialAd.loadAd()
                }
            }

            override fun onAdDisplayFailed(maxAd: MaxAd, maxError: MaxError) {
                Log.i(tag, "interstitial onAdDisplayFailed:\n$maxAd\n$maxError")
                isLoading = false
                maxInterstitialAd.loadAd()
                onAdPlayErr(maxAd, maxError)
                GameSDK.getCocosCallback()?.onAdPlayError(getAdType().name)
            }
        })
        maxInterstitialAd.setRevenueListener(this)
        onMaxAdLoadedStar("interstitial")
        isLoading = true
        loadStartTime = System.currentTimeMillis()
        maxInterstitialAd.loadAd()
    }


    override fun show() {
        Log.i(tag, "interstitial show:$maxAd")
        if (maxInterstitialAd.isReady) {
            maxInterstitialAd.showAd(null, "adid=" + CacheManager.aDID, activity)
        } else {
            GameSDK.getCocosCallback()?.onAdPlayError(getAdType().name)
            maxInterstitialAd.loadAd()
        }
    }

    override fun destroy() {
        maxInterstitialAd.destroy()
    }

    override fun isReady(): Boolean {
        return this::maxInterstitialAd.isInitialized && maxInterstitialAd.isReady
    }

    fun getAdRevenue(): Double? {
        return if (maxInterstitialAd.isReady) maxAd?.revenue else null
    }
}