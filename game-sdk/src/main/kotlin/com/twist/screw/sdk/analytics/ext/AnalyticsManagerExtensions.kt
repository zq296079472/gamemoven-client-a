package com.twist.screw.sdk.analytics.ext

import com.twist.screw.sdk.analytics.AnalyticsManager
import com.twist.screw.sdk.analytics.IAnalyticsStrategy

internal inline fun <reified T : IAnalyticsStrategy> AnalyticsManager.getStrategy(): T? {
    return strategies.find { it is T } as? T
}