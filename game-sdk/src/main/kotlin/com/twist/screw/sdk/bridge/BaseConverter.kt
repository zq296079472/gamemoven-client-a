package com.twist.screw.sdk.bridge

import android.app.Activity
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import com.cocos.service.SDKWrapper
import com.twist.screw.sdk.bridge.cocos.CocosNativeParams
import com.twist.screw.sdk.receiver.NetworkStateReceiver

/**
 * Tiger转换器基类（SDK框架层）
 * 提供网络监听、生命周期管理等通用功能
 */
abstract class BaseConverter : ILifecycleAware {
    private var networkReceiver: NetworkStateReceiver? = null
    private var isNetworkReceiverRegistered = false
    val TAG = "BaseGroupConvert"

    /**
     * 接收Cocos调用的抽象方法
     * 子类必须实现此方法来处理具体的业务逻辑
     */
    abstract fun onReceive(nativeParams: CocosNativeParams)

    override fun onResume(context: Activity) {
    }

    override fun onDestroy() {
    }

    /**
     * 注册网络状态监听器
     */
    fun registerNetworkReceiverIfNeeded() {
        val context = SDKWrapper.shared().activity ?: return
        if (networkReceiver != null) return

        networkReceiver = NetworkStateReceiver()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(networkReceiver, filter)
        isNetworkReceiverRegistered = true
        Log.i(TAG, "Network receiver registered")
    }

    /**
     * 注销网络状态监听器
     */
    fun unregisterNetworkReceiver() {
        val context = SDKWrapper.shared().activity ?: return
        try {
            networkReceiver?.let {
                context.unregisterReceiver(it)
                Log.i(TAG, "Network receiver unregistered")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering network receiver", e)
        } finally {
            networkReceiver = null
            isNetworkReceiverRegistered = false
        }
    }

    /**
     * 检查网络监听器是否激活
     */
    fun isNetworkReceiverActive(): Boolean {
        return isNetworkReceiverRegistered
    }

}