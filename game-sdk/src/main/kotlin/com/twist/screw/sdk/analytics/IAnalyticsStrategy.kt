package com.twist.screw.sdk.analytics

interface IAnalyticsStrategy {
    fun init()
    fun logEvent(name: String, params: Map<String?, Any?> = emptyMap(), isProgram: Boolean = false)
    fun logAdRevenue(
        revenue: Double?,
        network: String?,
        adUnitId: String?,
        format: String?,
        placement: String?,
        currency: String?
    )

    fun setUserProperty(name: String, value: String)
}