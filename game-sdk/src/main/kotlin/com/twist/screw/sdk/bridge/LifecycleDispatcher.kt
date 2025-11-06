package com.twist.screw.sdk.bridge

import android.app.Activity

object LifecycleDispatcher {

    fun onResume(context: Activity) {
        ConverterManager.Companion.getInstance().getConvert().values.forEach {
            if (it is ILifecycleAware) {
                it.onResume(context)
            }
        }
    }

    fun onDestroy() {
        ConverterManager.Companion.getInstance().getConvert().values.forEach {
            if (it is ILifecycleAware) {
                it.onDestroy()
            }
        }
    }
}