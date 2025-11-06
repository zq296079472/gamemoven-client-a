package com.twist.screw.sdk.utils

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import com.cocos.service.SDKWrapper
import com.twist.screw.sdk.GameSDK

object LauncherUtils {
    fun launcher(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            GameSDK.getContext().startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun openSetting() {
        try {
            val activity = SDKWrapper.shared().activity
            if (activity != null) {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            } else {
                Log.e("LauncherUtil", "Failed to open Wi-Fi settings: activity is null")
            }
        } catch (e: Exception) {
            Log.e("LauncherUtil", "Failed to open Wi-Fi settings", e)
        }

    }
}