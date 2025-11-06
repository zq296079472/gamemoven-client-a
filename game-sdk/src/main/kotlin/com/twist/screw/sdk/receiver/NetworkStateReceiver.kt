package com.twist.screw.sdk.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import com.twist.screw.sdk.GameSDK

class NetworkStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        val isConnected = connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting == true
        Log.d("reallyOnline", "isConnected == $isConnected")

        if (!isConnected) {
            // 通知UI层显示网络错误对话框
            GameSDK.getUICallback()?.showNetworkDialog()
        }
    }
}