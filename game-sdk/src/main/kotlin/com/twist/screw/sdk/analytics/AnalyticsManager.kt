package com.twist.screw.sdk.analytics

import com.applovin.mediation.MaxAd
import com.twist.screw.sdk.analytics.strategy.AdjustStrategy
import com.twist.screw.sdk.analytics.strategy.AliLogStrategy
import com.twist.screw.sdk.analytics.strategy.FacebookStrategy
import com.twist.screw.sdk.analytics.strategy.FirebaseStrategy

class AnalyticsManager {
    internal val strategies: List<IAnalyticsStrategy>
        get() = _strategies

    private val _strategies = mutableListOf<IAnalyticsStrategy>()

    fun register(strategy: IAnalyticsStrategy) {
        _strategies.add(strategy)
    }

    fun initAdjustStrategy() {
        val adjustStrategy = strategies.find { it is AdjustStrategy } as? AdjustStrategy
        adjustStrategy?.init()
    }

    fun initAliLogStrategy() {
        val aliLogStrategy = strategies.find { it is AliLogStrategy } as? AliLogStrategy
        aliLogStrategy?.init()
    }

    fun initFacebookStrategy() {
        val facebookStrategy = strategies.find { it is FacebookStrategy } as? FacebookStrategy
        facebookStrategy?.init()
    }


    fun logEvent(name: String, params: Map<String?, Any?> = emptyMap()) =
        strategies.forEach { it.logEvent(name, params) }

    fun logProgramEvent(name: String, params: Map<String?, Any?>) {
        strategies
            .filterIsInstance<AliLogStrategy>()
            .forEach { it.logEvent(name, params, isProgram = true) }

    }

    fun logAdRevenue(maxAd: MaxAd) {
        strategies.forEach { strategy ->
            strategy.logAdRevenue(
                revenue = maxAd.revenue,
                network = maxAd.networkName,
                adUnitId = maxAd.adUnitId,
                format = maxAd.format.label,
                placement = maxAd.placement,
                currency = "USD"
            )
        }
    }

    fun getStrategy(type: AnalyticsType): IAnalyticsStrategy? {
        return when (type) {
            AnalyticsType.Firebase -> strategies.find { it is FirebaseStrategy }
            AnalyticsType.Adjust -> strategies.find { it is AdjustStrategy }
            AnalyticsType.AliLog -> strategies.find { it is AliLogStrategy }
            AnalyticsType.Facebook -> strategies.find { it is FacebookStrategy }
            else -> null
        }
    }
}
