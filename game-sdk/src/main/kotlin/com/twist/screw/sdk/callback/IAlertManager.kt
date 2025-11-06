package com.twist.screw.sdk.callback

import android.app.Activity

/**
 * 弹窗管理器接口
 * APP层需要实现此接口来显示各种弹窗
 */
interface IAlertManager {
    /**
     * 显示网络错误对话框（如果需要）
     * @param activity 当前Activity
     */
    fun showNetworkDialogIfNeeded(activity: Activity)
}

