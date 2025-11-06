package com.twist.screw.sdk.callback

import android.util.Log
import com.twist.screw.sdk.ICocosCallback
import com.twist.screw.sdk.ads.AdType

/**
 * 默认Cocos回调适配器
 * 将SDK的Cocos事件转发给APP层的AdStatusHandler
 */
class DefaultCocosCallback(
    private val adStatusHandler: IAdStatusHandler
) : ICocosCallback {
    
    companion object {
        private const val TAG = "DefaultCocosCallback"
    }

    override fun notifyCocos(api: String, data: String) {
        // 通用Cocos通知（默认只记录日志）
        Log.d(TAG, "Cocos callback: $api, $data")
    }

    override fun onAdPlayStart(adType: String, revenue: Double) {
        val adTypeEnum = AdType.fromName(adType)
        if (adTypeEnum != null) {
            adStatusHandler.onAdPlayStart(adTypeEnum, revenue)
        } else {
            Log.w(TAG, "Unknown ad type: $adType")
        }
    }

    override fun onAdPlayOver(adType: String, revenue: Double) {
        val adTypeEnum = AdType.fromName(adType)
        if (adTypeEnum != null) {
            adStatusHandler.onAdPlayOver(adTypeEnum, revenue)
        } else {
            Log.w(TAG, "Unknown ad type: $adType")
        }
    }

    override fun onAdPlayError(adType: String) {
        val adTypeEnum = AdType.fromName(adType)
        if (adTypeEnum != null) {
            adStatusHandler.onAdPlayError(adTypeEnum)
        } else {
            Log.w(TAG, "Unknown ad type: $adType")
        }
    }

    override fun onAdjustAttribution(network: String) {
        adStatusHandler.onAdjustAttribution(network)
    }
}

