package com.twist.screw.sdk.bridge.cocos

import android.Manifest
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import com.cocos.lib.CocosHelper
import com.cocos.lib.CocosJavascriptJavaBridge
import com.twist.screw.sdk.GameSDK
import com.twist.screw.sdk.utils.CacheManager
import com.twist.screw.sdk.utils.DeviceUtils
import com.twist.screw.sdk.utils.LocaleUtils.getLocale
import org.json.JSONException
import org.json.JSONObject

data class CocosNativeParams(val api: String, val params: String) {

    var request: JSONObject = JSONObject(params)
    var requestData: JSONObject = request.getJSONObject(getCocosKey("data"))
    var response: JSONObject = JSONObject()
    private val TAG = CocosNativeParams::class.java.simpleName
    private val responseData = JSONObject()

    init {
        response.put(getCocosKey("cc_token"), request.getString(getCocosKey("cc_token")))
        response.put(getCocosKey("data"), responseData)
    }

    fun getShortApi(): String {
        Log.e(TAG, "getShortApi():" + api)
        return getShortKey(api)
    }

    fun putResponseData(key: String, value: Any?): CocosNativeParams {
        Log.e(TAG, "putResponseData():" + key + " == " + value.toString())
        responseData.put(getCocosKey(key), value)
        return this
    }

    fun <T> getRequestParam(key: String): T? {
        Log.e(TAG, "getRequestParam():" + key + " == " + requestData.toString())
        return if (requestData.has(getCocosKey(key))) requestData.get(getCocosKey(key)) as T? else null
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @Throws(JSONException::class)
    fun callCocos(api: String? = null) {
        val callbackApi = api ?: this.getShortApi()
        try {
            response.putOpt(getCocosKey("platform"), "android")
            response.putOpt(getCocosKey("android_version_name"), GameSDK.getConfig().versionName)
            response.putOpt(getCocosKey("device_id"), DeviceUtils.deviceId)
            response.putOpt(getCocosKey("pkg_name"), GameSDK.getConfig().packageName)
            response.putOpt(
                getCocosKey("app_name"),
                GameSDK.getConfig().appName
            )
            response.putOpt(getCocosKey("country"), getLocale().country)
            response.putOpt(getCocosKey("user_id"), CacheManager.userId)
            response.putOpt(getCocosKey("device_type"), Build.MODEL)
            val isProxy = if (DeviceUtils.isUsingVpn()) "Y" else "N"
            response.putOpt(getCocosKey("is_proxy"), isProxy)
            val devSettingsEnabled = Settings.Global.getInt(
                GameSDK.getContext().contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            )
            response.putOpt(getCocosKey("is_develop"), if ((devSettingsEnabled == 1)) "Y" else "N")
            Log.i(TAG, "CallCocos:$callbackApi\n$response")
            CocosHelper.runOnGameThread {
                val jsCall =
                    "AndroidNative.callByNative('$callbackApi', ${JSONObject.quote(response.toString())})"
                CocosJavascriptJavaBridge.evalString(jsCall)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "callCocos:", e)
        }

    }

    companion object {

        fun getCocosKey(key: String): String {
            return key
        }

        fun getShortKey(key: String): String {
            return key
        }
    }
}