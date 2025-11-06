package com.twist.screw.sdk.ads

import android.app.Application
import com.twist.screw.sdk.ads.max.MaxAdManager

object AdManager {

    fun init(context: Application) {
        MaxAdManager.init(context)
    }

    fun initBanner(): Boolean {
        return MaxAdManager.initBanner()
    }

    fun showBanner(isShow: Boolean) {
        MaxAdManager.showBanner(isShow)
    }

    fun showInterstitialAd() {
        MaxAdManager.showInterstitialAd()
    }

    fun showRewardItemAd() {
        MaxAdManager.showRewardItemAd()
    }

    fun showRewardCashAd() {
        MaxAdManager.showRewardCashAd()
    }

    fun getAdRevenue(adType: AdType?): Double? {
        if (adType == null) return null;
        return MaxAdManager.getAdRevenue(adType)
    }
}