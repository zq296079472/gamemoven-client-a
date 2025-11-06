package com.twist.screw.sdk.analytics.strategy

import android.os.Build
import android.util.Log
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.analytics.IAnalyticsStrategy
import com.twist.screw.sdk.utils.AliLogUtils
import com.twist.screw.sdk.utils.CacheManager
import com.twist.screw.sdk.utils.DeviceUtils
import com.twist.screw.sdk.utils.LocaleUtils

class AliLogStrategy : IAnalyticsStrategy {
    val TAG: String = "AnalyticsUtil"
    private var aliLog: AliLogUtils? = null
    private var aliProgramLog: AliLogUtils? = null
    private var uid: String? = null

    override fun init() {
        aliLog = AliLogUtils().apply { init(GameSDK.getContext()) }
        aliProgramLog = AliLogUtils().apply { init(GameSDK.getContext(), true) }
    }

    override fun logEvent(name: String, params: Map<String?, Any?>, isProgram: Boolean) {
        val apiParams = buildBaseParams(name, params)
        if (isProgram) {
            Log.i(TAG, "aliProgramLog addLog")
            aliProgramLog?.addLog(apiParams)
        } else {
            Log.i(TAG, "aliLog addLog")
            aliLog?.addLog(apiParams)
        }
    }

    private fun buildBaseParams(
        name: String,
        params: Map<String?, Any?>
    ): MutableMap<String?, Any?> {
        val keys = GameSDK.getConfig().analyticsKeys.aliLogKeys

        // 1. 构建基础参数（标准参数）
        val apiParams = mutableMapOf<String?, Any?>(
            keys.softwareName to GameSDK.getConfig().appName,
            keys.nation to LocaleUtils.getLocale().country,
            keys.machineId to DeviceUtils.deviceId,
            keys.deviceType to Build.MODEL,
            keys.activityTitle to name,
            keys.activityTime to LocaleUtils.getCurrentTime(),
            keys.activityVariety to (params["event_type"] ?: name),
            keys.releasePackage to GameSDK.getConfig().packageName,
            keys.framework to "android",
            keys.playerId to (CacheManager.userId.ifEmpty { uid } ?: ""),
            keys.version to GameSDK.getConfig().versionCode.toString(),
            keys.releaseTag to GameSDK.getConfig().versionName
        )

        // 2. 合并传入的参数（可能覆盖基础参数）
        apiParams.putAll(params)

        // 3. 调用额外参数构建器（如果APP层有提供）
        // 允许APP层添加自定义的额外参数
        val extraParams = GameSDK.getAliLogParamsBuilder()?.buildExtraParams(name, params)
        if (extraParams != null && extraParams.isNotEmpty()) {
            apiParams.putAll(extraParams)
        }

        // 4. 特殊处理uid参数
        if (apiParams["uid"] == "" && !uid.isNullOrEmpty()) {
            apiParams["uid"] = uid!!
        }

        return apiParams
    }

    override fun logAdRevenue(
        revenue: Double?,
        network: String?,
        adUnitId: String?,
        format: String?,
        placement: String?,
        currency: String?
    ) {
        val logParams: MutableMap<String?, Any?> = HashMap()
        logParams["adKey"] = adUnitId ?: ""
        logParams["networkName"] = network ?: ""
        logParams["revenue"] = revenue?.toString() ?: ""
        logParams["format"] = format
    }

    override fun setUserProperty(name: String, value: String) {}
}
