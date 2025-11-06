package com.twist.screw.sdk.utils

import android.content.Context
import android.content.SharedPreferences
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.model.GameStateRecord

object CacheManager {
    const val SP_FILE_NAME: String = "CacheMgrSysUtilsManager"
    const val COCOS_USER_ID: String = "to_user_id"
    const val USER_MAX_INIT_TIME: String = "user_max_init_time"
    const val DEVICE_ADID: String = "device_adid"
    const val DEVICE_LOG_RECORD: String = "device_log_record"
    const val DEVICE_LOCAL: String = "device_local"

    const val ADJUST_INIT: String = "adjust_init"
    private val sP: SharedPreferences
        get() = GameSDK.getContext().getSharedPreferences(
            GameSDK.getContext().packageName + SP_FILE_NAME,
            Context.MODE_PRIVATE
        )

    var userId: String
        get() = sP.getString(COCOS_USER_ID, "") ?: ""
        set(userId) {
            sP.edit().putString(COCOS_USER_ID, userId).apply()
        }

    var local: String
        get() = sP.getString(DEVICE_LOCAL, "") ?: ""
        set(local) {
            sP.edit().putString(DEVICE_LOCAL, local).apply()
        }

    var userMaxInitTime: String
        get() = sP.getString(USER_MAX_INIT_TIME, "") ?: ""
        set(userMaxInitTime) {
            sP.edit().putString(USER_MAX_INIT_TIME, userMaxInitTime).apply()
        }

    var aDID: String
        get() = sP.getString(DEVICE_ADID, "") ?: ""
        set(deviceAdid) {
            sP.edit().putString(DEVICE_ADID, deviceAdid).apply()
        }

    var deviceLogRecord: GameStateRecord
        get() = LocaleUtils.gson.fromJson(
            sP.getString(DEVICE_LOG_RECORD, "{}"),
            GameStateRecord::class.java
        )
        set(deviceLogRecord) {
            sP.edit().putString(DEVICE_LOG_RECORD, LocaleUtils.gson.toJson(deviceLogRecord))
                .apply()
        }

    var isAdjustInit: Boolean
        get() = sP.getBoolean(ADJUST_INIT, false) ?: false
        set(value) {
            sP.edit().putBoolean(ADJUST_INIT, value).apply()
        }

}