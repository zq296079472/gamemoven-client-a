package com.twist.screw.sdk.callback

import com.twist.screw.sdk.ads.AdType

/**
 * 广告状态处理器接口
 * APP层需要实现此接口来处理广告事件
 */
interface IAdStatusHandler {
    /**
     * 广告播放开始
     * @param adType 广告类型
     * @param revenue 广告收入
     */
    fun onAdPlayStart(adType: AdType, revenue: Double)

    /**
     * 广告播放结束
     * @param adType 广告类型
     * @param revenue 广告收入
     */
    fun onAdPlayOver(adType: AdType, revenue: Double)

    /**
     * 广告播放错误
     * @param adType 广告类型
     */
    fun onAdPlayError(adType: AdType)

    /**
     * Adjust归因回调
     * @param network 归因网络
     */
    fun onAdjustAttribution(network: String)
}

