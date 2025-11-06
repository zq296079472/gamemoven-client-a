package com.twist.screw.sdk

/**
 * 埋点Key配置类 - 管理每个APP不同的埋点key值
 *
 * 这些key值在不同APP中可能不同，通过配置统一管理
 */
data class AnalyticsKeysConfig(
    /**
     * 事件名称配置
     */
    val eventNames: EventNames = EventNames(),

    /**
     * 参数名称配置
     */
    val paramKeys: ParamKeys = ParamKeys(),

    /**
     * AliLog参数key配置
     */
    val aliLogKeys: AliLogKeys = AliLogKeys()
) {
    /**
     * 事件名称
     */
    data class EventNames(
        /** Adjust初始化事件 */
        val adjustInit: String = "ascrewssInit",

        /** 广告播放开始事件 */
        val adPlayStart: String = "ascrewssPlay",

        /** 广告播放结束事件 */
        val adPlayFinish: String = "ascrewsf",

        /** 广告播放错误事件 */
        val adPlayError: String = "ascrewse"
    )

    /**
     * 参数名称
     */
    data class ParamKeys(
        /** 广告收益参数key */
        val adRevenue: String = "adscrewsru",

        /** 广告类型参数key */
        val adType: String = "adscrewstyp",

        /** 广告错误参数key */
        val adError: String = "adError"
    )

    /**
     * AliLog参数key配置
     */
    data class AliLogKeys(
        /** 软件名称 */
        val softwareName: String = "softwarescrewsname",

        /** 国家/地区 */
        val nation: String = "screwsnation",

        /** 机器ID/设备ID */
        val machineId: String = "machinescrewsid",

        /** 设备类型 */
        val deviceType: String = "device_type",

        /** 活动标题/事件名称 */
        val activityTitle: String = "activityscrewstitle",

        /** 活动时间/事件时间 */
        val activityTime: String = "activityscrewstime",

        /** 活动类型/事件类型 */
        val activityVariety: String = "activityscrewsvariety",

        /** 发布包名 */
        val releasePackage: String = "releasescrewspkg",

        /** 框架类型 */
        val framework: String = "screwsframework",

        /** 玩家ID */
        val playerId: String = "playerscrewsid",

        /** 版本号 */
        val version: String = "version",

        /** 发布标签/版本名称 */
        val releaseTag: String = "releasescrewstag"
    )
}

