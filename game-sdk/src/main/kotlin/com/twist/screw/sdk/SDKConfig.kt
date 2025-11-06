package com.twist.screw.sdk

/**
 * SDK配置类 - 替代对BuildConfig的直接依赖
 */
data class SDKConfig(
    // AppLovin配置
    val applovinSdkKey: String,
    val applovinBannerUnit: String,
    val applovinInterstitialUnit: String,
    val applovinRewardItem: String,
    val applovinRewardCash: String,

    // Adjust配置
    val adjustAppId: String,

    // AliLog配置
    val aliLogEndpoint: String,
    val aliLogProject: String,
    val aliLogLogstore: String,
    val aliLogProgramLogstore: String,
    val aliLogAccessKeyID: String,
    val aliLogAccessKeySecret: String,
    val logType: String,

    // Facebook配置
    val facebookAppId: String,
    val facebookClientToken: String,
    val facebookAutoInit: String,
    val facebookAutoEvents: String,

    // APP信息
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val debugMode: Boolean = false,

    // 埋点Key配置（每个APP可能不同）
    val analyticsKeys: AnalyticsKeysConfig = AnalyticsKeysConfig()
)

