package com.twist.screw.sdk.analytics

import android.content.Context
import android.util.Log
import com.adjust.sdk.Adjust
import com.applovin.mediation.MaxAd
import com.google.firebase.messaging.FirebaseMessaging
import com.twist.screw.sdk.analytics.strategy.AdjustStrategy
import com.twist.screw.sdk.analytics.strategy.AliLogStrategy
import com.twist.screw.sdk.analytics.strategy.FacebookStrategy
import com.twist.screw.sdk.utils.CacheManager

enum class AnalyticsType { All, Firebase, Adjust, AliLog, Facebook }

object AnalyticsUtils {
    val TAG: String = "AnalyticsUtil"
    private val manager = AnalyticsManager()

    fun init(context: Context) {
        Log.i(TAG, "init")
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.i(TAG, "FiirebaseMessaging token ${it}")
            Adjust.setPushToken(it, context)
        }
        manager.register(AdjustStrategy())
        manager.register(AliLogStrategy())
        manager.register(FacebookStrategy())
        manager.initAliLogStrategy()
    }

    fun logEvent(name: String) {
        logEvent(name, type = AnalyticsType.All)
    }

    fun logEvent(name: String, params: MutableMap<String?, Any?> = mutableMapOf()) {
        logEvent(name, params, AnalyticsType.All)
    }

    fun logEvent(
        name: String,
        params: MutableMap<String?, Any?> = mutableMapOf(),
        type: AnalyticsType = AnalyticsType.All
    ) {
        val safeParams = params.filterValues {
            when (it) {
                is Double -> it.isFinite() // 过滤掉 NaN / Infinity
                else -> true
            }
        }.toMutableMap()
        Log.i(TAG, "logEvent $name $safeParams")
        when (type) {
            AnalyticsType.All -> {
                manager.strategies.forEach { strategy ->
                    // 跳过 Facebook（需要显式指定）
                    if (strategy is FacebookStrategy) {
                        return@forEach
                    }
                    strategy.logEvent(name, params)
                }
            }

            else -> manager.getStrategy(type)?.logEvent(name, params)
        }

    }

    fun logProgramEvent(name: String, params: MutableMap<String?, Any?> = mutableMapOf()) {
        val aliLogStrategy = manager.getStrategy(AnalyticsType.AliLog) as? AliLogStrategy
        aliLogStrategy?.logEvent(name, params, isProgram = true)
    }

    fun logEventEx(
        name: String,
        category: String? = null,
        itemName: String? = null,
        value: Double? = null,
        params: MutableMap<String?, Any?> = mutableMapOf(),
        type: AnalyticsType = AnalyticsType.All
    ) {
        if (!category.isNullOrEmpty()) {
            params["item_category"] = category
        }
        if (!itemName.isNullOrEmpty()) {
            params["item_name"] = itemName
        }
        if (!itemName.isNullOrEmpty()) {
            params["value"] = value!!
        }
        logEvent(name, params, type)
    }

    fun logAction(
        category: String,
        item: String? = null,
        value: Double? = null,
        type: AnalyticsType = AnalyticsType.All
    ) {
        logEventEx("action", category, item, value, type = type)
    }

    fun logAdRevenue(maxAd: MaxAd) = manager.logAdRevenue(maxAd)

    fun setUserProperty(name: String, value: String?) {
        setUserProperty(name, value, AnalyticsType.All)
    }

    fun setUserProperty(name: String, value: String?, type: AnalyticsType = AnalyticsType.All) {
        Log.i(TAG, "setUserProperty $name $value")
        if (value.isNullOrEmpty()) return

        when (type) {
            AnalyticsType.All -> manager.strategies.forEach {
                it.setUserProperty(name, value)
            }

            else -> manager.getStrategy(type)?.setUserProperty(name, value)
        }
    }

    fun initAdjust() {
        manager.initAdjustStrategy()
    }

    fun initFacebook() {
        manager.initFacebookStrategy()
    }

    fun logCocosEvent(eventName: String, params: String?, type: AnalyticsType = AnalyticsType.All) {
        Log.i(TAG, "logCocosEvent $eventName $params")
        val parsedParams = parseCocosParams(eventName, params)
        logEvent(eventName, parsedParams, type)
    }

    fun logCocosProgramEvent(eventName: String, params: String?) {
        Log.i(TAG, "logCocosProgramEvent $eventName $params")
        val parsedParams = parseCocosParams(eventName, params)
        manager.logProgramEvent(eventName, parsedParams)
    }

    private fun parseCocosParams(eventName: String, paramStr: String?): MutableMap<String?, Any?> {
        val allParams = mutableMapOf<String?, Any?>()

        if (!paramStr.isNullOrEmpty()) {
            val cocosParams = paramStr.split(";")
            for (entry in cocosParams) {
                try {
                    if (entry.isBlank()) continue
                    
                    val temp = entry.split("@")
                    if (temp.size < 2) continue // 安全检查

                    val key = temp[0]
                    val valuePart = temp[1]
                    
                    // 安全解析value: 格式为 "type:value"
                    val valueSplit = valuePart.split(":", limit = 2)
                    val value = if (valueSplit.size > 1) valueSplit[1] else valuePart

                    if (key == "userid" || key == "userId") {
                        CacheManager.userId = value
                    }

                    val deviceLogRecord = CacheManager.deviceLogRecord
                    when (eventName) {
                        "gameInitStart" -> deviceLogRecord.isGameInitStart = true
                        "gameInitFinish" -> deviceLogRecord.isGameInitFinish = true
                        "gameStart" -> deviceLogRecord.isGameStart = true
                    }
                    CacheManager.deviceLogRecord = deviceLogRecord

                    allParams[key] = value
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse cocos param: $entry", e)
                }
            }
        }
        return allParams
    }
}
