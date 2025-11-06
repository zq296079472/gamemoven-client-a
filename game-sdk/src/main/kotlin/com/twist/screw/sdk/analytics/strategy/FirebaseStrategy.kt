package com.twist.screw.sdk.analytics.strategy

import com.google.firebase.analytics.FirebaseAnalytics
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.analytics.IAnalyticsStrategy
import com.twist.screw.sdk.analytics.ext.toBundle

class FirebaseStrategy : IAnalyticsStrategy {
    private var analytics: FirebaseAnalytics? = null

    override fun init() {
        analytics = FirebaseAnalytics.getInstance(GameSDK.getContext()!!)
    }

    override fun logEvent(name: String, params: Map<String?, Any?>, isProgram: Boolean) {
        analytics?.logEvent(name, params.toBundle())
    }

    override fun logAdRevenue(
        revenue: Double?,
        network: String?,
        adUnitId: String?,
        format: String?,
        placement: String?,
        currency: String?
    ) {
        val params = mutableMapOf<String?, Any?>()
        params[FirebaseAnalytics.Param.AD_PLATFORM] = "applovin"
        params[FirebaseAnalytics.Param.AD_SOURCE] = network ?: ""
        params[FirebaseAnalytics.Param.AD_FORMAT] = format ?: ""
        params[FirebaseAnalytics.Param.AD_UNIT_NAME] = adUnitId ?: ""
        params[FirebaseAnalytics.Param.VALUE] = revenue ?: .0
        params[FirebaseAnalytics.Param.CURRENCY] = currency ?: ""
        logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, params)
    }

    override fun setUserProperty(name: String, value: String) {
        analytics?.setUserProperty(name, value)
    }
}
