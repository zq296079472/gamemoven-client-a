package com.twist.screw.sdk.callback

import android.app.Activity
import android.util.Log
import com.cocos.service.SDKWrapper
import com.twist.screw.sdk.IUICallback

/**
 * 默认UI回调适配器
 * 将SDK的UI事件转发给APP层的AlertManager和Activity
 */
class DefaultUICallback(
    private val alertManager: IAlertManager,
    private val activityClass: Class<*>
) : IUICallback {

    companion object {
        private const val TAG = "DefaultUICallback"
    }

    override fun showNetworkDialog() {
        val activity = SDKWrapper.shared().activity
        if (activity != null) {
            alertManager.showNetworkDialogIfNeeded(activity)
        } else {
            Log.w(TAG, "Activity is null, cannot show network dialog")
        }
    }

    override fun updateLoadingState(isLoading: Boolean, showLoading: Boolean) {
        val activity = SDKWrapper.shared().activity
        if (activity != null && activityClass.isInstance(activity)) {
            try {
                // 通过反射调用updateLoadingState方法
                val method = activityClass.getDeclaredMethod(
                    "updateLoadingState",
                    Boolean::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType
                )
                method.invoke(activity, isLoading, showLoading)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to call updateLoadingState", e)
            }
        } else {
            Log.w(TAG, "Activity is not instance of ${activityClass.simpleName}")
        }
    }

    override fun hideSplash() {
        val activity = SDKWrapper.shared().activity
        if (activity != null && activityClass.isInstance(activity)) {
            try {
                // 通过反射调用hideSplash方法
                val method = activityClass.getDeclaredMethod("hideSplash")
                method.invoke(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to call hideSplash", e)
            }
        } else {
            Log.w(TAG, "Activity is not instance of ${activityClass.simpleName}")
        }
    }
}

